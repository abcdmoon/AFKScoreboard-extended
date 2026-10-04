package space.gorogoro.afkscoreboard.prefix;

import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Prefix{
        private final String key;
        public String key(){return key;}
        private final String teamKey;
        public String teamKey(){return teamKey;}
        private final int requireScore;
        public int requireScore(){return requireScore;}
        private final Set<String> requireZones;
        public Set<String> requireZones(){return requireZones;}
        private final String prefixText;
        public String prefixText(){return prefixText;}
        private final NamedTextColor color;
        public NamedTextColor color(){return color;}

    public Prefix(String key, int requireScore, List<String> requireZones, String prefixText, String color) {
        this.key = key;
        this.teamKey = createTeamKey(key);
        this.requireScore = requireScore;
        this.requireZones = requireZones.isEmpty() ? Set.of("") : Set.copyOf(requireZones);
        this.prefixText = prefixText;
        this.color = NamedTextColor.NAMES.valueOr(color,NamedTextColor.DARK_GRAY);
    }

    private static String createTeamKey(String prefixKey) {
        return "afs_" + UUID.nameUUIDFromBytes(
                prefixKey.getBytes(StandardCharsets.UTF_8)
        ).toString().replace("-", "").substring(0, 12);
    }
}
