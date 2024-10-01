package com.ajtech.controller;

import com.ajtech.dto.LoginDto;
import com.ajtech.dto.UserResponseDto;
import com.ajtech.entity.User;
import com.ajtech.repo.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDao userDao;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User savedUser = userDao.save(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        try {
            List<UserResponseDto> users = userDao.findAll();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findUser(@PathVariable String id) {
        try {
            User user = userDao.findUserById(id);
            if (user != null) {
                UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getEmail(), user.getName(), user.getStatus());

                return new ResponseEntity<>(userResponse, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        try {
            String result = userDao.deleteUser(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDto loginDto) {
        try {
            User user = userDao.findByEmail(loginDto.getEmail());

            // Check if the password matches
            boolean isPasswordMatch = BCrypt.checkpw(loginDto.getPassword(), user.getPassword());
            if (!isPasswordMatch) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mot de passe incorrect!");
            }

                user.setStatus("Connected");
                userDao.save(user);
                UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getEmail(), user.getName(), user.getStatus());
                return new ResponseEntity<>(userResponse, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/logout/{userId}")
    public ResponseEntity<Object> logout(@PathVariable String userId) {
        try {
            User user = userDao.findUserById(userId);
            if (user != null) {

                user.setStatus("Disconnected");
                userDao.save(user);
                
                return new ResponseEntity<>("User logged out successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
