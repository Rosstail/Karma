package com.rosstail.karma.lang;

import com.rosstail.karma.FileResourcesUtils;
import com.rosstail.karma.Karma;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Lang {

    private static Lang lang = null;
    private final String langId;
    private String name;

    private final File langFile;
    private final YamlConfiguration langConfig;
    private final YamlConfiguration defaultLangConfig;

    public Lang(String langId) {
        this.langId = langId;
        File wantedLangFile = new File(Karma.getInstance().getDataFolder(), "lang/" + langId + ".yml");
        this.defaultLangConfig = FileResourcesUtils.getDefaultFileConfiguration();

        if (wantedLangFile.exists()) {
            this.langFile = wantedLangFile;
            this.langConfig = YamlConfiguration.loadConfiguration(this.langFile);
            this.name = this.langFile.getName();
        } else {
            this.langFile = null;
            this.langConfig = null;
            this.name = langId;
            AdaptMessage.print("Locale lang/" + langId + ".yml does not exists. use default.", AdaptMessage.prints.WARNING);
        }


        for (LangMessage langMessage : LangMessage.values()) {
            String stringPath = langMessage.getText();
            String gotMessage = null;
            if (langConfig != null) {
                gotMessage = langConfig.getString(stringPath);
                if (gotMessage != null) {
                    langMessage.setDisplayText(AdaptMessage.getAdaptMessage().adaptMessage(gotMessage));
                }
            }

            if (gotMessage == null && !langMessage.isNullable()) {
                langMessage.setDisplayText(AdaptMessage.getAdaptMessage().adaptMessage(defaultLangConfig.getString(stringPath)));
            }
        }
    }

    /**
     * @return the configuration model
     */
    public YamlConfiguration getLangConfig() {
        return langConfig;
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
        lang = new Lang(langId);
    }
}