package net.okocraft.afks;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class AFKListener implements Listener {

    public final AFKs plugin;

    public AFKListener(AFKs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        plugin.getScheduler().submit(() ->
                Optional.ofNullable(Bukkit.getPlayer(e.getWhoClicked().getUniqueId())).ifPresent(plugin::update));

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        plugin.getScheduler().submit(() -> plugin.unload(e.getPlayer()));
    }
}
