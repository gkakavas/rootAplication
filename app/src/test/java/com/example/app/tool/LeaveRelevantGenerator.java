package com.example.app.tool;

import com.example.app.entities.LeaveType;
import com.example.app.models.requests.LeaveRequestEntity;
import org.instancio.Instancio;

import static org.instancio.Select.field;

public class LeaveRelevantGenerator {
     public static LeaveRequestEntity generateValidLeaveRequestEntity(){
         return Instancio.of(LeaveRequestEntity.class)
                 .generate(field(LeaveRequestEntity::getLeaveType),gen->
                         gen.enumOf(LeaveType.class).asString())
                 .generate(field(LeaveRequestEntity::getLeaveStarts),gen ->
                         gen.temporal().localDate().past().asString())
                 .generate(field(LeaveRequestEntity::getLeaveEnds),gen ->
                         gen.temporal().localDate().future().asString())
                 .create();
     }
}
