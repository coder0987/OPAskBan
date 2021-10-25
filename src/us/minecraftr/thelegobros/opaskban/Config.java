package us.minecraftr.thelegobros.opaskban;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Objects;

public class Config implements CommandExecutor {

    private static JavaPlugin plugin;

    public Config(JavaPlugin instance) {
        plugin = instance;
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {


        FileConfiguration config = plugin.getConfig();
        String newMessage = config.getString("bannableMessages");
        assert newMessage != null;
        sender.sendMessage(newMessage);                                                                                   //DEBUG
        if (args.length == 0) {
            return false;
        } else if (args[0].equals("get")){
            sender.sendMessage(Objects.requireNonNull(config.getString("bannableMessages")));
        } else if (args.length == 1) {
            return false;
        } else if (args[0].equals("add")) {
            newMessage += ", ";
            for (int i = 1; i < args.length; i++) {
                newMessage += args[i] + " ";
            }
            newMessage = newMessage.substring(0,newMessage.length() - 1);
            config.set("bannableMessages", newMessage);
            sender.sendMessage("bannableMessages set to " + newMessage);
        } else if (args[0].equals("overwrite")) {
            newMessage = args[1] + " ";
            for (int i = 2; i < args.length; i++) {
                newMessage += args[i] + " ";
            }
            newMessage = newMessage.substring(0,newMessage.length() - 1);
            config.set("bannableMessages", newMessage);
            sender.sendMessage("bannableMessages set to " + newMessage);
        } else {
            return false;
        }

        sender.sendMessage(Arrays.toString(args));                                                                       //DEBUG

        // If the player (or console) uses our command correct, we can return true
        plugin.saveConfig();
        return true;
    }
}