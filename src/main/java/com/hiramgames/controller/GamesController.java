package com.hiramgames.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hiram 2018年11月14日 22:51
 */
@Controller
@RequestMapping("/games")
public class GamesController {

    @RequestMapping("/gobang")
    public String goBang() {
        return "games/gobang";
    }
}
