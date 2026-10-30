package com.adria.bank.account.web.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank String customerReference,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @PositiveOrZero double initialBalance) {
}
