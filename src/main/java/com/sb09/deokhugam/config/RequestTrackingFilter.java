package com.sb09.deokhugam.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

// 수업 자료에서 가져왔습니다.
@Component
public class RequestTrackingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String traceId = UUID.randomUUID().toString().substring(0, 8);
    // Cookie와 Sesseion에서 배운 Session
    String sessionId = ((HttpServletRequest) request).getSession().getId();
    String userIp = getClientIp((HttpServletRequest) request);

    try {

      // MDC에 컨텍스트 정보 설정
      MDC.put("traceId", traceId);
      MDC.put("sessionId", sessionId.length() >= 8 ? sessionId.substring(0, 8) : sessionId);
      MDC.put("userIp", userIp);

      ((HttpServletResponse) response).setHeader("X-Request-ID", traceId);
      // 다음 필터, 또는 원본 서블릿으로 요청을 흘려보냄
      chain.doFilter(request, response);

    } finally {
      // 메모리 누수 방지를 위해 MDC 정리
      MDC.clear();
    }
  }
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
      return ip.split(",")[0].trim();
    }
    ip = request.getHeader("X-Real-IP");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
      return ip;
    }
    return request.getRemoteAddr();
  }
}