package me.dragonwhale7.afkscoreboard;

import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import me.dragonwhale7.afkscoreboard.event.EventManager;
import me.dragonwhale7.afkscoreboard.prefix.PrefixRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager {

    private final AFKScoreboard plugin;

    public ZoneManager(AFKScoreboard plugin) {
        this.plugin = plugin;
    }

    // 読み込んだ各ゾーンの座標範囲データを保持するマップ
    private final Map<String, ZoneArea> loadedZones = new HashMap<>();

    /**
     * AxAFKZone の zones フォルダ内にある全 .yml から座標情報をパースして読み込む
     */
    public boolean reloadAxAFKZones(EventManager eventManager, PrefixRegistry prefixRegistry) {
        Map<String,Set<UUID>> oldAfkPlayers = new HashMap<>();
        for(Map.Entry<String,ZoneArea> entry : loadedZones.entrySet()) {
            oldAfkPlayers.put(entry.getKey(),entry.getValue().getAfkPlayers());
        }

        Plugin axPlugin = Bukkit.getPluginManager().getPlugin("AxAFKZone");
        if (axPlugin == null) {
            plugin.getLogger().warning("AxAFKZone がサーバーに導入されていないか、有効化されていません。");
            return false;
        }

        File afkZoneFolder = new File(axPlugin.getDataFolder(), "zones");
        if (!afkZoneFolder.exists() || afkZoneFolder.listFiles() == null) {
            plugin.getLogger().warning("AxAFKZoneのzonesフォルダが見つかりません。");
            return false;
        }
        Map<String, ZoneArea> newZones = new HashMap<>();
        ConcurrentHashMap<String, Zone> zones = Zones.getZones();
        out:for(Zone zone : zones.values()) {
            if(zone.getRegion().getWorld()==null){
                continue;
            }
            String name = zone.getName();
            name = name.replace(".","_");
            if(newZones.containsKey(name)){
                AFKScoreboard.warn("This zone name is already in use! : "+zone.getName()+" and "+name +" are the same!");
                continue;
            }
            ZoneArea newZone = new ZoneArea(
                    name
                    ,zone.getRegion().getWorld()
                    ,zone.getRegion().getCorner1()
                    ,zone.getRegion().getCorner2());

            for(ZoneArea zone1:newZones.values()){
                boolean overlapX =
                        zone1.minX <= newZone.maxX &&
                                zone1.maxX >= newZone.minX;

                boolean overlapY =
                        zone1.minY <= newZone.maxY &&
                                zone1.maxY >= newZone.minY;

                boolean overlapZ =
                        zone1.minZ <= newZone.maxZ &&
                                zone1.maxZ >= newZone.minZ;
                if(newZone.world.equals(zone1.world)&&overlapX && overlapY && overlapZ){
                    AFKScoreboard.warn("The area of "+newZone.name+" is overlapping");
                    continue out;
                }
            }

            newZones.put(name,newZone);
        }

        List<String> notFoundZoneNames = new ArrayList<>(prefixRegistry.getAllRequireZones().stream().filter(n -> !newZones.containsKey(n)).toList());
        notFoundZoneNames.remove("");
        if(!notFoundZoneNames.isEmpty()){
            AFKScoreboard.warn("There are prefixes that require zones which don't exist!: "+notFoundZoneNames);
        }

        loadedZones.clear();
        loadedZones.putAll(newZones);

        eventManager.onZoneReload(oldAfkPlayers);
        return true;
    }

    public void onPlayerEnterZone(Player player,String zoneName) {
        loadedZones.get(zoneName).addAfkPlayer(player.getUniqueId());
    }

    public void onPlayerLeaveZone(Player player,String zoneName) {
        loadedZones.get(zoneName).removeAfkPlayer(player.getUniqueId());
    }

    /**
     *
     * @return プレイヤーの所在ゾーンの名前 どこにも属していない場合 null
     */
    public String getZoneByPlayer(UUID uuid) {
        for(ZoneArea area : loadedZones.values()){
            if(area.isInArea(uuid)){
                return area.getName();
            }
        }
        return null;
    }

    /**
     * 座標がエリア内か取得する 毎秒のチェック以外で使う必要のある箇所はないはず
     * @return 座標の存在するゾーンの名前 どこにも入っていない場合 null
     */
    public String getZoneByLoc(Location loc) {
        for(ZoneArea area : loadedZones.values()){
            if(area.isInArea(loc)){
                return area.getName();
            }
        }
        return null;
    }

    public Collection<ZoneArea> getAllZones(){
        return loadedZones.values();
    }

    /**
     * ゾーンの立体範囲を表現・判定する内部データクラス
     */
    public static class ZoneArea {
        public String getName() {
            return name;
        }

        private final String name;
        private final String world;
        private final double minX, maxX;
        private final double minY, maxY;
        private final double minZ, maxZ;
        private final Set<UUID> afkPlayers;

        public ZoneArea(String name, World world,Location loc1, Location loc2) {
            this.name = name;
            this.world = world.getName();
            this.minX = Math.min(loc1.getX(), loc2.getX());
            this.maxX = Math.max(loc1.getX(), loc2.getX());
            this.minY = Math.min(loc1.getY(), loc2.getY());
            this.maxY = Math.max(loc1.getY(), loc2.getY());
            this.minZ = Math.min(loc1.getZ(), loc2.getZ());
            this.maxZ = Math.max(loc1.getZ(), loc2.getZ());
            this.afkPlayers = new HashSet<>();
        }

        public boolean isInArea(Location loc) {
            return world.equalsIgnoreCase(loc.getWorld().getName()) &&
                    loc.getX() >= minX && loc.getX() <= maxX &&
                    loc.getY() >= minY && loc.getY() <= maxY &&
                    loc.getZ() >= minZ && loc.getZ() <= maxZ;
        }

        public boolean isInArea(UUID uuid) {
            return afkPlayers.contains(uuid);
        }
        private void addAfkPlayer(UUID uuid) {
            afkPlayers.add(uuid);
        }
        private void removeAfkPlayer(UUID uuid) {
            afkPlayers.remove(uuid);
        }
        public Set<UUID> getAfkPlayers() {
            return Set.copyOf(afkPlayers);
        }
    }
}
