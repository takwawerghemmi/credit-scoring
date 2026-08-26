package com.creditscoring.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class CaptchaController {

    private final Random random = new Random();

    private final Map<String, String> captchaStore =
            new ConcurrentHashMap<>();

    // ==========================================
    // GENERATE CAPTCHA
    // ==========================================

    @GetMapping("/captcha")
    public ResponseEntity<Map<String, String>> generateCaptcha() {

        String captchaId =
                UUID.randomUUID().toString();

        String captchaCode =
                generateCaptchaCode();

        String imageBase64 =
                generateCaptchaImage(captchaCode);

        captchaStore.put(
                captchaId,
                captchaCode
        );

        return ResponseEntity.ok(
                Map.of(
                        "id", captchaId,
                        "imageBase64", imageBase64
                )
        );
    }

    // ==========================================
    // VERIFY CAPTCHA
    // ==========================================

    public boolean verifyCaptcha(
            String captchaId,
            String captchaCode) {

        if (captchaId == null ||
                captchaCode == null) {

            return false;
        }

        String expectedCode =
                captchaStore.get(captchaId);

        if (expectedCode == null) {
            return false;
        }

        boolean valid =
                expectedCode.equalsIgnoreCase(
                        captchaCode.trim()
                );

        /*
         * CAPTCHA utilisable une seule fois
         */
        if (valid) {
            captchaStore.remove(captchaId);
        }

        return valid;
    }

    // ==========================================
    // GET CAPTCHA CODE
    // ==========================================

    private String generateCaptchaCode() {

        String characters =
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        StringBuilder code =
                new StringBuilder();

        for (int i = 0; i < 5; i++) {

            int index =
                    random.nextInt(
                            characters.length()
                    );

            code.append(
                    characters.charAt(index)
            );
        }

        return code.toString();
    }

    // ==========================================
    // GENERATE IMAGE
    // ==========================================

    private String generateCaptchaImage(
            String captchaCode) {

        int width = 220;
        int height = 70;

        BufferedImage image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                image.createGraphics();

        // Background
        graphics.setColor(Color.WHITE);
        graphics.fillRect(
                0,
                0,
                width,
                height
        );

        // Border
        graphics.setColor(
                new Color(200, 200, 200)
        );

        graphics.drawRect(
                0,
                0,
                width - 1,
                height - 1
        );

        // Anti-aliasing
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Random lines
        for (int i = 0; i < 8; i++) {

            graphics.setColor(
                    new Color(
                            random.nextInt(180),
                            random.nextInt(180),
                            random.nextInt(180)
                    )
            );

            graphics.drawLine(
                    random.nextInt(width),
                    random.nextInt(height),
                    random.nextInt(width),
                    random.nextInt(height)
            );
        }

        // CAPTCHA text
        graphics.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );

        int x = 25;

        for (char character :
                captchaCode.toCharArray()) {

            graphics.setColor(
                    new Color(
                            random.nextInt(100),
                            random.nextInt(100),
                            random.nextInt(100)
                    )
            );

            int y =
                    45 + random.nextInt(10);

            graphics.drawString(
                    String.valueOf(character),
                    x,
                    y
            );

            x += 34;
        }

        graphics.dispose();

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            ImageIO.write(
                    image,
                    "png",
                    output
            );

            return "data:image/png;base64,"
                    + Base64.getEncoder()
                            .encodeToString(
                                    output.toByteArray()
                            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur génération CAPTCHA",
                    e
            );
        }
    }
}