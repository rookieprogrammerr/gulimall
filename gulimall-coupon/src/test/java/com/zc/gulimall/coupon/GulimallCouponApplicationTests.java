package com.zc.gulimall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
public class GulimallCouponApplicationTests {

    //@Test
    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(1);
        LocalDate plus2 = now.plusDays(2);

        System.out.println(now);
        System.out.println(plus);
        System.out.println(plus2);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;
        System.out.println(min);
        System.out.println(max);

        LocalDateTime start = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(plus2, LocalTime.MAX);
        System.out.println(start);
        System.out.println(end);

        System.out.println(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
