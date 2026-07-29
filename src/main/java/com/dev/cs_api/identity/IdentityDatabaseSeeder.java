package com.dev.cs_api.identity;

import com.dev.cs_api.identity.user.models.Admin;
import com.dev.cs_api.identity.user.models.Permission;
import com.dev.cs_api.identity.user.models.Role;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.enums.PermissionName;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.repositories.PermissionRepository;
import com.dev.cs_api.identity.user.repositories.RoleRepository;
import com.dev.cs_api.identity.user.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class IdentityDatabaseSeeder implements CommandLineRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(IdentityDatabaseSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityDatabaseSeeder(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            List<Role> roles = createRolesAndPermissions();
            LOGGER.info("Empty database! Creating the first System Administrator...");
            createAdminAccount(roles.getFirst());
            LOGGER.info("Admin account successfully created! Email: admin@master.com | Password: Master@2026");
            createUserAccount(roles.get(1));
            LOGGER.info("User account successfully created! Email: user@email.com | Password: User@2026");
        }
    }

    private void createAdminAccount(Role role) {
        Admin masterAdmin = new Admin();
        masterAdmin.setName("Admin Temporário");
        masterAdmin.setEmail("admin@master.com");
        masterAdmin.setPassword(passwordEncoder.encode("Master@2026"));
        masterAdmin.setPhoneNumber("34999999999");
        masterAdmin.setStatus(UserStatus.ACTIVE);
        masterAdmin.setRole(role);
        userRepository.save(masterAdmin);
    }

    private void createUserAccount(Role role) {
        User user = new User();
        user.setName("Usuário Temporário");
        user.setEmail("user@email.com");
        user.setPassword(passwordEncoder.encode("User@2026"));
        user.setPhoneNumber("34999999991");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        userRepository.save(user);
    }

    private List<Role> createRolesAndPermissions() {
        Role systemAdmin = new Role();
        systemAdmin.setName(RoleName.SYSTEM_ADMIN);
        Role tenantAdmin = new Role();
        tenantAdmin.setName(RoleName.TENANT_ADMIN);
        Permission tenantWritePermission = new Permission();
        tenantWritePermission.setName(PermissionName.TENANT_CREATE.getPermission());
        Permission tenantReadPermission = new Permission();
        tenantReadPermission.setName(PermissionName.TENANT_READ.getPermission());
        Permission userReadPermission = new Permission();
        userReadPermission.setName(PermissionName.USER_READ.getPermission());
        permissionRepository.saveAll(List.of(tenantWritePermission, tenantReadPermission, userReadPermission));

        systemAdmin.addPermission(tenantWritePermission);
        systemAdmin.addPermission(tenantReadPermission);
        systemAdmin.addPermission(userReadPermission);
        tenantAdmin.addPermission(tenantReadPermission);
        roleRepository.saveAll(List.of(systemAdmin, tenantAdmin));
        return List.of(systemAdmin, tenantAdmin);
    }
}
