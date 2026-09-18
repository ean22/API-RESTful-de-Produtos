package br.edu.ial.produtosapi.service;

import br.edu.ial.produtosapi.config.BaldeRequisicoes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServicoRateLimit {

    private final long requisicoesPorMinuto;
    private final Map<String, BaldeRequisicoes> baldesPorIp = new ConcurrentHashMap<>();

    public ServicoRateLimit(@Value("${rate.limit.produtos.requisicoes-por-minuto}") long requisicoesPorMinuto) {
        this.requisicoesPorMinuto = requisicoesPorMinuto;
    }

    public boolean permitirRequisicao(String chaveIp) {
        BaldeRequisicoes balde = baldesPorIp.computeIfAbsent(
                chaveIp,
                k -> new BaldeRequisicoes(requisicoesPorMinuto)
        );
        return balde.tentarConsumir();
    }

    public String obterIpCliente(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public void limparBaldes() {
        baldesPorIp.clear();
    }

    public long obterRequisicoesPorMinuto() {
        return requisicoesPorMinuto;
    }
}
