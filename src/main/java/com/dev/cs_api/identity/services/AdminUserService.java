package com.dev.cs_api.identity.services;

import com.dev.cs_api.identity.dtos.admin.AdminDetailResponse;
import com.dev.cs_api.identity.dtos.admin.AdminUpdateRequest;
import com.dev.cs_api.identity.dtos.global.AdminCreateRequest;
import com.dev.cs_api.identity.dtos.global.AdminUserSummaryResponse;
import com.dev.cs_api.identity.enums.RoleName;
import com.dev.cs_api.identity.enums.UserStatus;
import com.dev.cs_api.identity.exceptions.NoPermissionException;
import com.dev.cs_api.identity.models.Admin;
import com.dev.cs_api.identity.models.Role;
import com.dev.cs_api.identity.repositories.AdminRepository;
import com.dev.cs_api.identity.repositories.RoleRepository;
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
public class AdminUserService {

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminRepository adminRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminDetailResponse findOne(UUID id) {
        Admin admin = findAdmin(id);
        return new AdminDetailResponse(admin);
    }

    public Page<AdminUserSummaryResponse> findAll(Pageable pageable) {
        return adminRepository.findAll(pageable).map(AdminUserSummaryResponse::new);
    }

    @Transactional
    public AdminDetailResponse create(AdminCreateRequest request) {
        Role role = roleRepository.findByName(RoleName.SYSTEM_ADMIN).orElseThrow((EntityNotFoundException::new));

        if (adminRepository.findByEmail(request.email()).isPresent())
            throw new EntityExistsException("Já existe um usuário com este e-mail");

        Admin admin = new Admin();
        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setPhoneNumber(request.phoneNumber());
        admin.setRole(role);
        admin.setStatus(UserStatus.ACTIVE);
        return new AdminDetailResponse(adminRepository.save(admin));
    }

    @Transactional
    public AdminDetailResponse update(UUID id, AdminUpdateRequest request) {
        Admin admin = findAdmin(id);

        if (request.name() != null && !request.name().isBlank()) {
            admin.setName(request.name());
        }

        if (request.email() != null && !request.email().isBlank()) {
            if (adminRepository.findByEmail(request.email()).isPresent())
                throw new EntityExistsException("Já existe um usuário com este E-mail");
            admin.setEmail(request.email());
        }

        if (request.phoneNumber() != null) {
            admin.setPhoneNumber(request.phoneNumber());
        }

        if (request.active() != null) {
            if (request.active()) {
                admin.setStatus(UserStatus.ACTIVE);
            } else {
                admin.setStatus(UserStatus.DISABLED);
            }
        }

        return new AdminDetailResponse(adminRepository.save(admin));
    }

    public AdminDetailResponse toggleStatus(UUID id) {
        Admin admin = findAdmin(id);

        if (admin.getStatus() == UserStatus.ACTIVE) {
            admin.setStatus(UserStatus.DISABLED);
        } else  {
            admin.setStatus(UserStatus.ACTIVE);
        }
        return new AdminDetailResponse(adminRepository.save(admin));
    }

    @Transactional
    public void delete(UUID id) { adminRepository.deleteById(id); }

    private Admin findAdmin(UUID id) {
        return adminRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("Admin não encontrado")
        );
    }
}
