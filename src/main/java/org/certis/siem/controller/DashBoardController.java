package org.certis.siem.controller;

import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.entity.EventLog;
import org.certis.siem.entity.EventStream;
import org.certis.siem.service.EventDetectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

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
}
