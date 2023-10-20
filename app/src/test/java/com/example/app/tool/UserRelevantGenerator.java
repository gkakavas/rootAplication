package com.example.app.tool;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.models.requests.UserRequestEntity;
import com.example.app.models.responses.user.AdminUserResponse;
import com.example.app.models.responses.user.OtherUserResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.instancio.Instancio;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;

public class UserRelevantGenerator {
    public static User generateValidUser(@Nullable UUID creator,Role role, @Nullable Group group){
        var mockUser =  User.builder()
                .userId(UUID.randomUUID())
                .password("TestPass123")
                .firstname("testFirstname")
                .lastname("testLastname")
                .email(Instancio.create(String.class)+"@email.com")
                .specialization("testSpecialization")
                .currentProject("testCurrentProject")
                .createdBy(creator)
                .registerDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .roleValue(role.name())
                .role(role)
                .build();
        if(group!=null) mockUser.setGroup(group);
        return mockUser;
    }

    public static UserRequestEntity generateValidUserRequestEntity(String role, UUID group){
        var mockUserRequest = UserRequestEntity.builder()
                .password("TestPass123")
                .firstname("testFirstname1")
                .lastname("testLastname1")
                .email("test@email1.com")
                .specialization("testSpecialization1")
                .currentProject("testCurrentProject1")
                .role(role)
                .build();
        if(group!=null) mockUserRequest.setGroup(group);
        return mockUserRequest;
    }

    public static AdminUserResponse generateValidAdminUserResponse (@Nullable UUID userId,String email, @Nullable String createdBy, Role role, String groupName){
        var mockAdminUserResponse = AdminUserResponse.builder()
                .userId(UUID.randomUUID())
                .firstname("testFirstname1")
                .lastname("testLastname1")
                .email(email)
                .specialization("testSpecialization1")
                .currentProject("testCurrentProject1")
                .role(role)
                .registerDate(LocalDateTime.now().withNano(0))
                .build();
        if(userId!=null) mockAdminUserResponse.setUserId(userId);
        if(groupName!=null) mockAdminUserResponse.setGroupName(groupName);
        if(createdBy!=null) mockAdminUserResponse.setCreatedBy(createdBy);
        return mockAdminUserResponse;
    }

    public static OtherUserResponse generateValidOtherUserResponse(@Nullable UUID userId,String email, @Nullable String groupName){
        var mockOtherUserResponse = OtherUserResponse.builder()
                .userId(UUID.randomUUID())
                .firstname( "testFirstname1")
                .lastname("testLastname1")
                .email(email)
                .specialization("testSpecialization1")
                .currentProject("testCurrentProject1")
                .build();
        if(userId!=null) mockOtherUserResponse.setUserId(userId);
        if(groupName!=null) mockOtherUserResponse.setGroupName(groupName);
        return mockOtherUserResponse;
    }

    public static List<User> validUserList(int size){
        List<User> generatedUserList = new ArrayList<>();
        for(int i=0;i<=(size-1);i++){
            generatedUserList.add(Instancio.of(User.class).
                    generate(field("password"),gen -> gen.string()
                            .length(4,20)
                            .mixedCase()
                            .prefix("1")
                    )
                    .generate(field("firstname"),gen -> gen.string()
                            .length(4,20)
                            .mixedCase()
                    )
                    .generate(field("lastname"),gen -> gen.string()
                            .length(4,20)
                            .mixedCase()
                    )
                    .generate(field("email"),gen -> gen.string()
                            .length(4,20)
                            .mixedCase()
                            .suffix("@email.com")
                    )

                    .set(field("registerDate"),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .set(field("lastLogin"),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .set(field("roleValue"),"USER")
                    .set(field("role"),Role.USER)
                    .ignore(field("group"))
                    .ignore(field("userHasEvents"))
                    .ignore(field("userHasFiles"))
                    .ignore(field("userRequestedLeaves"))
                    .create()
            );
        }
        return generatedUserList;
    }

}
