package space.gorogoro.afkscoreboard;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class Util {
    /**
     * コンパクトな時間フォーマット
     */
    public static String formatTimeCompact(int totalSeconds) {
        if (totalSeconds < 60) return totalSeconds + "s";

        int totalMinutes = totalSeconds / 60;
        if (totalMinutes < 60) return totalMinutes + "m";

        int totalHours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (totalHours < 24) {
            if (minutes == 0) return totalHours + "h";
            return totalHours + "h" + minutes + "m";
        }

        int days = totalHours / 24;
        int hours = totalHours % 24;

        if (hours == 0 && minutes == 0) return days + "d";
        if (minutes == 0) return days + "d" + hours + "h";
        if (hours == 0) return days + "d" + minutes + "m";

        return days + "d" + hours + "h" + minutes + "m";
    }

    /**
     * スペクテイターかバニッシュ中か。該当する人には見た目・ランキング・本人のボードを出さない（秒数は数える）
     * バニッシュは EssentialsX などが付けるメタデータ vanished で見る。EssentialsX は解除時に false を入れるので値で判定する
     */
    public static boolean isConcealed(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }
        for (MetadataValue value : player.getMetadata("vanished")) {
            if (value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

}
