package com.insnergy.cofig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Ref: http://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
  
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2).select()
                                                  .apis(RequestHandlerSelectors.basePackage("com.insnergy.api"))
                                                  .paths(PathSelectors.any())
                                                  .build()
                                                  .apiInfo(apiInfo());
  }
  
  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("InAnalysis API")
                               .description("InAnalysis API Description")
                               .version("1.0.0")
                               .build();
  }
  
}