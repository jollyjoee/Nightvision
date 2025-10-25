package com.jolly.nightvision;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Nightvision extends JavaPlugin implements Listener, CommandExecutor {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private FileConfiguration config;

    // ✅ Store only UUIDs instead of Player objects
    private final Set<UUID> nightVisionPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("nv").setExecutor(this);
        getCommand("nvlist").setExecutor(this);
    }

    @Override
    public void onDisable() {
        nightVisionPlayers.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (nightVisionPlayers.contains(player.getUniqueId())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("nv")) {
            if (!player.hasPermission("nv.use")) {
                player.sendActionBar(mm.deserialize(config.getString("messages.no-permission", "<red>You have no permission to use this command!</red>")));
                return true;
            }

            UUID uuid = player.getUniqueId();
            if (nightVisionPlayers.contains(uuid)) {
                nightVisionPlayers.remove(uuid);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.sendActionBar(mm.deserialize(config.getString("messages.nv-disabled", "<gray>Night vision <red>disabled</red>.</gray>")));
            } else {
                nightVisionPlayers.add(uuid);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
                player.sendActionBar(mm.deserialize(config.getString("messages.nv-enabled", "<gray>Night vision <green>enabled</green>.</gray>")));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("nvlist")) {
            if (!player.hasPermission("nv.list")) {
                player.sendActionBar(mm.deserialize(config.getString("messages.nvlist-permission", "<red>You have no permission to use /nvlist!</red>")));
                return true;
            }

            player.sendMessage(mm.deserialize("<aqua>The users of /nv are: (<gold>" + nightVisionPlayers.size() + "</gold><aqua>)"));
            player.sendMessage(mm.deserialize("<gray>-------------------</gray>"));
            int count = 0;
            for (UUID uuid : nightVisionPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    count++;
                    player.sendMessage(mm.deserialize("<gray>" + count + ". <aqua>" + p.getName()));
                }
            }
            return true;
        }

        return false;
    }
}
