package com.rosstail.karma.lang;

import java.util.Collections;
import java.util.List;

/**
 * Static language manager
 */
public class LangManager {

    private static Lang currentLang;


    /**
     * Initialize the current lang from the configuration
     */
    public static void initCurrentLang(String lang) {
        Lang.initLang(lang);
        currentLang = Lang.getLang(); //file isn't found
        if (!currentLang.available()) {
            currentLang = null;
        }
    }

    /**
     * @return the current lang
     */
    public static Lang getCurrentLang() {
        return currentLang;
    }

    /**
     * @param message type of desired message
     * @return the string message in {@see LangManager.currentLang} language
     */
    public static String getMessage(LangMessage message) {
        return getMessage(currentLang, message);
    }

    public static List<String> getListMessage(LangMessage message) {
        return getListMessage(currentLang, message);
    }

    /**
     * @param message type of desired message
     * @param lang desired language
     * @return the string message in {@param lang} language
     */
    public static String getMessage(Lang lang, LangMessage message) {
        if (lang != null && lang.available()) {
            return lang.getConfiguration().getString(message.getDisplayText());
        }
        return "no-lang selected";
    }

    public static List<String> getListMessage(Lang lang, LangMessage message) {
        if (lang != null && lang.available()) {
            return lang.getConfiguration().getStringList(message.getText());
        }
        return Collections.singletonList("no-lang selected");
    }

}