package space.gorogoro.afkscoreboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ZoneManager {

    private final AFKScoreboard plugin;

    // 読み込んだ各ゾーンの座標範囲データ
    private final Map<String, ZoneArea> loadedZones = new HashMap<>();

    public ZoneManager(AFKScoreboard plugin) {
        this.plugin = plugin;
    }

    /**
     * 指定されたロケーションがいずれかの放置ゾーン内にあるかを判定します。
     */
    public boolean isInAnyZone(Location loc) {
        return getZoneByLoc(loc) != null;
    }

    /**
     * 指定されたロケーションが属しているゾーン名を返します。
     * どのゾーンにも属していない場合は null を返します。
     */
    public String getZoneByLoc(Location loc) {
        for (Map.Entry<String, ZoneArea> entry : loadedZones.entrySet()) {
            if (entry.getValue().isInArea(loc)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * AxAFKZone の zones フォルダ内にある .yml から
     * 座標情報を読み込んでゾーンを登録します。
     */
    public void reloadAxAFKZones() {
        loadedZones.clear();

        Plugin axPlugin = Bukkit.getPluginManager().getPlugin("AxAFKZone");
        if (axPlugin == null) {
            plugin.getLogger().warning(
                    "AxAFKZone がサーバーに導入されていないか、有効化されていません。"
            );
            return;
        }

        File afkZoneFolder = new File(axPlugin.getDataFolder(), "zones");
        if (!afkZoneFolder.exists() || afkZoneFolder.listFiles() == null) {
            plugin.getLogger().warning(
                    "AxAFKZoneのzonesフォルダが見つかりません。"
            );
            return;
        }

        for (File file : Objects.requireNonNull(afkZoneFolder.listFiles())) {
            if (!file.getName().endsWith(".yml")) {
                continue;
            }

            try {
                YamlConfiguration config =
                        YamlConfiguration.loadConfiguration(file);

                String locStr1 = config.getString("zone.location1");
                String locStr2 = config.getString("zone.location2");

                if (locStr1 == null || locStr2 == null) {
                    continue;
                }

                String[] split1 = locStr1.split(";");
                String[] split2 = locStr2.split(";");

                String world = split1[0];

                double x1 = Double.parseDouble(split1[1]);
                double y1 = Double.parseDouble(split1[2]);
                double z1 = Double.parseDouble(split1[3]);

                double x2 = Double.parseDouble(split2[1]);
                double y2 = Double.parseDouble(split2[2]);
                double z2 = Double.parseDouble(split2[3]);

                ZoneArea area = new ZoneArea(
                        world,
                        Math.min(x1, x2),
                        Math.max(x1, x2),
                        Math.min(y1, y2),
                        Math.max(y1, y2),
                        Math.min(z1, z2),
                        Math.max(z1, z2)
                );

                String zoneName = file.getName().replace(".yml", "");

                loadedZones.put(zoneName, area);

                plugin.getLogger().info(
                        "放置ゾーンを自動登録しました: " + zoneName
                );

            } catch (Exception e) {
                plugin.getLogger().severe(
                        "ゾーンファイルの解析に失敗しました(書式違いなど): "
                                + file.getName()
                );
            }
        }
    }

    /**
     * 読み込まれている全ゾーンを返します。
     */
    public Map<String, ZoneArea> getLoadedZones() {
        return Map.copyOf(loadedZones);
    }

    /**
     * ゾーンの立体範囲を表現・判定するデータクラス
     */
    public static class ZoneArea {

        private final String world;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;
        private final double minZ;
        private final double maxZ;

        public ZoneArea(
                String world,
                double minX,
                double maxX,
                double minY,
                double maxY,
                double minZ,
                double maxZ
        ) {
            this.world = world;

            this.minX = minX - 0.5;
            this.maxX = maxX + 0.5;

            this.minY = minY - 0.5;
            this.maxY = maxY + 0.5;

            this.minZ = minZ - 0.5;
            this.maxZ = maxZ + 0.5;
        }

        public boolean isInArea(Location loc) {
            if (loc.getWorld() == null) {
                return false;
            }

            return loc.getWorld().getName().equalsIgnoreCase(world)
                    && loc.getX() >= minX
                    && loc.getX() <= maxX
                    && loc.getY() >= minY
                    && loc.getY() <= maxY
                    && loc.getZ() >= minZ
                    && loc.getZ() <= maxZ;
        }
    }
}