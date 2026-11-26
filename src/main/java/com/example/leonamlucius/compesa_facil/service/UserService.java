package com.example.leonamlucius.compesa_facil.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Date;
import com.example.leonamlucius.compesa_facil.dto.UserResponse;

import lombok.RequiredArgsConstructor;
import com.example.leonamlucius.compesa_facil.repository.UserRepository;
import com.example.leonamlucius.compesa_facil.models.User;
import com.example.leonamlucius.compesa_facil.dto.UserRequest;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public int createUser(UserRequest userRequest) {
        return userRepository.createUser(userRequest.chatId(), userRequest.andress(), new Date());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public UserResponse getUserByChatId(long chatId) {
        return userRepository.findByChatId(chatId).stream().findFirst().orElse(null);
    }

    public int deleteByChatId(long chatId) {
        return userRepository.deleteByChatId(chatId);
    }
}
