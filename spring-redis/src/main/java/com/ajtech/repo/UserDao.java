package com.ajtech.repo;

import com.ajtech.dto.UserResponseDto;
import com.ajtech.entity.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDao {

    private final RedisTemplate<String, Object> redisTemplate;

    public static final String HASH_KEY = "User";

    public UserDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public User save(User user) {
        redisTemplate.opsForHash().put(HASH_KEY, user.getId(), user);
        return user;
    }

    public List<UserResponseDto> findAll() {
        List<Object> users = redisTemplate.opsForHash().values(HASH_KEY);

        List<UserResponseDto> userList = new ArrayList<>();
        for (Object obj : users) {
            if (obj instanceof User) {
                User user = (User) obj;
                UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getEmail(), user.getName(), user.getStatus());
                userList.add(userResponse);
            }
        }
        return userList;
    }

    public List<User> findAllForLogin() {
        List<Object> users = redisTemplate.opsForHash().values(HASH_KEY);
        List<User> userList = new ArrayList<>();
        for (Object obj : users) {
            if (obj instanceof User) {
                userList.add((User) obj);
            }
        }
        return userList;
    }

    public User findUserById(String id) {
        return (User) redisTemplate.opsForHash().get(HASH_KEY, id);
    }

    public User findByEmail(String email) {
        List<User> users = findAllForLogin();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public String deleteUser(String id) {
        redisTemplate.opsForHash().delete(HASH_KEY, id);
        return "User deleted!";
    }
}
