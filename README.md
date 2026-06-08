# Datadog Logging com Java - Event Attributes

Este projeto demonstra como enviar variáveis de log para o Datadog como event attributes em Java, utilizando diferentes abordagens e técnicas.

## 📋 Pré-requisitos

- Java 11 ou superior
- Maven 3.6+
- Agent do Datadog configurado
- Conta no Datadog

## 🚀 Como usar

### 1. Configuração do Ambiente

Defina as variáveis de ambiente do Datadog:

```bash
export DD_API_KEY="sua_api_key_aqui"
export DD_SERVICE="seu-servico"
export DD_ENV="production"  # ou development, staging, etc.
export DD_VERSION="1.0.0"
export DD_AGENT_HOST="localhost"
```

### 2. Executando com o Agent do Datadog

Para que o tracing funcione corretamente, execute sua aplicação com o Java Agent do Datadog:

```bash
# Baixar o agent (se ainda não tiver)
wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer

# Executar a aplicação
java -javaagent:dd-java-agent.jar \
     -Ddd.service=datadog-logging-example \
     -Ddd.env=development \
     -Ddd.version=1.0.0 \
     -jar target/datadog-logging-example-1.0.0.jar
```

## 📖 Técnicas Implementadas

### 1. **MDC (Mapped Diagnostic Context)**

Utiliza o SLF4J MDC para adicionar variáveis contextuais que aparecem automaticamente nos logs:

```java
// Configurar contexto
MDC.put("userId", "user123");
MDC.put("sessionId", "session-abc-456");
MDC.put("operacao", "processamento_pedido");

// Fazer log - as variáveis aparecerão automaticamente
logger.info("Processando operação");

// Sempre limpar após o uso
MDC.clear();
```

### 2. **Custom Attributes em Spans**

Adiciona attributes diretamente aos spans do Datadog APM:

```java
@Trace(operationName = "processar_pedido")
public void processarPedido() {
    MutableSpan span = GlobalTracer.get().activeSpan();
    if (span != null) {
        span.setTag("pedido.id", "pedido-123");
        span.setTag("pedido.valor", 299.99);
        span.setTag("cliente.tier", "premium");
    }
}
```

### 3. **Logging Estruturado com JSON**

Configuração do Logback para enviar logs em formato JSON com metadata do Datadog:

```xml
<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
    <providers>
        <timestamp/>
        <logLevel/>
        <message/>
        <mdc/>
        <pattern>
            <pattern>
                {
                "dd.trace_id": "%X{dd.trace_id:-}",
                "dd.span_id": "%X{dd.span_id:-}",
                "dd.service": "${DD_SERVICE:-datadog-logging-example}"
                }
            </pattern>
        </pattern>
    </providers>
</encoder>
```

### 4. **LoggingUtility - Builder Pattern**

Classe utilitária que facilita a criação de logs estruturados:

```java
LoggingUtility.log()
    .message("Processando pedido {}")
    .attribute("pedido_id", "PED-123")
    .attribute("valor", "299.99")
    .attribute("metodo_pagamento", "cartao")
    .userId("user123")
    .sessionId("sess456")
    .info();
```

## 🏗️ Estrutura do Projeto

```
src/
├── main/java/com/example/
│   ├── DatadogLoggingExample.java    # Exemplos básicos com MDC e spans
│   ├── LoggingUtility.java           # Classe utilitária com builder
│   └── ExemplosUsoPratico.java       # Cenários reais de uso
└── main/resources/
    ├── logback-spring.xml            # Configuração do Logback
    └── application.properties        # Propriedades da aplicação
```

## 📊 Event Attributes no Datadog

Com as configurações deste projeto, você verá os seguintes attributes nos eventos do Datadog:

### Attributes Automáticos
- `dd.trace_id` - ID do trace
- `dd.span_id` - ID do span
- `dd.service` - Nome do serviço
- `dd.env` - Ambiente (dev, prod, etc.)
- `dd.version` - Versão da aplicação

### Attributes Customizados
- `user_id` - ID do usuário
- `session_id` - ID da sessão
- `request_id` - ID da requisição
- `transaction_id` - ID da transação
- `operation` - Tipo de operação
- `error_code` - Código do erro (quando aplicável)
- `error_category` - Categoria do erro

## 🔍 Consultando no Datadog

### 1. **Logs Explorer**

Use estas queries para filtrar logs:

```
service:datadog-logging-example
@user_id:user123
@operation:processamento_pedido
@error_code:PED_001
```

### 2. **APM Traces**

Os custom attributes aparecerão na seção "Info" de cada span.

### 3. **Dashboards**

Crie dashboards usando os attributes como facets:

- Transações por `@transaction_type`
- Erros por `@error_category`
- Performance por `@operation`

## 🛠️ Exemplos de Uso

### Processamento de Pedido

```java
LoggingUtility.setUserContext("user123", "sess456");
LoggingUtility.setRequestContext("req789", "processar_pedido");

LoggingUtility.log()
    .message("Pedido processado com sucesso")
    .attribute("pedido_id", "PED-001")
    .attribute("valor_total", "899.97")
    .attribute("tempo_processamento", "2350ms")
    .info();
```

### Tratamento de Erro

```java
try {
    // operação que pode falhar
} catch (Exception e) {
    LoggingUtility.setErrorContext("PED_001", "processamento_falhou", e.getMessage());
    
    LoggingUtility.log()
        .message("Falha no processamento: {}")
        .attribute("etapa_falha", "pagamento")
        .error(e);
}
```

### Transação Financeira

```java
LoggingUtility.setTransactionContext("TXN-123", "transferencia", 1500.00);

LoggingUtility.log()
    .message("Transferência realizada")
    .attribute("conta_origem", "123456")
    .attribute("conta_destino", "789012")
    .attribute("codigo_autorizacao", "AUTH123")
    .info();
```

## 🎯 Melhores Práticas

1. **Sempre limpe o MDC**: Use `try/finally` ou `LoggingUtility.clearContext()`

2. **Use IDs únicos**: Para rastreabilidade (request_id, transaction_id, etc.)

3. **Padronize naming**: Use snake_case para attributes (`user_id`, não `userId`)

4. **Contexto hierárquico**: User → Session → Request → Transaction

5. **Error context**: Sempre adicione contexto detalhado em erros

6. **Performance**: Evite logs excessivos em operações de alta frequência

## 📱 Compilação e Execução

```bash
# Compilar
mvn clean compile

# Executar exemplo básico
mvn exec:java -Dexec.mainClass="com.example.DatadogLoggingExample"

# Executar exemplos práticos
mvn exec:java -Dexec.mainClass="com.example.ExemplosUsoPratico"

# Gerar JAR
mvn clean package
```

## 🔧 Troubleshooting

### Logs não aparecem no Datadog
1. Verifique se o agent está rodando
2. Confirme as variáveis de ambiente `DD_*`
3. Verifique a conectividade de rede

### Attributes não aparecem
1. Confirme que o MDC está sendo definido antes do log
2. Verifique se o Logback está configurado corretamente
3. Certifique-se de que está usando JSON encoding

### Traces não correlacionados
1. Verifique se está usando `@Trace` nas operações
2. Confirme que o Java Agent está sendo carregado
3. Verifique se `dd.trace_id` está aparecendo nos logs

## 📚 Links Úteis

- [Documentação Datadog Java](https://docs.datadoghq.com/tracing/setup_overview/setup/java/)
- [Logback Configuration](https://logback.qos.ch/manual/configuration.html)
- [SLF4J MDC](http://www.slf4j.org/manual.html#mdc)
