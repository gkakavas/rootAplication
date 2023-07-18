package com.example.app.models.requests;

import com.example.app.entities.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestEntity {
    private LeaveType leaveType;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveStarts;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveEnds;
}
