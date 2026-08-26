package com.creditscoring.service;

public interface EmailService {

    void envoyerEmail(String destinataire,
                      String sujet,
                      String contenu);

}