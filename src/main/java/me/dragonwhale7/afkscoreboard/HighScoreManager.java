package me.dragonwhale7.afkscoreboard;

import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HighScoreManager {

    private final Map<UUID,Integer> highScoreMap = new HashMap<>();
    private final PrefixManager prefixManager;


    public HighScoreManager(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
        init();
    }

    private void init(){

    }

    public int getHighScore(UUID uuid) {
        return highScoreMap.getOrDefault(uuid,0);
    }

    public void setHighScore(UUID uuid, int newScore) {
        int oldScore = highScoreMap.getOrDefault(uuid,0);
        if(oldScore<newScore){
            highScoreMap.put(uuid,newScore);
            prefixManager.onAchieveHighScore(uuid,oldScore,newScore);
        }

    }

    public List<Map.Entry<UUID, Integer>> getSortedList() {
        return highScoreMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();
    }


}
