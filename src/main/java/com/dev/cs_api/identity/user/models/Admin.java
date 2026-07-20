package com.dev.cs_api.identity.user.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "admins", schema = "master")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @Column(name = "last_login")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip")
    private String lastLoginIP;

    @Column(name = "last_login_user_agent")
    private String lastLoginUserAgent;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Override
    public boolean isAccountNonLocked() {
        return this.lockedUntil == null || this.lockedUntil.isBefore(LocalDateTime.now());
    }
}
