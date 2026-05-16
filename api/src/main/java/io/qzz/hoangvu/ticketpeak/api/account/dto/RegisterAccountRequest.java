package io.qzz.hoangvu.ticketpeak.api.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterAccountRequest(
        @Email(message = "must be a valid email address")
        @NotBlank(message = "must not be blank")
        String email,
        @NotBlank(message = "must not be blank")
        @Size(min = 8, max = 72, message = "must be between 8 and 72 characters")
        String password,
        @Size(max = 255, message = "must be at most 255 characters")
        String firstName,
        @Size(max = 255, message = "must be at most 255 characters")
        String lastName,
        @Size(max = 255, message = "must be at most 255 characters")
        String avatarUrl,
        @Past(message = "must be a past date")
        LocalDate birthDate,
        @Pattern(regexp = "^(MALE|FEMALE|UNKNOWN)?$", message = "must be MALE, FEMALE, or UNKNOWN")
        String gender,
        @Positive(message = "must be greater than 0")
        Integer cityId,
        @Size(max = 8, message = "must be at most 8 characters")
        String countryCode
) {
}
