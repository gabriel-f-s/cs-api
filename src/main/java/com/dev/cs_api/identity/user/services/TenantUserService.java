package com.dev.cs_api.identity.user.services;

import com.dev.cs_api.core.security.SecurityUtils;
import com.dev.cs_api.identity.user.dtos.UserCreateRequest;
import com.dev.cs_api.identity.user.dtos.UserDetailResponse;
import com.dev.cs_api.identity.user.dtos.AdminUserSummaryResponse;
import com.dev.cs_api.identity.user.dtos.UserUpdateRequest;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.exceptions.NoPermissionException;
import com.dev.cs_api.identity.user.models.Role;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.repositories.RoleRepository;
import com.dev.cs_api.identity.user.repositories.UserRepository;
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

    public Page<AdminUserSummaryResponse> findAll(Pageable pageable) {
        Page<User> users = userRepository.findAllByTenantId(SecurityUtils.getTenantId(), pageable);
        return users.map(AdminUserSummaryResponse::new);
    }

    @Transactional
    public UserDetailResponse create(UserCreateRequest request) {
        Role role = roleRepository.findByName(request.role()).orElseThrow(
                () -> new EntityNotFoundException("Role não encontrado"));
        User loggedUser = findUser(SecurityUtils.getUserId());

        if (userRepository.findByEmail(request.email()).isPresent())
            throw new EntityExistsException("Já existe um usuário com este E-mail");

        if (role.getName() == RoleName.SYSTEM_ADMIN)
            throw new NoPermissionException("Você não possui permissão para criar um Administrador do Sistema");

        if (loggedUser.getRole().getName() == RoleName.MANAGER && role.getName() == RoleName.TENANT_ADMIN)
            throw new NoPermissionException("Você não possui permissão para criar um Administrador");

        User newUser = new User();
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setRole(role);
        newUser.setTenantId(SecurityUtils.getTenantId());
        newUser.setStatus(UserStatus.ACTIVE);

        newUser.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(newUser);
        return new UserDetailResponse(newUser);
    }

    @Transactional
    public UserDetailResponse update(UUID userId, UserUpdateRequest request) {
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

            if (newRole.getName() == RoleName.SYSTEM_ADMIN)
                throw new NoPermissionException("Você não possui permissão para criar um Administrador do Sistema");

            user.setRole(newRole);
        }

        if (request.active() != null) {
            if (request.active()) {
                user.setStatus(UserStatus.ACTIVE);
            } else {
                user.setStatus(UserStatus.DISABLED);
            }
        }

        return new UserDetailResponse(userRepository.save(user));
    }

    @Transactional
    public void toggleStatus(UUID userId) {
        User user = findUser(userId);
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.DISABLED);
        } else  { user.setStatus(UserStatus.ACTIVE); }
        userRepository.save(user);
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
