package com.pbl.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCancellationStatsDTO {
    private Long userId;
    private String name;
    private String email;
    private Long cancellationCount;
}