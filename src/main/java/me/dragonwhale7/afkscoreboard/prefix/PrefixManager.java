package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.HighScoreManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PrefixManager {

    private final HashMap<UUID, Set<Prefix>> ownedPrefixes = new HashMap<>();
    private final HashMap<UUID,Integer> nextConditions = new HashMap<>();
    private final HighScoreManager highScoreManager;
    private final PrefixRegistry prefixRegistry;
    private final GameScoreBoardManager gameScoreBoardManager;

    public PrefixManager(GameScoreBoardManager gameScoreBoardManager,HighScoreManager highScoreManager,PrefixRegistry prefixRegistry) {
        this.gameScoreBoardManager = gameScoreBoardManager;
        this.highScoreManager = highScoreManager;
        this.prefixRegistry = prefixRegistry;
        init();
    }

    private void init(){
        for(Prefix prefix : prefixRegistry.getAllPrefixes()){
            gameScoreBoardManager.addTeamToAll(prefix.key());
            gameScoreBoardManager.modifyAllTeam(prefix.key(),team->{
                team.prefix(Component.text(prefix.prefixText()).color(prefix.color()));
            });
        }
    }

    public void onAchieveHighScore(UUID uuid,int oldscore, int score){
        for(Integer i : prefixRegistry.getAllConditions()){
            if(oldscore<=i&&i<=score){
                for(Prefix p : prefixRegistry.getPrefixesByCondition(i)){
                    grantPrefix(uuid,p);
                }
            }
        }
    }

    public void grantPrefix(UUID uuid, Prefix prefix){
        ownedPrefixes.computeIfAbsent(uuid,k->new HashSet<>()).add(prefix);
        changePrefix(uuid,prefix);
    }

    public void changePrefix(UUID uuid, Prefix prefix){
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if(name == null){
            return;
        }
        gameScoreBoardManager.addPlayerToAllTeam(name,prefix.key());
    }
}
