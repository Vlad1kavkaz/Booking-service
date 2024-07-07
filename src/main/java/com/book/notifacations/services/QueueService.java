package com.book.notifacations.services;

import com.book.notifacations.controllers.EmailController;
import com.book.notifacations.dto.EmailContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueueService {
    private final RabbitTemplate rabbitTemplate;
    private final Queue queue;
    private final ObjectMapper objectMapper;
    private final EmailController emailController;

    @Scheduled(fixedRate = 15000)
    public void checkQueue() throws JsonProcessingException {
        String message = (String) rabbitTemplate.receiveAndConvert(queue.getName());
        if (message != null) {
            EmailContext emailMessage = objectMapper.readValue(message, EmailContext.class);
            emailController.sendSimpleEmail(emailMessage);
            log.info("New message received from queue '{}': {}", queue.getName(), message);
        }
    }
}
