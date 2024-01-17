package nl.bamischrijft.rip.config;

import nl.bamischrijft.rip.Main;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config extends YamlConfiguration {

    private final File file;
    private final Main plugin;

    public Config(Main plugin) {
        this.plugin = plugin;

        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }

        this.file = new File(this.plugin.getDataFolder(), "config.yml");
        if (!this.file.exists()) {
            this.plugin.saveResource("config.yml", true);
        }

        this.load();
    }

    public void load() {
        try {
            super.load(this.file);
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
    }
}
