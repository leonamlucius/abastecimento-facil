package com.example.leonamlucius.compesa_facil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import com.example.leonamlucius.compesa_facil.models.User;
import com.example.leonamlucius.compesa_facil.dto.UserResponse;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

        @Modifying
        @Transactional
        @Query("""
                        INSERT INTO User (chatId, andress, createdAt) VALUES (:chatId, :andress, :createdAt)
                        """)
        int createUser(@Param("chatId") Long chatId, @Param("andress") String andress,
                        @Param("createdAt") Date createdAt);

        @Query("""
                        SELECT u FROM User u
                        """)
        List<UserResponse> getAllUsers();

        @Query("""
                        SELECT u FROM User u WHERE u.chatId = :chatId
                        """)
        List<UserResponse> findByChatId(@Param("chatId") Long chatId);

        @Modifying
        @Transactional
        @Query("""
                        DELETE FROM User u WHERE u.chatId = :chatId
                        """)
        int deleteByChatId(@Param("chatId") Long chatId);

}
