package com.example;

import org.slf4j.MDC;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utilitário para facilitar o uso do MDC (Mapped Diagnostic Context) 
 * com Datadog event attributes
 */
public class LoggingUtil {
    
    // Constantes para chaves padrão do Datadog
    public static final String DD_TRACE_ID = "dd.trace_id";
    public static final String DD_SPAN_ID = "dd.span_id";
    public static final String DD_SERVICE = "dd.service";
    public static final String DD_ENV = "dd.env";
    public static final String DD_VERSION = "dd.version";
    
    // Constantes para chaves customizadas comuns
    public static final String REQUEST_ID = "request.id";
    public static final String USER_ID = "user.id";
    public static final String OPERATION_TYPE = "operation.type";
    public static final String BUSINESS_UNIT = "business.unit";
    public static final String CORRELATION_ID = "correlation.id";
    
    /**
     * Builder para facilitar a criação de contexto estruturado
     */
    public static class ContextBuilder {
        private Map<String, String> context = new HashMap<>();
        
        public ContextBuilder requestId(String requestId) {
            context.put(REQUEST_ID, requestId);
            return this;
        }
        
        public ContextBuilder userId(String userId) {
            context.put(USER_ID, userId);
            return this;
        }
        
        public ContextBuilder operationType(String operationType) {
            context.put(OPERATION_TYPE, operationType);
            return this;
        }
        
        public ContextBuilder businessUnit(String businessUnit) {
            context.put(BUSINESS_UNIT, businessUnit);
            return this;
        }
        
        public ContextBuilder correlationId(String correlationId) {
            context.put(CORRELATION_ID, correlationId);
            return this;
        }
        
        public ContextBuilder service(String service) {
            context.put(DD_SERVICE, service);
            return this;
        }
        
        public ContextBuilder environment(String env) {
            context.put(DD_ENV, env);
            return this;
        }
        
        public ContextBuilder version(String version) {
            context.put(DD_VERSION, version);
            return this;
        }
        
        public ContextBuilder custom(String key, String value) {
            context.put(key, value);
            return this;
        }
        
        public ContextBuilder custom(String key, Number value) {
            context.put(key, value.toString());
            return this;
        }
        
        public ContextBuilder custom(String key, Boolean value) {
            context.put(key, value.toString());
            return this;
        }
        
        /**
         * Aplica o contexto ao MDC
         */
        public void apply() {
            context.forEach(MDC::put);
        }
        
        /**
         * Retorna o mapa de contexto construído
         */
        public Map<String, String> build() {
            return new HashMap<>(context);
        }
    }
    
    /**
     * Cria um novo builder de contexto
     */
    public static ContextBuilder context() {
        return new ContextBuilder();
    }
    
    /**
     * Executa uma operação com contexto específico e limpa automaticamente
     */
    public static void withContext(Map<String, String> context, Runnable operation) {
        try {
            context.forEach(MDC::put);
            operation.run();
        } finally {
            context.keySet().forEach(MDC::remove);
        }
    }
    
    /**
     * Executa uma operação com contexto de request automático
     */
    public static void withRequestContext(String operationType, Runnable operation) {
        withContext(
            context()
                .requestId(generateRequestId())
                .operationType(operationType)
                .correlationId(generateCorrelationId())
                .build(),
            operation
        );
    }
    
    /**
     * Adiciona contexto de usuário
     */
    public static void addUserContext(String userId, String email, String name) {
        MDC.put("user.id", userId);
        if (email != null) MDC.put("user.email", email);
        if (name != null) MDC.put("user.name", name);
    }
    
    /**
     * Adiciona contexto de transação financeira
     */
    public static void addTransactionContext(String transactionId, String type, Double amount, String currency) {
        MDC.put("transaction.id", transactionId);
        MDC.put("transaction.type", type);
        if (amount != null) MDC.put("transaction.amount", amount.toString());
        if (currency != null) MDC.put("transaction.currency", currency);
    }
    
    /**
     * Adiciona contexto de erro estruturado
     */
    public static void addErrorContext(String errorCode, String errorMessage, String errorType) {
        MDC.put("error.code", errorCode);
        MDC.put("error.message", errorMessage);
        MDC.put("error.type", errorType);
    }
    
    /**
     * Adiciona métricas de performance
     */
    public static void addPerformanceContext(long startTime, long endTime) {
        long duration = endTime - startTime;
        MDC.put("performance.start_time", String.valueOf(startTime));
        MDC.put("performance.end_time", String.valueOf(endTime));
        MDC.put("performance.duration_ms", String.valueOf(duration));
    }
    
    /**
     * Adiciona contexto de API
     */
    public static void addApiContext(String endpoint, String method, String version) {
        MDC.put("api.endpoint", endpoint);
        MDC.put("api.method", method);
        if (version != null) MDC.put("api.version", version);
    }
    
    /**
     * Adiciona contexto de banco de dados
     */
    public static void addDatabaseContext(String operation, String table, String database) {
        MDC.put("db.operation", operation);
        MDC.put("db.table", table);
        MDC.put("db.database", database);
    }
    
    /**
     * Limpa todo o contexto MDC
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Remove chaves específicas do contexto
     */
    public static void removeFromContext(String... keys) {
        for (String key : keys) {
            MDC.remove(key);
        }
    }
    
    /**
     * Gera um ID único para request
     */
    public static String generateRequestId() {
        return "REQ-" + UUID.randomUUID().toString();
    }
    
    /**
     * Gera um ID único para correlação
     */
    public static String generateCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString();
    }
    
    /**
     * Obtém o valor atual de uma chave do MDC
     */
    public static String getContextValue(String key) {
        return MDC.get(key);
    }
    
    /**
     * Obtém todo o contexto atual como Map
     */
    public static Map<String, String> getCurrentContext() {
        return MDC.getCopyOfContextMap();
    }
}