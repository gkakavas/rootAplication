package com.example.app.tool;

import com.example.app.entities.Event;
import com.example.app.models.requests.EventRequestEntity;
import org.instancio.Instancio;

import javax.annotation.Nullable;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import static org.instancio.Select.field;

public class EventRelevantGenerator {
    public static EventRequestEntity generateValidEventRequestEntity(String eventDescription){
        return Instancio.of(EventRequestEntity.class)
                .set(field(EventRequestEntity::getEventDescription),eventDescription)
                .generate(field(EventRequestEntity::getEventBody),gen -> gen.string()
                        .length(100,200)
                        .alphaNumeric()
                        .mixedCase()
                )
                .generate(field(EventRequestEntity::getEventDateTime),gen -> gen.temporal()
                        .localDateTime()
                        .future()
                        .as(dateTime -> dateTime.truncatedTo(ChronoUnit.SECONDS).toString()))

                .generate(field(EventRequestEntity::getEventExpiration),gen -> gen.temporal()
                        .localDateTime()
                        .future()
                        .as(dateTime -> dateTime.truncatedTo(ChronoUnit.SECONDS).toString()))
                //.ignore(field("eventCreator"))
                .ignore(field("idsSet"))
                .create();
    }

    public static Event generateValidEvent(){
        return Instancio.of(Event.class)
                .generate(field(Event::getEventDescription),gen -> gen.string()
                        .length(5,100)
                        .alphaNumeric()
                        .mixedCase()
                )
                .generate(field(Event::getEventBody),gen -> gen.string()
                        .length(100,200)
                        .alphaNumeric()
                        .mixedCase()
                )
                .generate(field(Event::getEventDateTime),gen -> gen.temporal()
                        .localDateTime()
                        .future()
                        .as(dateTime -> dateTime.truncatedTo(ChronoUnit.SECONDS))
                )
                .generate(field(Event::getEventExpiration),gen -> gen.temporal()
                        .localDateTime()
                        .future()
                        .as(dateTime -> dateTime.truncatedTo(ChronoUnit.SECONDS))
                )
                .ignore(field("usersJoinInEvent"))
                .create();
    }

    


}
