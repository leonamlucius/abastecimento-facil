package com.example.leonamlucius.compesa_facil.dto;

import java.util.Date;

public record CalendarResponse(
        byte[] screenshot,
        String address,
        Date sendDate) {

}
