package dev.djayden.minetopiabot.discord;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.minetopiasdb.api.playerdata.PlayerManager;
import nl.minetopiasdb.api.playerdata.objects.OfflineSDBPlayer;
import nl.minetopiasdb.api.playerdata.objects.SDBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class InfoCommand extends ListenerAdapter {

    private Connection con;

    public InfoCommand(Connection con) {
        this.con = con;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("info")) {
            User user = event.getUser();
            String MinecraftUUID = null;
            boolean connected = Boolean.parseBoolean(null);

            try {
                Statement statement1 = con.createStatement();
                ResultSet rs1 = statement1.executeQuery("SELECT connected FROM `PLAYERDC` WHERE discord_id = " + event.getUser().getId() + "");

                while (rs1.next())
                    connected = rs1.getBoolean(1);

                if (connected == false) {
                    event.reply("Jij bent niet aan een speler gekoppeld").queue();
                } else if (connected == true) {
                    try {
                        Statement statement = con.createStatement();
                        ResultSet rs = statement.executeQuery("SELECT player_id FROM `PLAYERDC` WHERE discord_id = " + event.getUser().getId() + "");

                        while (rs.next())
                            MinecraftUUID = rs.getString(1);

                        String displayname = Bukkit.getOfflinePlayer(UUID.fromString(MinecraftUUID)).getName();
                        Player player = (Player) Bukkit.getOfflinePlayer(UUID.fromString(MinecraftUUID)).getPlayer();

                        SDBPlayer sdbPlayer = PlayerManager.getPlayer(UUID.fromString(MinecraftUUID));

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("Informatie over " + displayname);
                        eb.setColor(Color.CYAN);
                        eb.addField("Bankrekening saldo: €" + Economy.getMoneyExact(displayname) + ",-", "", false);
                        eb.addField("Level: " + sdbPlayer.getLevel(), "", false);
                        eb.addField("Fitheid: " + sdbPlayer.getFitness().getTotalFitness(), "", false);
                        eb.addField("Baan: " + sdbPlayer.getPrefix(), "", false);
                        event.replyEmbeds(eb.build()).queue();


                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (UserDoesNotExistException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    event.reply("Jij bent niet aan een speler gekoppeld").queue();
                }


        } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
}
    }

