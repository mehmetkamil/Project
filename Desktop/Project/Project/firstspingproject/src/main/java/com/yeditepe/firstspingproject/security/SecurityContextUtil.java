package com.yeditepe.firstspingproject.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security Context Utility
 * Helper methods to access current authenticated user information
 */
@Component
public class SecurityContextUtil {

    /**
     * Get current authentication
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get current authenticated user principal
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Get current username
     */
    public String getCurrentUsername() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        UserPrincipal user = getCurrentUser();
        return user != null && user.hasRole(role);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is organizer
     */
    public boolean isOrganizer() {
        return hasRole("ORGANIZER");
    }

    /**
     * Check if current user is the owner of a resource
     */
    public boolean isOwner(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }

    /**
     * Check if current user is owner or admin
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        return isOwner(resourceOwnerId) || isAdmin();
    }
}
