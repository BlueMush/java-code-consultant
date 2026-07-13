package com.example.point;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/getPoint")
    public PointJpaEntity getPoint(@RequestParam Long id) {
        return pointService.findEntity(id);
    }

    @PostMapping("/point/calc")
    public Map<String, Object> doCalc(@RequestParam double price, @RequestParam String grade) {
        double rate;
        if (grade.equals("VIP")) {
            rate = 0.05;
        } else if (grade.equals("GOLD")) {
            rate = 0.03;
        } else {
            rate = 0.01;
        }
        double reward = pointService.calculateReward(price, rate);
        Map<String, Object> res = new HashMap<>();
        res.put("reward", reward);
        return res;
    }
}
