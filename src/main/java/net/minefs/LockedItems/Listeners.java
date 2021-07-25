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

import java.util.Objects;

public final class Listeners implements Listener {
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
                ItemStack cur = e.getCursor();
                ItemStack click = e.getCurrentItem();
                if (click==null||cur==null) return;
                String iName = click.hasItemMeta() ? click.getItemMeta().getDisplayName() : click.getType().name(),
                        curName = cur.hasItemMeta() ? cur.getItemMeta().getDisplayName() : cur.getType().name();

                Location loc = player.getLocation();
                int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
                World world = loc.getWorld();
                String worldName = world != null ? world.getName() : "không xác định";
                String suffix = " at " + x + "," + y + "," + z + "," + worldName;

                int amount = click.getAmount(), curAmount = cur.getAmount();
                if (e.getClickedInventory() != null) {
                    if (clicktype.equals(ClickType.NUMBER_KEY) || (Main.nodrop && e.getSlot() >= 36
                            && e.getSlot() <= 39) && player.getInventory().firstEmpty() == -1) {
                        e.setCancelled(true);
                    }
                    if (Functions.isLocked(click)) {
                        if (Objects.equals(e.getClickedInventory(), player.getOpenInventory().getBottomInventory())) {
                            Functions.addOwner(click, player.getName());
                            player.getOpenInventory().getBottomInventory().setItem(e.getSlot(), null);
                            player.getOpenInventory().getTopInventory().addItem(click);
                            player.updateInventory();
                            e.setCancelled(true);
                            return;
                        }
                        if (Objects.equals(e.getClickedInventory(), player.getOpenInventory().getTopInventory())) {
                            if (!Functions.haveOwnerName(click) || !Functions.isOwner(click, player.getName())) {
                                Main.getPlugin(Main.class).getLogger().warning(player.getName() + " is trying to get " +
                                        "locked item: " + iName + "§e x" + amount + suffix);
                                player.sendMessage("§cKhông phải đồ của bạn!");
                                e.setCancelled(true);
                                return;
                            }
                            if (Functions.isOwner(click, player.getName())) {
                                Functions.removeOwner(click, player.getName());
                                player.getOpenInventory().getTopInventory().setItem(e.getSlot(), null);
                                player.getOpenInventory().getBottomInventory().addItem(click);
                                player.updateInventory();
                                e.setCancelled(true);
                                Main.getPlugin(Main.class).getLogger().warning(player.getName() + " got " +
                                        "locked item: " + iName + "§e x" + amount + suffix);
                                return;
                            }
                        }
                    }
                    if (Functions.isLocked(cur)) {
                        if (!Functions.isOwner(cur, player.getName())) {
                            e.setCancelled(true);
                            Main.getPlugin(Main.class).getLogger().warning(player.getName() + " is trying to get " +
                                    "locked item: " + curName + "§e x" + curAmount + suffix);
                        } else if (Functions.haveOwnerName(cur) && !e.isCancelled()) {
                            Functions.removeOwner(cur, player.getName());
                            e.setCancelled(true);
                            player.setItemOnCursor(click);
                            player.getInventory().setItem(e.getSlot(), cur);
                            player.updateInventory();
                            Main.getPlugin(Main.class).getLogger().warning(player.getName() + " got " +
                                    "locked item: " + curName + "§e x" + curAmount + suffix);
                        }
                        if (!Functions.haveOwnerName(cur)) {
                            Functions.addOwner(cur, player.getName());
                            if (!e.isCancelled()) player.setItemOnCursor(click);
                            player.getOpenInventory().getTopInventory().setItem(e.getSlot(), cur);
                            player.updateInventory();
                            e.setCancelled(true);
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
    public void onHeldItem(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        Functions.checkPlayer(player);
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
            if (Functions.isLocked(cur)||Functions.isLocked(newCur)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean isContainer(Inventory inv){
        InventoryType it = inv.getType();
        return it.equals(InventoryType.CHEST) || it.equals(InventoryType.HOPPER) || it.equals(InventoryType.DROPPER)
                || it.equals(InventoryType.DISPENSER) || it.equals(InventoryType.BREWING)
                || it.equals(InventoryType.FURNACE) || it.equals(InventoryType.SHULKER_BOX)
                || it.equals(InventoryType.BARREL) || it.equals(InventoryType.SMOKER)
                || inv instanceof StorageMinecart;
    }
}
