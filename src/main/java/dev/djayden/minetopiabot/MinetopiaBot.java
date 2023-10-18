package dev.djayden.minetopiabot;

import dev.djayden.minetopiabot.commands.ConnectDC;
import dev.djayden.minetopiabot.discord.*;
import dev.djayden.minetopiabot.utils.FileEditor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;
import java.sql.*;

public final class MinetopiaBot extends JavaPlugin {

    final Logger logger = Bukkit.getLogger();
    private Plugin plugin = this;

    private Connection con;

    private JDA jda;

    public void mysql() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        String connectionurl = "jdbc:mysql://" + plugin.getConfig().get("IP") + ":" + plugin.getConfig().get("Port") + "/" + plugin.getConfig().get("Databasename");

        con = DriverManager.getConnection(connectionurl, plugin.getConfig().get("Username").toString(), plugin.getConfig().get("Password").toString());

        logger.info("De plugin werkt met de databank: " + con.getMetaData().getDatabaseProductName());


        InputStream inputStream = plugin.getClass().getResourceAsStream("/" + "create.sql");
        File file = FileEditor.copyInputStreamToFile(inputStream);

        FileEditor.modifyFile("./create.sql", "database", plugin.getConfig().get("Databasename").toString());

        ScriptRunner sr = new ScriptRunner(con);

        Reader reader = new BufferedReader(new FileReader(file));

        sr.runScript(reader);

        if (plugin.getConfig().getBoolean("CustomPlotPrijzen") == true){
            PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().get("Databasename").toString() + ".PLOTS (plot VARCHAR(100), plotprijs VARCHAR(100))");
            ps.executeUpdate();
            con.commit();
        }
    }

    @Override
    public void onEnable() {
        logger.info("MinetopiaBot probeert te activeren.......");
        plugin.saveDefaultConfig();

        logger.info("MySQL Opstarten......");
        try {
            mysql();
        } catch (SQLException e) {
            logger.info("SQLException = " + e.getMessage());
            logger.info("SQLState: " + e.getSQLState());
            logger.info("VendorError: " + e.getErrorCode());
        } catch (ClassNotFoundException e) {
            logger.info("ClassNotFoundException: " + e.getMessage());
        } catch (InstantiationException e) {

        } catch (IllegalAccessException e) {

        } catch (FileNotFoundException e) {

        }
        logger.info("MySQL Opstarten gelukt");

        logger.info("Discord Bot Opstarten......");
        jda();
        logger.info("Discord bot Opstarten gelukt");

        getCommand("connectdc").setExecutor(new ConnectDC(con, plugin));
    }


    public void jda(){
        jda = JDABuilder.createDefault(plugin.getConfig().get("Token").toString())
                .addEventListeners(new ConnectCommand(con), new CustomPlot(con, plugin), new PlotTransferCommand(con, plugin), new PlotSellCommand(con, plugin), new InfoCommand(con), new PlotInfoCommand(con, plugin), new PlotBuyCommand(con, plugin), new ModCommand(con, plugin))
                .build();


            jda.updateCommands().addCommands(
                    Commands.slash("connect", "Connect jou minecraft account aan je discord account").addOption(OptionType.INTEGER, "koppelcode", "Vul hier je koppelcode in", true),
                    Commands.slash("info", "Bekijk jou in game gegevens"),
                    Commands.slash("mod", "Het command om spelers hun data te veranderen"),
                    Commands.slash("customplot", "Maak/Bewerk custom plot prijzen"),
                    Commands.slash("plottransfer", "Verander de eigenaar van jou plot")
                            .addOption(OptionType.STRING, "nieuweowner", "Wie moet de nieuwe plot owner worden", true)
                            .addOption(OptionType.STRING, "plotnummer", "Het nummer van het plot", true)
                            .addOption(OptionType.STRING, "stad", "De naam van de stad waar het plot zich bevindt", true),
                    Commands.slash("plotwaarde", "Zie de waarde van een plot")
                            .addOption(OptionType.STRING, "plotnummer", "Het nummer van het plot", true)
                            .addOption(OptionType.STRING, "stad", "De naam van de stad waar het plot zich bevindt", true),
                    Commands.slash("plotbuy", "Koop een plot")
                            .addOption(OptionType.STRING, "plotnummer", "Het nummer van het plot", true)
                            .addOption(OptionType.STRING, "stad", "De naam van de stad waar het plot zich bevindt", true),
                    Commands.slash("plotsell", "Verkoop een plot")
                            .addOption(OptionType.STRING, "plotnummer", "Het nummer van het plot", true)
                            .addOption(OptionType.STRING, "stad", "De naam van de stad waar het plot zich bevindt", true)
                    ).queue();






    }

    @Override
    public void onDisable() {
        logger.info("MinetopiaBot probeert te deactiveren....");
        try {
            con.close();
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }

        jda.shutdown();
    }
}
