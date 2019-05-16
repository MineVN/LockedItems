package net.minefs.LockedItems;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckTask extends BukkitRunnable {

	private Main main;

	public CheckTask(Main main) {
		this.main = main;
		this.runTaskTimerAsynchronously(main, 20, 20);
	}

	@Override
	public void run() {
		for (Player p : new ArrayList<Player>(Bukkit.getOnlinePlayers())) {
			if (!p.hasPermission("lockeditems.ignore") && Functions.haveLockedItems(p))
				Bukkit.getScheduler().runTask(main, new Runnable() {
					@Override
					public void run() {
						Functions.checkPlayer(p);
					}
				});
		}
	}
}
