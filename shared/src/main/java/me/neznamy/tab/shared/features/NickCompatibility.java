package me.neznamy.tab.shared.features;

import java.util.Collections;
import java.util.UUID;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.feature.ProxyBelowName;
import me.neznamy.tab.shared.features.proxy.feature.ProxyTeams;
import me.neznamy.tab.shared.features.proxy.feature.ProxyYellowNumber;
import me.neznamy.tab.shared.features.types.EntryAddListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This feature attempts to provide compatibility with nick/disguise plugins by
 * listening to player add packet and see if nickname is different. If it is, player
 * is considered nicked and all name-bound features will use this new nickname.
 */
public class NickCompatibility extends TabFeature implements EntryAddListener {

    @Nullable private final NameTag nameTags = TAB.getInstance().getNameTagManager();
    @Nullable private final BelowName belowname = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
    @Nullable private final YellowNumber yellownumber = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER);
    @Nullable private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
    @Nullable private final ProxyTeams proxyTeams = proxy == null ? null : proxy.getProxyTeams();
    @Nullable private final ProxyYellowNumber proxyYellowNumber = proxy == null ? null : proxy.getProxyYellowNumber();
    @Nullable private final ProxyBelowName proxyBelowName = proxy == null ? null : proxy.getProxyBelowName();

    public synchronized void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
        TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(id);
        // Using "packetPlayer == packetReceiver" for now, as this should technically not matter, but it does
        // A nick plugin author said the nickname will be different for other players but same for nicking player,
        //      but the plugin does not even do that and changes it for everyone.
        // Another plugin changes name only for nicked player, for others it changes it in pipeline, which is
        //      injected after TAB, so this cannot be read properly and would false trigger un-nick.
        // For some very specific complicated plugins this may need to be different, either changing == to !=
        //      or completely removing the check. However, only this option worked for both nick plugins I tested.
        if (packetPlayer != null && packetPlayer == packetReceiver && !packetPlayer.getNickname().equals(name)) {
            packetPlayer.setNickname(name);
            TAB.getInstance().debug("Processing name change of player " + packetPlayer.getName() + " to " + name);
            processNameChange(packetPlayer);
        }
        if (proxy != null) {
            ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(id);
            if (proxyPlayer == null) return;
            if (!proxyPlayer.getNickname().equals(name)) {
                proxyPlayer.setNickname(name);
                TAB.getInstance().debug("Processing name change of proxy player " + proxyPlayer.getName() + " to " + name);
                processNameChange(proxyPlayer);
            }
        }
    }

    /**
     * Processes name change in all features.
     *
     * @param   player
     *          Player to update in all features
     */
    public void processNameChange(@NotNull TabPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.NICK_PLUGIN_COMPATIBILITY, () -> {
            if (nameTags != null && !nameTags.hasTeamHandlingPaused(player))
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    String prefix = player.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
                    viewer.getScoreboard().unregisterTeam(player.sortingData.getShortTeamName());
                    viewer.getScoreboard().registerTeam(
                            player.sortingData.getShortTeamName(),
                            prefix,
                            player.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer),
                            nameTags.getTeamVisibility(player, viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER,
                            player.teamData.getCollisionRule() ? Scoreboard.CollisionRule.ALWAYS : Scoreboard.CollisionRule.NEVER,
                            Collections.singletonList(player.getNickname()),
                            nameTags.getTeamOptions(),
                            EnumChatFormat.lastColorsOf(prefix)
                    );
                }
            if (belowname != null) {
                int value = belowname.getValue(player);
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    belowname.setScore(viewer, player, value, player.getProperty(belowname.getFANCY_FORMAT_PROPERTY()).get());
                }
            }
            if (yellownumber != null) {
                int value = yellownumber.getValueNumber(player);
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers())
                    yellownumber.setScore(viewer, player, value, player.getProperty(yellownumber.getPROPERTY_VALUE_FANCY()).get());
            }
        });
    }

    private void processNameChange(ProxyPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.NICK_PLUGIN_COMPATIBILITY, () -> {
            if (proxyTeams != null) {
                String teamName = player.getTeamName();
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(teamName);
                    viewer.getScoreboard().registerTeam(
                            teamName,
                            player.getTagPrefix(),
                            player.getTagSuffix(),
                            player.getNameVisibility(),
                            Scoreboard.CollisionRule.ALWAYS,
                            Collections.singletonList(player.getNickname()),
                            proxyTeams.getNameTags().getTeamOptions(),
                            EnumChatFormat.lastColorsOf(player.getTagPrefix())
                    );
                }
            }
            if (proxyBelowName != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.getScoreboard().setScore(
                            BelowName.OBJECTIVE_NAME,
                            player.getNickname(),
                            player.getBelowNameNumber(),
                            null, // Unused by this objective slot
                            player.getBelowNameFancy()
                    );
                }
            }
            if (proxyYellowNumber != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.getScoreboard().setScore(
                            YellowNumber.OBJECTIVE_NAME,
                            player.getNickname(),
                            player.getPlayerlistNumber(),
                            null, // Unused by this objective slot
                            player.getPlayerlistFancy()
                    );
                }
            }
        });
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "Nick compatibility";
    }
}
