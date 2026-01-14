package com.yeditepe.firstspingproject.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Security Annotations for Role-Based Access Control (RBAC)
 */
public class SecurityAnnotations {

    /**
     * Requires ADMIN role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('ADMIN')")
    public @interface AdminOnly {
    }

    /**
     * Requires ORGANIZER role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public @interface OrganizerOnly {
    }

    /**
     * Requires USER role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('USER')")
    public @interface UserOnly {
    }

    /**
     * Requires ADMIN or ORGANIZER role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ORGANIZER')")
    public @interface AdminOrOrganizer {
    }

    /**
     * Requires any authenticated user
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("isAuthenticated()")
    public @interface Authenticated {
    }

    /**
     * Allows access to resource owner or ADMIN
     * Usage: @OwnerOrAdmin with #userId parameter
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('ADMIN') or #userId == authentication.principal.id")
    public @interface OwnerOrAdmin {
    }
}
