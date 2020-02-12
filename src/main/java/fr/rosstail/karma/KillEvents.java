package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Mob;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.IOException;

/**
 * Changes the attacker karma when attacking / killing entities
 */
public class KillEvents implements Listener {
    private Karma karma = Karma.getInstance();
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    String message1 = null;
    String message2 = null;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = null;
        int reward = 0;
        String message;
        LivingEntity livingEntity;
        String livingEntityName;
        SetTier setTier = new SetTier();
        int killerKarma = 0;
        int killerModifiedKarma = 0;

        event.getEntity();
        if (event.getEntity().getKiller() != null)
        {
            livingEntity = (LivingEntity) event.getEntity();
            killer = livingEntity.getKiller();
            if (killer != null)
                livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            else
                return;
        }
        else
            return;

        reward = karma.getConfig().getInt("entities." + livingEntityName + ".kill-karma-reward");

        if (reward != 0) {
            File killerFile = new File(this.karma.getDataFolder(), "playerdata/" + killer.getUniqueId() + ".yml");
            YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(killerFile);
            killerKarma = killerConfig.getInt("karma");
            killerModifiedKarma = killerKarma + reward;

            killerConfig.set("karma", killerModifiedKarma);
            try {
                killerConfig.save(killerFile);
                verifyKarmaLimits.checkKarmaLimit(killer);
                setTier.checkTier(killer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        message = karma.getConfig().getString("entities." + livingEntityName + ".kill-message");

        if (message != null) {
            message = message.replaceAll("<attacker>", killer.getName());
            message = message.replaceAll("<reward>", Integer.toString(reward));
            message = message.replaceAll("<previousKarma>", Integer.toString(killerKarma));
            message = message.replaceAll("<karma>", Integer.toString(killerModifiedKarma));
            message = ChatColor.translateAlternateColorCodes('&', message);
            killer.sendMessage(message);
        }

    }

    /**
     * Launch When a player is killed.
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null)
            return;

        File killerFile = new File(this.karma.getDataFolder(), "playerdata/" + killer.getUniqueId() + ".yml");
        YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(killerFile);
        int killerKarma = killerConfig.getInt("karma");
        File victimFile = new File(this.karma.getDataFolder(), "playerdata/" + victim.getUniqueId() + ".yml");
        YamlConfiguration victimConfig = YamlConfiguration.loadConfiguration(victimFile);
        int victimKarma = victimConfig.getInt("karma");

        int killerModifiedKarma = killerKarma + (killerKarma - victimKarma) / 10;

        message1 = "Initial Karma from killer " + killer.getName() + "goes from " + killerKarma + " to " + killerModifiedKarma + ".";
        message1 = ChatColor.translateAlternateColorCodes('&', message1);
        killer.sendMessage(message1);

        message2 ="Difference " + (killerModifiedKarma - killerKarma);
        message2 = ChatColor.translateAlternateColorCodes('&', message2);
        killer.sendMessage(message2);

        killerConfig.set("karma", killerModifiedKarma);
        try {
            killerConfig.save(killerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
