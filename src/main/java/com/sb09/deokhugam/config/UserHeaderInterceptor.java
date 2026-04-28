package com.sb09.deokhugam.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//이 인터셉터는 문지기 역할을 함. /api/users/1 이런 요청이 들어오면 스프링이 컨트롤러에게 보내기 전 헤더가 있는지 없는지 검사함
@Component
public class UserHeaderInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    //요청이 GET 방식(조회)이면 검사하지 않고 통과하고
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    // 그 외(PATCH, DELETE 등)는 헤더가 있는지 검사함
    String header = request.getHeader("Deokhugam-Request-User-ID");
    if (header == null || header.isBlank()) {
      throw new IllegalArgumentException("필수 헤더가 누락되었습니다.");
    }
    return true; //헤더가 있으면 통과
  }
}
