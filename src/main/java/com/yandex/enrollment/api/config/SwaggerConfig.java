package com.yandex.enrollment.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Класс для конфигурации сваггера
 */
@Configuration
public class SwaggerConfig {

//  @Bean
//  public GroupedOpenApi publicUserApi() {
//    return GroupedOpenApi.builder()
//        .group("Users")
//        .pathsToMatch("/nodes/**")
//        .build();
//  }

  @Bean
  public OpenAPI customOpenApi(@Value("${application-title}") String appTitle,
      @Value("${application-version}") String appVersion) {
    return new OpenAPI().info(new Info().title(appTitle)
            .version(appVersion)
            .description("Вступительное задание в Летнюю Школу Бэкенд Разработки Яндекса 2022"))
        .servers(List.of(new Server().url("http://localhost:80")
            .description("Prod service")));
  }
}
