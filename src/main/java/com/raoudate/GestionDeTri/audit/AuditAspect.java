package com.raoudate.GestionDeTri.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect pour logger automatiquement toutes les opérations CRUD
 * Supporte à la fois l'annotation @Auditable et l'audit automatique des endpoints REST
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * Logger les méthodes annotées avec @Auditable (ancien système)
     */
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

    /**
     * Pointcut pour toutes les méthodes des contrôleurs REST
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    /**
     * Logger automatiquement les opérations CREATE (POST)
     */
    @Around("controllerMethods() && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public Object logCreateOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return logRestOperation(joinPoint, "CREATE");
    }

    /**
     * Logger automatiquement les opérations UPDATE (PUT/PATCH)
     */
    @Around("controllerMethods() && (@annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object logUpdateOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return logRestOperation(joinPoint, "UPDATE");
    }

    /**
     * Logger automatiquement les opérations DELETE
     */
    @Around("controllerMethods() && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logDeleteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return logRestOperation(joinPoint, "DELETE");
    }

    /**
     * Méthode générique pour logger une opération REST
     */
    private Object logRestOperation(ProceedingJoinPoint joinPoint, String action) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Ignorer certains contrôleurs pour éviter les boucles infinies
        if (shouldSkipLogging(className, methodName)) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        String entityType = extractEntityType(className);
        String entityId = extractEntityId(args);
        String oldValue = null;
        String newValue = null;

        try {
            // Capturer l'ancienne valeur pour UPDATE et DELETE
            if (("UPDATE".equals(action) || "DELETE".equals(action)) && args != null && args.length > 0) {
                oldValue = captureValue(args);
            }

            // Exécuter la méthode
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Capturer la nouvelle valeur pour CREATE et UPDATE
            if (("CREATE".equals(action) || "UPDATE".equals(action))) {
                newValue = captureValue(args);
                if (newValue == null && result != null) {
                    newValue = captureResultValue(result);
                }
            }

            // Logger l'opération réussie
            String message = String.format("%s %s - %s.%s() - Durée: %dms",
                    action, entityType, className, methodName, executionTime);

            try {
                auditLogService.logAction(
                        action,
                        entityType,
                        entityId,
                        truncate(oldValue, 500),
                        truncate(newValue, 500),
                        message
                );
            } catch (Exception auditEx) {
                log.error(" Erreur lors de l'enregistrement du log d'audit: {}", auditEx.getMessage());
                // Ne pas interrompre le flux pour une erreur d'audit
            }

            return result;

        } catch (Exception e) {
            // Logger l'erreur
            String errorMessage = String.format("%s %s échoué - %s.%s()",
                    action, entityType, className, methodName);

            try {
                auditLogService.logError(
                        action,
                        entityType,
                        errorMessage,
                        e.getMessage()
                );
            } catch (Exception auditEx) {
                log.error(" Erreur lors de l'enregistrement du log d'erreur: {}", auditEx.getMessage());
                // Ne pas interrompre le flux pour une erreur d'audit
            }

            throw e;
        }
    }

    /**
     * Extraire le type d'entité depuis le nom du contrôleur
     */
    private String extractEntityType(String className) {
        String entityName = className
                .replace("Controller", "")
                .replace("Rest", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase();
        return entityName.isEmpty() ? "UNKNOWN" : entityName;
    }

    /**
     * Extraire l'ID de l'entité depuis les arguments
     */
    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg != null) {
                // Si c'est un type primitif qui pourrait être un ID
                if (arg instanceof Long || arg instanceof Integer) {
                    return arg.toString();
                }
                
                // Si c'est un objet avec un champ "id"
                try {
                    var idField = arg.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object id = idField.get(arg);
                    if (id != null) {
                        return id.toString();
                    }
                } catch (Exception ignored) {
                    // Essayer avec getter
                    try {
                        Method getId = arg.getClass().getMethod("getId");
                        Object id = getId.invoke(arg);
                        if (id != null) {
                            return id.toString();
                        }
                    } catch (Exception e) {
                        // Pas d'ID trouvé
                    }
                }
            }
        }

        return null;
    }

    /**
     * Capturer la valeur d'un argument
     */
    private String captureValue(Object[] args) {
        try {
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg != null && !isPrimitiveOrWrapper(arg) && !isHttpRequest(arg)) {
                        return objectMapper.writeValueAsString(arg);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Impossible de capturer la valeur", e);
        }
        return null;
    }

    /**
     * Capturer la valeur du résultat
     */
    private String captureResultValue(Object result) {
        try {
            if (result != null && !isResponseEntity(result)) {
                return objectMapper.writeValueAsString(result);
            }
        } catch (Exception e) {
            log.debug("Impossible de capturer le résultat", e);
        }
        return null;
    }

    /**
     * Tronquer une chaîne si trop longue
     */
    private String truncate(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            return value.substring(0, maxLength) + "...";
        }
        return value;
    }

    /**
     * Vérifier si c'est un type primitif ou wrapper
     */
    private boolean isPrimitiveOrWrapper(Object obj) {
        Class<?> clazz = obj.getClass();
        return clazz.isPrimitive() ||
               clazz == Boolean.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == String.class ||
               clazz == Character.class ||
               clazz == Byte.class ||
               clazz == Short.class;
    }

    /**
     * Vérifier si c'est une ResponseEntity ou HttpServletRequest
     */
    private boolean isResponseEntity(Object obj) {
        return obj.getClass().getName().contains("ResponseEntity");
    }

    private boolean isHttpRequest(Object obj) {
        return obj.getClass().getName().contains("HttpServletRequest") ||
               obj.getClass().getName().contains("HttpServletResponse");
    }

    /**
     * Déterminer si on doit ignorer le logging pour cette méthode
     */
    private boolean shouldSkipLogging(String className, String methodName) {
        // Ignorer les contrôleurs d'audit
        if (className.contains("AuditLog") || className.contains("Audit")) {
            return true;
        }
        
        // Ignorer les authentifications et tokens (déjà loggées spécifiquement ou système)
        if (className.contains("Authentication") || 
            className.contains("Logout") || 
            className.contains("Token") || 
            className.contains("Auth")) {
            return true;
        }
        
        // Ignorer les méthodes GET (lectures) pour éviter trop de logs
        if (methodName.startsWith("get") || methodName.startsWith("find") || 
            methodName.startsWith("list") || methodName.startsWith("search")) {
            return true;
        }
        
        return false;
    }
}
