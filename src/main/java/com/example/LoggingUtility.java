package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import datadog.trace.api.GlobalTracer;
import datadog.trace.api.interceptor.MutableSpan;

import java.util.Map;
import java.util.HashMap;

/**
 * Utilitário para facilitar o logging com custom attributes para o Datadog
 */
public class LoggingUtility {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtility.class);
    
    /**
     * Builder para criar logs estruturados com facilidade
     */
    public static class LogBuilder {
        private final Map<String, String> attributes = new HashMap<>();
        private String message;
        private Object[] args;
        
        public LogBuilder message(String message, Object... args) {
            this.message = message;
            this.args = args;
            return this;
        }
        
        public LogBuilder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }
        
        public LogBuilder attribute(String key, Number value) {
            this.attributes.put(key, value.toString());
            return this;
        }
        
        public LogBuilder userId(String userId) {
            return attribute("user_id", userId);
        }
        
        public LogBuilder sessionId(String sessionId) {
            return attribute("session_id", sessionId);
        }
        
        public LogBuilder requestId(String requestId) {
            return attribute("request_id", requestId);
        }
        
        public LogBuilder transactionId(String transactionId) {
            return attribute("transaction_id", transactionId);
        }
        
        public LogBuilder operation(String operation) {
            return attribute("operation", operation);
        }
        
        public LogBuilder errorCode(String errorCode) {
            return attribute("error_code", errorCode);
        }
        
        public LogBuilder environment(String environment) {
            return attribute("environment", environment);
        }
        
        public void info() {
            logWithAttributes(logger::info);
        }
        
        public void debug() {
            logWithAttributes(logger::debug);
        }
        
        public void warn() {
            logWithAttributes(logger::warn);
        }
        
        public void error() {
            logWithAttributes(logger::error);
        }
        
        public void error(Throwable throwable) {
            logWithAttributes((msg, arguments) -> logger.error(msg, arguments, throwable));
        }
        
        private void logWithAttributes(LogAction logAction) {
            // Salvar o estado atual do MDC
            Map<String, String> originalMdc = MDC.getCopyOfContextMap();
            
            try {
                // Adicionar attributes ao MDC
                attributes.forEach(MDC::put);
                
                // Adicionar attributes ao span atual se disponível
                MutableSpan span = GlobalTracer.get().activeSpan();
                if (span != null) {
                    attributes.forEach(span::setTag);
                }
                
                // Fazer o log
                logAction.log(message, args);
                
            } finally {
                // Restaurar o estado original do MDC
                MDC.clear();
                if (originalMdc != null) {
                    MDC.setContextMap(originalMdc);
                }
            }
        }
        
        @FunctionalInterface
        private interface LogAction {
            void log(String message, Object... args);
        }
    }
    
    /**
     * Cria um novo LogBuilder
     */
    public static LogBuilder log() {
        return new LogBuilder();
    }
    
    /**
     * Método para contexto de usuário - adiciona informações do usuário ao MDC
     */
    public static void setUserContext(String userId, String sessionId) {
        MDC.put("user_id", userId);
        MDC.put("session_id", sessionId);
        
        // Também adicionar ao span atual
        MutableSpan span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag("user_id", userId);
            span.setTag("session_id", sessionId);
        }
    }
    
    /**
     * Método para contexto de requisição
     */
    public static void setRequestContext(String requestId, String operation) {
        MDC.put("request_id", requestId);
        MDC.put("operation", operation);
        
        MutableSpan span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag("request_id", requestId);
            span.setTag("operation", operation);
        }
    }
    
    /**
     * Método para contexto de transação
     */
    public static void setTransactionContext(String transactionId, String transactionType, double amount) {
        MDC.put("transaction_id", transactionId);
        MDC.put("transaction_type", transactionType);
        MDC.put("transaction_amount", String.valueOf(amount));
        
        MutableSpan span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag("transaction_id", transactionId);
            span.setTag("transaction_type", transactionType);
            span.setTag("transaction_amount", amount);
        }
    }
    
    /**
     * Método para contexto de erro
     */
    public static void setErrorContext(String errorCode, String errorCategory, String errorMessage) {
        MDC.put("error_code", errorCode);
        MDC.put("error_category", errorCategory);
        MDC.put("error_message", errorMessage);
        
        MutableSpan span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag("error", true);
            span.setTag("error.msg", errorMessage);
            span.setTag("error_code", errorCode);
            span.setTag("error_category", errorCategory);
        }
    }
    
    /**
     * Limpa todo o contexto
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Executa uma ação com contexto específico e limpa automaticamente
     */
    public static void withContext(Map<String, String> context, Runnable action) {
        Map<String, String> originalMdc = MDC.getCopyOfContextMap();
        
        try {
            context.forEach(MDC::put);
            
            // Adicionar ao span também
            MutableSpan span = GlobalTracer.get().activeSpan();
            if (span != null) {
                context.forEach(span::setTag);
            }
            
            action.run();
            
        } finally {
            MDC.clear();
            if (originalMdc != null) {
                MDC.setContextMap(originalMdc);
            }
        }
    }
}