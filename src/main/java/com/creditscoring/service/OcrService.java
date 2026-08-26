package com.creditscoring.service;

import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

    String extraireTexte(MultipartFile fichier);

}