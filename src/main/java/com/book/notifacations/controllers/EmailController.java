package com.book.notifacations.controllers;


import com.book.notifacations.dto.EmailContext;
import com.book.notifacations.services.EmailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping(value = "/simple-email")
    public void sendSimpleEmail(@RequestBody @Valid EmailContext context) {

        try {
            log.info("Sending email to " + context.getTo());
            emailService.sendSimpleEmail(context.getTo(), context.getSubject(), context.getBody());
        } catch (MailException mailException) {
            log.error("Error while sending out email..{}", (Object) mailException.getStackTrace());
        }
    }

}
