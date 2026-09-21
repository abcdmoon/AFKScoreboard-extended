package me.dragonwhale7.afkscoreboard.event;

import me.dragonwhale7.afkscoreboard.*;
import me.dragonwhale7.afkscoreboard.prefix.PrefixManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class EventManager implements Listener {

    private final ZoneManager zoneManager;
    private final MessageManager messageManager;
    private final RankingManager rankingManager;
    private final ScoreManager scoreManager;
    private final PrefixManager prefixManager;

    public EventManager(ZoneManager zoneManager, MessageManager messageManager, RankingManager rankingManager, ScoreManager scoreManager, PrefixManager prefixManager) {
        this.zoneManager = zoneManager;
        this.messageManager = messageManager;
        this.rankingManager = rankingManager;
        this.scoreManager = scoreManager;
        this.prefixManager = prefixManager;
        AFKScoreboard.addOnDisableTask(this::onDisable);
    }

    private void onDisable(){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(zoneManager.isLocInAnyZone(p.getLocation())){
                onPlayerLeaveZone(p);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(zoneManager.isLocInAnyZone(e.getPlayer().getLocation())) {
            scoreManager.onPlayerConnect(e.getPlayer());
            onPlayerEnterZone(e.getPlayer());
        }
        prefixManager.reloadPlayerPrefix(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if(zoneManager.isLocInAnyZone(e.getPlayer().getLocation())) {
            scoreManager.onPlayerDisconnect(e.getPlayer());
            onPlayerLeaveZone(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        // ブロックの境界線を越えて移動したときだけ判定（負荷対策）
        if (!e.hasChangedBlock()) {
            return;
        }

        //ここから領域に入ったときと出たときに分岐
        boolean wasInAnyZone = zoneManager.isLocInAnyZone(e.getFrom());
        boolean isInAnyZone = zoneManager.isLocInAnyZone(e.getTo());


        if(!wasInAnyZone && isInAnyZone) {
            onPlayerEnterZone(e.getPlayer());
        }else if(wasInAnyZone && !isInAnyZone) {
            onPlayerLeaveZone(e.getPlayer());
        }

    }

    public void onPlayerEnterZone(Player p) {
        messageManager.onPlayerEnterZone(p);

        rankingManager.onPlayerEnterZone(p);
        scoreManager.onPlayerEnterZone(p);
    }

    public void onPlayerLeaveZone(Player p) {
        rankingManager.onPlayerLeaveZone(p);
        scoreManager.onPlayerLeaveZone(p);
    }

    public void onReloadZone(Map<Player,Boolean> oldStateMap) {
        for(Player p : Bukkit.getOnlinePlayers()){
            if((!oldStateMap.get(p))&&zoneManager.isLocInAnyZone(p.getLocation())) {
                onPlayerEnterZone(p);
            }else if(oldStateMap.get(p)&&zoneManager.isLocInAnyZone(p.getLocation())) {
                onPlayerLeaveZone(p);
            }
        }
    }
}
