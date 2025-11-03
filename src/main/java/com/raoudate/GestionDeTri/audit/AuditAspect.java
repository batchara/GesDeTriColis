package com.raoudate.GestionDeTri.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.raoudate.GestionDeTri.audit.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        String entityId = null;
        String oldValue = null;
        String newValue = null;
        String message = "";

        try {
            // Exécuter la méthode
            Object result = joinPoint.proceed();

            // Extraire l'ID de l'entité du résultat
            if (result != null) {
                try {
                    newValue = objectMapper.writeValueAsString(result);
                    // Si le résultat a un champ "id", l'extraire
                    if (result.getClass().getDeclaredFields().length > 0) {
                        try {
                            Method getId = result.getClass().getMethod("getId");
                            Object id = getId.invoke(result);
                            entityId = id != null ? id.toString() : null;
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception e) {
                    log.warn("Impossible de sérialiser le résultat");
                }
            }

            message = String.format("Action %s réussie sur %s", 
                auditable.action(), auditable.entityType());

            // Logger l'action
            auditLogService.logAction(
                auditable.action(),
                auditable.entityType(),
                entityId,
                oldValue,
                newValue != null && newValue.length() > 500 ? newValue.substring(0, 500) + "..." : newValue,
                message
            );

            return result;

        } catch (Exception e) {
            // Logger l'erreur
            auditLogService.logError(
                auditable.action(),
                auditable.entityType(),
                "Erreur lors de l'action: " + e.getMessage(),
                e.getClass().getName()
            );
            throw e;
        }
    }
}
