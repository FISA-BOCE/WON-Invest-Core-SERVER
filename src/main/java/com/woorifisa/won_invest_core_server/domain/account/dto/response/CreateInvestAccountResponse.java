package com.woorifisa.won_invest_core_server.domain.account.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateInvestAccountResponse(
        UUID investAccountUuid,
        UUID investUserUuid,
        String accountNoDisplay,
        String accountStatus,
        String investConnectedStatus,
        LocalDateTime openedAt
) {}
