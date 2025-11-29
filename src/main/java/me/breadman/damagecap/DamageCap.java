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
        getLogger().info("DamageCap enabled.");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("DamageCap disabled.");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == null || item.getType() == Material.AIR) return;

        String weapon = item.getType().toString().toUpperCase();

        if (!config.contains("weapon-caps." + weapon)) {
            double damage = event.getDamage();
            config.set("weapon-caps." + weapon, damage);
            saveConfig();
            if (config.getBoolean("debug"))
                getLogger().info("Added new weapon: " + weapon + " = " + damage);
        }

        double maxDamage = config.getDouble("weapon-caps." + weapon, -1);
        double globalMax = config.getDouble("max-global-damage", -1);

        if (globalMax > 0) maxDamage = Math.min(maxDamage, globalMax);

        if (maxDamage > 0 && event.getDamage() > maxDamage) event.setDamage(maxDamage);

        if (config.getBoolean("debug")) {
            getLogger().info(player.getName() + " hit with " + weapon + " for " + event.getDamage() + " (max " + (maxDamage < 0 ? "infinite" : maxDamage) + ")");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("dcap.use")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§eUsage: /dcap <weapon> <damage|infinite>");
            return true;
        }

        String weapon = args[0].toUpperCase();
        double damage;

        if (args[1].equalsIgnoreCase("infinite") || args[1].equalsIgnoreCase("-1")) {
            damage = -1;
        } else {
            try {
                damage = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number format.");
                return true;
            }
        }

        config.set("weapon-caps." + weapon, damage);
        saveConfig();

        sender.sendMessage("§aSet max damage of §e" + weapon + " §ato " + (damage < 0 ? "infinite" : damage));
        return true;
    }
}
