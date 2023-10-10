package com.example.app.generators.group;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import org.instancio.Instancio;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.instancio.Select.field;

public class GroupRelevantGenerator {

    public static Group generateValidGroup(@Nullable List<User> users){
        return Instancio.of(Group.class)
                .generate(field("groupName"),gen -> gen.string().mixedCase().length(7,8))
                .set(field("groupCreationDate"), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .set(field("groupHasUsers"),users)
                .create();
    }
}
