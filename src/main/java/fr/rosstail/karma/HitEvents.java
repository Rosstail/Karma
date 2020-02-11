package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.IOException;

/**
 * Changes the attacker karma when attacking entities
 */
public class HitEvents implements Listener {
    private Karma karma = Karma.getInstance();
    Player attacker = null;
    int reward = 0;
    String message;
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    SetTier setTier = new SetTier();

    /**
     * Changes karma when player attack another entity (animal or monster)
     * @param event
     */
    @EventHandler
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Monster) // && event.getDamager() instanceof Player)
        {
            Monster monsterEnt = (Monster) event.getEntity();
            attacker = (Player) event.getDamager();
            String monsterName = monsterEnt.toString().replaceAll("Craft", "").replaceAll(" ", "_");
            reward = karma.getConfig().getInt("entities." + monsterName + ".hit-karma-reward");
            message = karma.getConfig().getString("entities." + monsterName + ".hit-message");

            if (reward == 0)
                return;
        }
        else if(event.getEntity() instanceof Animals) // && event.getDamager() instanceof Player)
        {
            Animals animalEnt = (Animals) event.getEntity();
            attacker = (Player) event.getDamager();
            String animalName = animalEnt.toString().replaceAll("Craft", "").replaceAll(" ", "_");
            reward = karma.getConfig().getInt("entities." + animalName + ".hit-karma-reward");
            message = karma.getConfig().getString("entities." + animalName + ".hit-message");

            if (reward == 0)
                return;
        }
        else
            return;

        File attackerFile = new File(this.karma.getDataFolder(), "playerdata/" + attacker.getUniqueId() + ".yml");
        YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(attackerFile);
        int attackerKarma = killerConfig.getInt("karma");
        int attackerModifiedKarma = attackerKarma + reward;

        if (message != null) {
            message = message.replaceAll("<attacker>", attacker.getName());
            message = message.replaceAll("<reward>", Integer.toString(reward));
            message = message.replaceAll("<previousKarma>", Integer.toString(attackerKarma));
            message = message.replaceAll("<karma>", Integer.toString(attackerModifiedKarma));
            attacker.sendMessage(message);
        }
        killerConfig.set("karma", attackerModifiedKarma);
        try {
            killerConfig.save(attackerFile);
            verifyKarmaLimits.checkKarmaLimit(attacker);
            setTier.checkTier(attacker);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch When a player is hurt by another player.
     * @param event
     */
    @EventHandler
    public void onPlayerHurt(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player attacker = victim.getKiller();

        if (attacker == null)
            return;

        File attackerFile = new File(this.karma.getDataFolder(), "playerdata/" + attacker.getUniqueId() + ".yml");
        YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(attackerFile);
        int attackerKarma = killerConfig.getInt("karma");
        File victimFile = new File(this.karma.getDataFolder(), "playerdata/" + victim.getUniqueId() + ".yml");
        YamlConfiguration victimConfig = YamlConfiguration.loadConfiguration(victimFile);
        int victimKarma = victimConfig.getInt("karma");

        int attackerModifiedKarma = attackerKarma + (attackerKarma - victimKarma) / 1000;
        attacker.sendMessage("Initial Karma from attacker " + attacker.getName() + "goes from " + attackerKarma + " to " + attackerModifiedKarma + ".");
        attacker.sendMessage("Difference " + (attackerModifiedKarma - attackerKarma));
        killerConfig.set("karma", attackerModifiedKarma);
        try {
            killerConfig.save(attackerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
