package com.dev.cs_api.identity.user.repositories;

import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.models.Role;
import com.dev.cs_api.identity.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private Role role;
    private UUID tenantId1;
    private UUID tenantId2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName(RoleName.OPERATOR);
        role.setDescription("User role");
        entityManager.persist(role);

        tenantId1 = UUID.randomUUID();
        tenantId2 = UUID.randomUUID();

        user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@test.com");
        user1.setPassword("pass123");
        user1.setPhoneNumber("11911111111");
        user1.setRole(role);
        user1.setTenantId(tenantId1);
        user1.setStatus(UserStatus.ACTIVE);
        entityManager.persist(user1);

        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@test.com");
        user2.setPassword("pass123");
        user2.setPhoneNumber("11922222222");
        user2.setRole(role);
        user2.setTenantId(tenantId2);
        user2.setStatus(UserStatus.ACTIVE);
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByEmail deve buscar usuário por e-mail")
    void shouldFindByEmail() {
        Optional<User> found = userRepository.findByEmail("user1@test.com");

        assertTrue(found.isPresent());
        assertEquals("User One", found.get().getName());
    }

    @Test
    @DisplayName("findByIdAndTenantId deve buscar usuário apenas se o id e tenantId coincidirem")
    void shouldFindByIdAndTenantId() {
        Optional<User> found = userRepository.findByIdAndTenantId(user1.getId(), tenantId1);
        assertTrue(found.isPresent());
        assertEquals("User One", found.get().getName());

        Optional<User> notFoundWrongTenant = userRepository.findByIdAndTenantId(user1.getId(), tenantId2);
        assertFalse(notFoundWrongTenant.isPresent());
    }

    @Test
    @DisplayName("findAllByTenantId deve filtrar usuários pelo tenantId")
    void shouldFindAllByTenantId() {
        Page<User> page = userRepository.findAllByTenantId(tenantId1, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("User One", page.getContent().getFirst().getName());
    }

    @Test
    @DisplayName("findAllWithTenantId deve buscar apenas usuários com tenantId não nulo")
    void shouldFindAllWithTenantId() {
        Page<User> page = userRepository.findAllWithTenantId(PageRequest.of(0, 10));

        assertTrue(page.getTotalElements() >= 2);
    }

    @Test
    @DisplayName("deleteByIdAndTenantId deve remover o usuário do tenant correspondente")
    void shouldDeleteByIdAndTenantId() {
        userRepository.deleteByIdAndTenantId(user1.getId(), tenantId1);
        entityManager.flush();

        Optional<User> found = userRepository.findById(user1.getId());
        assertFalse(found.isPresent());
    }
}
