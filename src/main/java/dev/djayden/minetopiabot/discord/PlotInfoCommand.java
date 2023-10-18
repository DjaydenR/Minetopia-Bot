package dev.djayden.minetopiabot.discord;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.ess3.api.MaxMoneyException;
import nl.minetopiasdb.api.objects.Plot;
import nl.minetopiasdb.api.playerdata.PlayerManager;
import nl.minetopiasdb.api.playerdata.objects.SDBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class PlotInfoCommand extends ListenerAdapter {

    private Connection con;
    private Plugin plugin;

    public PlotInfoCommand(Connection con, Plugin plugin) {
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
        if (event.getName().equals("plotwaarde")) {
            String plotnummer = event.getOption("plotnummer").getAsString();
            String wereld = event.getOption("stad").getAsString();
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
                        UUID newuiid = Bukkit.getOfflinePlayer(UUID.fromString(MinecraftUUID)).getUniqueId();
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(MinecraftUUID));

                        SDBPlayer sdbPlayer = PlayerManager.getPlayer(UUID.fromString(MinecraftUUID));

                        WorldGuardPlugin worldGuardPlugin = getWorldGuard();

                        RegionManager regionManager = worldGuardPlugin.getRegionManager(plugin.getServer().getWorld(wereld));
                        ProtectedRegion region = regionManager.getRegion(plotnummer);

                        Plot plot = new Plot(region, wereld);

                        boolean customplotprice = plugin.getConfig().getBoolean("CustomPlotPrijzen");

                        if (customplotprice == false) {
                            plot.calculateBuildingPrice().join();
                            plot.calculateGroundPrice().join();
                            double builderPrice = plot.getBuilderPrice();
                            double calculatedBuildingPrice = plot.getCalculatedBuildingPrice();
                            double calculatedGroundPrice = plot.getCalculatedGroundPrice();

                            double totalprice = calculatedBuildingPrice + builderPrice + calculatedGroundPrice;

                            EmbedBuilder eb = new EmbedBuilder();

                            eb.setTitle("Plotnummer: " + plotnummer);
                            eb.setColor(Color.CYAN);
                            eb.addField("", "Waarde: €" + totalprice + ",-", false);

                            event.replyEmbeds(eb.build()).queue();
                        } else if (customplotprice == true){
                            String plotpricee = null;
                            Statement statement12 = con.createStatement();
                            ResultSet rs12 = statement12.executeQuery("SELECT plotprijs FROM `plots` WHERE plot = " + plotnummer + "");

                            while (rs12.next())
                                plotpricee = rs12.getString(1);

                            EmbedBuilder eb = new EmbedBuilder();

                            eb.setTitle("Plotnummer: " + plotnummer);
                            eb.setColor(Color.CYAN);
                            eb.addField("", "Waarde: €" + plotpricee + ",-", false);

                            event.replyEmbeds(eb.build()).queue();
                        }


                    } catch (NullPointerException ex){
                        event.reply("Dit plot bestaat niet in de stad die jij hebt opgegeven").queue();
                    } catch (Exception e) {
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
