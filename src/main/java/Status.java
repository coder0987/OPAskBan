import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Status implements TabExecutor {

    private static JavaPlugin plugin;

    public Status(JavaPlugin instance) {
        plugin = instance;
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player player) {
            //This is where the plugin sends a message to the player
            player.sendMessage( "The plugin is working" );
            new UpdateChecker(plugin, 96976).getVersion(version -> {
                version = version.substring(version.indexOf("-")+1);
                String thisVersion = plugin.getDescription().getVersion();
                thisVersion = thisVersion.substring(thisVersion.indexOf("-")+1);
                if (thisVersion.compareToIgnoreCase(version) < 0) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        //Removes all autofill from the tab list
        return new ArrayList<>();
    }
}