package com.bobo.boboagent.controller;


import com.bobo.boboagent.aiservice.LuckyAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LuckyController {

    @Autowired
    private LuckyAssistant luckyAssistant;


    @GetMapping("/lucky")
    public String lucky(@RequestParam String question){
        return luckyAssistant.lucky(question);
    }
}
