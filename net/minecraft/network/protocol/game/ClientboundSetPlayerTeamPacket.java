/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public class ClientboundSetPlayerTeamPacket
implements Packet<ClientGamePacketListener> {
    private final int method;
    private final String name;
    private final Collection<String> players;
    private final Optional<Parameters> parameters;

    private ClientboundSetPlayerTeamPacket(String string, int i, Optional<Parameters> optional, Collection<String> collection) {
        this.name = string;
        this.method = i;
        this.parameters = optional;
        this.players = ImmutableList.copyOf(collection);
    }

    public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam playerTeam, boolean bl) {
        return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), bl ? 0 : 2, Optional.of(new Parameters(playerTeam)), bl ? playerTeam.getPlayers() : ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam playerTeam) {
        return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), 1, Optional.empty(), ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam playerTeam, String string, Action action) {
        return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), action == Action.ADD ? 3 : 1, Optional.empty(), ImmutableList.of(string));
    }

    public ClientboundSetPlayerTeamPacket(FriendlyByteBuf friendlyByteBuf) {
        this.name = friendlyByteBuf.readUtf(16);
        this.method = friendlyByteBuf.readByte();
        this.parameters = ClientboundSetPlayerTeamPacket.shouldHaveParameters(this.method) ? Optional.of(new Parameters(friendlyByteBuf)) : Optional.empty();
        this.players = ClientboundSetPlayerTeamPacket.shouldHavePlayerList(this.method) ? friendlyByteBuf.readList(FriendlyByteBuf::readUtf) : ImmutableList.of();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.name);
        friendlyByteBuf.writeByte(this.method);
        if (ClientboundSetPlayerTeamPacket.shouldHaveParameters(this.method)) {
            this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)).write(friendlyByteBuf);
        }
        if (ClientboundSetPlayerTeamPacket.shouldHavePlayerList(this.method)) {
            friendlyByteBuf.writeCollection(this.players, FriendlyByteBuf::writeUtf);
        }
    }

    private static boolean shouldHavePlayerList(int i) {
        return i == 0 || i == 3 || i == 4;
    }

    private static boolean shouldHaveParameters(int i) {
        return i == 0 || i == 2;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public Action getPlayerAction() {
        switch (this.method) {
            case 0: 
            case 3: {
                return Action.ADD;
            }
            case 4: {
                return Action.REMOVE;
            }
        }
        return null;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public Action getTeamAction() {
        switch (this.method) {
            case 0: {
                return Action.ADD;
            }
            case 1: {
                return Action.REMOVE;
            }
        }
        return null;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetPlayerTeamPacket(this);
    }

    @Environment(value=EnvType.CLIENT)
    public String getName() {
        return this.name;
    }

    @Environment(value=EnvType.CLIENT)
    public Collection<String> getPlayers() {
        return this.players;
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<Parameters> getParameters() {
        return this.parameters;
    }

    public static class Parameters {
        private final Component displayName;
        private final Component playerPrefix;
        private final Component playerSuffix;
        private final String nametagVisibility;
        private final String collisionRule;
        private final ChatFormatting color;
        private final int options;

        public Parameters(PlayerTeam playerTeam) {
            this.displayName = playerTeam.getDisplayName();
            this.options = playerTeam.packOptions();
            this.nametagVisibility = playerTeam.getNameTagVisibility().name;
            this.collisionRule = playerTeam.getCollisionRule().name;
            this.color = playerTeam.getColor();
            this.playerPrefix = playerTeam.getPlayerPrefix();
            this.playerSuffix = playerTeam.getPlayerSuffix();
        }

        public Parameters(FriendlyByteBuf friendlyByteBuf) {
            this.displayName = friendlyByteBuf.readComponent();
            this.options = friendlyByteBuf.readByte();
            this.nametagVisibility = friendlyByteBuf.readUtf(40);
            this.collisionRule = friendlyByteBuf.readUtf(40);
            this.color = friendlyByteBuf.readEnum(ChatFormatting.class);
            this.playerPrefix = friendlyByteBuf.readComponent();
            this.playerSuffix = friendlyByteBuf.readComponent();
        }

        @Environment(value=EnvType.CLIENT)
        public Component getDisplayName() {
            return this.displayName;
        }

        @Environment(value=EnvType.CLIENT)
        public int getOptions() {
            return this.options;
        }

        @Environment(value=EnvType.CLIENT)
        public ChatFormatting getColor() {
            return this.color;
        }

        @Environment(value=EnvType.CLIENT)
        public String getNametagVisibility() {
            return this.nametagVisibility;
        }

        @Environment(value=EnvType.CLIENT)
        public String getCollisionRule() {
            return this.collisionRule;
        }

        @Environment(value=EnvType.CLIENT)
        public Component getPlayerPrefix() {
            return this.playerPrefix;
        }

        @Environment(value=EnvType.CLIENT)
        public Component getPlayerSuffix() {
            return this.playerSuffix;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeComponent(this.displayName);
            friendlyByteBuf.writeByte(this.options);
            friendlyByteBuf.writeUtf(this.nametagVisibility);
            friendlyByteBuf.writeUtf(this.collisionRule);
            friendlyByteBuf.writeEnum(this.color);
            friendlyByteBuf.writeComponent(this.playerPrefix);
            friendlyByteBuf.writeComponent(this.playerSuffix);
        }
    }

    public static enum Action {
        ADD,
        REMOVE;

    }
}

