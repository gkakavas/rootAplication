package com.example.app.config;

import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.repositories.GroupRepository;
import com.example.app.repositories.UserRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepo;
    @Override
    public void run(String... args) throws Exception {
        User user1 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName1","LastName1","email@softcom1.com","specialization1","currentProject1",null, LocalDateTime.now(),null,"ADMIN", Role.ADMIN,null,null,null,null);
        User user2 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName2","LastName2","email@softcom2.com","specialization2","currentProject2",user1.getUserId(),LocalDateTime.now(),null,"MANAGER", Role.MANAGER,null,null,null,null);
        User user3 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName3","LastName3","email@softcom3.com","specialization3","currentProject3",user1.getUserId(),LocalDateTime.now(),null,"MANAGER", Role.MANAGER,null,null,null,null);
        User user4 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName4","LastName4","email@softcom4.com","specialization4","currentProject4",user1.getUserId(),LocalDateTime.now(),null,"HR", Role.HR,null,null,null,null);
        User user5 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName5","LastName5","email@softcom5.com","specialization5","currentProject5",user1.getUserId(),LocalDateTime.now(),null,"HR", Role.HR,null,null,null,null);
        User user6 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName6","LastName6","email@softcom6.com","specialization6","currentProject6",user1.getUserId(),LocalDateTime.now(),null,"USER", Role.USER,null,null,null,null);
        User user7 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName7","LastName7","email@softcom7.com","specialization7","currentProject7",user1.getUserId(),LocalDateTime.now(),null,"USER", Role.USER,null,null,null,null);
        User user8 = new User(UUID.randomUUID(),passwordEncoder.encode("Cdb3zgy2"),"FirstName8","LastName8","email@softcom8.com","specialization8","currentProject8",user1.getUserId(),LocalDateTime.now(),null,"USER", Role.USER,null,null,null,null);
        Group group = new Group(UUID.randomUUID(),"Softcom",user1.getUserId(), LocalDateTime.now(), Set.of(user1,user2,user3,user4,user5,user6,user7,user8));
        user1.setGroup(group);
        user2.setGroup(group);
        user3.setGroup(group);
        user4.setGroup(group);
        user5.setGroup(group);
        user6.setGroup(group);
        user7.setGroup(group);
        user8.setGroup(group);
        groupRepo.save(group);
    }
}
