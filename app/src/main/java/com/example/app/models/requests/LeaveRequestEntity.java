package com.example.app.models.requests;

import com.example.app.entities.LeaveType;
import com.example.app.utils.validators.leave.ValidLeaveRequest;
import com.example.app.utils.validators.venum.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidLeaveRequest
public class LeaveRequestEntity implements RequestEntity {
    @ValueOfEnum(enumClass = LeaveType.class)
    private String leaveType;
    private String leaveStarts;
    private String leaveEnds;
}
