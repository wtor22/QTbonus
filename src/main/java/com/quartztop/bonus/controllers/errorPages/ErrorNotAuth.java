package com.quartztop.bonus.controllers.errorPages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/403")
@RequiredArgsConstructor
public class ErrorNotAuth {
    @GetMapping()
    public String getPage(Model model){
        return "403";
    }
}
