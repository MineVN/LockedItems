package net.minefs.LockedItems;

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
		for (ItemStack i : p.getInventory().getContents()) {
			if (!isOwner(i, p.getName()) && haveOwnerName(i)) {
				p.getInventory().remove(i);
				Main.li.getLogger().info(p.getName() + " lost " + i.getItemMeta().getDisplayName());
				p.sendMessage("§c§lYou lost " + i.getItemMeta().getDisplayName()
						+ " §c§l: this is a locked item with someone else's name on it.");
				p.getWorld().dropItemNaturally(p.getLocation(), i);
			} else
				Functions.removeOwner(i, p.getName());
		}
		ItemStack[] armor = p.getInventory().getArmorContents();
		int n = 0;
		for (ItemStack i : p.getInventory().getArmorContents()) {
			if (!isOwner(i, p.getName()) && haveOwnerName(i)) {
				armor[n]=null;
				Main.li.getLogger().info(p.getName() + " lost " + i.getItemMeta().getDisplayName());
				p.sendMessage("§c§lYou lost " + i.getItemMeta().getDisplayName()
						+ " §c§l: this is a locked item with someone else's name on it.");
				p.getWorld().dropItemNaturally(p.getLocation(), i);
			} else
				Functions.removeOwner(i, p.getName());
			n++;
		}
		p.getInventory().setArmorContents(armor);
	}
}
