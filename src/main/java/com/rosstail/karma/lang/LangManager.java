package com.rosstail.karma.lang;

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
    }

    /**
     * @return the current lang
     */
    public static Lang getCurrentLang() {
        return currentLang;
    }

    /**
     * @param langMessage type of desired message
     * @return the string message in {@see LangManager.currentLang} language
     */
    public static String getMessage(LangMessage langMessage) {
        if (langMessage.getDisplayText() != null) {
            return langMessage.getDisplayText();
        }
        return null;
    }

}