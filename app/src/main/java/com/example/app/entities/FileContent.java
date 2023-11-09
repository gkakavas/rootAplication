package com.example.app.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileContent {
    txt("text/plain") ,
    docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    rtf("application/rtf"),
    xls("application/vnd.ms-excel"),
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @Getter
    private final String fileContent;
}
