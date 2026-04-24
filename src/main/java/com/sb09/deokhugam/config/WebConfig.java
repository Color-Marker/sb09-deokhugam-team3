package com.sb09.deokhugam.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
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
}
