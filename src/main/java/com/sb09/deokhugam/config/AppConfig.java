package com.sb09.deokhugam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableJpaAuditing
public class AppConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  //jackson 컨버터가 application/octet-stream도 처리 가능 하도록 빈 추가
  @Bean
  public MappingJackson2HttpMessageConverter octetStreamJsonConverter(ObjectMapper objectMapper) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
        objectMapper);
    List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
    supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
    converter.setSupportedMediaTypes(supportedMediaTypes);
    return converter;
  }
}