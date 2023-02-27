package com.hyperlink.server.global.exception;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
    ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<String> handledException(BusinessException e) {
    log.info(e.getMessage(), e);
    HttpStatus httpStatus = e.getStatus();
    return ResponseEntity.status(httpStatus).body(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<String> handleBindException() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청하신 필드값의 유효성이 잘못되었습니다.");
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequestBody() {
    ErrorResponse errorResponse = new ErrorResponse("잘못된 형식의 Request Body 입니다!");
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch() {
    ErrorResponse errorResponse = new ErrorResponse("잘못된 데이터 타입입니다!");
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleNotSupportedMethod() {
    ErrorResponse errorResponse = new ErrorResponse("지원하지 않는 HTTP 메소드 요청입니다!");
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(final Exception e,
      final HttpServletRequest request) {
    ErrorReportRequest errorReport = new ErrorReportRequest(request, e);
    log.error(errorReport.getLogMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse("예상 하지 못한 서버 에러가 발생하였습니다.");
    return ResponseEntity.internalServerError().body(errorResponse);
  }
}
