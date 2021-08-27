package fr.rosstail.karma.events;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.customEvents.PlayerKarmaChangeEvent;
import fr.rosstail.karma.customEvents.PlayerKarmaHasChangedEvent;
import fr.rosstail.karma.customEvents.PlayerTierChangeEvent;
import fr.rosstail.karma.customEvents.PlayerTierHasChangedEvent;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.tiers.Tier;
import fr.rosstail.karma.timeManagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CustomEventHandler implements Listener {
    private final Karma plugin;
    private final ConfigData configData = ConfigData.getConfigData();

    public CustomEventHandler(Karma plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKarmaChange(PlayerKarmaChangeEvent event) {
        PlayerData playerData = PlayerData.getPlayerList().get(event.getPlayer());

        playerData.setKarma(event.getValue());

        if (event.isOverTimeChange()) {
            playerData.setOverTimerChange();
        }
        PlayerKarmaHasChangedEvent playerKarmaHasChangedEvent = new PlayerKarmaHasChangedEvent(event.getPlayer(), playerData.getKarma(), event.isOverTimeChange());
        Bukkit.getPluginManager().callEvent(playerKarmaHasChangedEvent);
    }

    @EventHandler
    public void onPlayerKarmaHasChanged(PlayerKarmaHasChangedEvent event) {
        PlayerData playerData = PlayerData.getPlayerList().get(event.getPlayer());
        playerData.checkTier();
    }

    @EventHandler
    public void onPlayerTierChange(PlayerTierChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getPlayerList().get(player);
        playerData.setPreviousTier(playerData.getTier());
        playerData.setTier(event.getTier());

        PlayerTierHasChangedEvent playerTierHasChangedEvent = new PlayerTierHasChangedEvent(event.getPlayer(), event.getTier());
        Bukkit.getPluginManager().callEvent(playerTierHasChangedEvent);
    }

    @EventHandler void onPlayerTierHasChanged(PlayerTierHasChangedEvent event) {
        Player player = event.getPlayer();
        Tier tier = event.getTier();
        PlayerData playerData = PlayerData.getPlayerList().get(player);
        Tier previousTier = playerData.getPreviousTier();
        double karma = playerData.getKarma();
        double previousKarma = playerData.getPreviousKarma();

        playerData.changePlayerTierMessage();
        PlayerData.commandsLauncher(player, tier.getJoinCommands());
        if (previousTier != null) {
            if (karma > previousKarma) {
                PlayerData.commandsLauncher(player, tier.getJoinOnUpCommands());
            } else {
                PlayerData.commandsLauncher(player, tier.getJoinOnDownCommands());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = configData.getSaveDelay();
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player, plugin);
        playerData.initPlayerData();
        playerData.setUpdateDataTimer(delay);
        playerData.setOverTimerChange();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player, plugin);
        playerData.getUpdateDataTimer().cancel();
        playerData.updateData();
        playerData.stopOverTimer();
    }

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!CustomFightWorlds.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }

        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            Fights.pveHandler(killer, victim, Reasons.KILL);
        }
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!CustomFightWorlds.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        Player killer = victim.getKiller();
        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            if (victim != killer) {
                Fights.pvpHandler(killer, victim, Reasons.KILL);
            }
        }
    }

    /**
     * Changes karma when player attack another entity (animal or monster)
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        LivingEntity victimEntity;

        double damage = event.getFinalDamage();
        if (!(event.getEntity() instanceof LivingEntity && damage >= 1d
                && ((LivingEntity) event.getEntity()).getHealth() - damage > 0)) {
            return;
        }

        victimEntity = (LivingEntity) event.getEntity();
        if (!CustomFightWorlds.isFightEnabledInWorld(victimEntity.getWorld())) {
            return;
        }

        Player attacker = getFightAttacker(event);
        if (attacker != null) {
            if (Fights.isPlayerNPC(attacker) || TimeManager.getTimeManager().isPlayerInTime(attacker)) {
                return;
            }

            if (victimEntity instanceof Player) {
                Fights.pvpHandler(attacker, (Player) victimEntity, Reasons.HIT);
            } else {
                Fights.pveHandler(attacker, victimEntity, Reasons.HIT);
            }
        }
    }

    private Player getFightAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        } else if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        return null;
    }
}