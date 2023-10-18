package dev.djayden.minetopiabot.discord;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ConnectCommand extends ListenerAdapter {

    private Connection con;

    public ConnectCommand(Connection con){
        this.con = con;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("connect")){
            String connectcode = event.getOption("koppelcode").getAsString();
            User user = event.getUser();

             try {
                PreparedStatement ps = con.prepareStatement("UPDATE `PLAYERDC` SET connected = 'true' WHERE connect_code = " + connectcode + "");
                PreparedStatement ps1 = con.prepareStatement("UPDATE `PLAYERDC` SET discord_id = '" + user.getId() +"' WHERE connect_code = " + connectcode + "");
                ps.executeUpdate();
                ps1.executeUpdate();
                con.commit();
                event.reply("Wanneer het koppelcode klopte dan ben jij succesvol gekoppeld").queue();
            } catch (SQLException e) {
                 System.out.println(e.getMessage());
            }

        }
    }
}
