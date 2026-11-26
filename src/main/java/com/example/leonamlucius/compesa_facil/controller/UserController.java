package com.example.leonamlucius.compesa_facil.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import com.example.leonamlucius.compesa_facil.dto.UserResponse;
import com.example.leonamlucius.compesa_facil.dto.UserRequest;
import lombok.RequiredArgsConstructor;

import com.example.leonamlucius.compesa_facil.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/create")
    public int createUser(@RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @GetMapping(value = "/all")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping(value = "/delete/{chatId}")
    public int deleteByChatId(@PathVariable long chatId) {
        return userService.deleteByChatId(chatId);
    }

}
