package com.quartztop.bonus.user.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/")
public class UserWebController {

    @GetMapping()
    public String getStart(Model model){

        model.addAttribute("formUrl","/api/user");
        return "index";
    }
}
