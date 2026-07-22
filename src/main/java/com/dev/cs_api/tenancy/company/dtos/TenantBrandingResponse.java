package com.dev.cs_api.tenancy.company.dtos;

import com.dev.cs_api.tenancy.company.enums.TenantPlan;
import com.dev.cs_api.tenancy.company.models.Tenant;

public record TenantBrandingResponse(
    String tradeName,
    TenantPlan plan,
    String logoUrl,
    String primaryColor,
    String secondaryColor
) {
    public TenantBrandingResponse(Tenant tenant) {
        this(
                tenant.getTradeName(),
                tenant.getCurrentPlan(),
                tenant.getCustomization().getLogoUrl(),
                tenant.getCustomization().getPrimaryColor(),
                tenant.getCustomization().getSecondaryColor()
        );
    }
}
