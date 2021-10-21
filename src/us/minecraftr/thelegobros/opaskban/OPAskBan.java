package us.minecraftr.thelegobros.opaskban;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.BanList;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class OPAskBan extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();

    private boolean updateAvailable = false;

    @Override
    public void onEnable() {
        config.addDefault("announcePlugin",true);
        config.addDefault("Separate the values by", ",");
        config.addDefault("opInquiry", "can i have op?");
        config.addDefault("banTime", 3600000);
        config.addDefault("banMessage", "Be patient. You have been banned for one hour.");
        config.addDefault("kickMessage", "Don't beg. You have been banned for one hour.");
        config.options().copyDefaults(true);
        saveConfig();

        // Enable our class to check for new players using onPlayerJoin()
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("status")).setExecutor(new Status(this));

        //Checks online for updates
        new UpdateChecker(this, 96976).getVersion(version -> {
            if (this.getDescription().getVersion().compareToIgnoreCase(version) < 0) {
                getLogger().info("There is a new update available.");
                getLogger().info("Current version: " + this.getDescription().getVersion());
                getLogger().info("Latest available version: " + version);
                updateAvailable = true;
            } else {
                getLogger().info("You are running the latest version.");
            }
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        final Player player =  event.getPlayer();
        String banMessages = config.getString("opInquiry");
        if (banMessages == null) {
            banMessages = "can i have op?";
        }
        char divider = Objects.requireNonNull(config.get("Separate the values by")).toString().charAt(0);
        String[] opAsk = new String[StringUtils.countMatches(banMessages, String.valueOf(divider)) + 1];
        int splitPoint;
        int commaCount = StringUtils.countMatches(banMessages, String.valueOf(divider));
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

        boolean isAskingForOp = false;
        for (String s : opAsk) {
            if (event.getMessage().equalsIgnoreCase(s)) {
                isAskingForOp = true;
                break;
            }
        }

        if(isAskingForOp && !player.isOp()){
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                            config.getString("banMessage")
                    ,new Date(System.currentTimeMillis()+config.getInt("banTime")),null);
            Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(config.getString("kickMessage")));
        } else if (player.isOp() && isAskingForOp){
            player.sendMessage("Why are you asking for OP???");
        }
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
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
