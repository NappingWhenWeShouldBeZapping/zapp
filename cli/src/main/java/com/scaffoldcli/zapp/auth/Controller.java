package com.scaffoldcli.zapp.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Controller {
    @GetMapping("login/success")
    public String getUserInfo(){
        return "Login success!";
    }
}