package com.adria.bank.account.web.rest;

import com.adria.bank.account.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull AccountStatus status) {
}
