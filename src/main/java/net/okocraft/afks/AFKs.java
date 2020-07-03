package net.okocraft.afks;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AFKs extends JavaPlugin {

    private final Map<Player, PlayerData> dataMap = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private long kickPeriod;
    private int playerLimit;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // reloadConfig();

        kickPeriod = Math.max(getConfig().getLong("time-to-kick"), 1) * 1000;
        playerLimit = Math.max(getConfig().getInt("player-limit"), 1);

        getServer().getPluginManager().registerEvents(new AFKListener(this), this);

        long checkPeriod = Math.max(getConfig().getLong("afk-check-task-period"), 1);
        scheduler.scheduleAtFixedRate(this::runCheckTask, 1L, checkPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        scheduler.shutdownNow();
    }

    public void runCheckTask() {
        Collection<? extends Player> players = Set.copyOf(getServer().getOnlinePlayers());

        if (players.size() < playerLimit) {
            return;
        }

        players.forEach(this::check);
    }

    ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    void update(Player player) {
        PlayerData data = dataMap.getOrDefault(player, new PlayerData(this));
        data.update(player);

        if (!dataMap.containsKey(player)) {
            dataMap.put(player, data);
        }
    }

    void unload(Player player) {
        dataMap.remove(player);
    }

    long getKickPeriod() {
        return kickPeriod;
    }

    private void check(Player player) {
        if (!player.isOnline() || player.hasPermission("afks.bypass")) {
            return;
        }

        PlayerData data = dataMap.getOrDefault(player, new PlayerData(this));

        if (data.isAfk(player)) {
            getLogger().info("kick " + player.getName());
            getServer().getScheduler().runTask(this, () -> kick(player));
            unload(player);
        }
    }

    private void kick(Player player) {
        for (String cmd : getConfig().getStringList("command-for-afk")) {
            getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replace("%player%", player.getName()));
        }
    }
}
