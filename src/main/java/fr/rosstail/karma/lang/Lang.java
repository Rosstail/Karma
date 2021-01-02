package fr.rosstail.karma.lang;

import fr.rosstail.karma.Karma;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Lang {

    private final String langId;
    private String name;

    private final File file;
    private YamlConfiguration configuration;

    public Lang(String langId) {
        this.langId = langId;
        this.file = new File(Karma.getInstance().getDataFolder(), "lang/" + langId + ".yml");
        if (this.file.exists()) {
            this.configuration = YamlConfiguration.loadConfiguration(this.file);
            this.name = this.configuration.getString("lang-name");
        } else {
            System.out.println("[KARMA] The language file/" + langId + ".yml does not exists");
        }
    }

    /**
     * @return true if the lang exists
     */
    public boolean available() {
        return this.file.exists();
    }

    /**
     * @return the configuration model
     */
    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @return the language id
     */
    public String getId() {
        return langId;
    }

    /**
     * @return the language name
     */
    public String getName() {
        return name;
    }
}