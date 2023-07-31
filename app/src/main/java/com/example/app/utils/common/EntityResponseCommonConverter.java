package com.example.app.utils.common;

import com.example.app.entities.User;
import com.example.app.models.responses.common.UserWithFiles;
import com.example.app.models.responses.common.UserWithLeaves;

import java.util.List;

public interface EntityResponseCommonConverter {
    List<UserWithLeaves> usersWithLeaves(List<User> users);
    List<UserWithFiles> usersWithFilesList(List<User> users);
}
