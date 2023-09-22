package com.example.app.models.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
public class RequestId {
    private UUID uuid;
}
