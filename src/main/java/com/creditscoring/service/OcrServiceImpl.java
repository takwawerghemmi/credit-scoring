package com.creditscoring.service;

import lombok.extern.slf4j.Slf4j;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@Slf4j
public class OcrServiceImpl implements OcrService {

    @Override
    public String extraireTexte(MultipartFile fichier) {

        try {

            File temp = File.createTempFile("ocr-", ".tmp");
            fichier.transferTo(temp);

            ITesseract tesseract = new Tesseract();

            tesseract.setDatapath("tessdata");
            tesseract.setLanguage("fra");

            String texte = tesseract.doOCR(temp);

            temp.delete();

            return texte;

        } catch (Exception e) {
            throw new RuntimeException("Erreur OCR : " + e.getMessage());
        }
    }

}