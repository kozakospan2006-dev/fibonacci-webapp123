package com.example.roulette;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class RouletteController {

    @GetMapping("/spin")
    public String spinRoulette() {
        int result = new Random().nextInt(37); // 0–36
        return "Roulette result: " + result;
    }
}