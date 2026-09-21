package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.AFKScoreboard;
import me.dragonwhale7.afkscoreboard.ConfigManager;
import me.dragonwhale7.afkscoreboard.HighScoreManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PrefixManager {

    private final HashMap<UUID, Set<Prefix>> ownedPrefixes = new HashMap<>();
    private final HashMap<UUID,Prefix> displayedPrefixes = new HashMap<>();
    private final PrefixRegistry prefixRegistry;
    private final GameScoreBoardManager gameScoreBoardManager;
    private final HighScoreManager highScoreManager;

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final ConfigManager configManager;

    public PrefixManager(ConfigManager configManager,GameScoreBoardManager gameScoreBoardManager, PrefixRegistry prefixRegistry, HighScoreManager highScoreManager) {
        this.configManager = configManager;
        this.gameScoreBoardManager = gameScoreBoardManager;
        this.highScoreManager = highScoreManager;
        this.prefixRegistry = prefixRegistry;
        init();
    }

    private void init(){
        for(Prefix prefix : prefixRegistry.getAllPrefixes()){
            gameScoreBoardManager.removeTeamFromAll(prefix.key());
            gameScoreBoardManager.addTeamToAll(prefix.key());
            gameScoreBoardManager.modifyAllTeam(prefix.key(),team->{
                team.prefix(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color()));
            });
        }
        hiddenPlayers.clear();
        hiddenPlayers.addAll(configManager.loadPrefixHiddenPlayers());
    }

    public void reload(Set<Prefix> loadedPrefixes){
        for(Prefix prefix : loadedPrefixes){
            gameScoreBoardManager.removeTeamFromAll(prefix.key());
        }
        for(Prefix prefix : prefixRegistry.getAllPrefixes()){
            gameScoreBoardManager.removeTeamFromAll(prefix.key());
            gameScoreBoardManager.addTeamToAll(prefix.key());
            gameScoreBoardManager.modifyAllTeam(prefix.key(),team->{
                team.prefix(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color()));
            });
        }
        for(Player player : Bukkit.getOnlinePlayers()){
            reloadPlayerHighScore(player.getUniqueId());
        }
    }

    public void reloadPlayerHighScore(UUID uuid){
        int highScore = highScoreManager.getHighScore(uuid);
        ownedPrefixes.remove(uuid);
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if(name!=null){
            gameScoreBoardManager.removePlayerFromAllTeam(name);
        }
        for(Integer i : prefixRegistry.getAllConditions()){
            if(highScore < i){
                continue;
            }
            for(Prefix prefix : prefixRegistry.getPrefixesByCondition(i)){
                grantPrefix(uuid,prefix);
            }
        }
    }

    public void onAchieveHighScore(UUID uuid,int oldscore, int score){
        for(Integer i : prefixRegistry.getAllConditions()){
            if(oldscore<i&&i<=score){
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
        if(hiddenPlayers.contains(uuid)){
            gameScoreBoardManager.removePlayerFromAllTeam(name);
            return;
        }
        if(prefix!=null){
            gameScoreBoardManager.addPlayerToAllTeam(name,prefix.key());

        }else{
            gameScoreBoardManager.removePlayerFromAllTeam(name);
        }
        displayedPrefixes.put(uuid,prefix);
    }

    public  Set<Prefix> getOwnedPrefixes(UUID uuid){
        return ownedPrefixes.getOrDefault(uuid,Set.of());
    }

    public void toggleHidden(UUID uuid){
        if(hiddenPlayers.contains(uuid)){
            hiddenPlayers.remove(uuid);
            changePrefix(uuid,displayedPrefixes.get(uuid));
        }else {
            hiddenPlayers.add(uuid);
            changePrefix(uuid,null);
        }
        configManager.savePrefixHiddenPlayers(hiddenPlayers);
    }
}
