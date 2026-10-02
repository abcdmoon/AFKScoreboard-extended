package me.dragonwhale7.afkscoreboard.event;

import me.dragonwhale7.afkscoreboard.*;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class EventManager implements Listener {

    private final ZoneManager zoneManager;
    private final MessageManager messageManager;
    private final RankingManager rankingManager;
    private final ScoreManager scoreManager;
    private final PlayerDataManager playerDataManager;

    public EventManager(ZoneManager zoneManager, MessageManager messageManager, RankingManager rankingManager, ScoreManager scoreManager,PlayerDataManager playerDataManager) {
        this.zoneManager = zoneManager;
        this.messageManager = messageManager;
        this.rankingManager = rankingManager;
        this.scoreManager = scoreManager;
        this.playerDataManager = playerDataManager;
        AFKScoreboard.registerTaskTimer(this::checkPlayerZone,0,20L);
        AFKScoreboard.addOnDisableTask(this::onDisable);
    }

    private void checkPlayerZone(){
        for(Player p : Bukkit.getOnlinePlayers()){
            String before = zoneManager.getZoneByPlayer(p.getUniqueId());
            String now = zoneManager.getZoneByLoc(p.getLocation());
            if(!Objects.equals(before, now)){
                if(before!=null){
                    onPlayerLeaveZone(p,before);
                }
                if(now!=null){
                    onPlayerEnterZone(p,now);
                }
            }
        }
    }

    private void onDisable(){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(zoneManager.getZoneByPlayer(p.getUniqueId()) != null){
                onPlayerLeaveZone(p, zoneManager.getZoneByPlayer(p.getUniqueId()));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        playerDataManager.onPlayerJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        scoreManager.onPlayerDisconnect(e.getPlayer());
        if(zoneManager.getZoneByPlayer(e.getPlayer().getUniqueId()) != null) {
            onPlayerLeaveZone(e.getPlayer(), zoneManager.getZoneByPlayer(e.getPlayer().getUniqueId()));
        }
    }

    public void onZoneReload(Map<String, Set<UUID>> oldAfkPlayers){
        for(Player p : Bukkit.getOnlinePlayers()){
            String before = null;
            for(Map.Entry<String, Set<UUID>> entry : oldAfkPlayers.entrySet()){
                if(entry.getValue().contains(p.getUniqueId())){
                    before = entry.getKey();
                }
            }
            String now = zoneManager.getZoneByLoc(p.getLocation());
            if(!Objects.equals(before, now)){
                if(before!=null){
                    rankingManager.onPlayerLeaveZone(p);
                    scoreManager.onPlayerLeaveZone(p);
                }
                if(now!=null){
                    onPlayerEnterZone(p,now);
                }
            }
        }
    }

    public void onPlayerEnterZone(Player p, String zoneName) {
        zoneManager.onPlayerEnterZone(p,zoneName);
        messageManager.onPlayerEnterZone(p);

        rankingManager.onPlayerEnterZone(p);
        scoreManager.onPlayerEnterZone(p);
    }

    public void onPlayerLeaveZone(Player p, String zoneName) {
        zoneManager.onPlayerLeaveZone(p,zoneName);
        rankingManager.onPlayerLeaveZone(p);
        scoreManager.onPlayerLeaveZone(p);
    }

    /*
    public void onReloadZone(Map<UUID,Boolean> oldStateMap) {
        for(Player p : Bukkit.getOnlinePlayers()){
            if((!oldStateMap.get(p.getUniqueId()))&&zoneManager.isLocInAnyZone(p.getLocation())) {
                onPlayerEnterZone(p);
            }else if(oldStateMap.get(p.getUniqueId())&&!zoneManager.isLocInAnyZone(p.getLocation())) {
                onPlayerLeaveZone(p);
            }
        }
    }

     */
}
