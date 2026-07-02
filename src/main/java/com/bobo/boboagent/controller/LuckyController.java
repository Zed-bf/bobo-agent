package com.bobo.boboagent.controller;


//import com.bobo.boboagent.aiservice.LuckyAssistant;
import com.bobo.boboagent.aiservice.LuckyApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class LuckyController {


    //
//    @Autowired
//    private LuckyAssistant luckyAssistant;
//
//
//    @GetMapping("/lucky")
//    public String lucky(@RequestParam String question){
//        return luckyAssistant.lucky(question);
//    }

    @Autowired
    private LuckyApp luckyApp;

    @GetMapping("/stream/lucky")
    public SseEmitter luckyByStream(@RequestParam String question){


        SseEmitter emitter = new SseEmitter(1000 * 60 * 5L);

        luckyApp.luckyByStream( "dev_test",question).subscribe(
                data -> {
                        try{
                            emitter.send(SseEmitter.event()
                                    .data(data, MediaType.TEXT_PLAIN));
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    },
                emitter::completeWithError,
                emitter::complete

        );

        return emitter;
    }

    @GetMapping("/lucky")
    public String lucky(@RequestParam String question){


        return luckyApp.lucky("dev_test",question);
    }



}
