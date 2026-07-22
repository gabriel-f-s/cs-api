package com.dev.cs_api.tenancy.api;

import java.util.UUID;

public record TenantCompanyResponse(
    UUID id,
    String tradeName,
    String corporateName,
    String document
) { }
