package io.qzz.hoangvu.ticketpeak.api.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GAHoldRequest(
    @NotBlank(message = "must not be blank")
    String areaId,

    @Min(value = 1, message = "must be greater than or equal to 1")
    int quantity
) {}
