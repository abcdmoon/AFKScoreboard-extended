package space.gorogoro.afkscoreboard;

import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager {

    private final Map<String, ZoneArea> loadedZones = new HashMap<>();

    // プレイヤーが現在いるゾーン
    private final Map<UUID, String> playerZones = new HashMap<>();

    /**
     * AxAFKZoneからゾーンを読み込む
     */
    public void reloadAxAFKZones() {
        loadedZones.clear();

        ConcurrentHashMap<String, Zone> zones = Zones.getZones();

        for (Zone zone : zones.values()) {
            if (zone.getRegion().getWorld() == null) {
                continue;
            }

            String name = zone.getName().replace(".", "_");

            ZoneArea newZone = new ZoneArea(
                    name,
                    zone.getRegion().getWorld(),
                    zone.getRegion().getCorner1(),
                    zone.getRegion().getCorner2()
            );

            loadedZones.put(name, newZone);
        }
    }

    /**
     * Locationが属しているゾーン名を返す。
     * どのゾーンにも属していない場合はnull。
     */
    public String getZoneByLoc(Location location) {
        for (ZoneArea zone : loadedZones.values()) {
            if (zone.isInArea(location)) {
                return zone.getName();
            }
        }
        return null;
    }

    /**
     * プレイヤーが現在いるゾーン名を返す。
     * どのゾーンにも属していない場合はnull。
     */
    public String getZoneByPlayer(UUID uuid) {
        return playerZones.get(uuid);
    }

    /**
     * プレイヤーの現在位置を調べ、
     * ゾーンが変化した場合にその変化を返す。
     */
    public ZoneChange updatePlayerZone(Player player) {
        UUID uuid = player.getUniqueId();

        String before = playerZones.get(uuid);
        String now = getZoneByLoc(player.getLocation());

        if (Objects.equals(before, now)) {
            return null;
        }

        if (now == null) {
            playerZones.remove(uuid);
        } else {
            playerZones.put(uuid, now);
        }

        return new ZoneChange(before, now);
    }

    /**
     * プレイヤーを管理対象から削除します。
     */
    public void removePlayer(UUID uuid) {
        playerZones.remove(uuid);
    }

    public boolean isInAnyZone(Location location) {
        return getZoneByLoc(location) != null;
    }

    public Collection<ZoneArea> getAllZones() {
        return loadedZones.values();
    }

    /**
     * ゾーン変更情報
     *
     * before = null, now != null → 進入
     * before != null, now = null → 退出
     * before != null, now != null → 別ゾーンへ移動
     */
    public record ZoneChange(String before, String now) {

        public boolean entered() {
            return before == null && now != null;
        }

        public boolean left() {
            return before != null && now == null;
        }

        public boolean changedZone() {
            return before != null
                    && now != null
                    && !before.equals(now);
        }
    }

    /**
     * ゾーンの立体範囲
     */
    public static class ZoneArea {

        private final String name;
        private final String world;

        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;
        private final double minZ;
        private final double maxZ;

        public ZoneArea(
                String name,
                World world,
                Location loc1,
                Location loc2
        ) {
            this.name = name;
            this.world = world.getName();

            this.minX = Math.min(loc1.getX(), loc2.getX());
            this.maxX = Math.max(loc1.getX(), loc2.getX())+1;

            this.minY = Math.min(loc1.getY(), loc2.getY());
            this.maxY = Math.max(loc1.getY(), loc2.getY())+1;

            this.minZ = Math.min(loc1.getZ(), loc2.getZ());
            this.maxZ = Math.max(loc1.getZ(), loc2.getZ())+1;
        }

        public String getName() {
            return name;
        }

        public boolean isInArea(Location location) {
            if (location.getWorld() == null) {
                return false;
            }

            return world.equalsIgnoreCase(location.getWorld().getName())
                    && location.getX() >= minX
                    && location.getX() <= maxX
                    && location.getY() >= minY
                    && location.getY() <= maxY
                    && location.getZ() >= minZ
                    && location.getZ() <= maxZ;
        }
    }
}