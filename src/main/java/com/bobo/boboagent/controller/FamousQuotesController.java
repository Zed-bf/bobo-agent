package com.bobo.boboagent.controller;


import com.bobo.boboagent.aiservice.FamousQuotesApp;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/famousQuotes")
public class FamousQuotesController {

    private final FamousQuotesApp famousQuotesApp;

    public FamousQuotesController(FamousQuotesApp famousQuotesApp) {
        this.famousQuotesApp = famousQuotesApp;
    }

    @RequestMapping("/chat")
    public String chat( String question) {

        return famousQuotesApp.chat( question);
    }
}
