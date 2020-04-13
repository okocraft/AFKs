package net.okocraft.afks;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AFKs extends JavaPlugin {

    private final Map<HumanEntity, Location> previousRotation = new HashMap<>();
    private final Map<HumanEntity, Long> lastActionTime = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private long kickPeriod;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        kickPeriod = getConfig().getLong("time-to-kick", 300) * 1000;

        getServer().getPluginManager().registerEvents(new AFKListener(this), this);
        scheduler.scheduleAtFixedRate(this::runCheckTask, 1L, getConfig().getLong("afk-check-task-period", 60), TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        scheduler.shutdownNow();
    }

    public void runCheckTask() {
        int i = 0;
        for (HumanEntity player : getServer().getOnlinePlayers()) {
            i++;
            getServer().getScheduler().runTaskLater(this, () -> check(player), i);
        }
    }

    private void check(HumanEntity player) {
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
        update(player);
    }

    private boolean checkAFKTime(HumanEntity player) {
        Long prevTime = lastActionTime.get(player);
        if (prevTime == null) {
            update(player);
            return false;
        } else {
            return kickPeriod < System.currentTimeMillis() - prevTime;
        }
    }

    private void kickAction(HumanEntity player) {
        for (String cmd : getConfig().getStringList("command-for-afk")) {
            getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replaceAll("%player%", player.getName()));
        }
    }

    public void update(HumanEntity player) {
        previousRotation.put(player, player.getLocation().clone());
        lastActionTime.put(player, System.currentTimeMillis());
    }
}
