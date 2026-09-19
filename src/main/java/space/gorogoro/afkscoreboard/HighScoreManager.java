package space.gorogoro.afkscoreboard;

import java.util.HashMap;
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
        if(oldScore<newScore){
            highScoreMap.put(uuid,newScore);
        }

    }
}
