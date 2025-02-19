package me.neznamy.tab.shared.features.proxy.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class ProxyTeams extends ProxyFeature {

    private final ProxySupport proxySupport;
    private final NameTag nameTags;

    public ProxyTeams(@NotNull ProxySupport proxySupport, @NotNull NameTag nameTags) {
        this.proxySupport = proxySupport;
        this.nameTags = nameTags;
        proxySupport.registerMessage("teams", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (ProxyPlayer proxied : proxySupport.getProxyPlayers().values()) {
            player.getScoreboard().registerTeam(proxied.getTeamName(), proxied.getTagPrefix(), proxied.getTagSuffix(),
                        proxied.getNameVisibility(), CollisionRule.ALWAYS,
                        Collections.singletonList(proxied.getNickname()), 2, EnumChatFormat.lastColorsOf(proxied.getTagPrefix()));
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().registerTeam(player.getTeamName(), player.getTagPrefix(), player.getTagSuffix(),
                    player.getNameVisibility(), CollisionRule.ALWAYS,
                    Collections.singletonList(player.getNickname()), 2, EnumChatFormat.lastColorsOf(player.getTagPrefix()));
        }
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().unregisterTeam(player.getTeamName());
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeUTF(player.sortingData.getShortTeamName());
        out.writeUTF(player.getProperty(TabConstants.Property.TAGPREFIX).get());
        out.writeUTF(player.getProperty(TabConstants.Property.TAGSUFFIX).get());
        out.writeUTF((nameTags.getTeamVisibility(player, player) ? NameVisibility.ALWAYS : NameVisibility.NEVER).toString());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull ProxyPlayer player) {
        String teamName = in.readUTF();
        teamName = checkTeamName(player, teamName.substring(0, teamName.length()-1), 65);
        player.setTeamName(teamName);
        player.setTagPrefix(in.readUTF());
        player.setTagSuffix(in.readUTF());
        player.setNameVisibility(NameVisibility.getByName(in.readUTF()));
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer player) {
        onJoin(player);
    }

    private @NotNull String checkTeamName(@NotNull ProxyPlayer player, @NotNull String currentName15, int id) {
        String potentialTeamName = currentName15 + (char)id;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.sortingData.getShortTeamName().equals(potentialTeamName)) {
                return checkTeamName(player, currentName15, id+1);
            }
        }
        for (ProxyPlayer all : proxySupport.getProxyPlayers().values()) {
            if (all == player) continue;
            if (potentialTeamName.equals(all.getTeamName())) {
                return checkTeamName(player, currentName15, id+1);
            }
        }
        return potentialTeamName;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends ProxyMessage {

        private UUID playerId;
        private String teamName;
        private String prefix;
        private String suffix;
        private NameVisibility nameVisibility;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(teamName);
            out.writeUTF(prefix);
            out.writeUTF(suffix);
            out.writeUTF(nameVisibility.toString());
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            teamName = in.readUTF();
            prefix = in.readUTF();
            suffix = in.readUTF();
            nameVisibility = NameVisibility.getByName(in.readUTF());
        }

        @Override
        public void process(@NotNull ProxySupport proxySupport) {
            ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
            if (target == null) return; // Print warn?
            // Team is already being processed by connected player
            if (TAB.getInstance().isPlayerConnected(target.getUniqueId())) {
                TAB.getInstance().debug("The player " + target.getName() + " is already connected");
                return;
            }
            String oldTeamName = target.getTeamName();
            String newTeamName = checkTeamName(target, teamName.substring(0, teamName.length()-1), 65);
            target.setTeamName(newTeamName);
            target.setTagPrefix(prefix);
            target.setTagSuffix(suffix);
            if (!oldTeamName.equals(newTeamName)) {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(oldTeamName);
                    viewer.getScoreboard().registerTeam(newTeamName, prefix, suffix, nameVisibility,
                            CollisionRule.ALWAYS, Collections.singletonList(target.getNickname()), 2, EnumChatFormat.lastColorsOf(prefix));
                }
            } else {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().updateTeam(oldTeamName, prefix, suffix, nameVisibility,
                            CollisionRule.ALWAYS, 2, EnumChatFormat.lastColorsOf(prefix));
                }
            }
        }
    }
}
