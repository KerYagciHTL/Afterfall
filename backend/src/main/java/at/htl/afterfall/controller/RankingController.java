package at.htl.afterfall.controller;

import at.htl.afterfall.model.RankingEntry;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final Map<String, Double> scores = new ConcurrentHashMap<>();

    @GetMapping
    public List<RankingEntry> getRanking() {
        AtomicInteger rank = new AtomicInteger(1);
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(100)
                .map(e -> new RankingEntry(rank.getAndIncrement(), e.getKey(), e.getValue()))
                .toList();
    }

    @PostMapping
    public Map<String, Integer> submitScore(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("playerName");
        double worth = ((Number) body.get("netWorth")).doubleValue();
        scores.put(name, worth);
        List<String> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
        return Map.of("rank", sorted.indexOf(name) + 1);
    }
}
