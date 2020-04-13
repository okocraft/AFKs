package net.okocraft.afks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AFKs extends JavaPlugin {

    private final Map<Player, Location> previousRotation = new HashMap<>();
    private final Map<Player, Long> lastActionTime = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private long kickPeriod;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        kickPeriod = getConfig().getLong("time-to-kick", 300) * 1000;
        scheduler.scheduleAtFixedRate(this::runCheckTask, 1L, getConfig().getLong("afk-check-task-period", 60), TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        scheduler.shutdownNow();
    }

    public void runCheckTask() {
        int i = 0;
        for (Player player : getServer().getOnlinePlayers()) {
            i++;
            getServer().getScheduler().runTaskLater(this, () -> check(player), i);
        }
    }

    private void check(Player player) {
        if (player.hasPermission("afks.bypass.command")) {
            return;
        }

        Location prev = previousRotation.get(player);
        Location now = player.getLocation().clone();

        if (prev != null) {
            if (prev.getPitch() == now.getPitch() && prev.getYaw() == now.getYaw()) {
                if (checkAFKTime(player)) {
                    kickAction(player);
                }
                return;
            }
        }
        previousRotation.put(player, now);
        lastActionTime.put(player, System.currentTimeMillis());
    }

    private boolean checkAFKTime(Player player) {
        Long prevTime = lastActionTime.get(player);
        if (prevTime == null) {
            lastActionTime.put(player, System.currentTimeMillis());
            return false;
        } else {
            return kickPeriod < System.currentTimeMillis() - prevTime;
        }
    }

    private void kickAction(Player player) {
        for (String cmd : getConfig().getStringList("command-for-afk")) {
            getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replaceAll("%player%", player.getName()));
        }
    }
}
