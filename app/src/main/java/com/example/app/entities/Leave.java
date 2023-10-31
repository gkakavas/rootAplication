package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "requestedBy")
@Entity(name = "Leave")
@Table(name = "leaves")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    private UUID leaveId;
    @Enumerated(value = EnumType.STRING)
    private LeaveType leaveType;
    private LocalDate leaveStarts;
    private LocalDate leaveEnds;
    private UUID approvedBy;
    private LocalDateTime approvedOn;
    private Boolean approved;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User requestedBy;

    public Boolean isApproved(){
        return this.approved;
    }
}
