package me.dragonwhale7.afkscoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HighScoreManager {

    private final Map<UUID,Integer> highScoreMap = new HashMap<>();


    public HighScoreManager() {
    }


    public int getHighScore(UUID uuid) {
        return highScoreMap.getOrDefault(uuid,0);
    }

    public void setHighScore(UUID uuid, int newScore) {
        int oldScore = highScoreMap.getOrDefault(uuid,0);
        highScoreMap.put(uuid,newScore);
    }

    public List<Map.Entry<UUID, Integer>> getSortedList() {
        return highScoreMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();
    }


}
