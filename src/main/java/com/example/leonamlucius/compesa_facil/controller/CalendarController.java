package com.example.leonamlucius.compesa_facil.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.leonamlucius.compesa_facil.services.CalendarService;
import com.example.leonamlucius.compesa_facil.dto.CalendarResponse;
import com.example.leonamlucius.compesa_facil.dto.CalendarRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/calendario")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarioService;

    @PostMapping(value = "/get", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCalendario(@RequestBody CalendarRequest request) {
        CalendarResponse response = calendarioService.takeScreenshot(request.address());
        return ResponseEntity.ok(response.screenshot());
    }

}
