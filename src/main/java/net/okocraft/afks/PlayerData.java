package net.okocraft.afks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PlayerData {

    private final AFKs plugin;
    private Location prevLoc;
    private long lastAction = -1;

    PlayerData(AFKs plugin) {
        this.plugin = plugin;
    }

    void update(Player player) {
        prevLoc = player.getLocation().clone();
        lastAction = System.currentTimeMillis();
    }

    boolean isAfk(Player player) {
        if (prevLoc == null || lastAction < 0) {
            plugin.update(player);
            return false;
        }

        Location now = player.getLocation().clone();

        if (prevLoc.getPitch() == now.getPitch() && prevLoc.getYaw() == now.getYaw()) {
            return plugin.getKickPeriod() < getAFKTime();
        } else {
            plugin.update(player);
            return false;
        }
    }

    private long getAFKTime() {
        return System.currentTimeMillis() - lastAction;
    }
}
