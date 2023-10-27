package com.example.app;


import com.example.app.entities.Event;
import com.example.app.entities.Group;
import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.repositories.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.instancio.Instancio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@SpringBootTest
public class TestContainerInit {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private LeaveRepository leaveRepo;
    @Autowired
    private FileRepository fileRepo;

    public TestContainerInit(PasswordEncoder passwordEncoder, GroupRepository groupRepo, UserRepository userRepo, EventRepository eventRepo, LeaveRepository leaveRepo, FileRepository fileRepo) {
        this.passwordEncoder = passwordEncoder;
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
        this.leaveRepo = leaveRepo;
        this.fileRepo = fileRepo;
    }

    public void initialize(){
        groupRepo.saveAll(
                Set.of(
                        Group.builder().groupName("Group1").groupCreator(UUID.randomUUID()).groupCreationDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).build(),
                        Group.builder().groupName("Group2").groupCreator(UUID.randomUUID()).groupCreationDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).build(),
                        Group.builder().groupName("Group3").groupCreator(UUID.randomUUID()).groupCreationDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)).build()
                ));
        var group1 = groupRepo.findByGroupName("Group1").orElseThrow();
        var group2 = groupRepo.findByGroupName("Group2").orElseThrow();
        var group3 = groupRepo.findByGroupName("Group3").orElseThrow();

        eventRepo.saveAll(
                Set.of(
                        new Event(UUID.randomUUID(),"EventDescription1","EventBody1",UUID.randomUUID(),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),null),
                        new Event(UUID.randomUUID(),"EventDescription2","EventBody2",UUID.randomUUID(),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),null),
                        new Event(UUID.randomUUID(),"EventDescription3","EventBody3",UUID.randomUUID(),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),null)
                )
        );
        var event1 = eventRepo.findByEventDescription("EventDescription1").orElseThrow();
        var event2 = eventRepo.findByEventDescription("EventDescription2").orElseThrow();
        var event3 = eventRepo.findByEventDescription("EventDescription3").orElseThrow();
        Comparator<User> userComparator = Comparator.comparing(User::getFirstname);
        userRepo.saveAll(
                Set.of(
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName1", "LastName1", "email@test1.com", "specialization1", "currentProject1", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "ADMIN", Role.ADMIN, group1, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName2", "LastName2", "email@test2.com", "specialization2", "currentProject2", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "MANAGER", Role.MANAGER, group2, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName3", "LastName3", "email@test3.com", "specialization3", "currentProject3", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "MANAGER", Role.MANAGER, group3, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName4", "LastName4", "email@test4.com", "specialization4", "currentProject4", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "HR", Role.HR, group1, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName5", "LastName5", "email@test5.com", "specialization5", "currentProject5", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "HR", Role.HR, group1, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName6", "LastName6", "email@test6.com", "specialization6", "currentProject6", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "USER", Role.USER, group2, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName7", "LastName7", "email@test7.com", "specialization7", "currentProject7", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "USER", Role.USER, group3, null, null, null),
                        new User(UUID.randomUUID(), passwordEncoder.encode("Cdb3zgy2"), "FirstName8", "LastName8", "email@test8.com", "specialization8", "currentProject8", UUID.randomUUID(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, "USER", Role.USER, group3, null, null, null)
                ));
        List<User> userList = userRepo.findAll();
        userList.sort(userComparator);
        userList.get(0).getUserHasEvents().add(event1);
        userList.get(1).getUserHasEvents().add(event1);
        userList.get(2).getUserHasEvents().add(event2);
        userList.get(3).getUserHasEvents().add(event2);
        userList.get(4).getUserHasEvents().add(event2);
        userList.get(5).getUserHasEvents().add(event3);
        userList.get(6).getUserHasEvents().add(event3);
        userList.get(7).getUserHasEvents().add(event3);
        userRepo.saveAll(userList);
    }
}
