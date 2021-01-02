package fr.rosstail.karma.lang;

import fr.rosstail.karma.Karma;

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
    public static void initCurrentLang() {
        LangManager.currentLang = new Lang(Karma.getInstance().getConfig().getString("general.lang")); //file isn't found
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
        return lang != null && lang.available() ? lang.getConfiguration().getString(message.getId()) : "no-lang selected";
    }

    public static List<String> getListMessage(Lang lang, LangMessage message) {
        return lang != null && lang.available() ? lang.getConfiguration().getStringList(message.getId()) : Collections.singletonList("no-lang selected");
    }

}