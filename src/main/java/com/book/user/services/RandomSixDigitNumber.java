package com.book.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class RandomSixDigitNumber {
    public String getCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
