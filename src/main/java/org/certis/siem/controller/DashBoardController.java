package org.certis.siem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashBoardController {

    @GetMapping("")
    public String main(){
        return "index";
    }


    @GetMapping("/report")
    public String report(){
        return "report";
    }

    @GetMapping("/waf")
    public String waf(){
        return "waf";
    }

    @GetMapping("/handmade")
    public String handmade(){
        return "handmade";
    }
}
