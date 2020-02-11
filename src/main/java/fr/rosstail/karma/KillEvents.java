package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    Player killer = null;
    int reward = 0;
    String message;
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    SetTier setTier = new SetTier();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Monster)
        {
            Monster monsterEnt = (Monster) event.getEntity();
            killer = monsterEnt.getKiller();
            if (killer != null) {
                String monsterName = monsterEnt.toString().replaceAll("Craft", "");
                reward = karma.getConfig().getInt("entities." + monsterName + ".kill-karma-reward");
                message = karma.getConfig().getString("entities." + monsterName + ".kill-message");
                if (reward == 0)
                    return;
            }
            else
                return;
        }
        else if(event.getEntity() instanceof Animals)
        {
            Animals animalEnt = (Animals) event.getEntity();
            killer = animalEnt.getKiller();
            if (killer != null) {
                String animalName = animalEnt.toString().replaceAll("Craft", "").replaceAll(" ", "_");
                reward = karma.getConfig().getInt("entities." + animalName + ".kill-karma-reward");
                message = karma.getConfig().getString("entities." + animalName + ".kill-message");
                if (reward == 0)
                    return;
            }
            else
                return;
        }
        else
            return;

        File killerFile = new File(this.karma.getDataFolder(), "playerdata/" + killer.getUniqueId() + ".yml");
        YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(killerFile);
        int killerKarma = killerConfig.getInt("karma");
        int killerModifiedKarma = killerKarma + reward;

        if (message != null) {
            message = message.replaceAll("<attacker>", killer.getName());
            message = message.replaceAll("<reward>", Integer.toString(reward));
            message = message.replaceAll("<previousKarma>", Integer.toString(killerKarma));
            message = message.replaceAll("<karma>", Integer.toString(killerModifiedKarma));
            killer.sendMessage(message);
        }

        killerConfig.set("karma", killerModifiedKarma);
        try {
            killerConfig.save(killerFile);
            verifyKarmaLimits.checkKarmaLimit(killer);
            setTier.checkTier(killer);
        } catch (IOException e) {
            e.printStackTrace();
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
        killer.sendMessage("Initial Karma from killer " + killer.getName() + "goes from " + killerKarma + " to " + killerModifiedKarma + ".");
        killer.sendMessage("Difference " + (killerModifiedKarma - killerKarma));
        killerConfig.set("karma", killerModifiedKarma);
        try {
            killerConfig.save(killerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
