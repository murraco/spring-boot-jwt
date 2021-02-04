package murraco.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {

  @Bean
  public ErrorAttributes errorAttributes() {
    // Hide exception field in the return object
    return new DefaultErrorAttributes() {
      @Override
      public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
        errorAttributes.remove("exception");
        return errorAttributes;
      }
    };
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, String>> exception(ValidationException ex) {
    return returnBadRequest(ex.getCause().getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public void handleAccessDeniedException(HttpServletResponse res) throws IOException {
    res.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
  }

  @ExceptionHandler(NotValidPasswordException.class)
  public void handleNotValidPasswordException(HttpServletResponse res) throws IOException {
    res.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid password");
  }

  @ExceptionHandler(Exception.class)
  public void handleException(HttpServletResponse res) throws IOException {
    res.sendError(HttpStatus.BAD_REQUEST.value(), "Something went wrong");
  }

  private Map<String, String> prepareResponse(String error, String solution, String status) {
    // You can define any other class for better visualization for response
    Map<String, String> response = new HashMap<>();
    response.put("Cause", error);
    response.put("Solution", solution);
    response.put("Status", status);
    return response;
  }

  private ResponseEntity<Map<String, String>> returnBadRequest(String message) {
    Map<String, String> response = prepareResponse(
            message,
            "Please enter a valid entity with proper constraints",
            HttpStatus.BAD_REQUEST.toString());
    log.info("Entity is not valid.", message);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

}
