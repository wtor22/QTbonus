package com.quartztop.bonus.tokens.controllers;

import com.quartztop.bonus.tokens.TokenCrudService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/token-outdated")
public class TokenOutdatedController {

    private TokenCrudService tokenCrudService;

    @GetMapping()
    public String doRegistry() {

        return "token-outdated";
    }


}
