package us.minecraftr.thelegobros.opaskban;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Status implements CommandExecutor {

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Here we need to give items to our player
            player.sendMessage( "The plugin is working" );
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}