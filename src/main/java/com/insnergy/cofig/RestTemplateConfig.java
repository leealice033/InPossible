package com.insnergy.cofig;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insnergy.filter.LogRequestResponseFilter;

@Configuration
public class RestTemplateConfig {
  
  @Bean
  public RestTemplate restTemplate() {
    RestTemplate rest = new RestTemplate();
    rest.getMessageConverters()
        .add(0, mappingJacksonHttpMessageConverter());
    
    // set up a buffering request factory, so response body is always buffered
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(
        requestFactory);
    requestFactory.setOutputStreaming(false);
    rest.setRequestFactory(bufferingClientHttpRequestFactory);
    
    // add the interceptor that will handle logging
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
    interceptors.add(new LogRequestResponseFilter());
    rest.setInterceptors(interceptors);
    
    return rest;
  }
  
  public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(myObjectMapper());
    return converter;
  }
  
  @Bean
  public ObjectMapper myObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }
  
}
