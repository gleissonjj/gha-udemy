package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import datadog.trace.api.DDTags;
import datadog.trace.api.Trace;

import java.util.UUID;

/**
 * Exemplo de como enviar variáveis customizadas para o Datadog como event attributes
 * usando MDC (Mapped Diagnostic Context) do SLF4J
 */
public class DatadogLoggingExample {
    
    private static final Logger logger = LoggerFactory.getLogger(DatadogLoggingExample.class);
    
    public static void main(String[] args) {
        DatadogLoggingExample example = new DatadogLoggingExample();
        
        // Exemplo 1: Processamento de usuário
        example.processUser("12345", "joao.silva@email.com", "João Silva");
        
        // Exemplo 2: Processamento de pedido
        example.processOrder("ORD-789", "12345", 299.99, "PENDING");
        
        // Exemplo 3: Operação com erro
        example.processPayment("PAY-456", "ORD-789", 299.99, false);
    }
    
    /**
     * Exemplo de processamento de usuário com custom attributes
     */
    @Trace(operationName = "user.process", resourceName = "processUser")
    public void processUser(String userId, String email, String name) {
        try {
            // Adicionar variáveis customizadas ao MDC
            MDC.put("user.id", userId);
            MDC.put("user.email", email);
            MDC.put("user.name", name);
            MDC.put("operation.type", "USER_PROCESSING");
            MDC.put("request.id", UUID.randomUUID().toString());
            
            logger.info("Iniciando processamento do usuário");
            
            // Simulação de processamento
            Thread.sleep(100);
            
            // Log de sucesso com informações adicionais
            MDC.put("processing.status", "SUCCESS");
            MDC.put("processing.duration_ms", "100");
            logger.info("Usuário processado com sucesso");
            
        } catch (Exception e) {
            MDC.put("processing.status", "ERROR");
            MDC.put("error.message", e.getMessage());
            logger.error("Erro ao processar usuário", e);
        } finally {
            // Limpar MDC para evitar vazamento de contexto
            MDC.clear();
        }
    }
    
    /**
     * Exemplo de processamento de pedido com métricas customizadas
     */
    @Trace(operationName = "order.process", resourceName = "processOrder")
    public void processOrder(String orderId, String userId, double amount, String status) {
        try {
            // Context variables para o Datadog
            MDC.put("order.id", orderId);
            MDC.put("order.user_id", userId);
            MDC.put("order.amount", String.valueOf(amount));
            MDC.put("order.status", status);
            MDC.put("order.currency", "BRL");
            MDC.put("business.unit", "ecommerce");
            MDC.put("operation.type", "ORDER_PROCESSING");
            
            // Tags customizadas para APM
            MDC.put(DDTags.RESOURCE_NAME, "order-" + orderId);
            MDC.put("dd.service", "order-service");
            
            logger.info("Processando pedido");
            
            // Simulação de validação
            if (amount > 0) {
                MDC.put("validation.result", "PASSED");
                logger.debug("Validação do pedido aprovada");
            } else {
                MDC.put("validation.result", "FAILED");
                MDC.put("validation.error", "INVALID_AMOUNT");
                logger.warn("Validação do pedido falhou: valor inválido");
            }
            
            // Metrics customizadas
            MDC.put("metrics.order_value", String.valueOf(amount));
            MDC.put("metrics.processing_time", "250");
            
            logger.info("Pedido processado com sucesso");
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Exemplo de processamento de pagamento com tratamento de erro
     */
    @Trace(operationName = "payment.process", resourceName = "processPayment")
    public void processPayment(String paymentId, String orderId, double amount, boolean shouldSucceed) {
        try {
            // Variáveis de contexto para pagamento
            MDC.put("payment.id", paymentId);
            MDC.put("payment.order_id", orderId);
            MDC.put("payment.amount", String.valueOf(amount));
            MDC.put("payment.method", "CREDIT_CARD");
            MDC.put("payment.provider", "stripe");
            MDC.put("operation.type", "PAYMENT_PROCESSING");
            
            // Informações de ambiente
            MDC.put("env.region", "sa-east-1");
            MDC.put("env.datacenter", "aws-sao-paulo");
            
            logger.info("Iniciando processamento de pagamento");
            
            if (!shouldSucceed) {
                // Simulação de erro
                MDC.put("payment.status", "FAILED");
                MDC.put("payment.error_code", "INSUFFICIENT_FUNDS");
                MDC.put("payment.error_message", "Saldo insuficiente");
                
                logger.error("Falha no processamento do pagamento");
                throw new RuntimeException("Pagamento falhou");
            }
            
            // Sucesso
            MDC.put("payment.status", "APPROVED");
            MDC.put("payment.transaction_id", "TXN-" + UUID.randomUUID().toString());
            MDC.put("payment.approval_code", "123456");
            
            logger.info("Pagamento processado com sucesso");
            
        } catch (Exception e) {
            MDC.put("error.type", e.getClass().getSimpleName());
            MDC.put("error.message", e.getMessage());
            logger.error("Erro crítico no processamento de pagamento", e);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Exemplo de como adicionar informações de contexto de forma estruturada
     */
    public static void addBusinessContext(String businessUnit, String feature, String version) {
        MDC.put("business.unit", businessUnit);
        MDC.put("feature.name", feature);
        MDC.put("feature.version", version);
        MDC.put("correlation.id", UUID.randomUUID().toString());
    }
    
    /**
     * Exemplo de como adicionar informações técnicas
     */
    public static void addTechnicalContext(String component, String method, long startTime) {
        MDC.put("component.name", component);
        MDC.put("method.name", method);
        MDC.put("performance.start_time", String.valueOf(startTime));
        MDC.put("thread.name", Thread.currentThread().getName());
    }
}