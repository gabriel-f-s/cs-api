package com.dev.cs_api.identity.user.services;

import com.dev.cs_api.core.security.SecurityUtils;
import com.dev.cs_api.identity.user.dtos.ProfileResponse;
import com.dev.cs_api.identity.user.dtos.AdminSecurityProfileResponse;
import com.dev.cs_api.identity.user.dtos.MeResponse;
import com.dev.cs_api.identity.user.dtos.SecurityProfileResponse;
import com.dev.cs_api.identity.user.dtos.UserChangeEmailRequest;
import com.dev.cs_api.identity.user.dtos.UserChangePasswordRequest;
import com.dev.cs_api.identity.user.dtos.UserSecurityProfileResponse;
import com.dev.cs_api.identity.user.models.Admin;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.*;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, ObjectMapper objectMapper, Validator validator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    public MeResponse findMe() {
        User user = findUser();
        return new MeResponse(user);
    }

    public ProfileResponse findProfile() {
        User user = findUser();
        return new ProfileResponse(user);
    }

    public SecurityProfileResponse findSecurityProfile() {
        User user = findUser();
        if (user instanceof Admin admin) return new AdminSecurityProfileResponse(admin);
        return new UserSecurityProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(JsonNode updatedFields) {
        try {
            User user = findUser();
            if (updatedFields instanceof ObjectNode objectNode) {
                List<String> allowedFields = new ArrayList<>(List.of(
                        "name",
                        "phoneNumber"
                ));
                objectNode.retain(allowedFields);
            }
            ObjectReader reader = objectMapper.readerForUpdating(user);
            reader.readValue(updatedFields);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return new ProfileResponse(userRepository.save(user));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar a atualização do perfil", e);
        }
    }

    @Transactional
    public ProfileResponse changeEmail(UserChangeEmailRequest request) {
        if (request.email() == null || request.email().isBlank() || request.confirmEmail() == null || request.confirmEmail().isBlank())
            throw new IllegalArgumentException("Os e-mails não podem estar vazios");
        if (!request.email().equals(request.confirmEmail())) throw new IllegalArgumentException("O e-mail e a confirmação não conferem");

        User user = findUser();

        if (user instanceof Admin) {
            user.setEmail(request.email());
            return new ProfileResponse(userRepository.save(user));
        } else {
            throw new AccessDeniedException("Você não tem permissão para alterar o seu e-mail. Entre em contato com o suporte");
        }
    }

    @Transactional
    public void changePassword(UserChangePasswordRequest request) {
        if (request.password() == null || request.password().isBlank() || request.confirmPassword() == null || request.confirmPassword().isBlank())
            throw new IllegalArgumentException("As senhas não podem estar vazias");
        if (!request.password().equals(request.confirmPassword())) throw new IllegalArgumentException("As senhas não conferem");
        User user = findUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    private User findUser() {
        UUID userId = SecurityUtils.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return user;
    }
}
