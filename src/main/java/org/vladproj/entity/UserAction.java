package org.vladproj.entity;

import java.util.Optional;

public enum UserAction {
    REGISTER, LOGOUT;

    public static Optional<UserAction> find(String type){
        if (type == null || type.isBlank()){
            return Optional.empty();
        }

        try {
            return Optional.of(UserAction.valueOf(type.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
