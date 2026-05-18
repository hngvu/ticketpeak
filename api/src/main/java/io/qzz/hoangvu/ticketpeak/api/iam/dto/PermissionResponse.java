package io.qzz.hoangvu.ticketpeak.api.iam.dto;

import io.qzz.hoangvu.ticketpeak.api.iam.model.Permission;
import io.qzz.hoangvu.ticketpeak.api.iam.model.PermissionScope;

public record PermissionResponse(
        String code,
        String name,
        PermissionScope scope,
        String action,
        String resource
) {

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getCode(),
                permission.getName(),
                permission.getScope(),
                permission.getAction(),
                permission.getResource()
        );
    }
}
