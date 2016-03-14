package net.minefs.LockedItems;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minefs.DeathDropsAPI.PlayerDeathDropEvent;

public final class Listeners implements Listener {
	@EventHandler
	public void checkClick(InventoryClickEvent e) {
		ClickType clicktype = e.getClick();
		if (clicktype.equals(ClickType.NUMBER_KEY)) {
			e.setCancelled(true);
			return;
		}
		InventoryType it = e.getInventory().getType();
		if (it.equals(InventoryType.CHEST) || it.equals(InventoryType.HOPPER) || it.equals(InventoryType.DROPPER)
				|| it.equals(InventoryType.DISPENSER) || it.equals(InventoryType.BREWING)
				|| it.equals(InventoryType.FURNACE)) {
			Player player = (Player) e.getWhoClicked();
			if (!player.hasPermission("lockeditems.ignore")) {
				ItemStack cur = e.getCursor();
				ItemStack click = e.getCurrentItem();
				if (e.getRawSlot() < e.getInventory().getSize()) {
					if (Functions.isLocked(click)) {
						if (!Functions.isOwner(click, player.getName()))
							e.setCancelled(true);
						else if (e.isShiftClick() && Functions.haveOwnerName(click))
							Functions.removeOwner(click, player.getName());
					}
				} else {
					if (!Functions.isOwner(click, player.getName()))
						e.setCancelled(true);
					else {
						Functions.addOwner(click, player.getName());
						if (Functions.isLocked(cur) && Functions.haveOwnerName(cur)) {
							if (Functions.isOwner(cur, player.getName())) {
								Functions.removeOwner(cur, player.getName());
								e.setCancelled(true);
								player.setItemOnCursor(click);
								player.getInventory().setItem(e.getSlot(), cur);
								player.updateInventory();
							} else
								e.setCancelled(true);
						}
					}
				}
			}
		} else if (it.equals(InventoryType.ANVIL) && e.getRawSlot() == 2) {
			ItemStack i = e.getInventory().getItem(1);
			ItemStack i0 = e.getInventory().getItem(0);
			ItemStack i2 = e.getInventory().getItem(2);
			if (Functions.isLocked(i))
				e.setCancelled(true);
			if (i0 != null && i0.hasItemMeta() && i2 != null && i2.hasItemMeta() && i2.getItemMeta().hasDisplayName()) {
				String name = i2.getItemMeta().getDisplayName();
				ItemMeta im = i0.getItemMeta();
				if ((im.hasDisplayName() && !im.getDisplayName().equals(name)) || (!im.hasDisplayName()))
					e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void checkPlayer(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Functions.checkPlayer(p);
	}

	@EventHandler
	public void checkPlayerInv(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		Functions.checkPlayer(p);
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent e) {
		ItemStack i = e.getItemDrop().getItemStack();
		Player p = e.getPlayer();
		Functions.addOwner(i, p.getName());
	}

	@EventHandler
	public void pickupItem(PlayerPickupItemEvent e) {
		ItemStack i = e.getItem().getItemStack();
		Player player = (Player) e.getPlayer();
		if (player.hasPermission("lockeditems.ignore"))
			return;
		if (!Functions.isOwner(i, player.getName()) && Functions.isLocked(i))
			e.setCancelled(true);
		else
			Functions.removeOwner(i, player.getName());
	}

	@EventHandler
	public void itemFrames(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		ItemStack i = p.getItemInHand();
		Entity et = e.getRightClicked();
		if (Functions.isLocked(i) && (et instanceof ItemFrame || p.isSneaking()))
			Functions.addOwner(i, p.getName());
	}
	
	@EventHandler
	public void armorStand(PlayerInteractAtEntityEvent e) {
		Player p = e.getPlayer();
		ItemStack i = p.getItemInHand();
		Entity et = e.getRightClicked();
		if (Functions.isLocked(i) && (et instanceof ArmorStand))
			Functions.addOwner(i, p.getName());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDeath(PlayerDeathDropEvent e) {
		if(e.isCancelled())
			return;
		ItemStack i = e.getItem();
		Player p = e.getPlayer();
		if (Functions.isLocked(i) && !Functions.haveOwnerName(i) && !e.isCancelled()) {
			Functions.addOwner(i, p.getName());
			e.setItem(i);
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		ItemStack i = p.getItemInHand();
		if (i == null || i.getType().equals(Material.AIR) || !Functions.isLocked(i))
			return;
		String command = e.getMessage().substring(1).split(" ")[0];
		if (Main.blockedcmds.contains(command)) {
			e.setCancelled(true);
			p.sendMessage("§cYou can't use this command while holding a locked item.");
		}
	}
}
