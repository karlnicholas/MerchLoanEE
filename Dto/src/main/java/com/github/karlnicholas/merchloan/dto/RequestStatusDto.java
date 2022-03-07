package com.github.karlnicholas.merchloan.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RequestStatusDto {
    private UUID id;
    private LocalDateTime localDateTime;
    private String status;
    private String statusMessage;
}
