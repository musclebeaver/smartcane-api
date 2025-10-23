package com.smartcane.api.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----- 공통 에러 응답 포맷 -----
    public static final class ErrorDetail {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public ErrorDetail(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        public String getField() { return field; }
        public String getMessage() { return message; }
        public Object getRejectedValue() { return rejectedValue; }
    }

    public static final class ErrorResponse {
        private final String path;
        private final int status;
        private final String code;
        private final String message;
        private final List<ErrorDetail> errors;
        private final OffsetDateTime timestamp;

        private ErrorResponse(String path, int status, String code, String message, List<ErrorDetail> errors, OffsetDateTime timestamp) {
            this.path = path;
            this.status = status;
            this.code = code;
            this.message = message;
            this.errors = errors == null ? Collections.emptyList() : errors;
            this.timestamp = timestamp;
        }

        public static ErrorResponse of(HttpServletRequest req, HttpStatus status, String code, String message, List<ErrorDetail> errors) {
            return new ErrorResponse(
                    req != null ? req.getRequestURI() : "",
                    status.value(),
                    code,
                    message,
                    errors,
                    OffsetDateTime.now()
            );
        }

        public String getPath() { return path; }
        public int getStatus() { return status; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public List<ErrorDetail> getErrors() { return errors; }
        public OffsetDateTime getTimestamp() { return timestamp; }
    }

    // ============ 400: @Valid 바디 검증 실패 ============
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest req) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorDetail)
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.of(req, HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR", "검증 실패", details);
        return ResponseEntity.badRequest().body(body);
    }

    // ============ 400: @Validated + Param/Path 검증 실패 ============
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest req) {
        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(this::toErrorDetail)
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.of(req, HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION", "검증 실패", details);
        return ResponseEntity.badRequest().body(body);
    }

    // ============ 400: 바인딩 실패(Form/Query -> Object) ============
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex,
                                                             HttpServletRequest req) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorDetail)
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.of(req, HttpStatus.BAD_REQUEST,
                "BINDING_ERROR", "요청 바인딩 실패", details);
        return ResponseEntity.badRequest().body(body);
    }

    // ============ 400: PathVariable/RequestParam 타입 변환 실패 (ex. UUID 파싱 오류) ============
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest req) {
        // 어떤 파라미터가 어떤 값으로 전달되었는지 응답에 포함하여 클라이언트가 원인 파악을 쉽게 하도록 함.
        String field = ex.getName();
        Object rejected = ex.getValue();
        String expectedType = Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("요구되는 형식");

        ErrorDetail detail = new ErrorDetail(
                field,
                String.format("%s(으)로 해석할 수 없는 값입니다", expectedType),
                rejected
        );

        ErrorResponse body = ErrorResponse.of(
                req,
                HttpStatus.BAD_REQUEST,
                "TYPE_MISMATCH",
                String.format("요청 파라미터 '%s'가 올바른 %s 형식이 아닙니다.", field, expectedType),
                List.of(detail)
        );

        return ResponseEntity.badRequest().body(body);
    }

    // ============ 400: JSON 파싱 오류 ============
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(req, HttpStatus.BAD_REQUEST,
                "MESSAGE_NOT_READABLE", "요청 본문을 읽을 수 없습니다 (JSON 형식 오류 등)", null);
        return ResponseEntity.badRequest().body(body);
    }

    // ============ 400: 필수 파라미터 누락 ============
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                            HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(req, HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                String.format("필수 파라미터 누락: %s", ex.getParameterName()),
                List.of(new ErrorDetail(ex.getParameterName(), "필수 파라미터입니다", null)));
        return ResponseEntity.badRequest().body(body);
    }

    // ============ 401: 인증 오류 (Spring Security 사용 시) ============
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                                                              HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(req, HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED", "인증이 필요합니다", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // ============ 403: 권한 오류 ============
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(req, HttpStatus.FORBIDDEN,
                "FORBIDDEN", "접근 권한이 없습니다", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ============ 4xx/5xx: ResponseStatusException ============
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                              HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        String message = ex.getReason() != null ? ex.getReason() : "요청 처리 중 오류가 발생했습니다";
        ErrorResponse body = ErrorResponse.of(req, status, status.name(), message, null);
        return ResponseEntity.status(status).body(body);
    }

    // ============ 마지막: 예상 못한 예외는 500 ============
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex,
                                                         HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(req, HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ----- 매핑 유틸 -----
    private ErrorDetail toErrorDetail(FieldError e) {
        return new ErrorDetail(
                e.getField(),
                Optional.ofNullable(e.getDefaultMessage()).orElse("유효하지 않은 값입니다"),
                e.getRejectedValue()
        );
    }

    private ErrorDetail toErrorDetail(ConstraintViolation<?> v) {
        // propertyPath: "signup.arg0.email" 같은 형태 → 마지막 노드만 사용
        String field = Optional.ofNullable(v.getPropertyPath()).map(Object::toString).orElse("");
        if (field.contains(".")) field = field.substring(field.lastIndexOf('.') + 1);
        return new ErrorDetail(
                field,
                Optional.ofNullable(v.getMessage()).orElse("유효하지 않은 값입니다"),
                v.getInvalidValue()
        );
    }
}
