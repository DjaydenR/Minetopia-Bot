package dev.djayden.minetopiabot.discord;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.PlayerExtension;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.ess3.api.MaxMoneyException;
import nl.minetopiasdb.api.objects.Plot;
import nl.minetopiasdb.api.playerdata.PlayerManager;
import nl.minetopiasdb.api.playerdata.objects.SDBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlotBuyCommand extends ListenerAdapter {

    private Connection con;
    private Plugin plugin;

    public PlotBuyCommand(Connection con, Plugin plugin) {
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

    private Essentials essentials() {
        Plugin plugin2 = plugin.getServer().getPluginManager().getPlugin("Essentials");

        if (plugin2 == null || !(plugin2 instanceof Essentials)) {
            return null; // Maybe you want throw an exception instead
        }

        return (Essentials) plugin2;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("plotbuy")) {
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


                        if (plot.getRegion().hasMembersOrOwners()) {
                            event.reply("Dit plot is al van iemand").queue();
                        } else {
                            if (customplotprice == false) {
                                plot.calculateBuildingPrice().join();
                                plot.calculateGroundPrice().join();
                                double builderPrice = plot.getBuilderPrice();
                                double calculatedBuildingPrice = plot.getCalculatedBuildingPrice();
                                double calculatedGroundPrice = plot.getCalculatedGroundPrice();

                                double totalprice = calculatedBuildingPrice + builderPrice + calculatedGroundPrice;

                                if (Double.valueOf(String.valueOf(Economy.getMoneyExact(displayname))) > totalprice) {
                                    Economy.subtract(displayname, totalprice);
                                    plot.getRegion().getOwners().addPlayer(newuiid);
                                    event.reply("Je hebt succesvol dit plot gekocht!").queue();
                                } else {
                                    event.reply("Je kan het niet kopen").queue();
                                }
                            } else if (customplotprice == true) {
                                String plotpricee = null;
                                Statement statement12 = con.createStatement();
                                ResultSet rs12 = statement12.executeQuery("SELECT plotprijs FROM PLOTS WHERE plot = '" + plotnummer + "'");

                                while (rs12.next())
                                    plotpricee = rs12.getString(1);

                                double plotpriceasdouble = Double.parseDouble(plotpricee);

                                if (Double.valueOf(String.valueOf(Economy.getMoneyExact(displayname))) > plotpriceasdouble){
                                    Economy.subtract(displayname, plotpriceasdouble);
                                    plot.getRegion().getOwners().addPlayer(newuiid);
                                    event.reply("Je hebt succesvol dit plot gekocht!").queue();
                                } else {
                                    event.reply("Je kan het niet kopen").queue();
                                }
                            }

                        }


                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (NullPointerException e){
                        event.reply("Het plot bestaat niet in de stad die jij hebt opgegeven").queue();
                    } catch (UserDoesNotExistException e) {
                        throw new RuntimeException(e);
                    } catch (MaxMoneyException e) {
                        throw new RuntimeException(e);
                    } catch (NoLoanPermittedException e) {
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
