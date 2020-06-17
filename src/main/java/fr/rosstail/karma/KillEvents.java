package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;


/**
 * Changes the attacker karma when killing living entities
 */
public class KillEvents implements Listener {

    private final Karma plugin;
    private final File langFile;
    private final YamlConfiguration configLang;
    private AdaptMessage adaptMessage;
    private final GetSet getSet;

    KillEvents(Karma plugin) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(), "lang/" + plugin.getConfig().getString("general.lang") + ".yml");
        this.configLang = YamlConfiguration.loadConfiguration(langFile);
        this.adaptMessage = new AdaptMessage(plugin);
        this.getSet = new GetSet(plugin);
    }

    Player killer = null;
    Player victim = null;
    String message = null;

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = null;
        double killerKarma = 0F;
        double reward = 0F;
        LivingEntity livingEntity;
        String livingEntityName;

        event.getEntity();
        if (event.getEntity().getKiller() != null)
        {
            livingEntity = (LivingEntity) event.getEntity();
            killer = livingEntity.getKiller();
            if (killer != null && getSet.getTime(killer))
                livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            else
                return;
        }
        else
            return;

        if (killer.hasMetadata("NPC")) {
            return;
        }

        reward = plugin.getConfig().getInt("entities." + livingEntityName + ".kill-karma-reward");

        if (reward != 0) {
            killerKarma = getSet.getPlayerKarma(killer);

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && plugin.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(killer);
                reward = reward * mult;
            }

            getSet.setKarmaToPlayer(killer,killerKarma + reward);
            getSet.setTierToPlayer(killer);
        }

        message = plugin.getConfig().getString("entities." + livingEntityName + ".kill-message");
        adaptMessage.entityKillMessage(message, killer, reward);
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        victim = event.getEntity();
        killer = victim.getKiller();

        if (killer == null || !getSet.getTime(killer))
            return;

        double killerInitialKarma = getSet.getPlayerKarma(killer);
        double victimKarma = getSet.getPlayerKarma(victim);

        if (killer.hasMetadata("NPC")) {
            return;
        }

        if (!victim.getName().equals(killer.getName())) {

            double arg1 = plugin.getConfig().getInt("pvp.kill-reward-variables.1");
            String arg2Str = plugin.getConfig().getString("pvp.kill-reward-variables.2");
            double arg2 = 0;
            double arg3 = plugin.getConfig().getInt("pvp.kill-reward-variables.3");
            double arg4 = plugin.getConfig().getInt("pvp.kill-reward-variables.4");

            if (arg2Str != null) {
                if (arg2Str.equalsIgnoreCase("<VICTIM_KARMA>")) {
                    if (!victim.hasMetadata("NPC")) {
                        arg2 = victimKarma;
                    } else if (victim.hasMetadata("Karma")) {
                        if (victim.getMetadata("Karma").get(0) != null) {
                            arg2 = victim.getMetadata("Karma").get(0).asDouble();
                        } else {
                            return;
                        }
                    }
                } else {
                    arg2 = Double.parseDouble(arg2Str);
                }
            }

            double reward = arg1 * (arg2 + arg3) / arg4;

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && plugin.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(killer);
                reward = reward * mult;
            }

            double killerNewKarma = killerInitialKarma + reward;

            if (plugin.getConfig().getBoolean("pvp.crime-time.enable") && !(killer.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
                Long timeStamp = System.currentTimeMillis();
                Long delay = plugin.getConfig().getLong("pvp.crime-time.delay");

                Long attackEnd = getSet.getPlayerLastAttack(killer) + delay * 1000;
                Long victimEnd = getSet.getPlayerLastAttack(victim) + delay * 1000;

                if (getSet.getPlayerLastAttack(killer) != 0L && getSet.getPlayerLastAttack(victim) != 0L) {
                    if ( (timeStamp >= getSet.getPlayerLastAttack(killer) && timeStamp <= attackEnd)
                            || timeStamp > victimEnd ) {
                        getSet.setLastAttackToPlayer(killer);
                    } else {
                        if (!doesDefendChangeKarma(killerInitialKarma, killerNewKarma)) {
                            message = configLang.getString("self-defending-off");
                            adaptMessage.message(null, killer, 0, message);
                            return;
                        }
                        message = configLang.getString("self-defending-on");
                        adaptMessage.message(null, killer, 0, message);
                    }
                } else if (getSet.getPlayerLastAttack(victim) == 0L) {
                    getSet.setLastAttackToPlayer(killer);
                } else if (getSet.getPlayerLastAttack(victim) != 0L) {
                    if (timeStamp >= getSet.getPlayerLastAttack(victim) && timeStamp <= victimEnd) {
                        if (!doesDefendChangeKarma(killerInitialKarma, killerNewKarma)) {
                            message = configLang.getString("self-defending-off");
                            adaptMessage.message(null, killer, 0, message);
                            return;
                        }
                        message = configLang.getString("self-defending-on");
                        adaptMessage.message(null, killer, 0, message);
                    } else {
                        getSet.setLastAttackToPlayer(killer);
                    }
                }

            }

            getSet.setKarmaToPlayer(killer,killerNewKarma);
            getSet.setTierToPlayer(killer);

            message = null;
            if (killerNewKarma > killerInitialKarma) {
                message = plugin.getConfig().getString("pvp.kill-message-on-karma-increase");
            }
            else if (killerNewKarma < killerInitialKarma) {
                message = plugin.getConfig().getString("pvp.kill-message-on-karma-decrease");
            }
            adaptMessage.playerKillMessage(message, killer, victim, killerInitialKarma);
        }
    }

    private boolean doesDefendChangeKarma(double attackerInitialKarma, double attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return plugin.getConfig().getBoolean("pvp.crime-time.active-on-up");
        } else if (attackerNewKarma == attackerInitialKarma) {
            return plugin.getConfig().getBoolean("pvp.crime-time.active-on-still");
        } else {
            return plugin.getConfig().getBoolean("pvp.crime-time.active-on-down");
        }
    }
}