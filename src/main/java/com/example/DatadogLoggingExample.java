package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import datadog.trace.api.CorrelationIdentifier;
import datadog.trace.api.Trace;
import datadog.trace.api.DDTags;
import datadog.trace.api.interceptor.MutableSpan;
import datadog.trace.api.GlobalTracer;

import java.util.Map;
import java.util.HashMap;

/**
 * Exemplo de como enviar variáveis de log para o Datadog como event attributes
 */
public class DatadogLoggingExample {
    
    private static final Logger logger = LoggerFactory.getLogger(DatadogLoggingExample.class);
    
    public static void main(String[] args) {
        DatadogLoggingExample example = new DatadogLoggingExample();
        
        // Exemplo 1: Usando MDC para variáveis contextuais
        example.exemploComMDC();
        
        // Exemplo 2: Usando custom attributes em spans
        example.exemploComCustomAttributes();
        
        // Exemplo 3: Logging estruturado com variáveis específicas
        example.exemploLoggingEstruturado();
        
        // Exemplo 4: Tratamento de erro com contexto
        example.exemploTratamentoErro();
    }
    
    /**
     * Exemplo usando MDC (Mapped Diagnostic Context) para adicionar variáveis contextuais
     * Essas variáveis aparecerão automaticamente nos logs e no Datadog
     */
    public void exemploComMDC() {
        try {
            // Adicionando variáveis ao contexto MDC
            MDC.put("userId", "user123");
            MDC.put("sessionId", "session-abc-456");
            MDC.put("requestId", "req-789");
            MDC.put("operacao", "processamento_pedido");
            MDC.put("ambiente", "producao");
            
            logger.info("Iniciando processamento do pedido");
            
            // Simulando algum processamento
            processarPedido("pedido-001", 150.75);
            
            logger.info("Pedido processado com sucesso");
            
        } finally {
            // Importante: limpar o MDC após o uso
            MDC.clear();
        }
    }
    
    /**
     * Exemplo usando custom attributes em spans do Datadog APM
     */
    @Trace(operationName = "processar_pedido_custom")
    public void exemploComCustomAttributes() {
        // Obtendo o span atual
        MutableSpan span = GlobalTracer.get().activeSpan();
        
        if (span != null) {
            // Adicionando custom attributes que aparecerão no Datadog
            span.setTag("pedido.id", "pedido-002");
            span.setTag("pedido.valor", 299.99);
            span.setTag("pedido.categoria", "eletronicos");
            span.setTag("cliente.tier", "premium");
            span.setTag("metodo.pagamento", "cartao_credito");
            span.setTag(DDTags.USER_ID, "user456");
            
            // Também podemos adicionar ao MDC para aparecer nos logs
            MDC.put("pedido.id", "pedido-002");
            MDC.put("pedido.valor", "299.99");
            MDC.put("cliente.tier", "premium");
        }
        
        logger.info("Processando pedido com custom attributes");
        
        // Simulando processamento
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("Custom attributes processadas");
        MDC.clear();
    }
    
    /**
     * Exemplo de logging estruturado com variáveis específicas
     */
    public void exemploLoggingEstruturado() {
        String userId = "user789";
        String operacao = "consulta_saldo";
        double saldoAtual = 1250.50;
        String tipoTransacao = "consulta";
        
        // Usando MDC para contexto
        MDC.put("user_id", userId);
        MDC.put("operacao", operacao);
        MDC.put("tipo_transacao", tipoTransacao);
        
        // Log estruturado com informações específicas
        logger.info("Operação bancária realizada - saldo_atual: {}, timestamp: {}", 
                   saldoAtual, System.currentTimeMillis());
        
        // Adicionando mais contexto para operações específicas
        MDC.put("saldo_anterior", "1000.00");
        MDC.put("diferenca", "250.50");
        
        logger.debug("Detalhes da operação processados");
        
        MDC.clear();
    }
    
    /**
     * Exemplo de tratamento de erro com contexto detalhado
     */
    public void exemploTratamentoErro() {
        String transacaoId = "txn-error-123";
        String userId = "user-error-456";
        
        try {
            MDC.put("transacao_id", transacaoId);
            MDC.put("user_id", userId);
            MDC.put("operacao", "transferencia");
            
            // Simulando uma operação que pode falhar
            realizarTransferencia(1000.0, "conta-destino-invalida");
            
        } catch (Exception e) {
            // Adicionando contexto do erro
            MDC.put("erro_codigo", "TF001");
            MDC.put("erro_categoria", "conta_invalida");
            MDC.put("tentativa_numero", "1");
            
            logger.error("Erro durante transferência: {}", e.getMessage(), e);
            
            // Reportando métricas customizadas para o Datadog
            reportarMetricaErro("transferencia_falhada", transacaoId);
            
        } finally {
            MDC.clear();
        }
    }
    
    @Trace(operationName = "processar_pedido")
    private void processarPedido(String pedidoId, double valor) {
        // Adicionando tags específicas ao span
        MutableSpan span = GlobalTracer.get().activeSpan();
        if (span != null) {
            span.setTag("pedido.id", pedidoId);
            span.setTag("pedido.valor", valor);
        }
        
        logger.debug("Processando pedido {} com valor {}", pedidoId, valor);
        
        // Simulando processamento
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void realizarTransferencia(double valor, String contaDestino) throws Exception {
        if ("conta-destino-invalida".equals(contaDestino)) {
            throw new IllegalArgumentException("Conta de destino inválida: " + contaDestino);
        }
        
        logger.info("Transferência realizada: valor={}, destino={}", valor, contaDestino);
    }
    
    private void reportarMetricaErro(String metrica, String transacaoId) {
        // Aqui você pode integrar com a API de métricas do Datadog
        // Por exemplo, usando DogStatsD
        logger.warn("Métrica de erro reportada: {} para transação {}", metrica, transacaoId);
    }
}