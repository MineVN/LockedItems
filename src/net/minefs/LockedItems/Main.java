package net.minefs.LockedItems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static List<String> blockedcmds;
	public static String lockedstring;
	public static Plugin li;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		lockedstring = getConfig().getString("locked").replace("&", "§");
		blockedcmds = getConfig().getStringList("blocked-cmds");
		li = this;
		getServer().getPluginManager().registerEvents(new Listeners(), this);
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String labels, String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			reloadConfig();
			lockedstring = getConfig().getString("locked").replace("&", "§");
			blockedcmds = getConfig().getStringList("blocked-cmds");
			sender.sendMessage("§aConfig reloaded!");
		} else {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can't be used in console.");
				return true;
			}
			Player p = (Player) sender;
			ItemStack i = p.getItemInHand();
			if (i == null || i.getType().equals(Material.AIR)) {
				p.sendMessage("§cHold the item you want to lock.");
				return true;
			}
			ItemMeta im = i.getItemMeta();
			if (im == null) {
				p.sendMessage("§cError occurred, please try again later.");
				return true;
			}
			if(!im.hasDisplayName()){
				p.sendMessage("§cYou must set a display name for this item first!");
				return true;
			}
			List<String> lores = (im.hasLore()) ? im.getLore() : new ArrayList<String>();
			if (Functions.isLocked(i)) {
				lores.remove(lockedstring);
				p.sendMessage("§aUnlocked!");
			} else {
				lores.add(lockedstring);
				p.sendMessage("§aLocked!");
			}
			im.setLore(lores);
			i.setItemMeta(im);
		}
		return true;
	}

}
