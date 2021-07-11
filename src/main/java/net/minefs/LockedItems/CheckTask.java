package net.minefs.LockedItems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class CheckTask extends BukkitRunnable {

    private final Main main;

    public CheckTask(Main main) {
        this.main = main;
        this.runTaskTimerAsynchronously(main, 20, 20);
    }

    @Override
    public void run() {
        for (Player p : new ArrayList<Player>(Bukkit.getOnlinePlayers())) {
            if (!p.hasPermission("lockeditems.ignore") && Functions.haveLockedItems(p))
                Bukkit.getScheduler().runTask(main, () -> Functions.checkPlayer(p));
        }
    }
}
