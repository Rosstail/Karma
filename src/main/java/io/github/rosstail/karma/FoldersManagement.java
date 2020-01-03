package io.github.rosstail.karma;

import org.bukkit.Bukkit;

import java.io.File;

public class FoldersManagement {

    public static void CreateFolders() {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Karma")
                .getDataFolder().getPath() + System.getProperty("file.separator") + "playderdata");
        if ( !file.exists() ) {
            System.out.println("[Karma] \"playerdata\" folder doesn't exists. Creating it.");
            file.mkdir();
        }
    }

}
