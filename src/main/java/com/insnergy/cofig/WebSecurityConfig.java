package com.insnergy.cofig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.EndpointRequest;
import org.springframework.boot.autoconfigure.security.StaticResourceRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.insnergy.api.CsvFileDownloadApi;
import com.insnergy.service.UserAuthService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Autowired
  private UserAuthService userAuthService;
  
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsServiceBean());
  }
  
  @Override
  public UserDetailsService userDetailsServiceBean() throws Exception {
    return userAuthService;
  }
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    log.debug("configure start={}", http);
    
    http.authorizeRequests()
        .requestMatchers(StaticResourceRequest.toCommonLocations())
        .permitAll()
        
        // for api
        .antMatchers(CsvFileDownloadApi.FILE_ZIP_PATH + "/**")
        .permitAll()
        
        // for signup
        .antMatchers("/signup")
        .permitAll()
        
        // for ACTUATOR
        .requestMatchers(EndpointRequest.to("status", "info"))
        .permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint())
        .hasRole("ACTUATOR")
        
        // admin authorize & authenticate
        .antMatchers("/admin/**")
        .hasRole("ADMIN")
        .anyRequest()
        .authenticated()
        
        // user authorize & authenticate
        .antMatchers("/**")
        .hasRole("USER")
        .anyRequest()
        .authenticated()
        
        // TODO for h2-console, disable when PROD
        .antMatchers("/h2-console/**")
        .hasRole("ADMIN")
        .anyRequest()
        .authenticated()
        
        .and()
        .formLogin()
       // .loginPage("/login")//FIXME
        .permitAll()
        .and()
        .logout()
       // .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .permitAll()
        .and()
        .csrf()
        .disable()
        .headers()
        .frameOptions()
        .disable();
    
    log.debug("configure end={}", http);
  }
  
}