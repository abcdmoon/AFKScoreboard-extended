package space.gorogoro.afkscoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MessageManager {
    // 過去に一度でも放置ゾーンに入ったことがあるプレイヤーを記憶するセット
    private final Set<UUID> welcomedPlayers = new HashSet<>();

    public MessageManager(AFKScoreboard afkScoreboard) {
    }
}
