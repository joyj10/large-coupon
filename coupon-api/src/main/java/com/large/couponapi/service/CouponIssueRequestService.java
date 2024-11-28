package com.large.couponapi.service;

import com.large.couponapi.controller.dto.CouponIssueRequestDto;
import com.large.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {
    private final CouponIssueService couponIssueService;

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        log.info("쿠폰 발급 완료. couponI:d %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
}
