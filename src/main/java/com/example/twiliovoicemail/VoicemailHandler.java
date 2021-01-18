package com.example.twiliovoicemail;

import com.twilio.http.HttpMethod;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("TwiML")
public class VoicemailHandler {
    
    private final String MY_CELLPHONE_NUMBER = System.getenv("MY_CELLPHONE_NUMBER");
    private final int ANSWERPHONE_TIMEOUT_SECONDS = 10;

    @GetMapping(value = "/initial-answer", produces = "application/xml")
    public String initialAnswer(){
        return new VoiceResponse.Builder()
            .dial(new Dial.Builder()
                .number(MY_CELLPHONE_NUMBER)
                .timeout(ANSWERPHONE_TIMEOUT_SECONDS)
                .action("/handle-unanswered-call")
                .method(HttpMethod.GET)
                .build())
            .build().toXml();
    }

}
