package com.AMO.autismGame.Login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @PostMapping("/api/auth/identify")
    void login(@RequestParam("username") String username, @RequestParam("password") String password) {
        System.out.println("hi");
    }
}
