package net.okocraft.afks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AFKs extends JavaPlugin {

	private Map<Player, Location> previousRotation = new HashMap<>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		new BukkitRunnable(){
	
			@Override
			public void run() {
				Bukkit.getOnlinePlayers().forEach(player -> {
					if (player.hasPermission("afks.bypass.command")) {
						return;
					}

					Location location = previousRotation.get(player);
					if (location != null) {
						if (player.getLocation().getYaw() == location.getYaw()
								&& player.getLocation().getPitch() == location.getPitch()) {
							getConfig().getStringList("command-for-afk")
									.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", player.getName())));
						}
					}
	
					previousRotation.put(player, player.getLocation());
				});
			}
		}.runTaskTimer(this, 1L, getConfig().getInt("afk-check-task-priod", 6000));
	}

	@Override
	public void onDisable() {
	}
}
