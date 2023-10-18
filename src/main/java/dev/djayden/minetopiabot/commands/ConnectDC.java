package dev.djayden.minetopiabot.commands;
import dev.djayden.minetopiabot.utils.FileEditor;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.sql.*;
import java.util.Random;
import java.util.logging.Logger;

public class ConnectDC implements CommandExecutor {

    final Logger logger = Bukkit.getLogger();

    private Connection con;

    private Plugin plugin;


    public ConnectDC(Connection con, Plugin plugin) {
        this.con = con;
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player){
                Player p = (Player) sender;
                Random random = new Random();
                int randomsql = random.nextInt(99999 - 10000);
                int randomm = randomsql;
                try {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO `PLAYERDC` (`player_id`, `connect_code`, `connected`, `discord_id`) VALUES (?, ?, 'false', '')");
                        ps.setString(1, String.valueOf(p.getUniqueId()));
                        ps.setInt(2, randomm);
                        ps.executeUpdate();
                        p.sendMessage("Dit is jouw koppelcode " + String.valueOf(randomm));
                        con.commit();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                }

            } else {
                logger.info("Only players can execute this command");
            }
        return true;
    }
}
