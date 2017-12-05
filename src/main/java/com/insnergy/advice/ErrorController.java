package com.insnergy.advice;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ErrorController {
  
  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String exception(final Throwable throwable, final Model model) {
    log.error("Exception during execution of SpringSecurity application", throwable);
    String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");
    model.addAttribute("errorMessage", errorMessage);
    model.addAttribute("stackTrace", ExceptionUtils.getStackTrace(throwable));
    return "error";
  }
  
}