package com.example.app.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {
    USER_CREATE("user::create"),
    USER_READ_ONE("user::readOne"),
    USER_READ_ALL("user::readAll"),
    USER_UPDATE("user::update"),
    USER_DELETE("user::delete"),
    USER_PATCH("user::patch"),
    READ_USER_EVENTS("user::readUserEvents"),


    EVENT_CREATE("event::create"),
    EVENT_CREATE_BY_GROUP("event::createByGroup"),
    EVENT_READ_ONE("event::readOne"),
    EVENT_READ_ALL("event::readAll"),
    EVENT_UPDATE("event::update"),
    EVENT_DELETE("event::delete"),
    ADD_USERS_TO_EVENT("event::addUsersToEvent"),
    REMOVE_USERS_FROM_EVENT("event::removeUsersFromEvent"),
    PATCH_EVENT_DETAILS("event::patchEventDetails"),


    GROUP_CREATE("group::create"),
    GROUP_READ_ONE("group::readOne"),
    GROUP_READ_ALL("group::readAll"),
    GROUP_UPDATE("group::update"),
    GROUP_DELETE("group::delete"),


    LEAVE_CREATE("leave::create"),
    LEAVE_READ_ONE("leave::readOne"),
    LEAVE_READ_ALL("leave::readAll"),
    LEAVE_UPDATE("leave::update"),
    LEAVE_DELETE("leave::delete"),
    APPROVE_LEAVE("leave::approve"),

    FILE_UPLOAD("file::upload"),
    FILE_DOWNLOAD_EVALUATION("file::downloadEvaluation"),
    FILE_DOWNLOAD_TIMESHEET("file::downloadTimesheet"),
    FILE_READ_ALL_TIMESHEETS("file::readAllTimesheets"),
    FILE_READ_ALL_EVALUATIONS("file::readAllEvaluations"),
    FILE_DELETE("file::delete"),
    APPROVE_EVALUATION("file::approveEvaluation")
    ;
    @Getter
    private final String permission;
}
