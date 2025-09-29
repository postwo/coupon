package com.example.coupon_core.model;

import com.example.coupon_core.exception.CouponIssueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.example.coupon_core.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.example.coupon_core.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    @DisplayName("발급 수량이 남아있다면 true를 반환")
    void availableIssueQuantity_test1()  {
        // given
        Coupon  coupon = Coupon.builder()
                .totalQuantity(100) // 총 100개 쿠폰 발급
                .issuedQuantity(99) // 발급된 쿠폰 수량 99개 1개남음
                .build();
        // when(검증)
        boolean result = coupon.availableIssueQuantity();
        // then(결과)
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 수량이 다 소진되었다면 false를 반환")
    void availableIssueQuantity_test2()  {
        // given
        Coupon  coupon = Coupon.builder()
                .totalQuantity(100) // 총 100개 쿠폰 발급
                .issuedQuantity(100) // 발급된 쿠폰 수량 100개 0개남음
                .build();
        // when(검증)
        boolean result = coupon.availableIssueQuantity();
        // then(결과)
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("최대 발급 수량이 설정되지 않았다면 true를 반환")
    void availableIssueQuantity_test3()  {
        // given
        Coupon  coupon = Coupon.builder()
                .totalQuantity(null) // 개수제한 없음
                .issuedQuantity(100) // 발급된 쿠폰 수량 100개
                .build();
        // when(검증)
        boolean result = coupon.availableIssueQuantity();
        // then(결과)
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 시작되지 않았다면 false를 반환")
    void availableIssueDate_test1()  {
        // given
        Coupon  coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1)) //현재 시간 + 1일 (내일)
                .dateIssueEnd(LocalDateTime.now().plusDays(2)) //현재 시간 + 2일 (모레)
                .build();
        // when(검증)
        boolean result = coupon.availableIssueDate();
        // then(결과)
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 기간이 해당되면 true를 반환")
    void availableIssueDate_test2()  {
        // given
        Coupon  coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1)) //현재 시간 - 1일 (어제)
                .dateIssueEnd(LocalDateTime.now().plusDays(2)) //현재 시간 + 2일 (모레)
                .build();
        // when(검증)
        boolean result = coupon.availableIssueDate();
        // then(결과)
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 종료되면 false를 반환")
    void availableIssueDate_test3()  {
        // given
        Coupon  coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2)) // 현재 시간 - 2일 (그저께)
                .dateIssueEnd(LocalDateTime.now().minusDays(1)) // 현재 시간 - 1일 (어제)
                .build();
        // when(검증)
        boolean result = coupon.availableIssueDate();
        // then(결과)
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 수량과 발급 기간이 유요하다면 발급에 성공한다")
    void issue_test1()  {
        // given
        Coupon  coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when(검증)
        coupon.issue();
        // then(결과)
        Assertions.assertEquals(coupon.getIssuedQuantity(), 100);
    }

    // issue_test2,issue_test3 는 정확하게 에러를 구별해줘야 한다
    // 이렇게 안하면 어디서 에러가 터졌는지 구별하기 어렵기 때문이다
    @Test
    @DisplayName("발급 수량을 초과하면 예외를 반환")
    void issue_test2()  {
        // given
        Coupon  coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when(검증)&then(결과)
        //RuntimeException.class 이결과 하고 coupon::issue 여기서 나오는 exception 하고 일치하는지
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 기간이 아니면 예외를 반환")
    void issue_test3()  {
        // given
        Coupon  coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        // when(검증)&then(결과)
        //RuntimeException.class 이결과 하고 coupon::issue 여기서 나오는 exception 하고 일치하는지
        CouponIssueException exception =  Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);
    }



}