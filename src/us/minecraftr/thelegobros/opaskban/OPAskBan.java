package us.minecraftr.thelegobros.opaskban;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.BanList;

import java.util.Date;
import java.util.Objects;

public class OPAskBan extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();

    private boolean updateAvailable = false;

    //The onEnable method runs as the plugin boots up and when the server reloads
    @Override
    public void onEnable() {
        //These add values to the config file
        config.addDefault("announcePlugin",true);//Whether the plugin will send a message to players on join
        config.addDefault("Separate the values by", ",");//What char to separate the next config's messages by
        config.addDefault("bannableMessages", "can i have op?");//Messages that, when sent by the player, will cause the player to be banned
        config.addDefault("deleteBannableMessages", false);//Whether the plugin should delete messages defined in the config above
        config.addDefault("logDeletedMessages", true);//Whether the plugin should log deleted messages to the console
        config.addDefault("banTime", 3600000);//How long the ban will last, in milliseconds
        config.addDefault("banMessage", "Be patient. You have been banned for one hour.");//What message the player will be sent when attempting to rejoin the world
        config.addDefault("kickMessage", "Don't beg. You have been banned for one hour.");//What message the player will be sent when initially ejected from the world
        config.options().copyDefaults(true);
        saveConfig();

        // Enable our class to check for new players using onPlayerJoin()
        getServer().getPluginManager().registerEvents(this, this);

        //Enable commands
        Objects.requireNonNull(this.getCommand("status")).setExecutor(new Status(this));
        Objects.requireNonNull(this.getCommand("config")).setExecutor(new Config(this));

        //Checks online for updates
        new UpdateChecker(this, 96976).getVersion(version -> {
            if (this.getDescription().getVersion().compareToIgnoreCase(version) < 0) {
                getLogger().warning("There is a new update available.");
                getLogger().info("Current version: " + this.getDescription().getVersion());
                getLogger().info("Latest available version: " + version);
                updateAvailable = true;
            } else {
                getLogger().info("You are running the latest version.");
            }
        });
    }

    //This runs whenever a player sends a message
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        //Grabs the player and initializes certain variables
        final Player player =  event.getPlayer();
        String banMessages = config.getString("bannableMessages");
        if (banMessages == null) {
            banMessages = "can i have op?";
        }
        char divider = Objects.requireNonNull(config.get("Separate the values by")).toString().charAt(0);
        String[] opAsk = new String[StringUtils.countMatches(banMessages, String.valueOf(divider)) + 1];
        int splitPoint;
        int commaCount = StringUtils.countMatches(banMessages, String.valueOf(divider));

        //Splits the config text into an array of Strings
        for (int i = 0; i < commaCount + 2; i++) {
            splitPoint = banMessages.indexOf(divider);
            if (splitPoint != -1) {
                opAsk[i] = banMessages.substring(0,splitPoint);
                banMessages = banMessages.substring(splitPoint + 2);
            } else {
                opAsk[opAsk.length - 1] = banMessages;
                break;
            }
        }

        //Checks the message sent by the player against the array of Strings
        boolean isAskingForOp = false;
        for (String s : opAsk) {
            if (event.getMessage().equalsIgnoreCase(s)) {
                isAskingForOp = true;
                break;
            }
        }

        //If the message was in the array then ban the player, unless they are already OP
        if(isAskingForOp && !player.isOp()){
            //Bans the player
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                            config.getString("banMessage")
                    ,new Date(System.currentTimeMillis()+config.getInt("banTime")),null);
            //Kicks the player
            Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(config.getString("kickMessage")));
            //Deletes the message if the config is true
            if (config.getBoolean("deleteBannableMessages")) {
                event.setCancelled(true);
                //Logs the message to console if the config is true
                if (config.getBoolean("logDeletedMessages")){
                    getLogger().info("Deleted message sent by " + event.getPlayer() + ":");
                    getLogger().info("\"" + event.getMessage() + "\"");
                }
            }
        } else if (player.isOp() && isAskingForOp){
            //Asks why the OP is asking for OP
            player.sendMessage("Why are you asking for OP???");
        }
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (config.getBoolean("announcePlugin")) {
            player.sendMessage("Running OPAskBan v" + this.getDescription().getVersion());
        } else if (Objects.requireNonNull(config.getString("announcePlugin")).equalsIgnoreCase("op") && player.isOp()) {
            player.sendMessage("Running OPAskBan v" + this.getDescription().getVersion());
        }
        if (updateAvailable && player.isOp()) {
            player.sendMessage("[OPAskBan]: A new version of OPAskBan is available. Download it from: https://www.spigotmc.org/resources/opaskban.96976/");
        }
    }

    @Override
    public void onDisable(){

    }
}
