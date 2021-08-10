package net.minefs.LockedItems;

import net.minefs.DeathDropsAPI.PlayerDeathDropEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumSet;

public final class Listeners implements Listener {

    Main main = Main.getPlugin(Main.class);

    @EventHandler(priority = EventPriority.MONITOR)
    public void checkClick(InventoryClickEvent e) {
        if (e.getView().getTitle().toLowerCase().contains("kho hàng") && Functions.isLocked(e.getCurrentItem())) {
            e.setCancelled(true);
            return;
        }
        InventoryType it = e.getInventory().getType();
        ClickType clicktype = e.getClick();
        Player player = (Player) e.getWhoClicked();
        if (isContainer(e.getInventory())) {
            if (!player.hasPermission("lockeditems.ignore")) {
                if (e.getClickedInventory() != null) {
                    if (clicktype.equals(ClickType.NUMBER_KEY) || (Main.nodrop && e.getSlot() >= 36
                            && e.getSlot() <= 39) && player.getInventory().firstEmpty() == -1) {
                        e.setCancelled(true);
                        return;
                    }
                    ItemStack cur = e.getCursor();
                    ItemStack click = e.getCurrentItem();
                    Location loc = player.getLocation();
                    int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
                    World world = loc.getWorld();
                    String worldName = world != null ? world.getName() : "không xác định";
                    String suffix = " at " + x + "," + y + "," + z + "," + worldName;

                    if (click == null || cur == null) return;
                    String iName = click.hasItemMeta() ? click.getItemMeta().getDisplayName() : click.getType().name();
                    String curName = cur.hasItemMeta() ? cur.getItemMeta().getDisplayName() : cur.getType().name();

                    int amount = click.getAmount();
                    if (e.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
                        if (!Functions.isLocked(click)) return;
                        if (Functions.isLocked(click)) {
                            if (!Functions.isOwner(click, player.getName()) || !Functions.haveOwnerName(click)) {
                                main.getLogger().warning(player.getName() + " is trying to get " +
                                        "locked item: " + iName + "§e x" + amount + suffix);
                                player.sendMessage("§cKhông phải đồ của bạn!");
                                e.setCancelled(true);
                            } else {
                                if (e.isShiftClick() && Functions.haveOwnerName(click)) {
                                    Functions.removeOwner(click, player.getName());
                                    main.getLogger().warning(player.getName() + " got " +
                                            "locked item: " + iName + "§e x" + amount + suffix);
                                }
                            }
                        }
                    } else if (!Functions.isOwner(click, player.getName())) {
                        main.getLogger().warning(player.getName() + " is trying to get " +
                                "locked item: " + iName + "§e x" + amount + suffix);
                        player.sendMessage("§cKhông phải đồ của bạn!");
                        e.setCancelled(true);
                    } else {
                        Functions.addOwner(click, player.getName());
                        if (e.isShiftClick())
                        if (Functions.isLocked(cur) && Functions.haveOwnerName(cur)) {
                            e.setCancelled(true);
                            if (e.getClickedInventory() != null && Functions.isOwner(cur, player.getName())) {
                                Functions.removeOwner(cur, player.getName());
                                player.setItemOnCursor(click);
                                player.getInventory().setItem(e.getSlot(), cur);
                                player.updateInventory();
                                main.getLogger().warning(player.getName() + " got " +
                                        "locked item: " + curName + "§e x" + amount + suffix);
                            }
                        }
                    }
                }
            }
        } else if (it.equals(InventoryType.ANVIL) && e.getRawSlot() == 2) {
            ItemStack i0 = e.getInventory().getItem(0);
            ItemStack i = e.getInventory().getItem(1);
            ItemStack i2 = e.getInventory().getItem(2);
            if (Functions.isLocked(i)
                    && !(i.getType().equals(Material.BOOK) || i.getType().equals(Material.ENCHANTED_BOOK)))
                e.setCancelled(true);
            if (i0 != null && i0.hasItemMeta() && i2 != null && i2.hasItemMeta() && i2.getItemMeta().hasDisplayName()) {
                String name = i2.getItemMeta().getDisplayName();
                ItemMeta im = i0.getItemMeta();
                if ((im.hasDisplayName() && !im.getDisplayName().equals(name)) || (!im.hasDisplayName()))
                    e.setCancelled(true);
            }
            if ((Functions.isLocked(i0) && i2 != null && !i2.getType().equals(Material.AIR)
                    && !i2.getItemMeta().hasDisplayName()) || e.isShiftClick())
                e.setCancelled(true);
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

    @EventHandler(ignoreCancelled = true)
    public void dropItem(PlayerDropItemEvent e) {
        ItemStack i = e.getItemDrop().getItemStack();
        if (Main.nodrop && Functions.isLocked(i)) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        Functions.addOwner(i, p.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void pickupItem(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            ItemStack i = e.getItem().getItemStack();
            Player player = (Player) e.getEntity();
            if (player.hasPermission("lockeditems.ignore"))
                return;
            if (!Functions.isOwner(i, player.getName()) && Functions.isLocked(i))
                e.setCancelled(true);
            else
                Functions.removeOwner(i, player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void itemFrames(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getInventory().getItemInMainHand();
        Entity et = e.getRightClicked();
        if (Functions.isLocked(i) && (et instanceof ItemFrame || p.isSneaking()))
            Functions.addOwner(i, p.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void armorStand(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getInventory().getItemInMainHand();
        Entity et = e.getRightClicked();
        if (Functions.isLocked(i) && et instanceof ArmorStand)
            Functions.addOwner(i, p.getName());
    }

    @EventHandler
    public void onEquipArmorStand(PlayerArmorStandManipulateEvent e) {
        Player player = e.getPlayer();
        ItemStack i = e.getArmorStandItem();
        if (!Functions.isOwner(i, player.getName())) {
            e.setCancelled(true);
            player.sendMessage("§cKhông phải đồ của bạn!");
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathDropEvent e) {
        ItemStack i = e.getItem();
        if (Main.keep && Functions.isLocked(i)) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        if (Functions.isLocked(i) && !Functions.haveOwnerName(i)) {
            Functions.addOwner(i, p.getName());
            e.setItem(i);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getInventory().getItemInMainHand();
        if (i.getType().equals(Material.AIR) || !Functions.isLocked(i))
            return;
        String command = e.getMessage().substring(1).split(" ")[0];
        if (Functions.containsCommand(Main.blockedcmds, command) || command.contains(":")) {
            e.setCancelled(true);
            p.sendMessage(Main.blockedcmd);
        }
    }

    @EventHandler
    public void onDragItem(InventoryDragEvent e) {
        if (e.getView().getTitle().toLowerCase().contains("kho hàng") && Functions.isLocked(e.getCursor())) {
            e.setCancelled(true);
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (!player.hasPermission("lockeditems.ignore")) {
            ItemStack cur = e.getOldCursor();
            ItemStack newCur = e.getCursor();
            if (Functions.isLocked(cur) || Functions.isLocked(newCur)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean isContainer(Inventory inv) {
        InventoryType it = inv.getType();
        EnumSet<InventoryType> set = EnumSet.of(InventoryType.CHEST, InventoryType.HOPPER,
                InventoryType.DROPPER, InventoryType.DISPENSER, InventoryType.BREWING,
                InventoryType.FURNACE, InventoryType.SHULKER_BOX, InventoryType.BARREL, InventoryType.SMOKER);
        return set.contains(it) || inv instanceof StorageMinecart;
    }
}
