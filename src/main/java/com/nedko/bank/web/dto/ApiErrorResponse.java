package com.nedko.bank.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public record ApiErrorResponse(
        int status,
        String title,
        String detail,
        Instant timestamp,
        Map<String, List<String>> errors) {
}
