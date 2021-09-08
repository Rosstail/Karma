package com.rosstail.karma.lang;

import com.rosstail.karma.Karma;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Lang {

    private static Lang lang = null;
    private final String langId;
    private String name;

    private final File file;
    private final YamlConfiguration configuration;

    public Lang(String langId) {
        this.langId = langId;
        this.file = new File(Karma.getInstance().getDataFolder(), "lang/" + langId + ".yml");
        if (available()) {
            this.configuration = YamlConfiguration.loadConfiguration(this.file);
            this.name = this.file.getName();
        } else {
            this.configuration = null;
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

    public static Lang getLang() {
        return lang;
    }

    public static void initLang(String langId) {
        if (lang == null) {
            lang = new Lang(langId);
        }
    }
}