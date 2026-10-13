# Voice Budget API

API de orçamento pessoal controlada por voz. A pessoa usuária envia um áudio como *"passei na farmácia e deixei 80 reais"* e a API transcreve, entende a intenção com um modelo de linguagem, registra ou consulta transações no banco e responde com um áudio.

Projeto desenvolvido no Desafio de Projeto de Spring AI da [DIO](https://www.dio.me/), evoluindo a [API base da trilha](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai). **Esta versão roda 100% local, sem custo com APIs pagas.**

## Fluxo principal

```
áudio ──► transcrição (Whisper) ──► modelo de linguagem (Qwen 2.5) ──► tool calling ──► MySQL
                                                │
resposta em áudio ◄── síntese de voz (Kokoro) ◄─┘
```

1. `POST /transactions/ai` recebe o arquivo de áudio.
2. O áudio é transcrito para texto.
3. O modelo interpreta o comando e decide qual ferramenta (*tool*) chamar.
4. A ferramenta executa um caso de uso real: registrar, listar ou somar transações.
5. O modelo redige a resposta, que é convertida em áudio MP3.

## Melhorias implementadas

### 1. Correção de bug: valores retornados 100 vezes maiores

O projeto base salvava os valores em centavos, mas convertia de volta usando `setScale(2)`, que apenas adiciona casas decimais. Uma compra de R$ 25,50 (salva como `2550`) era devolvida como `2550.00`, e a IA respondia *"você gastou 2.550 reais"*.

- A conversão passou a ser `BigDecimal.valueOf(centavos, 2)`, centralizada em um único lugar (`Money`).
- O retorno trocou `double` por `BigDecimal`, evitando erros de arredondamento com dinheiro.
- Os campos foram renomeados para `amountInCents` e `amountInReais`. A ambiguidade do nome `amount` foi a origem do bug, e o nome explícito também orienta a IA, que lê esses campos no schema das ferramentas.

### 2. Validações antes de salvar

- **No domínio:** uma transação só existe com valor positivo, descrição preenchida e categoria definida. Violações lançam `InvalidTransactionException`. A validação fica na entidade porque é o único ponto por onde passam todos os caminhos: a ferramenta da IA, o endpoint REST e a leitura do banco.
- **Na API:** dados inválidos retornam `400` no formato padrão [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457), em vez de `500`. Só a exceção do domínio vira `400`; exceções genéricas não são expostas, para não vazar detalhes internos.
- **No upload:** arquivo vazio retorna `400`, tipo que não é áudio retorna `415`, e o tamanho máximo é 5 MB.
- **Categoria `OTHER`:** antes, um gasto em restaurante era forçado em `GROCERIES`, `PHARMA` ou `AUTO`.

### 3. Nova ferramenta: total gasto por categoria

A tool `sum-transactions-by-category` responde perguntas como *"quanto eu gastei com farmácia?"*. A soma é feita pelo banco (`SUM` com `coalesce`), sem carregar todas as transações na memória, e o modelo recebe o total pronto em vez de somar uma lista, coisa em que modelos pequenos erram com frequência.

### 4. Execução 100% local, sem custo

Os modelos da OpenAI foram substituídos por servidores locais que expõem APIs compatíveis com a da OpenAI. Por isso **nenhuma linha de código Java mudou para essa troca**, só o `application.properties`:

| Função | Projeto base | Esta versão |
|---|---|---|
| Chat e tool calling | OpenAI `gpt-4o-mini` | [Ollama](https://ollama.com/) com `qwen2.5:7b` |
| Transcrição | OpenAI Whisper | [Speaches](https://speaches.ai/) com `faster-whisper-small` |
| Síntese de voz | OpenAI TTS | Speaches com Kokoro 82M, voz `pf_dora` (pt-BR) |

### 5. Problemas encontrados ao integrar o modelo local

Trocar o modelo revelou problemas que o modelo da OpenAI escondia. Todos foram diagnosticados pelos logs (`SimpleLoggerAdvisor`):

- **Schema aninhado:** as ferramentas recebiam um objeto `input` com os campos dentro. O Qwen enviava os campos soltos, o Spring não encontrava `input` e passava `null`. A solução foi criar a classe `TransactionTools`, com parâmetros planos, que também tirou as anotações do Spring AI da camada de aplicação: expor uma funcionalidade à IA é responsabilidade da infraestrutura, assim como expor via HTTP é do controller.
- **Prompt ambíguo:** a instrução *"Informe o valor em centavos"* foi lida pelo modelo como um pedido para a pessoa usuária informar o valor. O prompt foi reescrito para deixar claro que quem converte é o modelo.
- **Respostas variáveis:** a temperatura padrão fazia a mesma frase às vezes chamar a ferramenta e às vezes não. Com `temperature=0`, o comportamento ficou previsível.
- **Alucinação de sucesso:** quando a ferramenta falhava, o modelo ainda respondia *"transação registrada com sucesso"*. O prompt passou a exigir que erros sejam informados, e a validação no domínio garante que nada inválido seja salvo, mesmo que o modelo erre.

### 6. Refatoração

- A orquestração da IA (transcrever, consultar o modelo, gerar voz) saiu do controller para o `VoiceAssistantService`. O controller agora cuida só de HTTP.
- O endpoint passou a se chamar `handleVoiceCommand` (antes `transcribe`, que fazia muito mais do que transcrever) e a declarar o tipo MIME oficial `audio/mpeg`.
- O prompt de sistema passou a ser lido explicitamente em UTF-8.

### 7. Testes automatizados

17 testes unitários, que rodam em cerca de 1 segundo, sem banco, sem Docker e sem IA:

- validação do domínio, com casos de borda (zero, negativo, nulo, vazio, só espaços);
- conversão de centavos para reais, incluindo um teste de regressão do bug original;
- casos de uso de registro e soma, com um repositório falso em memória.

O teste `contextLoads` do projeto base foi removido: ele não tinha asserção e dependia de MySQL rodando para passar.

## Tecnologias

Java 25 · Spring Boot 4 · Spring AI 2.0 · Spring Data JPA · MySQL 9 · Docker Compose · Ollama · Speaches · JUnit 5 · AssertJ · Gradle

## Como executar

### Pré-requisitos

- JDK 25
- Docker Desktop
- [Ollama](https://ollama.com/download)
- Cerca de 8 GB de RAM livres (o modelo de linguagem roda na CPU se não houver GPU)

### 1. Baixar o modelo de linguagem

```bash
ollama pull qwen2.5:7b
```

### 2. Subir os containers e baixar os modelos de áudio

```bash
docker compose up -d
curl -X POST http://localhost:8000/v1/models/Systran/faster-whisper-small
curl -X POST http://localhost:8000/v1/models/speaches-ai/Kokoro-82M-v1.0-ONNX
```

Os modelos ficam salvos em um volume do Docker e só precisam ser baixados uma vez. Se o primeiro `POST` falhar, o Speaches ainda está iniciando: aguarde alguns segundos e repita.

> Suba **todos** os serviços com `docker compose up -d`. Se apenas parte deles estiver rodando, o Spring Boot não inicia os demais e a aplicação falha sem conexão com o banco.

### 3. Iniciar a aplicação

```bash
./gradlew bootRun
```

No Windows, use `.\gradlew.bat bootRun`. A API fica disponível em `http://localhost:8080`.

## Como testar

### Testes automatizados

```bash
./gradlew test
```

Os testes `*IT` herdados do projeto base são ignorados automaticamente: eles só rodam com a variável `OPENAI_API_KEY` definida.

### Endpoints REST

Registrar uma transação (valor em centavos):

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Posto","category":"AUTO","amountInCents":15000}'
```

```json
{"id":"0a0254dc-...","category":"AUTO","description":"Posto","amountInReais":150.00}
```

Listar transações de uma categoria (`GROCERIES`, `PHARMA`, `AUTO` ou `OTHER`):

```bash
curl http://localhost:8080/transactions/AUTO
```

Valor inválido:

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Posto","category":"AUTO","amountInCents":0}'
```

```json
{"detail":"Transaction amount must be greater than zero","instance":"/transactions","status":400,"title":"Bad Request"}
```

### Fluxo de voz

```bash
curl -F "file=@src/test/resources/audio/recording-1.m4a;type=audio/mp4" \
  http://localhost:8080/transactions/ai -o resposta.mp3
```

O áudio diz *"Passei na farmácia rapidinho e deixei 80 reais em 3 itens"*. A transação é registrada como `PHARMA` com `80.00`, e o `resposta.mp3` traz a confirmação falada.

> Informe o tipo do arquivo (`;type=audio/mp4` para `.m4a`, `;type=audio/mpeg` para `.mp3`). Sem ele, o `curl` envia `application/octet-stream`, que a API recusa com `415`.

Na primeira chamada, os modelos ainda precisam ser carregados na memória, e a resposta pode levar de 1 a 3 minutos em CPU.

## Limitações conhecidas

- **Desempenho:** em CPU, cada comando de voz leva de 30 segundos a 2 minutos.
- **Qualidade dos modelos locais:** o `faster-whisper-small` às vezes erra palavras ("A cena farmácia" em vez de "Passei na farmácia"), o Kokoro fala português com sotaque americano, e o Qwen de 7B comete erros de concordância ocasionais. Modelos maiores melhoram isso, ao custo de memória e tempo.
- **Cancelamento:** se o cliente desistir da requisição, o servidor ainda conclui o processamento e salva a transação.
- **Cobertura da query de soma:** os testes usam um repositório em memória, que não exercita a query JPQL. Ela foi verificada manualmente; cobri-la automaticamente exigiria um teste de integração com banco real (por exemplo, Testcontainers).
- **Segurança:** a API não tem autenticação e é adequada apenas para uso local. O tipo do arquivo enviado é declarado pelo cliente e pode ser falsificado; o limite de tamanho é a proteção efetiva. O log em `DEBUG` do Spring AI registra os textos dos comandos, que contêm dados financeiros, e deve ser desativado fora do ambiente de desenvolvimento.

## O que aprendi

Integrar IA em uma aplicação real envolve bastante coisa que já faz parte do desenvolvimento de software. A IA pode ajudar a tomar decisões, mas é o código que garante que tudo funcione corretamente.

Não podemos confiar apenas no que a IA diz que fez. Durante o projeto, ela informou que tinha registrado uma transação, mas os dados não foram salvos. Por isso, é importante validar as informações e manter as regras de negócio no código.

O prompt também precisa ser bem escrito e objetivo, assim como uma API. Uma palavra com sentido muito aberto, como "Informe", fez a IA solicitar dados que já estavam disponíveis.

Usar nomes claros nas variáveis também ajuda a evitar erros. Por exemplo, `amount` não deixa claro qual é a unidade do valor, enquanto `amountInCents` deixa isso explícito.

Os logs foram muito importantes para encontrar os problemas durante o desenvolvimento. Com eles, foi possível verificar o que realmente estava sendo enviado para a IA e quais respostas ela retornava.

Por fim, utilizar APIs compatíveis com a OpenAI facilita a troca de provedores sem precisar alterar grande parte do código. Isso também permitiu executar o projeto localmente, sem custos com o serviço de IA.
## Créditos

Projeto base: [dio-spring-boot-learning-track / 05-spring-ai](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai), da Digital Innovation One.