package com.zhangtao.controller;

import com.zhangtao.entity.ResponseBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:58
 * @description : 全局错误处理，可进一步细分
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Log4j2
@Component
@RestControllerAdvice
public class ZtExceptionController extends Throwable {
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBean handleGlobalException(Exception e) {
        log.error("handleGlobalException:" + e.getMessage());

        return ResponseBean.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .msg("fail")
                .data("发生异常，请稍后再试")
                .build();
    }

    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseBean handleConstraintViolationException(javax.validation.ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> fieldError = ex.getConstraintViolations();
        log.error("handleConstraintViolationException:" + ex.getMessage());

        return ResponseBean.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .msg("fail")
                .data(fieldError.toString())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBean handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException:" + e.getMessage());
        BindingResult result = e.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();
        List<String> errorMsg = new ArrayList<>();

        for (FieldError error : fieldErrors) {
            errorMsg.add(error.getRejectedValue() + "," + error.getDefaultMessage());
        }

        return ResponseBean.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .msg("fail")
                .data(errorMsg)
                .build();
    }
}
