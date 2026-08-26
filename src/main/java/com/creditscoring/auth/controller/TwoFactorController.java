package com.creditscoring.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class TwoFactorController {

    /*
     * Stockage temporaire des OTP.
     *
     * email -> OTP
     *
     * Pour une vraie production :
     * Redis ou table DB + expiration.
     */
    private final Map<String, OtpData> otpCodes =
            new ConcurrentHashMap<>();

    private static final int OTP_EXPIRATION_MINUTES = 5;

    // =====================================================
    // VERIFY PHONE
    // =====================================================

    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyPhone(
            @RequestBody OtpRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            return badRequest("Email obligatoire.");
        }

        if (request.getCode() == null ||
                request.getCode().isBlank()) {

            return badRequest("Code OTP obligatoire.");
        }

        return verifyOtp(
                request.getEmail(),
                request.getCode(),
                "Numéro de téléphone vérifié.",
                "Code OTP"
        );
    }

    // =====================================================
    // VERIFY 2FA CODE
    // =====================================================

    @PostMapping("/verify-2fa-code")
    public ResponseEntity<?> verify2faCode(
            @RequestBody OtpRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            return badRequest("Email obligatoire.");
        }

        if (request.getCode() == null ||
                request.getCode().isBlank()) {

            return badRequest("Code 2FA obligatoire.");
        }

        return verifyOtp(
                request.getEmail(),
                request.getCode(),
                "Authentification 2FA réussie.",
                "Code 2FA"
        );
    }

    // =====================================================
    // GENERATE OTP
    // =====================================================

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(
            @RequestBody EmailRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            return badRequest("Email obligatoire.");
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String code = String.format(
                "%06d",
                new Random().nextInt(1_000_000)
        );

        LocalDateTime expiration =
                LocalDateTime.now()
                        .plusMinutes(OTP_EXPIRATION_MINUTES);

        otpCodes.put(
                email,
                new OtpData(code, expiration)
        );

        /*
         * Pour le développement/test.
         *
         * En production, il faudra envoyer le code
         * par email/SMS et ne pas le retourner.
         */
        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Code OTP généré.",
                        "code", code,
                        "expiresInMinutes",
                        OTP_EXPIRATION_MINUTES
                )
        );
    }

    // =====================================================
    // VERIFY 2FA LINK
    // =====================================================

    @PostMapping("/verify-2fa-link")
    public ResponseEntity<?> verify2faLink(
            @RequestBody LinkRequest request) {

        if (request.getToken() == null ||
                request.getToken().isBlank()) {

            return badRequest("Token obligatoire.");
        }

        /*
         * Le Frontend actuel envoie un token.
         *
         * On accepte ici le token sous forme de code OTP
         * généré par /generate-otp.
         *
         * Le vrai lien signé avec JWT/token unique pourra
         * être ajouté plus tard sans changer l'endpoint.
         */

        String token = request.getToken().trim();

        for (Map.Entry<String, OtpData> entry :
                otpCodes.entrySet()) {

            OtpData otp = entry.getValue();

            if (isExpired(otp)) {
                otpCodes.remove(entry.getKey());
                continue;
            }

            if (otp.code.equals(token)) {

                otpCodes.remove(entry.getKey());

                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "message",
                                "Authentification 2FA réussie."
                        )
                );
            }
        }

        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "success", false,
                                "message",
                                "Token 2FA invalide ou expiré."
                        )
                );
    }

    // =====================================================
    // COMMON OTP VERIFICATION
    // =====================================================

    private ResponseEntity<?> verifyOtp(
            String email,
            String code,
            String successMessage,
            String codeName) {

        String normalizedEmail =
                email.trim().toLowerCase();

        OtpData otp =
                otpCodes.get(normalizedEmail);

        if (otp == null) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message",
                                    "Aucun " +
                                    codeName +
                                    " demandé."
                            )
                    );
        }

        if (isExpired(otp)) {

            otpCodes.remove(normalizedEmail);

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message",
                                    codeName +
                                    " expiré."
                            )
                    );
        }

        if (!otp.code.equals(code.trim())) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message",
                                    codeName +
                                    " incorrect."
                            )
                    );
        }

        /*
         * OTP à usage unique.
         */
        otpCodes.remove(normalizedEmail);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", successMessage
                )
        );
    }

    // =====================================================
    // EXPIRATION
    // =====================================================

    private boolean isExpired(OtpData otp) {

        return LocalDateTime.now()
                .isAfter(otp.expiration);
    }

    // =====================================================
    // BAD REQUEST
    // =====================================================

    private ResponseEntity<?> badRequest(
            String message) {

        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "success", false,
                                "message", message
                        )
                );
    }

    // =====================================================
    // OTP DATA
    // =====================================================

    private static class OtpData {

        private final String code;
        private final LocalDateTime expiration;

        private OtpData(
                String code,
                LocalDateTime expiration) {

            this.code = code;
            this.expiration = expiration;
        }
    }

    // =====================================================
    // DTO EMAIL
    // =====================================================

    public static class EmailRequest {

        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // =====================================================
    // DTO OTP
    // =====================================================

    public static class OtpRequest {

        private String email;
        private String code;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // =====================================================
    // DTO LINK
    // =====================================================

    public static class LinkRequest {

        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}