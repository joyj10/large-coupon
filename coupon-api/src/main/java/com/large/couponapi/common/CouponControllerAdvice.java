package com.large.couponapi.common;

import com.large.couponapi.controller.dto.CouponIssueResponseDto;
import com.large.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponControllerAdvice {

    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDto couponIssueException(CouponIssueException exception) {
        return new CouponIssueResponseDto(false, exception.getErrorCode().message);
    }

}