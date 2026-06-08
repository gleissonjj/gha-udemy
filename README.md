# Datadog Logging com Java - Event Attributes

Este projeto demonstra como enviar variáveis customizadas de logs Java para o Datadog como **event attributes** usando MDC (Mapped Diagnostic Context).

## 🎯 Objetivo

Permitir que variáveis específicas do seu código Java apareçam como **event attributes** no Datadog, facilitando:
- Filtros avançados nos logs
- Dashboards customizados
- Alertas baseados em valores específicos
- Correlação entre logs e métricas

## 🏗️ Estrutura do Projeto

```
├── pom.xml                                    # Dependências Maven
├── src/main/resources/logback.xml            # Configuração de logging
├── src/main/java/com/example/
│   ├── DatadogLoggingExample.java           # Exemplos práticos
│   └── LoggingUtil.java                     # Utilitário para MDC
└── README.md                                # Esta documentação
```

## 📦 Dependências Principais

- **Logback**: Framework de logging
- **Datadog Trace API**: Integração com APM
- **Logstash Encoder**: Formato JSON para logs
- **SLF4J MDC**: Context mapping para custom attributes

## 🚀 Como Usar

### 1. Configuração Básica

As variáveis adicionadas ao MDC aparecerão automaticamente como **event attributes** no Datadog:

```java
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

Logger logger = LoggerFactory.getLogger(MyClass.class);

// Adicionar variáveis ao contexto
MDC.put("user.id", "12345");
MDC.put("order.amount", "299.99");
MDC.put("operation.type", "PAYMENT");

// Este log terá as variáveis como event attributes
logger.info("Processando pagamento");

// Sempre limpar o contexto
MDC.clear();
```

### 2. Usando o LoggingUtil (Recomendado)

```java
import static com.example.LoggingUtil.*;

// Contexto com builder pattern
context()
    .userId("12345")
    .operationType("PAYMENT_PROCESSING")
    .custom("order.amount", 299.99)
    .custom("payment.method", "CREDIT_CARD")
    .apply();

logger.info("Processamento iniciado");
clearContext();

// Ou usando context automático
withRequestContext("USER_REGISTRATION", () -> {
    addUserContext("12345", "user@email.com", "João Silva");
    logger.info("Usuário registrado");
});
```

### 3. Exemplos por Domínio

#### 🛒 E-commerce
```java
// Contexto de pedido
MDC.put("order.id", "ORD-789");
MDC.put("order.total", "459.90");
MDC.put("order.items_count", "3");
MDC.put("customer.segment", "PREMIUM");
logger.info("Pedido criado");
```

#### 💳 Pagamentos
```java
// Contexto de transação
MDC.put("payment.id", "PAY-123");
MDC.put("payment.method", "CREDIT_CARD");
MDC.put("payment.provider", "stripe");
MDC.put("payment.amount", "299.99");
MDC.put("payment.currency", "BRL");
logger.info("Pagamento processado");
```

#### 🔐 Autenticação
```java
// Contexto de login
MDC.put("auth.user_id", "user123");
MDC.put("auth.method", "OAuth2");
MDC.put("auth.provider", "google");
MDC.put("session.id", "sess_abc123");
logger.info("Login realizado");
```

### 4. Métricas de Performance
```java
long startTime = System.currentTimeMillis();
// ... operação ...
long endTime = System.currentTimeMillis();

addPerformanceContext(startTime, endTime);
MDC.put("operation.success", "true");
logger.info("Operação concluída");
```

## 🔧 Configuração do Environment

### Variáveis de Ambiente para Datadog

```bash
# Configuração básica
export DD_API_KEY="sua_api_key_aqui"
export DD_SITE="datadoghq.com"  # ou datadoghq.eu
export DD_SERVICE="meu-servico"
export DD_ENV="production"
export DD_VERSION="1.0.0"

# Configuração de logs
export DD_LOGS_ENABLED=true
export DD_LOGS_CONFIG_USE_HTTP=true
export DD_LOGS_CONFIG_COMPRESSION=true
```

### Executando com Datadog Agent

```bash
# Com Java Agent
java -javaagent:dd-java-agent.jar \
     -Ddd.service=meu-servico \
     -Ddd.env=production \
     -Ddd.version=1.0.0 \
     -jar aplicacao.jar

# Ou via Maven
mvn exec:java -Dexec.mainClass="com.example.DatadogLoggingExample"
```

## 📊 Visualização no Datadog

### Event Attributes Disponíveis

Todos os valores do MDC aparecerão como **event attributes** filtráveis:

```json
{
  "@timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO",
  "message": "Processando pagamento",
  "user.id": "12345",
  "payment.amount": "299.99",
  "payment.method": "CREDIT_CARD",
  "operation.type": "PAYMENT_PROCESSING",
  "dd.trace_id": "1234567890",
  "dd.span_id": "9876543210"
}
```

### Filtros Úteis no Datadog

```
# Filtrar por usuário específico
@user.id:12345

# Filtrar por tipo de operação
@operation.type:PAYMENT_PROCESSING

# Filtrar por valor de pagamento
@payment.amount:>100

# Filtrar por método de pagamento
@payment.method:CREDIT_CARD

# Combinar filtros
@user.id:12345 AND @payment.amount:>50 AND @operation.type:PAYMENT*
```

## 🎯 Melhores Práticas

### ✅ Recomendações

1. **Sempre limpe o MDC**: Use `MDC.clear()` ou try-finally
2. **Use chaves padronizadas**: `user.id`, `order.amount`, etc.
3. **Valores como string**: Converta números para string
4. **Context scoping**: Use `withContext()` para escopo automático
5. **Prefixos consistentes**: `user.*`, `order.*`, `payment.*`

### ❌ Evite

1. **Informações sensíveis**: Nunca logue senhas, tokens, CPF
2. **Objetos complexos**: Use apenas strings e números
3. **Muitas variáveis**: Limite a ~10-15 attributes por log
4. **Vazamento de contexto**: Sempre limpe o MDC

## 🔍 Troubleshooting

### Logs não aparecem no Datadog
```bash
# Verificar conectividade
curl -X POST "https://http-intake.logs.datadoghq.com/v1/input/$DD_API_KEY" \
     -H "Content-Type: application/json" \
     -d '{"message":"test","dd.service":"test"}'
```

### Event attributes não aparecem
1. Verifique se está usando `MDC.put()`
2. Confirme configuração do `logback.xml`
3. Verifique se está limpando o MDC corretamente

### Performance Issues
1. Limite o número de attributes
2. Use `withContext()` para escopo automático
3. Evite operações custosas no MDC

## 📋 Executando o Exemplo

```bash
# Compilar
mvn clean compile

# Executar exemplo
mvn exec:java -Dexec.mainClass="com.example.DatadogLoggingExample"

# Com Datadog Agent local
DD_SERVICE=exemplo-java mvn exec:java -Dexec.mainClass="com.example.DatadogLoggingExample"
```

## 📚 Recursos Adicionais

- [Documentação Oficial Datadog Java](https://docs.datadoghq.com/logs/log_collection/java/)
- [SLF4J MDC Documentation](http://www.slf4j.org/manual.html#mdc)
- [Logback Configuration](http://logback.qos.ch/manual/configuration.html)

---

💡 **Dica**: Os event attributes permitem criar dashboards poderosos e alertas precisos baseados nos dados específicos da sua aplicação!
