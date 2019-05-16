package net.minefs.LockedItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Functions {
	public static boolean isLocked(ItemStack i) {
		if (i == null)
			return false;
		ItemMeta im = i.getItemMeta();
		if (im == null || !im.hasLore() || !im.hasDisplayName())
			return false;
		return im.getLore().contains(Main.lockedstring);
	}

	public static boolean haveOwnerName(ItemStack i) {
		if (!isLocked(i))
			return false;
		return i.getItemMeta().getDisplayName().matches("(.*) §c\\[[a-zA-Z0-9_]+\\]");
	}

	public static boolean isOwner(ItemStack i, String o) {
		if (!haveOwnerName(i))
			return true;
		return i.getItemMeta().getDisplayName().endsWith(" §c[" + o + "]");
	}

	public static void addOwner(ItemStack i, String o) {
		if (haveOwnerName(i) || !isLocked(i))
			return;
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(im.getDisplayName() + " §c[" + o + "]");
		i.setItemMeta(im);
	}

	public static void removeOwner(ItemStack i, String o) {
		if (!isOwner(i, o) || !isLocked(i))
			return;
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(im.getDisplayName().replace(" §c[" + o + "]", ""));
		i.setItemMeta(im);
	}

	public static void checkPlayer(Player p) {
		ItemStack[] inventory = p.getInventory().getContents();
		for (int a = 0; a < inventory.length; a++) {
			ItemStack i = inventory[a];
			if (i == null || i.getType().equals(Material.AIR))
				continue;
			if (!isOwner(i, p.getName()) && haveOwnerName(i)) {
				inventory[a] = null;
				Main.li.getLogger().info(p.getName() + " lost " + i.getItemMeta().getDisplayName());
				p.sendMessage(Main.removed.replaceAll("%item%", i.getItemMeta().getDisplayName()));
				p.getWorld().dropItemNaturally(p.getLocation(), i);
			} else {
				Functions.removeOwner(i, p.getName());
				inventory[a] = i;
			}
		}
		p.getInventory().setContents(inventory);
		if (!Main.newversion) {
			ItemStack[] armor = p.getInventory().getArmorContents();
			int n = 0;
			for (ItemStack i : p.getInventory().getArmorContents()) {
				if (!isOwner(i, p.getName()) && haveOwnerName(i)) {
					armor[n] = null;
					Main.li.getLogger().info(p.getName() + " lost " + i.getItemMeta().getDisplayName());
					p.sendMessage(Main.removed.replaceAll("%item%", i.getItemMeta().getDisplayName()));
					p.getWorld().dropItemNaturally(p.getLocation(), i);
				} else
					Functions.removeOwner(i, p.getName());
				n++;
			}
			p.getInventory().setArmorContents(armor);
		}
	}

	public static void lockAll(Player p) {
		for (ItemStack i : p.getInventory().getContents())
			addOwner(i, p.getName());
		ItemStack[] armor = p.getInventory().getArmorContents();
		for (ItemStack i : p.getInventory().getArmorContents())
			addOwner(i, p.getName());
		p.getInventory().setArmorContents(armor);
	}

	public static boolean containsCommand(List<String> commands, String command) {
		for (String c : commands) {
			if (c.equalsIgnoreCase(command))
				return true;
		}
		return false;
	}

	public static boolean haveLockedItems(Player p) {
		List<ItemStack> c = new ArrayList<ItemStack>(Arrays.asList(p.getInventory().getContents().clone()));
		List<ItemStack> c2 = new ArrayList<ItemStack>(Arrays.asList(p.getInventory().getArmorContents().clone()));
		c.addAll(c2);
		for (ItemStack i : c) {
			if (!isOwner(i, p.getName()))
				return true;
		}
		return false;
	}
}
