package com.example.app.models.requests;

import com.example.app.entities.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestEntity implements RequestEntity {
    private LeaveType leaveType;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveStarts;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveEnds;
}
