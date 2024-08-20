package com.quartztop.bonus.tokens.controllers;

import com.quartztop.bonus.tokens.TokenCrudService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/token-not-found")
public class TokenNotFound {

    private TokenCrudService tokenCrudService;

    @GetMapping()
    public String doRegistry() {

        return "token-not-found";
    }
}
