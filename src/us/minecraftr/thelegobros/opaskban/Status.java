package us.minecraftr.thelegobros.opaskban;

import net.minecraft.server.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Status implements CommandExecutor {

    private static JavaPlugin plugin;

    public Status(JavaPlugin instance) {
        plugin = instance;
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Here we need to give items to our player
            player.sendMessage( "The plugin is working" );
            new UpdateChecker(plugin, 96976).getVersion(version -> {
                if (!plugin.getDescription().getVersion().equals(version)) {
                    player.sendMessage("You are not running the latest release of this plugin :/");
                    player.sendMessage("Current version: " + plugin.getDescription().getVersion());
                    player.sendMessage("Latest available version: " + version);
                } else {
                    player.sendMessage("You are running the latest release :)");
                }
            });
        }


        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}