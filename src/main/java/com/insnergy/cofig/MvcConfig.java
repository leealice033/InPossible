package com.insnergy.cofig;

import org.h2.server.web.WebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
  
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/project")
            .setViewName("project");
    
//    registry.addViewController("/login")
//            .setViewName("login");
    
    registry.addViewController("/docs")
            .setViewName("document");
  }
  
  @Bean
  public ServletRegistrationBean<WebServlet> h2servletRegistration() {
    ServletRegistrationBean<WebServlet> registrationBean = new ServletRegistrationBean<>(new WebServlet());
    registrationBean.addUrlMappings("/h2-console/*");
    return registrationBean;
  }
  
}