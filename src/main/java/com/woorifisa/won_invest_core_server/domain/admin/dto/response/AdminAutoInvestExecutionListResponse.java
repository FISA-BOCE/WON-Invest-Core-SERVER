package com.woorifisa.won_invest_core_server.domain.admin.dto.response;

import java.util.List;

public record AdminAutoInvestExecutionListResponse(
        AdminAutoInvestExecutionSummaryResponse summary,
        List<AdminAutoInvestExecutionItemResponse> items,
        int page,
        int size,
        long totalCount,
        int totalPages
) {
}
