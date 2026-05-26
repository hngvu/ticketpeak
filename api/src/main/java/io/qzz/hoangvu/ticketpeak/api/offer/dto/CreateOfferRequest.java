package io.qzz.hoangvu.ticketpeak.api.offer.dto;

import io.qzz.hoangvu.ticketpeak.api.offer.model.SeatingMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOfferRequest(
        @NotNull(message = "must not be null")
        UUID ticketTypeId,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,

        @Size(max = 4000, message = "must be at most 4000 characters")
        String description,

        @NotBlank(message = "must not be blank")
        @Size(max = 8, message = "must be at most 8 characters")
        String currency,

        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        BigDecimal faceValue,

        boolean restrictedPayment,

        @NotNull(message = "must not be null")
        @Positive(message = "must be greater than 0")
        Integer eventTicketMinimum,

        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        Integer capacityCap,

        @NotEmpty(message = "must not be empty")
        List<@NotNull(message = "must not be null") @Positive(message = "must be greater than 0") Integer> sellableQuantities,

        List<@Valid CreateSaleWindowRequest> saleWindows,

        @NotNull(message = "must not be null")
        SeatingMode seatingMode,

        @Size(max = 64, message = "must be at most 64 characters")
        String sectionId,

        @Size(max = 64, message = "must be at most 64 characters")
        String priceLevelId,

        List<@Valid ChargeRequest> charges
) {
}
