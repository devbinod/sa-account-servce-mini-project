package edu.miu.cs590.accountservice.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartUpController {

    @GetMapping("/")
    public String get(){
        return "Hello world from order service";
    }
}
