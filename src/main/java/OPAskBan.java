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
        config.addDefault("Announce Plugin",true);//Whether the plugin will send a message to players on join
        config.addDefault("Ignored Characters", ""); //Sets what characters should be ignored by the plugin
        config.addDefault("Ignore Non-Alphabetic Characters", true);
        config.addDefault("Separate The Values By", ",");//What char to separate the next config's messages by
        config.addDefault("Bannable Messages", "can i have op?");//Messages that, when sent by the player, will cause the player to be banned
        config.addDefault("Delete Bannable Messages", false);//Whether the plugin should delete messages defined in the config above
        config.addDefault("Log Deleted Messages", true);//Whether the plugin should log deleted messages to the console
        config.addDefault("OP Asked", "Why are you asking for OP???");//What message should be sent to operators that send a bannableMessage
        config.addDefault("Ban Time", 3600000);//How long the ban will last, in milliseconds
        config.addDefault("Ban Message", "Be patient. You have been banned for one hour.");//What message the player will be sent when attempting to rejoin the world
        config.addDefault("Kick Message", "Don't beg. You have been banned for one hour.");//What message the player will be sent when initially ejected from the world
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
        getLogger().warning("Config \"Ignored Characters\" does not ignore characters in the config,\n just characters in player-sent messages\n Certain characters are automatically filtered from messages: !#");
    }

    //This runs whenever a player sends a message
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        //Grabs the player and initializes certain variables
        final Player player =  event.getPlayer();
        String message = event.getMessage();
        String banMessages = config.getString("Bannable Messages");
        if (banMessages == null) {
            banMessages = "can i have op?";
        }
        char divider = Objects.requireNonNull(config.get("Separate The Values By")).toString().charAt(0);
        int commaCount = StringUtils.countMatches(banMessages, String.valueOf(divider));
        String[] opAsk = new String[commaCount + 1];
        int splitPoint;
        char[] ignoredCharacters = new char[0];

        //This code removes characters from player-sent messages
        if (Objects.requireNonNull(config.getString("Ignored Characters")).length() != 0) {
            ignoredCharacters = new char[Objects.requireNonNull(config.getString("Ignored Characters")).length()];

            for (int i = 0; i < Objects.requireNonNull(config.getString("Ignored Characters")).length(); i++) {
                ignoredCharacters[i] = Objects.requireNonNull(config.getString("Ignored Characters")).charAt(i);
            }

            for (char ignoredCharacter : ignoredCharacters) {
                message = removeChar(message, ignoredCharacter);
            }
        }
        if (config.getBoolean("Ignore Non-Alphabetic Characters")){
            message = removeNonAlphabetic(message);
        }

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

        //Remove illegal characters from Bannable Messages
        if (config.getBoolean("Ignore Non-Alphabetic Characters")){
            for (int i = 0; i < opAsk.length; i++){
                opAsk[i] = removeNonAlphabetic(opAsk[i]);
            }
        }
        if (Objects.requireNonNull(config.getString("Ignored Characters")).length() != 0) {
            for (int i = 0; i < opAsk.length; i++) {
                for (char ignoredCharacter : ignoredCharacters) {
                    opAsk[i] = removeChar(opAsk[i], ignoredCharacter);
                }
            }
        }

        //Checks the message sent by the player against the array of Strings
        boolean isAskingForOp = false;
        for (String s : opAsk) {
            if (message.equalsIgnoreCase(s)) {
                isAskingForOp = true;
                break;
            }
        }

        //If the message was in the array then ban the player, unless they are already OP
        if(isAskingForOp && !player.hasPermission("OPAsk.bypass")){
            //Bans the player
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                            config.getString("Ban Message")
                    ,new Date(System.currentTimeMillis()+config.getInt("Ban Time")),null);
            //Kicks the player
            Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(config.getString("Kick Message")));
            //Deletes the message if the config is true
            if (config.getBoolean("Delete Bannable Messages")) {
                event.setCancelled(true);
                //Logs the message to console if the config is true
                if (config.getBoolean("Log Deleted Messages")){
                    getLogger().info("Deleted message sent by " + event.getPlayer() + ":");
                    getLogger().info("\"" + event.getMessage() + "\"");
                }
            }
        } else if (player.isOp() && isAskingForOp){
            if (!(Objects.requireNonNull(config.getString("OP Asked")).isEmpty()) && !(Objects.requireNonNull(config.getString("OP Asked")).isBlank()) && (config.getString("OP Asked") != null)) {
                //Asks why the OP is asking for OP
                player.sendMessage(Objects.requireNonNull(config.getString("OP Asked")));
            } else {
                getLogger().info("[Config] \"OP asked\" is empty");
            }
        } else if (isAskingForOp && player.hasPermission("OPAsk.bypass")) {
            getLogger().info("Player has OPAsk.bypass permission. They were not banned.");
        }
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (config.getBoolean("Announce Plugin")) {
            player.sendMessage("Running OPAskBan v" + this.getDescription().getVersion());
        } else if (Objects.requireNonNull(config.getString("Announce Plugin")).equalsIgnoreCase("op") && player.isOp()) {
            player.sendMessage("Running OPAskBan v" + this.getDescription().getVersion());
        }
        if (updateAvailable && player.isOp()) {
            player.sendMessage("[OPAskBan]: A new version of OPAskBan is available. Download it from: https://www.spigotmc.org/resources/opaskban.96976/");
        }
    }

    @Override
    public void onDisable(){

    }

    //This method removes every instance of a specified character from a string
    public String removeChar(String chop, char charToRemove){
        String chopped;
        int indexOf = chop.indexOf(charToRemove);
        if (indexOf != -1){
            chopped = chop.substring(0, indexOf);
            chopped += chop.substring(indexOf + 1);
            chop = chop.substring(indexOf + 1);
            while (chopped.indexOf(charToRemove) != -1){
                indexOf = chop.indexOf(charToRemove);
                chopped = chop.substring(0, indexOf);
                chopped += chop.substring(indexOf + 1);
                chop = chop.substring(indexOf + 1);
            }
        } else {
            return chop;
        }
        return chopped;
    }
    public String removeNonAlphabetic(String chop) {
        StringBuilder chopped = new StringBuilder();
        for (int i = 0; i < chop.length(); i++) {
            char mightChop = chop.charAt(i);
            if (mightChop==32||(mightChop<=122&&mightChop>=97)||(mightChop<=90&&mightChop>=65)){
                chopped.append(mightChop);
            }
        }
        return String.valueOf(chopped);
    }

}
