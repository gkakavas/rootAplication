package com.example.app.models.responses.common;

import com.example.app.models.responses.event.EventResponseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class EventWithUsers implements EventResponseEntity{
    private EventResponseEntity event;
    private List<String> userEmails;
}
