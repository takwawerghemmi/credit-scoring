package com.creditscoring.entity;

import com.creditscoring.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @AfterReturning(
            "execution(* com.creditscoring.service.*.*(..)) && " +
                    "!execution(* com.creditscoring.service.AuditService.*(..)) && " +
                    "!execution(* com.creditscoring.service.AuditServiceImpl.*(..))"
    )
    public void audit(JoinPoint joinPoint) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String utilisateur =
                auth != null ? auth.getName() : "ANONYMOUS";

        String methode =
                joinPoint.getSignature().getName();

        String classe =
                joinPoint.getTarget()
                        .getClass()
                        .getSimpleName();

        auditService.enregistrerAction(
                utilisateur,
                classe,
                methode,
                "Exécution réussie"
        );

    }
}