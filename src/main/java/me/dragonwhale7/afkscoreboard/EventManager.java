package me.dragonwhale7.afkscoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventManager implements Listener {

    private final ZoneManager zoneManager;
    private final MessageManager messageManager;
    private final RankingManager rankingManager;
    private final ScoreManager scoreManager;

    public EventManager(ZoneManager zoneManager,MessageManager messageManager, RankingManager rankingManager,ScoreManager scoreManager) {
        this.zoneManager = zoneManager;
        this.messageManager = messageManager;
        this.rankingManager = rankingManager;
        this.scoreManager = scoreManager;
        onEnable();
        AFKScoreboard.addOnDisableTask(this::onDisable);
    }
    private void onEnable(){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(zoneManager.isLocInAnyZone(p.getLocation())){
                onPlayerEnterZone(p);
            }
        }
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
}
