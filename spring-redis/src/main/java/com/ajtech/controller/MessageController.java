package com.ajtech.controller;

import com.ajtech.dto.ConversationDto;
import com.ajtech.entity.Message;
import com.ajtech.repo.MessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageDao messageDao;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        try {

            Message savedMessage = messageDao.save(message);
            return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Object>> getAllMessages() {
        try {
            List<Object> messages = messageDao.findAll();
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findMessage(@PathVariable String id) {
        try {
            Message message = messageDao.findMessageById(id);
            if (message != null) {
                return new ResponseEntity<>(message, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable String id) {
        try {
            String result = messageDao.deleteMessage(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Message>> getMessagesByUser(@PathVariable String userId) {
        try {
            List<Message> messages = messageDao.findMessagesByUserId(userId);
            if (!messages.isEmpty()) {
                return new ResponseEntity<>(messages, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user")
    public ResponseEntity<List<Message>> getMyMessage(@RequestBody ConversationDto conversationDto) {
        try {
            List<Message> messages = messageDao.findConversation(conversationDto.getCurrentUserId(), conversationDto.getUserId());
            if (!messages.isEmpty()) {
                return new ResponseEntity<>(messages, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
