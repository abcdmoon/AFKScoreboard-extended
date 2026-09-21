package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.AFKScoreboard;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Prefix{
        private final String key;
        public String key(){return key;}
        private final String teamKey;
        public String teamKey(){return teamKey;}
        private final Integer requireScore;
        public Integer requireScore(){return requireScore;}
        private final String prefixText;
        public String prefixText(){return prefixText;}
        private final NamedTextColor color;
        public NamedTextColor color(){return color;}

    public Prefix(String key, int requireScore, String prefixText, String color) {
        this.key = "afkscoreboard"+key;
        this.teamKey = createTeamKey(key);
        this.requireScore = requireScore;
        this.prefixText = prefixText;
        this.color = NamedTextColor.NAMES.valueOr(color,NamedTextColor.DARK_GRAY);
    }

    private static String createTeamKey(String prefixKey) {
        return "afs_" + UUID.nameUUIDFromBytes(
                prefixKey.getBytes(StandardCharsets.UTF_8)
        ).toString().replace("-", "").substring(0, 12);
    }
}
