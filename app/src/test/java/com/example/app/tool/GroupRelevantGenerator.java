package com.example.app.tool;

import com.example.app.entities.Group;
import com.example.app.entities.User;
import com.example.app.models.requests.GroupRequestEntity;
import org.instancio.Instancio;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.instancio.Select.field;

public class GroupRelevantGenerator {

    public static Group generateValidGroup(@Nullable List<User> users) {
        var group = Instancio.of(Group.class)
                .generate(field("groupName"), gen -> gen.string().mixedCase().length(7, 8))
                .set(field("groupCreationDate"), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .ignore(field("groupHasUsers"))
                .create();
        if (users != null) group.getGroupHasUsers().addAll(users);
        return group;
    }

    public static GroupRequestEntity generateGroupRequestEntity(@Nullable List<User> users){
        var request = Instancio.of(GroupRequestEntity.class)
                .generate(field("groupName"), gen -> gen.string().mixedCase().length(7, 8))
                .create();
        if (users!=null) {
            request.getIdsSet().addAll(users.stream().map(User::getUserId).collect(Collectors.toSet()));
        }
        return request;
    }
}
