package com.example.twiliovoicemail;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Record;
import com.twilio.twiml.voice.Play;
import com.twilio.type.PhoneNumber;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("TwiML")
public class VoicemailHandler {

    static {
        Twilio.init(
            System.getenv("TWILIO_ACCOUNT_SID"),
            System.getenv("TWILIO_AUTH_TOKEN"));
    }


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

    @GetMapping(value = "/handle-unanswered-call", produces = "application/xml")
    public String handleUnansweredCall(@RequestParam("DialCallStatus") String dialCallStatus){

        if ("busy".equals(dialCallStatus) || "no-answer".equals(dialCallStatus)){
            return voicemailTwiml();
        }

        return null;
    }

    private String voicemailTwiml() {
        return new VoiceResponse.Builder()
            .pause(new Pause.Builder().length(2).build())
            .play(new Play.Builder("/message.mp3").build())
            .record(new Record.Builder()
                .playBeep(true)
                .action("/recordings")
                .build())
            .build().toXml();
    }

    @PostMapping("/recordings")
    public void handleRecording(
        @RequestParam("RecordingUrl") String requestUrl,
        @RequestParam("From") String callerNumber,
        @RequestParam("To") String twilioNumber){

        String mp3RecordingUrl = requestUrl + ".mp3";

        String smsNotification = String.format("You got an answerphone message from %s - listen here: %s", callerNumber, mp3RecordingUrl);

        Message.creator(
            new PhoneNumber(MY_CELLPHONE_NUMBER),
            new PhoneNumber(twilioNumber),
            smsNotification)
            .create();
    }


}
