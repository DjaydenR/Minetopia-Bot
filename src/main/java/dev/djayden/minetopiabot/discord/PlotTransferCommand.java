package dev.djayden.minetopiabot.discord;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.minetopiasdb.api.objects.Plot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class PlotTransferCommand extends ListenerAdapter {

    private Connection con;
    private Plugin plugin;

    public PlotTransferCommand(Connection con, Plugin plugin) {
        this.con = con;
        this.plugin = plugin;
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin1 = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin1 == null || !(plugin1 instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin1;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("plottransfer")){
            JDA jda = event.getJDA();
            String plotnummer = event.getOption("plotnummer").getAsString();
            String wereld = event.getOption("stad").getAsString();
            String nieuweplayername = event.getOption("nieuweowner").getAsString();
            boolean connected = Boolean.parseBoolean(null);
            try {
                Statement statement1 = con.createStatement();
                ResultSet rs1 = statement1.executeQuery("SELECT connected FROM `PLAYERDC` WHERE discord_id = " + event.getUser().getId() + "");

                while (rs1.next())
                    connected = rs1.getBoolean(1);


                if (connected == true){
                        String oudeplayeruuid = null;
                        Statement statement4 = con.createStatement();
                        ResultSet rs4 = statement4.executeQuery("SELECT player_id FROM `PLAYERDC` WHERE discord_id = " + event.getUser().getId() + "");

                        while (rs4.next())
                            oudeplayeruuid = rs4.getString(1);


                        OfflinePlayer oudeplayer = Bukkit.getOfflinePlayer(UUID.fromString(oudeplayeruuid));


                        OfflinePlayer nieuweplayer = Bukkit.getOfflinePlayer(nieuweplayername);

                        if (nieuweplayer.hasPlayedBefore()){
                            WorldGuardPlugin worldGuardPlugin = getWorldGuard();

                            RegionManager regionManager = worldGuardPlugin.getRegionManager(plugin.getServer().getWorld(wereld));
                            ProtectedRegion region = regionManager.getRegion(plotnummer);

                            Plot plot = new Plot(region, wereld);


                            if (plot.getRegion().getOwners().getUniqueIds().contains(oudeplayer.getUniqueId())) {
                                plot.getRegion().getOwners().removeAll();
                                plot.getRegion().getMembers().removeAll();
                                plot.getRegion().getOwners().addPlayer(nieuweplayer.getUniqueId());

                                event.reply("Je hebt succesvol dit plot getransferd").queue();
                            } else {
                                event.reply("Je kan dit plot niet transferen omdat dit plot niet van jou is").queue();
                            }
                        } else {
                            event.reply("Deze speler heeft nog nooit gespeeld").queue();
                        }



                } else if (connected == false) {
                    event.reply("Jij bent niet aan een speler gekoppeld").queue();
                } else {
                    event.reply("Jij bent niet aan een speler gekoppeld").queue();
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }  catch (NullPointerException e){
            event.reply("Het plot bestaat niet in de stad die jij hebt opgegeven").queue();
        }
        }
    }
}
