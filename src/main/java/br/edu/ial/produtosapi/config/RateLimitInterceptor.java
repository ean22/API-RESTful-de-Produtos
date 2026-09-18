package br.edu.ial.produtosapi.config;

import br.edu.ial.produtosapi.service.ServicoRateLimit;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ServicoRateLimit servicoRateLimit;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(ServicoRateLimit servicoRateLimit) {
        this.servicoRateLimit = servicoRateLimit;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipCliente = servicoRateLimit.obterIpCliente(request);

        if (!servicoRateLimit.permitirRequisicao(ipCliente)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> corpoResposta = new HashMap<>();
            corpoResposta.put("timestamp", LocalDateTime.now().toString());
            corpoResposta.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            corpoResposta.put("erro", "Muitas requisicoes");
            corpoResposta.put("mensagem", "Limite de requisicoes excedido. Maximo permitido: " 
                    + servicoRateLimit.obterRequisicoesPorMinuto() + " requisicoes por minuto.");

            response.getWriter().write(objectMapper.writeValueAsString(corpoResposta));
            return false;
        }

        return true;
    }
}
