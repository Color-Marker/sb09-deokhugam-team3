package com.sb09.deokhugam.config;

import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final UserHeaderInterceptor userHeaderInterceptor;

  @Value("${deokhugam.storage.type}")
  private String storageType;

  @Value("${deokhugam.storage.local.root-path}")
  private String localRootPath;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if ("local".equals(storageType)) {
      String absolutePath = Paths.get(localRootPath).toAbsolutePath().normalize().toString();
      registry.addResourceHandler("/storage/thumbnails/**")
          .addResourceLocations("file:" + absolutePath + "/thumbnails/");
    }
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) { //연결을 담당하는 메서드
    registry.addInterceptor(userHeaderInterceptor) // 문지기를 등록하고
        .addPathPatterns("/api/users/**")          // "/api/users"로 시작하는 모든 길을 감시함
        .excludePathPatterns(                      // 하지만 아래 길은 검사하지 마라
            "/api/users",                          // 회원가입 (아직 ID가 없을 때니까)
            "/api/users/login"                     // 로그인 (ID 확인 전이니까)
        );
  }
}
