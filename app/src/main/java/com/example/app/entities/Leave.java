package com.example.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Leave")
@Table(name = "_leave")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NotNull
    private UUID leaveId;
    @Enumerated(value = EnumType.STRING)
    private LeaveType leaveType;
    private LocalDate leaveStarts;
    private LocalDate leaveEnds;
    private String approvedBy;
    private LocalDate approvedOn;
    private Boolean approved;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.PERSIST,fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User requestedBy;

    public Boolean isApproved(){
        return this.approved;
    }
}
