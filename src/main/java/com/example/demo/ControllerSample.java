package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControllerSample {
    @GetMapping("/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/secure/memberonly")
    public String memberPage() {
        return "memberonly";
    }

    @GetMapping("/secure/adminonly")
    public String adminPage() {
        return "adminonly";
    }

    @GetMapping("/secure/all")
    public String all() {
        return "all";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/secure/api")
    @ResponseBody
    public String api() {
        return "API for ajax";
    }


}
