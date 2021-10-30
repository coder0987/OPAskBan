package us.minecraftr.thelegobros.opaskban;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Config implements TabExecutor {

    private static JavaPlugin plugin;

    public Config(JavaPlugin instance) {
        plugin = instance;
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {


        FileConfiguration config = plugin.getConfig();
        StringBuilder newMessage = new StringBuilder(Objects.requireNonNull(config.getString("Bannable Messages")));
        if (args.length == 0) {
            //Returns false if the command was executed without arguments
            return false;
        } else if (args[0].equals("get")){
            //Returns the Bannable Messages config if the /config get command was executed
            sender.sendMessage(Objects.requireNonNull(config.getString("Bannable Messages")));
        } else if (args.length == 1) {
            //Returns false if /config set|add|remove was sent without a message to set add or remove
            return false;
        } else if (args[0].equals("add")) {
            //Concatenates the message onto the config and sets the config to it
            if (!(String.valueOf(newMessage).equals(" ")) || !(newMessage.isEmpty()) || !(String.valueOf(newMessage).equals(""))) {
                newMessage.append(", ");
            }
            for (int i = 1; i < args.length; i++) {
                newMessage.append(args[i]).append(" ");
            }
            newMessage = new StringBuilder(newMessage.substring(0, newMessage.length() - 1));
            config.set("Bannable Messages", newMessage.toString());
            sender.sendMessage("Bannable Messages set to " + newMessage);
        } else if (args[0].equals("overwrite")||args[0].equals("set")) {
            //Sets the bannable messages to whatever came after set
            newMessage = new StringBuilder(args[1] + " ");
            for (int i = 2; i < args.length; i++) {
                newMessage.append(args[i]).append(" ");
            }
            newMessage = new StringBuilder(newMessage.substring(0, newMessage.length() - 1));
            config.set("Bannable Messages", newMessage.toString());
            sender.sendMessage("Bannable Messages set to " + newMessage);
        } else if (args[0].equals("remove")) {
            //Removes the specified message from the config
            String banMessages = config.getString("Bannable Messages");
            assert banMessages != null;
            newMessage = new StringBuilder(args[1] + " ");
            for (int i = 2; i < args.length; i++) {
                newMessage.append(args[i]).append(" ");
            }
            newMessage = new StringBuilder(newMessage.substring(0, newMessage.length() - 1));
            //Prevents the plugin from removing the last item from the config, as well as removes the commas and spaces
            if (banMessages.contains(", " + newMessage)) {
                newMessage = new StringBuilder(", " + newMessage);
            } else if (banMessages.contains(newMessage + ", ")) {
                newMessage.append(", ");
            } else {
                sender.sendMessage("The config does not contain \"" + newMessage + "\" or it is the only item in the config");
                return false;
            }

            newMessage = new StringBuilder(banMessages.substring(0,banMessages.indexOf(String.valueOf(newMessage))) + banMessages.substring(banMessages.indexOf(String.valueOf(newMessage)) + String.valueOf(newMessage).length()));
            config.set("Bannable Messages", newMessage.toString());
            sender.sendMessage("Bannable Messages set to " + newMessage);
        } else {
            //Returns false if a command other than /config get|set|add|remove was sent
            return false;
        }

        // If the player (or console) uses our command correct, we can return true
        plugin.saveConfig();
        return true;
    }

    //This adds a tab-able list to the command
    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        if (args.length == 1){
            List<String> options = new ArrayList<>();
            options.add("get");
            options.add("set");
            options.add("add");
            options.add("remove");
            return options;
        }else if (args.length == 2 && !(args[0].equals("get"))){
            List<String> arguments = new ArrayList<>();
            arguments.add("Message");
            return arguments;
        } else {
            //Removes all autofill from the tab list
            return new ArrayList<>();
        }
    }
}