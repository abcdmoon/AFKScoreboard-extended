package space.gorogoro.afkscoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RankingManager {

    // ランキングから自分を非表示にしているプレイヤーのUUIDを保持するセット
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public RankingManager(AFKScoreboard afkScoreboard) {
    }
}
