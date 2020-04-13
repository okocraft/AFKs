package net.okocraft.afks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AFKListener implements Listener {

    public final AFKs plugin;

    public AFKListener(AFKs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        plugin.update(e.getWhoClicked());
    }
}
