package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;
import java.io.IOException;

/**
 * Changes the attacker karma when attacking entities
 */
public class HitEvents implements Listener {
    private Karma karma = Karma.getInstance();
    VerifyKarmaLimits verifyKarmaLimits = new VerifyKarmaLimits();
    SetTier setTier = new SetTier();

    /**
     * Changes karma when player attack another entity (animal or monster)
     * @param event
     */
    @EventHandler
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        Player attacker = null;
        int reward = 0;
        int attackerKarma = 0;
        int attackerModifiedKarma = 0;
        String message;
        LivingEntity livingEntity;
        String livingEntityName;

        if (event.getEntity() instanceof LivingEntity)
        {
            livingEntity = (LivingEntity) event.getEntity();
            livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player)
                    attacker = (Player) projectile.getShooter();
            }
            else if (event.getDamager() instanceof Player)
                attacker = (Player) event.getDamager();
            else
                return;
        }
        else
            return;

        reward = karma.getConfig().getInt("entities." + livingEntityName + ".hit-karma-reward");

        if (reward != 0) {
            File attackerFile = new File(this.karma.getDataFolder(), "playerdata/" + attacker.getUniqueId() + ".yml");
            YamlConfiguration killerConfig = YamlConfiguration.loadConfiguration(attackerFile);
            attackerKarma = killerConfig.getInt("karma");
            attackerModifiedKarma = attackerKarma + reward;

            killerConfig.set("karma", attackerModifiedKarma);
            try {
                killerConfig.save(attackerFile);
                verifyKarmaLimits.checkKarmaLimit(attacker);
                setTier.checkTier(attacker);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        message = karma.getConfig().getString("entities." + livingEntityName + ".hit-message");

        if (message != null) {
            message = message.replaceAll("<attacker>", attacker.getName());
            message = message.replaceAll("<reward>", Integer.toString(reward));
            message = message.replaceAll("<previousKarma>", Integer.toString(attackerKarma));
            message = message.replaceAll("<karma>", Integer.toString(attackerModifiedKarma));
            message = ChatColor.translateAlternateColorCodes('&', message);
            attacker.sendMessage(message);
        }

    }

    /**
     * Launch When a player is hurt by another player.
     * @param event
     */
    /*@EventHandler
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
    }*/
}
