package io.github.limuqy.easyweb.config;


import io.github.limuqy.easyweb.core.exception.BusinessException;
import io.github.limuqy.easyweb.core.exception.ErrorException;
import io.github.limuqy.easyweb.core.exception.RowIdException;
import io.github.limuqy.easyweb.core.util.TraceIdUtil;
import io.github.limuqy.easyweb.mybitis.base.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @Value("${spring.application.name}")
    private String serverName;

    @ExceptionHandler(value = Exception.class)
    public RestResponse<?> errorHandler(Exception e) {
        String msg = String.format("系统发生未知异常, 服务名 [%s], 异常ID [%s]", serverName, TraceIdUtil.getTraceId());
        log.error(msg, e);
        return RestResponse.fail(msg);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public RestResponse<?> errorHandler(IllegalArgumentException e) {
        log.error("非法参数错误：", e);
        return RestResponse.fail(e.getMessage());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public RestResponse<?> accessHandler(AccessDeniedException e) {
        log.error("拒绝访问：", e);
        return RestResponse.fail(HttpStatus.UNAUTHORIZED, "auth.error");
    }

    @ExceptionHandler(value = BusinessException.class)
    public RestResponse<?> errorHandler(BusinessException e) {
        return RestResponse.fail(e.getMessage());
    }

    @ExceptionHandler(value = ErrorException.class)
    public RestResponse<?> errorHandler(ErrorException e) {
        return RestResponse.fail(e.getMessage());
    }

    /**
     * 校验异常返回拦截
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResponse<?> handleValidException(MethodArgumentNotValidException e) {
        Method method = e.getParameter().getMethod();
        List<FieldError> fieldErrors = e.getFieldErrors();
        String validMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(";"));
        if (method != null) {
            log.error("{}.{}: ValidException", method.getDeclaringClass().getName(), method.getName());
        }
        log.error("校验错误：{}", validMessage);
        return RestResponse.fail(validMessage);
    }

    @ExceptionHandler(value = RowIdException.class)
    public RestResponse<?> errorHandler(RowIdException e) {
        log.error("RowId处理异常：", e);
        return RestResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public RestResponse<?> errorHandler(NoResourceFoundException e) {
        log.error("请求路径异常：", e);
        return RestResponse.fail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public RestResponse<?> errorHandler(HttpRequestMethodNotSupportedException e) {
        log.error("方法异常：", e);
        return RestResponse.fail(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public RestResponse<?> errorHandler(HttpMessageNotReadableException e) {
        String message = e.getMessage();
        if (message.startsWith("JSON parse error: Cannot deserialize value of type `java.lang.Long` from String")) {
            String rowId = message.substring(message.lastIndexOf("from String \"") + 13, message.lastIndexOf("\": not a valid `java.lang.Long` value"));
            return RestResponse.fail(HttpStatus.BAD_REQUEST, "Entity not have an encrypted ID configured => " + rowId);
        }
        log.error("参数异常：", e);
        return RestResponse.fail(HttpStatus.BAD_REQUEST, message);
    }


}
