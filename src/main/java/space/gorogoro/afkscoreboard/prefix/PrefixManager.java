package space.gorogoro.afkscoreboard.prefix;

import space.gorogoro.afkscoreboard.data.ConfigManager;
import space.gorogoro.afkscoreboard.data.ConfigManager.PrefixMode;
import space.gorogoro.afkscoreboard.GameScoreBoardManager;
import space.gorogoro.afkscoreboard.data.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PrefixManager {

    private final PrefixRegistry prefixRegistry;
    private final GameScoreBoardManager gameScoreBoardManager;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;

    private final PrefixMode mode;


    public PrefixManager(GameScoreBoardManager gameScoreBoardManager, PrefixRegistry prefixRegistry, ConfigManager configManager,PlayerDataManager playerDataManager) {
        this.gameScoreBoardManager = gameScoreBoardManager;
        this.configManager = configManager;
        this.playerDataManager = playerDataManager;
        this.prefixRegistry = prefixRegistry;
        mode = configManager.loadPrefixMode();
        init();
    }

    private void init(){
        switch (mode){
            case PrefixMode.team:{
                for(Prefix prefix : prefixRegistry.getAllPrefixes()){
                    gameScoreBoardManager.removeTeamFromAll(prefix.teamKey());
                    gameScoreBoardManager.addTeamToAll(prefix.teamKey());
                    gameScoreBoardManager.modifyAllTeam(prefix.teamKey(),team->{
                        team.prefix(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color()));
                    });
                }
                break;
            }
            case PrefixMode.tab:{
                break;
            }


        }
        for(Player player : Bukkit.getOnlinePlayers()){
            changePrefix(player.getUniqueId(),prefixRegistry.getPrefix(playerDataManager.getShowedPrefix(player.getUniqueId())));
        }

    }


    public void recreatePrefixes(Set<Prefix> oldPrefixes){
        switch (mode){
            case PrefixMode.team:{
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
                break;
            }
            case PrefixMode.tab:{
                break;
            }
        }

    }

    public void onScoreChange(UUID uuid,String zoneName,int score){
        if(prefixRegistry.getAllConditions("").contains(score)){
            for(Prefix prefix : prefixRegistry.getPrefixesByRequirement("",score)){
                grantPrefix(uuid,prefix);
            }
        }

        if(prefixRegistry.getAllConditions(zoneName).contains(score)){
            for(Prefix prefix : prefixRegistry.getPrefixesByRequirement(zoneName,score)){
                grantPrefix(uuid,prefix);
            }
        }
    }

    public void grantPrefix(UUID uuid, Prefix prefix){
        if(playerDataManager.getPlayerPrefixes(uuid).contains(prefix.key())){
            return;
        }
        playerDataManager.addPlayerPrefix(uuid,prefix.key());
        changePrefix(uuid,prefix);
        Player player = Bukkit.getPlayer(uuid);
        if(player!=null){
            player.sendMessage(Component.text("あなたは称号 ").append(Component.text(prefix.prefixText()).decorate(TextDecoration.BOLD).color(prefix.color())).append(Component.text(" を獲得しました")));
        }
    }

    public void changePrefix(UUID uuid, Prefix prefix){
        switch (mode){
            case PrefixMode.team:{
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                if(name == null){
                    return;
                }
                if(playerDataManager.isHidingPrefix(uuid)){
                    gameScoreBoardManager.removePlayerFromAllTeam(name);
                    return;
                }
                if(prefix!=null&&!prefix.key().isEmpty()){
                    gameScoreBoardManager.addPlayerToAllTeam(name,prefix.teamKey());

                }else{
                    gameScoreBoardManager.removePlayerFromAllTeam(name);
                }
                playerDataManager.setShowedPrefix(uuid,prefix==null?"":prefix.key());
                break;
            }
            case PrefixMode.tab:{
                Player player =Bukkit.getPlayer(uuid);
                if(player==null){
                    return;
                }
                if(playerDataManager.isHidingPrefix(uuid)){
                    player.playerListName(Component.text(player.getName()));
                    break;
                }
                if(prefix!=null&&!prefix.key().isEmpty()){
                    player.playerListName(Component.text(prefix.prefixText()).color(prefix.color()).decorate(TextDecoration.BOLD).append(Component.text(player.getName()).color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD,false)));
                }else{
                    player.playerListName(Component.text(player.getName()));
                }
                playerDataManager.setShowedPrefix(uuid,prefix==null?"":prefix.key());
                break;
            }
        }

    }

    public void onPlayerJoin(Player player){
        changePrefix(player.getUniqueId(),prefixRegistry.getPrefix(playerDataManager.getShowedPrefix(player.getUniqueId())));
    }
}
