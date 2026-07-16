package com.dev.cs_api.identity.services;

import com.dev.cs_api.core.security.SecurityUtils;
import com.dev.cs_api.identity.dtos.user.UserCreateRequest;
import com.dev.cs_api.identity.dtos.user.UserDetailResponse;
import com.dev.cs_api.identity.dtos.user.UserSummaryResponse;
import com.dev.cs_api.identity.dtos.user.UserUpdateRequest;
import com.dev.cs_api.identity.enums.RoleName;
import com.dev.cs_api.identity.enums.UserStatus;
import com.dev.cs_api.identity.models.Role;
import com.dev.cs_api.identity.models.User;
import com.dev.cs_api.identity.repositories.RoleRepository;
import com.dev.cs_api.identity.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantUserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetailResponse findOne(UUID userId) {
        User user = findUser(userId);
        return new UserDetailResponse(user);
    }

    public Page<UserSummaryResponse> findAll(Pageable pageable) {
        Page<User> users = userRepository.findAllByTenantId(SecurityUtils.getTenantId(), pageable);
        return users.map(UserSummaryResponse::new);
    }

    @Transactional
    public UserSummaryResponse create(UserCreateRequest request) {
        Role role = roleRepository.findByName(request.role()).orElseThrow(
                () -> new EntityNotFoundException("Role não encontrado"));

        if (userRepository.findByEmail(request.email()).isPresent())
            throw new EntityExistsException("Já existe um usuário com este E-mail");

        try {
            User newUser = new User();
            newUser.setName(request.name());
            newUser.setEmail(request.email());
            newUser.setPhoneNumber(request.phoneNumber());
            newUser.setRole(role);
            newUser.setTenantId(SecurityUtils.getTenantId());
            newUser.setStatus(UserStatus.ACTIVE);

            newUser.setPassword(passwordEncoder.encode(request.password()));

            userRepository.save(newUser);
            return new UserSummaryResponse(newUser);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public UserSummaryResponse update(UUID userId, UserUpdateRequest request) {
        User user = findUser(userId);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.findByEmail(request.email()).isPresent())
                throw new EntityExistsException("Já existe um usuário com este E-mail");
            user.setEmail(request.email());
        }

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }

        if (request.role() != null && !request.role().isBlank()) {
            Role newRole = roleRepository.findByName(RoleName.valueOf(request.role()))
                    .orElseThrow(() -> new EntityNotFoundException("Role inválida"));
            user.setRole(newRole);
        }

        if (request.active() != null) {
            if (request.active()) {
                user.setStatus(UserStatus.ACTIVE);
            } else {
                user.setStatus(UserStatus.DISABLED);
            }
        }

        return new UserSummaryResponse(user);
    }

    @Transactional
    public UserDetailResponse toggleStatus(UUID userId) {
        User user = findUser(userId);

        if (user.getStatus().equals(UserStatus.ACTIVE)) {
            user.setStatus(UserStatus.DISABLED);
        } else  { user.setStatus(UserStatus.ACTIVE); }
        return new UserDetailResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID userId) {
        userRepository.deleteByIdAndTenantId(userId, SecurityUtils.getTenantId());
    }

    private User findUser(UUID userId) {
        return userRepository.findByIdAndTenantId(userId, SecurityUtils.getTenantId()).orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado")
        );
    }
}
