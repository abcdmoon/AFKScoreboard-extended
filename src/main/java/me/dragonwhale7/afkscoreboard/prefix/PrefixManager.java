package me.dragonwhale7.afkscoreboard.prefix;

import me.dragonwhale7.afkscoreboard.ConfigManager;
import me.dragonwhale7.afkscoreboard.GameScoreBoardManager;
import me.dragonwhale7.afkscoreboard.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PrefixManager {

    private final PrefixRegistry prefixRegistry;
    private final GameScoreBoardManager gameScoreBoardManager;
    private final PlayerDataManager playerDataManager;


    public PrefixManager(GameScoreBoardManager gameScoreBoardManager, PrefixRegistry prefixRegistry, PlayerDataManager playerDataManager) {
        this.gameScoreBoardManager = gameScoreBoardManager;
        this.playerDataManager = playerDataManager;
        this.prefixRegistry = prefixRegistry;
        init();
    }

    private void init(){
        for(Prefix prefix : prefixRegistry.getAllPrefixes()){
            gameScoreBoardManager.removeTeamFromAll(prefix.teamKey());
            gameScoreBoardManager.addTeamToAll(prefix.teamKey());
            gameScoreBoardManager.modifyAllTeam(prefix.teamKey(),team->{
                team.prefix(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color()));
            });
        }
    }


    public void recreatePrefixes(Set<Prefix> oldPrefixes){
        for(Prefix prefix : oldPrefixes){
            gameScoreBoardManager.removeTeamFromAll(prefix.teamKey());
        }
        for(Prefix prefix : prefixRegistry.getAllPrefixes()){
            gameScoreBoardManager.addTeamToAll(prefix.teamKey());
            gameScoreBoardManager.modifyAllTeam(prefix.teamKey(),team->{
                team.prefix(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color()));
            });
        }
        for(Player player : Bukkit.getOnlinePlayers()){
            changePrefix(player.getUniqueId(),prefixRegistry.getPrefix(playerDataManager.getShowedPrefix(player.getUniqueId())));
        }
    }

    /*
    public void reloadPlayerPrefix(UUID uuid){
        int highScore = playerDataManager.getHighScore(uuid);
        ownedPrefixes.remove(uuid);
        Prefix oldPrefix = displayedPrefixes.remove(uuid);
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
        if(oldPrefix!=null){
            Prefix prefix = prefixRegistry.getPrefix(oldPrefix.key());
            if(prefix!=null){
                changePrefix(uuid,prefix);
                displayedPrefixes.put(uuid,prefixRegistry.getPrefix(oldPrefix.key()));
            }
        }
    }

     */

    public void onScoreChange(UUID uuid,int score){
        if(prefixRegistry.getAllConditions().contains(score)){
            for(Prefix prefix : prefixRegistry.getPrefixesByCondition(score)){
                grantPrefix(uuid,prefix);
                Player player = Bukkit.getPlayer(uuid);
                if(player!=null){
                    player.sendMessage(Component.text("あなたは称号 ").append(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color())).append(Component.text(" を獲得しました")));
                }
            }
        }
    }

    public void grantPrefix(UUID uuid, Prefix prefix){
        playerDataManager.addPlayerPrefix(uuid,prefix.key());
        changePrefix(uuid,prefix);
    }

    public void changePrefix(UUID uuid, Prefix prefix){
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if(name == null){
            return;
        }
        if(playerDataManager.isHidingPrefix(uuid)){
            gameScoreBoardManager.removePlayerFromAllTeam(name);
            return;
        }
        if(prefix!=null&&prefix.key().isEmpty()){
            gameScoreBoardManager.addPlayerToAllTeam(name,prefix.teamKey());

        }else{
            gameScoreBoardManager.removePlayerFromAllTeam(name);
        }
        playerDataManager.setShowedPrefix(uuid,prefix==null?"":prefix.key());
    }

    /*
    public  Set<Prefix> getOwnedPrefixes(UUID uuid){
        return Set.copyOf(ownedPrefixes.getOrDefault(uuid,Set.of()));
    }

    public boolean isHidden(UUID uuid){
        return hiddenPlayers.contains(uuid);
    }

    public void toggleHidden(UUID uuid){
        if(hiddenPlayers.contains(uuid)){
            hiddenPlayers.remove(uuid);
            if(displayedPrefixes.get(uuid)!=null){
                changePrefix(uuid,displayedPrefixes.get(uuid));
            }else{
                List<Prefix> prefixList = new ArrayList<>(ownedPrefixes.getOrDefault(uuid,Set.of()));
                if(prefixList.isEmpty()){
                    changePrefix(uuid,null);
                }else {
                    prefixList.sort(Comparator.comparingInt(Prefix::requireScore));
                    changePrefix(uuid,prefixList.getLast());
                }
            }
        }else {
            hiddenPlayers.add(uuid);
            changePrefix(uuid,null);
        }
            configManager.savePrefixHiddenPlayers(hiddenPlayers);
    }

     */
}
