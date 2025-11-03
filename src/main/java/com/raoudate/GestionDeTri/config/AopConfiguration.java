package com.raoudate.GestionDeTri.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AopConfiguration {
    // Cette configuration active le support AOP dans Spring
    // Les aspects annotés avec @Aspect seront automatiquement détectés
}
