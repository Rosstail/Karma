package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ChangeKarma {
    private Karma karma = Karma.getInstance();

    public ChangeKarma() {
    }

    public void checkKarmaLimit(Player player) {
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        if (configuration.getInt("karma") > this.karma.getConfig().getInt("karma.maximum-karma")) {
            this.setKarmaToMinimum(player, file, configuration);
        } else if (configuration.getInt("karma") < this.karma.getConfig().getInt("karma.minimum-karma")) {
            this.setKarmaToMaximum(player, file, configuration);
        }

    }

    public void setKarmaToMinimum(Player player, File file, YamlConfiguration configuration) {
        try {
            configuration.set("karma", this.karma.getConfig().getInt("karma.maximum-karma"));
            configuration.save(file);
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        System.out.println(player.getName() + " has a karma higher than maximum, now set to maximum karma defined in config.yml");
    }

    public void setKarmaToMaximum(Player player, File file, YamlConfiguration configuration) {
        try {
            configuration.set("karma", this.karma.getConfig().getInt("karma.minimum-karma"));
            configuration.save(file);
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        System.out.println(player.getName() + " has a karma higher than maximum, now set to minimum karma defined in config.yml");
    }
}
