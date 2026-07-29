package com.dev.cs_api.tenancy;

import com.dev.cs_api.tenancy.company.enums.TenantPlan;
import com.dev.cs_api.tenancy.company.enums.TenantStatus;
import com.dev.cs_api.tenancy.company.models.Tenant;
import com.dev.cs_api.tenancy.company.models.TenantCustomization;
import com.dev.cs_api.tenancy.company.repositories.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Profile("dev")
public class TenancyDatabaseSeeder implements CommandLineRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(TenancyDatabaseSeeder.class);

    private final TenantRepository tenantRepository;

    public TenancyDatabaseSeeder(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (tenantRepository.count() == 0) {
            LOGGER.info("Empty database! Creating the first Tenant...");
            createTenant();
            LOGGER.info("Tenant created!");
        }
    }

    private Tenant createTenant() {
        Tenant tenant = new Tenant();
        tenant.setSlug("empresa-teste");
        tenant.setTradeName("Empresa Teste");
        tenant.setCorporateName("Empresa Teste LTDA");
        tenant.setDocument("12345678000190");
        tenant.setEmail("empresa-teste@email.com");
        tenant.setPhoneNumber("34988999999");
        tenant.setCurrentPlan(TenantPlan.BASIC);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setTimezone(ZonedDateTime.now());

        TenantCustomization customization = new TenantCustomization();
        customization.setTenant(tenant);
        customization.setLogoUrl("https://www.flaticon.com/br/icone-gratis/enterprise_9166850");
        customization.setPrimaryColor("#9500ff");
        customization.setSecondaryColor("#000000");

        tenant.setCustomization(customization);
        return tenantRepository.save(tenant);
    }
}
