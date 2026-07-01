package com.bobo.boboagent.controller;


import com.bobo.boboagent.service.WhoRU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhoRUController {


    @Autowired
    private WhoRU whoRU;


    @GetMapping("/whoRU")
    public String whoRU(@RequestParam String model){
        return whoRU.whoRU(model);
    }

}
