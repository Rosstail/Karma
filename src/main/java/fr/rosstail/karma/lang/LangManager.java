package fr.rosstail.karma.lang;

import fr.rosstail.karma.Karma;

/**
 * Static language manager
 */
public class LangManager {

    private static Lang currentLang;

    /**
     * Initialize the current lang from the configuration
     */
    public static void initCurrentLang() {
        Lang.initDefaultLang();
        LangManager.currentLang = new Lang(Karma.getInstance().getConfig().getString("general.lang"));
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

    /**
     * @param message type of desired message
     * @param lang desired language
     * @return the string message in {@param lang} language
     */
    public static String getMessage(Lang lang, LangMessage message) {
        return lang != null && lang.available() ? lang.getConfiguration().getString(message.getId()) : "no-lang selected";
    }

}
