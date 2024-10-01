package com.ajtech.repo;

import com.ajtech.entity.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MessageDao {

    private final RedisTemplate<String, Object> redisTemplate;

    public MessageDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public Message save(Message message) {
        message.setCreatedAt(LocalDateTime.now().toString());
        redisTemplate.opsForHash().put("Message", message.getId(), message);
        return message;
    }

    public List<Object> findAll() {
        try {
            List<Object> messages = (List<Object>) redisTemplate.opsForHash().values("Message");

            return messages;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch messages", e);
        }
    }

    public Message findMessageById(String id) {
        return (Message) redisTemplate.opsForHash().get("Message", id);
    }

    public String deleteMessage(String id) {
        redisTemplate.opsForHash().delete("Message", id);
        return "Message deleted!";
    }


    public List<Message> findMessagesByUserId(String userId) {
        List<Message> userMessages = new ArrayList<>();
        for (Object obj : redisTemplate.opsForHash().values("Message")) {
            Message message = (Message) obj;
            if (message.getReceiverId().equals(userId) || message.getSenderId().equals(userId)) {
                userMessages.add(message);
            }
        }
        return userMessages;
    }

    public List<Message> findConversation(String currentUserId, String userId) {
        List<Message> userMessages = new ArrayList<>();
        for (Object obj : redisTemplate.opsForHash().values("Message")) {
            Message message = (Message) obj;
            if (message.getReceiverId().equals(userId) && message.getSenderId().equals(currentUserId) || message.getSenderId().equals(userId) && message.getReceiverId().equals(currentUserId) ) {
                userMessages.add(message);
            }
        }
        return userMessages;
    }
}
