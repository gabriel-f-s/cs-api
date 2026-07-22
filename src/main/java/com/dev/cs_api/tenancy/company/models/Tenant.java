package com.dev.cs_api.tenancy.company.models;

import com.dev.cs_api.tenancy.company.enums.TenantPlan;
import com.dev.cs_api.tenancy.company.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "master")
@Getter
@Setter
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "trade_name", nullable = false)
    private String tradeName; // Nome fantasia

    @Column(name = "corporate_name", nullable = false)
    private String corporateName; // Razão social

    @Column(unique = true)
    private String document;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToOne(mappedBy = "tenant", cascade = CascadeType.ALL, optional = false)
    private TenantCustomization customization;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_plan")
    private TenantPlan currentPlan;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    @Column(name = "is_exclusive_db")
    private Boolean isExclusiveDB = false;

    @Column(name = "database_url")
    private String databaseUrl;

    @Column(name = "schema_name")
    private String schemaName;

    private ZonedDateTime timezone;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


}
