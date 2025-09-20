package com.example.roulette;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootApplication
public class RouletteApplication {
    public static void main(String[] args) {
        SpringApplication.run(RouletteApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return args -> System.out.println("✅ Fibonacci Roulette Web App is running...");
    }
}

@RestController
class RouletteController {
    @Autowired
    private RouletteService service;

    @GetMapping("/play")
    public String play() {
        return service.runSimulation();
    }
}

@Service
class RouletteService {
    public String runSimulation() {
        final double INITIAL_STAKE = 100.0;
        final double TARGET_STAKE = 200.0;
        final double BASE_BET = 0.10;
        List<Integer> history = new ArrayList<>();
        List<Double> fibonacci = generateFibonacci(BASE_BET, 30);
        double balance = INITIAL_STAKE;
        int fibIndex = 0;
        Random rand = new Random();
        StringBuilder log = new StringBuilder();

        while (balance > 0 && balance < TARGET_STAKE) {
            int spin = rand.nextInt(37);
            history.add(spin);
            if (history.size() > 10) history.remove(0);
            int column = getColumnToBet(history);
            double bet = fibonacci.get(fibIndex);
            if (bet > balance) break;
            boolean win = isInColumn(spin, column);
            log.append(String.format("Spin: %d | Bet: %.2f | Column: %d | Balance: %.2f | %s\n",
                    spin, bet, column, balance, win ? "WIN" : "LOSS"));
            balance += win ? bet : -bet;
            fibIndex = win ? Math.max(fibIndex - 2, 0) : fibIndex + 1;
        }

        log.append(String.format("Final Balance: %.2f\n", balance));
        log.append(balance >= TARGET_STAKE ? "🎉 Κέρδος!" : "💸 Ήττα.");
        return log.toString();
    }

    private List<Double> generateFibonacci(double base, int length) {
        List<Double> seq = new ArrayList<>();
        seq.add(base);
        seq.add(base);
        for (int i = 2; i < length; i++)
            seq.add(seq.get(i - 1) + seq.get(i - 2));
        return seq;
    }

    private int getColumnToBet(List<Integer> history) {
        int[] lastSeen = {-1, -1, -1};
        for (int i = history.size() - 1; i >= 0; i--) {
            int n = history.get(i);
            if (isInColumn(n, 1) && lastSeen[0] == -1) lastSeen[0] = i;
            if (isInColumn(n, 2) && lastSeen[1] == -1) lastSeen[1] = i;
            if (isInColumn(n, 3) && lastSeen[2] == -1) lastSeen[2] = i;
        }
        int maxIndex = 0;
        for (int i = 1; i < 3; i++)
            if (lastSeen[i] > lastSeen[maxIndex]) maxIndex = i;
        return maxIndex + 1;
    }

    private boolean isInColumn(int num, int col) {
        if (num == 0) return false;
        int[][] columns = {
            {1,4,7,10,13,16,19,22,25,28,31,34},
            {2,5,8,11,14,17,20,23,26,29,32,35},
            {3,6,9,12,15,18,21,24,27,30,33,36}
        };
        for (int n : columns[col - 1])
            if (n == num) return true;
        return false;
    }
}

@Controller
class WebController {
    @GetMapping("/")
    @ResponseBody
    public String index() throws Exception {
        ClassPathResource html = new ClassPathResource("static/index.html");
        return StreamUtils.copyToString(html.getInputStream(), StandardCharsets.UTF_8);
    }
}