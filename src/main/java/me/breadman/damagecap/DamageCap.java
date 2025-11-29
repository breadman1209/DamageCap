package me.breadman.damagecap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class DamageCap extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("DamageCap by BreadMan enabled.");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        String key = item.getType().toString().toLowerCase();

        if (config.contains("caps." + key)) {
            double max = config.getDouble("caps." + key);
            if (event.getDamage() > max) {
                event.setDamage(max);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("dcap.use")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /dcap <weapon> maxd:<hearts>");
            return true;
        }

        String weapon = args[0].toUpperCase();
        Material mat = Material.matchMaterial(weapon);
        if (mat == null) {
            sender.sendMessage("Invalid weapon name: " + weapon);
            return true;
        }

        String param = args[1];
        if (!param.startsWith("maxd:")) {
            sender.sendMessage("Second argument must be maxd:<hearts>");
            return true;
        }

        double hearts;
        try {
            hearts = Double.parseDouble(param.substring(5));
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number format for hearts.");
            return true;
        }

        double maxDamage = hearts * 2;
        config.set("caps." + weapon.toLowerCase(), maxDamage);
        saveConfig();

        sender.sendMessage("Set max damage of " + weapon + " to " + hearts + " hearts.");
        return true;
    }
}
