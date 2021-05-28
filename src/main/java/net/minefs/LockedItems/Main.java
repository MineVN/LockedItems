package net.minefs.LockedItems;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public class Main extends JavaPlugin {
	public static boolean keep, nodrop, newversion = false;
	public static List<String> blockedcmds;
	public static String lockedstring, blockedcmd, musthavename, noitem, locked, unlocked, removed;
	public static Plugin li;

	@Override
	public void onEnable() {
		String packageName = this.getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);
		getLogger().info("Spigot " + version + " support.");
		if (!version.startsWith("v1_8_"))
			newversion = true;
		saveDefaultConfig();
		loadConfig();
		li = this;
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		new CheckTask(this);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String labels, String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			reloadConfig();
			loadConfig();
			sender.sendMessage("§aReload complete.");
		} else {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be in the server.");
				return true;
			}
			Player p = (Player) sender;
			ItemStack i = p.getInventory().getItemInMainHand();
			if (i == null || i.getType().equals(Material.AIR)) {
				p.sendMessage(noitem);
				return true;
			}
			ItemMeta im = i.getItemMeta();
			if (im == null) {
				p.sendMessage("§cSomething happened: Null item meta");
				return true;
			}
			if (!im.hasDisplayName()) {
				p.sendMessage(musthavename);
				return true;
			}
			List<String> lores = (im.hasLore()) ? im.getLore() : new ArrayList<String>();
			if (Functions.isLocked(i)) {
				lores.remove(lockedstring);
				p.sendMessage(unlocked);
			} else {
				lores.add(lockedstring);
				p.sendMessage(locked);
			}
			im.setLore(lores);
			i.setItemMeta(im);
		}
		return true;
	}

	private void loadConfig() {
		lockedstring = getConfig().getString("locked").replace("&", "§");
		blockedcmds = getConfig().getStringList("blocked-cmds");
		keep = getConfig().getBoolean("keep-on-death");
		nodrop = getConfig().getBoolean("block-drop");
		blockedcmd = getConfig().getString("messages.blocked-command").replace("&", "§");
		noitem = getConfig().getString("messages.no-item").replace("&", "§");
		locked = getConfig().getString("messages.locked").replace("&", "§");
		unlocked = getConfig().getString("messages.unlocked").replace("&", "§");
		removed = getConfig().getString("messages.removed").replace("&", "§");
		musthavename = getConfig().getString("messages.must-have-name").replace("&", "§");
	}

}
