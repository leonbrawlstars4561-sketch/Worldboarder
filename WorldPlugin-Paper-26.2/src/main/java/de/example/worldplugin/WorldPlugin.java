package de.example.worldplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldPlugin extends JavaPlugin implements CommandExecutor {

    private Location center;
    private int radius;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        radius = getConfig().getInt("radius", 10);
        loadRegion();

        if (getCommand("world") != null) {
            getCommand("world").setExecutor(this);
        }

        getLogger().info("WorldPlugin wurde aktiviert.");
    }

    @Override
    public void onDisable() {
        saveRegion();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler benutzt werden.");
            return true;
        }

        if (!player.hasPermission("world.use")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung dafür.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> setRegion(player);
            case "remove" -> removeRegion(player);
            case "info" -> showInfo(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void setRegion(Player player) {
        Location location = player.getLocation();

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        center = new Location(player.getWorld(), x, y, z);
        saveRegion();

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "World-Bereich gesetzt!");
        player.sendMessage(ChatColor.GRAY + "Mittelpunkt: " + ChatColor.WHITE + x + " " + y + " " + z);
        player.sendMessage(ChatColor.GRAY + "Radius: " + ChatColor.WHITE + radius);
        player.sendMessage(ChatColor.GRAY + "X: " + ChatColor.WHITE + (x - radius) + " bis " + (x + radius));
        player.sendMessage(ChatColor.GRAY + "Z: " + ChatColor.WHITE + (z - radius) + " bis " + (z + radius));
        player.sendMessage("");
    }

    private void removeRegion(Player player) {
        if (center == null) {
            player.sendMessage(ChatColor.RED + "Es wurde noch kein Bereich gesetzt.");
            return;
        }

        center = null;
        getConfig().set("region", null);
        saveConfig();

        player.sendMessage(ChatColor.GREEN + "Der World-Bereich wurde entfernt.");
    }

    private void showInfo(Player player) {
        if (center == null) {
            player.sendMessage(ChatColor.YELLOW + "Es wurde noch kein Bereich gesetzt.");
            return;
        }

        int x = center.getBlockX();
        int y = center.getBlockY();
        int z = center.getBlockZ();

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "World-Bereich");
        player.sendMessage(ChatColor.GRAY + "Welt: " + ChatColor.WHITE + center.getWorld().getName());
        player.sendMessage(ChatColor.GRAY + "Mittelpunkt: " + ChatColor.WHITE + x + " " + y + " " + z);
        player.sendMessage(ChatColor.GRAY + "Radius: " + ChatColor.WHITE + radius);
        player.sendMessage(ChatColor.GRAY + "X: " + ChatColor.WHITE + (x - radius) + " bis " + (x + radius));
        player.sendMessage(ChatColor.GRAY + "Z: " + ChatColor.WHITE + (z - radius) + " bis " + (z + radius));
        player.sendMessage("");
    }

    private void saveRegion() {
        if (!getConfig().getBoolean("save-region", true) || center == null) {
            return;
        }

        getConfig().set("region.world", center.getWorld().getName());
        getConfig().set("region.x", center.getBlockX());
        getConfig().set("region.y", center.getBlockY());
        getConfig().set("region.z", center.getBlockZ());
        saveConfig();
    }

    private void loadRegion() {
        if (!getConfig().getBoolean("save-region", true)) {
            return;
        }

        String worldName = getConfig().getString("region.world");
        if (worldName == null) {
            return;
        }

        World world = getServer().getWorld(worldName);
        if (world == null) {
            getLogger().warning("Die gespeicherte Welt '" + worldName + "' wurde nicht gefunden.");
            return;
        }

        center = new Location(
                world,
                getConfig().getInt("region.x"),
                getConfig().getInt("region.y"),
                getConfig().getInt("region.z")
        );
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "WorldPlugin");
        player.sendMessage(ChatColor.YELLOW + "/world set" + ChatColor.GRAY + " - Bereich an deiner Position setzen");
        player.sendMessage(ChatColor.YELLOW + "/world info" + ChatColor.GRAY + " - Bereich anzeigen");
        player.sendMessage(ChatColor.YELLOW + "/world remove" + ChatColor.GRAY + " - Bereich entfernen");
        player.sendMessage("");
    }
}
