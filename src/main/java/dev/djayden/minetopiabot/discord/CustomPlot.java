package dev.djayden.minetopiabot.discord;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomPlot extends ListenerAdapter {

    private Connection con;
    private Plugin plugin;

    public CustomPlot(Connection con, Plugin plugin) {
        this.con = con;
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("customplot")){
            try {
                if (plugin.getConfig().getBoolean("CustomPlotPrijzen") == true) {
                    JDA jda = event.getJDA();
                    Role role = jda.getRoleById(plugin.getConfig().getLong("ModRoleId"));
                    if (event.getMember().getRoles().contains(role)) {
                        event.reply("Selecteer wat je wilt doen")
                                .addActionRow(StringSelectMenu.create("Selecteer wat je wilt doen")
                                        .addOption("Voeg toe", "maak", "Maak een custom plot prijs")
                                        .addOption("Bewerk", "bewerk", "Bewerk een custom plot prijs")
                                        .addOption("Verwijder", "verwijder", "Verwijder een custom plot prijs")
                                        .build()
                                ).queue();
                    } else {
                        event.reply("Jij hebt niet de role genaamd `" + role.getName() + "`").queue();
                    }
                } else {
                    event.reply("Jij hebt niet custom plot prijzen aan staan in de config.yml").queue();
                }
                } catch (NumberFormatException e){
                    event.reply("Jij hebt niet de mod role ingesteld doe dit via de config.yml").queue();
                }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("Selecteer wat je wilt doen")){
            if (event.getValues().get(0).equals("maak")){
                TextInput plotnummer = TextInput.create("plotnummer", "Plotnummer", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier het plotnummer in")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                TextInput plotprijs = TextInput.create("plotprijs", "Plot Prijs", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de prijs van het plot in rond getal dus bijvoorbeeld je wilt €250,- dan vul je 250 in")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                TextInput plotstad = TextInput.create("plotstad", "Stad", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de stad in waar het plot zich bevindt")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                Modal modal = Modal.create("toevoegen", "Voeg een plot toe")
                        .addComponents(ActionRow.of(plotnummer), ActionRow.of(plotprijs))
                        .build();

                event.replyModal(modal).queue();

            }

            if (event.getValues().get(0).equals("bewerk")){
                TextInput plotnummer = TextInput.create("plotnummer", "Plotnummer", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier het plotnummer in")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                TextInput plotprijs = TextInput.create("plotprijs", "Plot Prijs", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de prijs van het plot in rond getal dus bijvoorbeeld je wilt €250,- dan vul je 250 in")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                TextInput plotstad = TextInput.create("plotstad", "Stad", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de stad in waar het plot zich bevindt")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                Modal modal = Modal.create("bewerkplot", "Bewerk een plot toe")
                        .addComponents(ActionRow.of(plotnummer), ActionRow.of(plotprijs))
                        .build();


                event.replyModal(modal).queue();
            }

            if (event.getValues().get(0).equals("verwijder")){
                TextInput plotnummer = TextInput.create("plotnummer", "Plotnummer", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier het plotnummer in")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                TextInput plotstad = TextInput.create("plotstad", "Stad", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de stad in waar het plot zich bevindt")
                        .setMinLength(1)
                        .setMaxLength(50)
                        .build();

                Modal modal = Modal.create("verwijderplot", "Verwijder een plot")
                        .addComponents(ActionRow.of(plotnummer))
                        .build();

                event.replyModal(modal).queue();
            }
        }
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
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("toevoegen")){
            String plotnummer = event.getValue("plotnummer").getAsString();
            String plotprijs = event.getValue("plotprijs").getAsString();
            double plotprijsdouble = Double.parseDouble(plotprijs);
            try {
                WorldGuardPlugin worldGuardPlugin = getWorldGuard();

                PreparedStatement ps = con.prepareStatement("INSERT INTO `PLOTS` (`plot`, `plotprijs`) VALUES (?, ?)");
                ps.setString(1, plotnummer);
                ps.setDouble(2, plotprijsdouble);
                ps.executeUpdate();
                con.commit();
                event.reply("Succesvol dit plot aangemaakt").queue();
            } catch (NullPointerException e){
                event.reply("Dit plot bestaat niet").queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getModalId().equals("bewerkplot")){
            String plotnummer = event.getValue("plotnummer").getAsString();
            String plotprijs = event.getValue("plotprijs").getAsString();
            double plotprijsdouble = Double.parseDouble(plotprijs);
            try {
                WorldGuardPlugin worldGuardPlugin = getWorldGuard();

                PreparedStatement ps = con.prepareStatement("UPDATE `PLOTS` SET plotprijs = ? WHERE plot = ?");
                ps.setString(2, plotnummer);
                ps.setDouble(1, plotprijsdouble);
                ps.executeUpdate();
                con.commit();
                event.reply("Succesvol dit plot bewerkt").queue();
            } catch (NullPointerException e){
                event.reply("Dit plot bestaat niet").queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getModalId().equals("verwijderplot")){
            String plotnummer = event.getValue("plotnummer").getAsString();
            try {
                WorldGuardPlugin worldGuardPlugin = getWorldGuard();

                PreparedStatement ps = con.prepareStatement("DELETE FROM `plots` WHERE plot = ?");
                ps.setString(1, plotnummer);
                ps.executeUpdate();
                con.commit();
                event.reply("Succesvol dit plot verwijderd").queue();
            } catch (NullPointerException e){
                event.reply("Dit plot bestaat niet").queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
