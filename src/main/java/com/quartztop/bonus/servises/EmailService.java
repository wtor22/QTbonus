package com.quartztop.bonus.servises;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class EmailService {
    private JavaMailSender mailSender;
    private MessageService messageService;


    public void sendEmail(String to, String plainText, String htmlText) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Устанавливаем кодировку UTF-8

        helper.setTo(to);
        helper.setSubject(messageService.getEmailSubject());

        // Указываем текстовую и HTML-версию письма одновременно
        helper.setText(plainText, htmlText); // Первый параметр — текстовая версия, второй — HTML-версия

        helper.setFrom(messageService.getEmailFrom());
        mailSender.send(message);
    }
}
