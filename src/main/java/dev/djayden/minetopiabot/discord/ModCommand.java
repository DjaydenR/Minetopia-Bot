package dev.djayden.minetopiabot.discord;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.ess3.api.Economy;
import net.ess3.api.MaxMoneyException;
import nl.minetopiasdb.api.enums.LevelChangeReason;
import nl.minetopiasdb.api.playerdata.PlayerManager;
import nl.minetopiasdb.api.playerdata.fitness.FitnessManager;
import nl.minetopiasdb.api.playerdata.objects.SDBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.Text;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class ModCommand extends ListenerAdapter {

    private Connection con;
    private Plugin plugin;

    public ModCommand(Connection con, Plugin plugin) {
        this.con = con;
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("mod")) {
            try {
                JDA jda = event.getJDA();
                Role role = jda.getRoleById(String.valueOf(plugin.getConfig().get("ModRoleId")));
                {
                    User user = event.getUser();
                    Member member = event.getMember();
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
                            Statement statement = con.createStatement();
                            ResultSet rs = statement.executeQuery("SELECT player_id FROM `PLAYERDC` WHERE discord_id = " + event.getUser().getId() + "");

                            while (rs.next())
                                MinecraftUUID = rs.getString(1);

                            String displayname = Bukkit.getOfflinePlayer(UUID.fromString(MinecraftUUID)).getName();
                            UUID newuiid = Bukkit.getOfflinePlayer(UUID.fromString(MinecraftUUID)).getUniqueId();

                            SDBPlayer sdbPlayer = PlayerManager.getPlayer(UUID.fromString(MinecraftUUID));

                            if (member.getRoles().contains(role)) {

                                event.reply("Selecteer wat jij wilt doen")
                                        .addActionRow(
                                                StringSelectMenu.create("Selecteer wat je wilt doen")
                                                        .addOption("Level", "level", "Verander iemand zijn level")
                                                        .addOption("Fitheid", "fitheid", "Verander iemand zijn fitheid")
                                                        .addOption("Prefix", "prefix", "Verander iemand zijn prefix")
                                                        .addOption("Geld", "geld", "Verander iemand zijn banksaldo")
                                                        .addOption("Informatie", "info", "Zie een speler zijn informatie")
                                                        .build()
                                        ).queue();

                            } else {
                                event.reply("Jij hebt niet de role genaamd `" + role.getName() + "`").queue();
                            }

                        } else {
                            event.reply("Jij bent niet aan een speler gekoppeld").queue();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (NumberFormatException ex) {
                event.reply("Jij hebt geen mod role ingesteld doe dit via de config.yml").queue();
            }
        }
    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("Selecteer wat je wilt doen")){

            if (event.getValues().get(0).equals("level")){
                TextInput level = TextInput.create("level", "Level", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier het nieuwe level in van de speler")
                        .setMinLength(1)
                        .setMaxLength(100)
                        .build();

                TextInput player = TextInput.create("speler", "Spelernaam", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de naam van de speler in")
                        .setMinLength(2)
                        .setMaxLength(100)
                        .build();

                Modal modal = Modal.create("level", "Level veranderen van speler")
                        .addComponents(ActionRow.of(player), ActionRow.of(level))
                        .build();

                event.replyModal(modal).queue();
            }

            if (event.getValues().get(0).equals("fitheid")){
                TextInput level = TextInput.create("fitheid", "Fitheid", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de nieuwe fitheid in van de speler")
                        .setMinLength(1)
                        .setMaxLength(100)
                        .build();

                TextInput player = TextInput.create("speler", "Spelernaam", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de naam van de speler in")
                        .setMinLength(2)
                        .setMaxLength(100)
                        .build();

                Modal modal = Modal.create("fitheid", "Fitheid veranderen van speler")
                        .addComponents(ActionRow.of(player), ActionRow.of(level))
                        .build();

                event.replyModal(modal).queue();
            }

            if (event.getValues().get(0).equals("prefix")){
                TextInput level = TextInput.create("prefix", "Prefix", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de nieuwe prefix in van de speler")
                        .setMinLength(1)
                        .setMaxLength(100)
                        .build();

                TextInput player = TextInput.create("speler", "Spelernaam", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de naam van de speler in")
                        .setMinLength(2)
                        .setMaxLength(100)
                        .build();

                Modal modal = Modal.create("prefix", "Prefix veranderen van speler")
                        .addComponents(ActionRow.of(player), ActionRow.of(level))
                        .build();

                event.replyModal(modal).queue();
            }

            if (event.getValues().get(0).equals("geld")){
                TextInput level = TextInput.create("geld", "Banksaldo:", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier het nieuwe banksaldo in")
                        .setMinLength(1)
                        .setMaxLength(100)
                        .build();

                TextInput player = TextInput.create("speler", "Spelernaam", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de naam van de speler in")
                        .setMinLength(2)
                        .setMaxLength(100)
                        .build();

                Modal modal = Modal.create("geld", "Banksaldo veranderen van speler")
                        .addComponents(ActionRow.of(player), ActionRow.of(level))
                        .build();

                event.replyModal(modal).queue();
            }

            if (event.getValues().get(0).equals("info")){

                TextInput player = TextInput.create("speler", "Spelernaam", TextInputStyle.SHORT)
                        .setPlaceholder("Vul hier de naam van de speler in")
                        .setMinLength(2)
                        .setMaxLength(100)
                        .build();

                Modal modal = Modal.create("info", "Level veranderen van speler")
                        .addComponents(ActionRow.of(player))
                        .build();

                event.replyModal(modal).queue();
            }

        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("level")) {
            String slevel = event.getValue("level").getAsString();
            int level = Integer.parseInt(slevel);
            String player = event.getValue("speler").getAsString();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

            if (offlinePlayer.hasPlayedBefore()){
                SDBPlayer sdbPlayer = PlayerManager.getPlayer(offlinePlayer.getUniqueId());

                sdbPlayer.setLevel(level, LevelChangeReason.API_CHANGED);
                event.reply("Het level van: " + player + " is veranderd naar level: " + level).queue();
            } else {
                event.reply("Deze speler heeft nog nooit gespeeld").queue();
            }

        }

        if (event.getModalId().equals("fitheid")) {
            String slevel = event.getValue("fitheid").getAsString();
            int level = Integer.parseInt(slevel);
            String player = event.getValue("speler").getAsString();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

            if (offlinePlayer.hasPlayedBefore()){
                SDBPlayer sdbPlayer = PlayerManager.getPlayer(offlinePlayer.getUniqueId());

                if (level > 225){
                    event.reply("De fitheid kan niet hoger zijn dan 225").queue();
                } else {
                    sdbPlayer.getFitness().setTotalFitness(level);

                    event.reply("De fitheid van: " + player + " is veranderd naar fitheid level: " + level).queue();
                }
            } else {
                event.reply("Deze speler heeft nog nooit gespeeld").queue();
            }

        }

        if (event.getModalId().equals("prefix")) {
            String prefix = event.getValue("prefix").getAsString();
            String player = event.getValue("speler").getAsString();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

            if (offlinePlayer.hasPlayedBefore()){
                SDBPlayer sdbPlayer = PlayerManager.getPlayer(offlinePlayer.getUniqueId());

                sdbPlayer.setPrefix(prefix);
                event.reply("De prefix van: " + player + " is veranderd naar de prefix: " + prefix).queue();
            } else {
                event.reply("Deze speler heeft nog nooit gespeeld").queue();
            }


        }

        if (event.getModalId().equals("geld")) {
            String slevel = event.getValue("geld").getAsString();
            double level = Integer.parseInt(slevel);
            String player = event.getValue("speler").getAsString();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

            if (offlinePlayer.hasPlayedBefore()){
                SDBPlayer sdbPlayer = PlayerManager.getPlayer(offlinePlayer.getUniqueId());

                try {
                    Economy.setMoney(offlinePlayer.getName(), level);
                } catch (UserDoesNotExistException e) {
                    throw new RuntimeException(e);
                } catch (NoLoanPermittedException e) {
                    throw new RuntimeException(e);
                } catch (MaxMoneyException e) {
                    throw new RuntimeException(e);
                }
                event.reply("Het banksaldo van: " + player + " is veranderd naar: €" + level + ",-").queue();
            } else {
                event.reply("Deze speler heeft nog nooit gespeeld").queue();
            }

        }

        if (event.getModalId().equals("info")) {
            String player = event.getValue("speler").getAsString();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

            if (offlinePlayer.hasPlayedBefore()){
                SDBPlayer sdbPlayer = PlayerManager.getPlayer(offlinePlayer.getUniqueId());

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Informatie over " + offlinePlayer.getName());
                eb.setColor(Color.CYAN);
                try {
                    eb.addField("Bankrekening saldo: €" + com.earth2me.essentials.api.Economy.getMoneyExact(offlinePlayer.getName()) + ",-", "", false);
                } catch (UserDoesNotExistException e) {
                    throw new RuntimeException(e);
                }
                eb.addField("Level: " + sdbPlayer.getLevel(), "", false);
                eb.addField("Fitheid: " + sdbPlayer.getFitness().getTotalFitness(), "", false);
                eb.addField("Baan: " + sdbPlayer.getPrefix(), "", false);
                event.replyEmbeds(eb.build()).queue();
            } else {
                event.reply("Deze speler heeft nog nooit gespeeld").queue();
            }

        }

    }
}
