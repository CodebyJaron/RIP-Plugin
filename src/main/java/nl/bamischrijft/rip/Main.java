package nl.bamischrijft.rip;

import lombok.Getter;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import nl.bamischrijft.rip.config.Config;
import nl.bamischrijft.rip.listeners.ChestCloseListener;
import nl.bamischrijft.rip.listeners.ChestOpenListener;
import nl.bamischrijft.rip.listeners.DeathListener;
import nl.bamischrijft.rip.loader.RecipeLoader;
import nl.bamischrijft.rip.manager.DeathManager;
import nl.bamischrijft.rip.objects.DeathChest;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Plugin(
        name="DeathChest",
        description="Plugin for Devroom trial",
        version="1.0.0",
        authors={
                "BamiSchijf"
        },
        depends={
                @PluginDependency("helper")
        }
)
@Getter
public final class Main extends ExtendedJavaPlugin {

    @Getter
    private static Main instance;

    private Config config;
    private DeathManager deathManager;
    private RecipeLoader recipeLoader;

    public static String DATABASE_PATH;

    @Override
    protected void enable() {
        instance = this;

        this.config = new Config(this);
        this.deathManager = new DeathManager();

        this.recipeLoader = new RecipeLoader(this);
        this.recipeLoader.registerRecipes();

        this.bindModule(new DeathListener(this));
        this.bindModule(new ChestOpenListener(this));
        this.bindModule(new ChestCloseListener(this));

        DATABASE_PATH = getDataFolder().getAbsolutePath() + File.separator + "data.db";
        if (!Files.exists(Paths.get(DATABASE_PATH))) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);

                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS rip(" +
                        "uuid VARCHAR(50) NOT NULL, " +
                        "location VARCHAR(50) NOT NULL" +
                        ");");

                statement.execute();
                statement.close();

                Bukkit.getLogger().info("SQLite created");
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Error with creating SQLite db. Shutting down.");
                e.printStackTrace();
                setEnabled(false);
            }
        }
    }

    @Override
    protected void disable() {
        for (DeathChest chest : deathManager.getDeathChests()) {
            chest.despawn();
        }
    }
}
