package com.book.notifacations.messages;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum Messages {
    CHECK_AUTORIZATIONS("Hello, it's a booking service, please enter your code in application"),
    BOOKING_INFO("Hello, it's a your booking info"),
    CODE("Your authentification code is: "),
    BOOKING_UPDATE("Hello, your booking update");


    private String message;
}
