package com.rosstail.karma;

import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.lang.AdaptMessage;
import org.bukkit.entity.Player;

public class KarmaAPI {

    public static void doStuff() {
        AdaptMessage.print("doStuff API", AdaptMessage.prints.WARNING);
    }

    public static double getPlayerKarma(Player player) {
        AdaptMessage.print("getkarma API for " + player.getName(), AdaptMessage.prints.WARNING);
        return PlayerData.gets(player).getKarma();
    }
}
