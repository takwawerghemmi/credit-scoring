package com.creditscoring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void envoyerEmail(String destinataire,
                             String sujet,
                             String contenu) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(contenu);

        javaMailSender.send(message);
    }
}