package com.yeditepe.firstspingproject.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Logging Aspect
 * Provides automatic logging for controllers and services
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("within(com.yeditepe.firstspingproject.controllers..*)")
    public void controllerMethods() {}

    /**
     * Pointcut for all service methods
     */
    @Pointcut("within(com.yeditepe.firstspingproject.service..*) || within(com.yeditepe.firstspingproject.services..*)")
    public void serviceMethods() {}

    /**
     * Pointcut for all repository methods
     */
    @Pointcut("within(com.yeditepe.firstspingproject.repository..*)")
    public void repositoryMethods() {}

    /**
     * Log controller method entry
     */
    @Before("controllerMethods()")
    public void logControllerEntry(JoinPoint joinPoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("➡️ Entering: {}.{}() with arguments: {}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }
    }

    /**
     * Log controller method exit
     */
    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logControllerExit(JoinPoint joinPoint, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("⬅️ Exiting: {}.{}() with result: {}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    /**
     * Log controller exceptions
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "error")
    public void logControllerException(JoinPoint joinPoint, Throwable error) {
        logger.error("❌ Exception in {}.{}(): {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                error.getMessage());
    }

    /**
     * Log service method execution time
     */
    @Around("serviceMethods()")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + 
                "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > 500) {
                logger.warn("⚠️ Slow service method: {} took {}ms", methodName, executionTime);
            } else if (logger.isDebugEnabled()) {
                logger.debug("⏱️ Service method: {} executed in {}ms", methodName, executionTime);
            }

            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("❌ Service method {} failed after {}ms: {}", 
                    methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Log repository operations
     */
    @Around("repositoryMethods()")
    public Object logRepositoryOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String repoName = joinPoint.getSignature().getDeclaringType().getSimpleName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > 200) {
                logger.warn("⚠️ Slow database query: {}.{} took {}ms", 
                        repoName, methodName, executionTime);
            } else if (logger.isTraceEnabled()) {
                logger.trace("🗄️ Repository: {}.{} executed in {}ms", 
                        repoName, methodName, executionTime);
            }

            return result;
        } catch (Throwable e) {
            logger.error("❌ Repository {}.{} failed: {}", repoName, methodName, e.getMessage());
            throw e;
        }
    }
}
