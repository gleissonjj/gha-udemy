package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * Exemplos práticos de como usar o logging com Datadog em diferentes cenários
 */
public class ExemplosUsoPratico {
    
    private static final Logger logger = LoggerFactory.getLogger(ExemplosUsoPratico.class);
    
    public static void main(String[] args) {
        ExemplosUsoPratico exemplos = new ExemplosUsoPratico();
        
        exemplos.exemploProcessamentoPedido();
        exemplos.exemploTransacaoFinanceira();
        exemplos.exemploAutenticacaoUsuario();
        exemplos.exemploIntegracaoExterna();
    }
    
    /**
     * Exemplo: Processamento de pedido de e-commerce
     */
    public void exemploProcessamentoPedido() {
        String pedidoId = "PED-2024-001";
        String userId = "user_12345";
        String sessionId = "sess_abcdef";
        
        // Configurar contexto para toda a operação
        LoggingUtility.setUserContext(userId, sessionId);
        LoggingUtility.setRequestContext("req_" + System.currentTimeMillis(), "processar_pedido");
        
        // Log inicial com contexto
        LoggingUtility.log()
            .message("Iniciando processamento do pedido {}")
            .attribute("pedido_id", pedidoId)
            .attribute("canal", "web")
            .attribute("origem", "checkout")
            .info();
        
        try {
            // Validação do pedido
            validarPedido(pedidoId, userId);
            
            // Cálculo de preços
            calcularPrecos(pedidoId, 3, 299.99);
            
            // Processamento do pagamento
            processarPagamento(pedidoId, "cartao_credito", 899.97);
            
            // Atualização do estoque
            atualizarEstoque(pedidoId);
            
            // Log de sucesso
            LoggingUtility.log()
                .message("Pedido processado com sucesso")
                .attribute("pedido_id", pedidoId)
                .attribute("status", "aprovado")
                .attribute("valor_total", "899.97")
                .attribute("tempo_processamento", "2350ms")
                .info();
                
        } catch (Exception e) {
            // Log de erro com contexto detalhado
            LoggingUtility.setErrorContext("PED_001", "processamento_falhou", e.getMessage());
            
            LoggingUtility.log()
                .message("Falha no processamento do pedido: {}")
                .attribute("pedido_id", pedidoId)
                .attribute("etapa_falha", "pagamento")
                .error(e);
        } finally {
            LoggingUtility.clearContext();
        }
    }
    
    /**
     * Exemplo: Transação financeira
     */
    public void exemploTransacaoFinanceira() {
        String transacaoId = "TXN-" + System.currentTimeMillis();
        String contaOrigem = "123456789";
        String contaDestino = "987654321";
        double valor = 1500.00;
        
        // Configurar contexto da transação
        LoggingUtility.setTransactionContext(transacaoId, "transferencia", valor);
        LoggingUtility.setUserContext("user_financial_001", "sess_banking_123");
        
        try {
            // Log início da transação
            LoggingUtility.log()
                .message("Iniciando transferência bancária")
                .attribute("conta_origem", contaOrigem)
                .attribute("conta_destino", contaDestino)
                .attribute("canal_transacao", "mobile_app")
                .attribute("ip_origem", "192.168.1.100")
                .info();
            
            // Validação de saldo
            validarSaldo(contaOrigem, valor);
            
            // Validação de limites
            validarLimites("user_financial_001", valor);
            
            // Executar transferência
            executarTransferencia(transacaoId, contaOrigem, contaDestino, valor);
            
            // Log de sucesso
            LoggingUtility.log()
                .message("Transferência realizada com sucesso")
                .attribute("status_transacao", "concluida")
                .attribute("codigo_autorizacao", "AUTH_123456")
                .attribute("taxa_aplicada", "2.50")
                .info();
                
        } catch (Exception e) {
            LoggingUtility.setErrorContext("TXN_ERROR", "transferencia_falhada", e.getMessage());
            
            LoggingUtility.log()
                .message("Erro na transferência: {}")
                .attribute("motivo_falha", determinarMotivoFalha(e))
                .attribute("codigo_retorno", "ERR_TXN_001")
                .error(e);
        } finally {
            LoggingUtility.clearContext();
        }
    }
    
    /**
     * Exemplo: Autenticação de usuário
     */
    public void exemploAutenticacaoUsuario() {
        String email = "usuario@exemplo.com";
        String sessionId = "auth_sess_" + System.currentTimeMillis();
        
        // Contexto de autenticação
        Map<String, String> contextAuth = new HashMap<>();
        contextAuth.put("operation", "login");
        contextAuth.put("user_email", email);
        contextAuth.put("session_id", sessionId);
        contextAuth.put("client_ip", "203.0.113.1");
        contextAuth.put("user_agent", "Mozilla/5.0 Chrome/91.0");
        
        LoggingUtility.withContext(contextAuth, () -> {
            try {
                // Log tentativa de login
                LoggingUtility.log()
                    .message("Tentativa de autenticação")
                    .attribute("metodo_auth", "email_password")
                    .attribute("origem", "web_app")
                    .info();
                
                // Validar credenciais
                boolean credenciaisValidas = validarCredenciais(email, "senha123");
                
                if (credenciaisValidas) {
                    // Gerar token
                    String token = gerarToken(email);
                    
                    LoggingUtility.log()
                        .message("Autenticação bem-sucedida")
                        .attribute("user_id", "user_auth_456")
                        .attribute("token_type", "JWT")
                        .attribute("expires_in", "3600")
                        .attribute("permissions", "read,write")
                        .info();
                } else {
                    LoggingUtility.setErrorContext("AUTH_001", "credenciais_invalidas", "Email ou senha incorretos");
                    
                    LoggingUtility.log()
                        .message("Falha na autenticação")
                        .attribute("tentativa_numero", "1")
                        .attribute("bloqueio_temporario", "false")
                        .warn();
                }
                
            } catch (Exception e) {
                LoggingUtility.setErrorContext("AUTH_SYS", "erro_sistema", e.getMessage());
                
                LoggingUtility.log()
                    .message("Erro no sistema de autenticação: {}")
                    .attribute("componente_falha", "auth_service")
                    .error(e);
            }
        });
    }
    
    /**
     * Exemplo: Integração com serviço externo
     */
    public void exemploIntegracaoExterna() {
        String requestId = "EXT_REQ_" + System.currentTimeMillis();
        String serviceName = "payment_gateway";
        
        LoggingUtility.setRequestContext(requestId, "chamada_externa");
        
        try {
            // Log antes da chamada
            LoggingUtility.log()
                .message("Iniciando chamada para serviço externo")
                .attribute("service_name", serviceName)
                .attribute("endpoint", "https://api.payment.com/v1/charge")
                .attribute("timeout", "30000")
                .attribute("retry_count", "0")
                .info();
            
            long startTime = System.currentTimeMillis();
            
            // Simular chamada externa
            String response = chamarServicoExterno(serviceName, requestId);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Log de sucesso
            LoggingUtility.log()
                .message("Chamada externa bem-sucedida")
                .attribute("response_time", String.valueOf(duration))
                .attribute("response_size", "1024")
                .attribute("status_code", "200")
                .attribute("transaction_ref", "EXT_TXN_789")
                .info();
                
        } catch (Exception e) {
            LoggingUtility.setErrorContext("EXT_001", "servico_indisponivel", e.getMessage());
            
            LoggingUtility.log()
                .message("Falha na integração externa: {}")
                .attribute("service_name", serviceName)
                .attribute("error_type", "timeout")
                .attribute("retry_scheduled", "true")
                .attribute("next_retry", "300000")
                .error(e);
        } finally {
            LoggingUtility.clearContext();
        }
    }
    
    // Métodos auxiliares para simular operações
    private void validarPedido(String pedidoId, String userId) {
        LoggingUtility.log()
            .message("Validando pedido")
            .attribute("validacao_tipo", "estrutural")
            .debug();
    }
    
    private void calcularPrecos(String pedidoId, int quantidade, double precoUnitario) {
        LoggingUtility.log()
            .message("Calculando preços")
            .attribute("quantidade", String.valueOf(quantidade))
            .attribute("preco_unitario", String.valueOf(precoUnitario))
            .debug();
    }
    
    private void processarPagamento(String pedidoId, String metodoPagamento, double valor) {
        LoggingUtility.log()
            .message("Processando pagamento")
            .attribute("metodo", metodoPagamento)
            .attribute("valor", String.valueOf(valor))
            .debug();
    }
    
    private void atualizarEstoque(String pedidoId) {
        LoggingUtility.log()
            .message("Atualizando estoque")
            .attribute("operacao", "decrementar")
            .debug();
    }
    
    private void validarSaldo(String conta, double valor) {
        // Simular validação
    }
    
    private void validarLimites(String userId, double valor) {
        // Simular validação de limites
    }
    
    private void executarTransferencia(String transacaoId, String origem, String destino, double valor) {
        // Simular transferência
    }
    
    private String determinarMotivoFalha(Exception e) {
        return "saldo_insuficiente"; // Simplificado
    }
    
    private boolean validarCredenciais(String email, String senha) {
        return true; // Simplificado
    }
    
    private String gerarToken(String email) {
        return "jwt_token_example_123";
    }
    
    private String chamarServicoExterno(String serviceName, String requestId) {
        // Simular chamada externa
        return "response_data";
    }
}