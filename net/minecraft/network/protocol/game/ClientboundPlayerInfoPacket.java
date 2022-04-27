/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public class ClientboundPlayerInfoPacket
implements Packet<ClientGamePacketListener> {
    private final Action action;
    private final List<PlayerUpdate> entries;

    public ClientboundPlayerInfoPacket(Action action, ServerPlayer ... serverPlayers) {
        this.action = action;
        this.entries = Lists.newArrayListWithCapacity(serverPlayers.length);
        for (ServerPlayer serverPlayer : serverPlayers) {
            this.entries.add(new PlayerUpdate(serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName()));
        }
    }

    public ClientboundPlayerInfoPacket(Action action, Collection<ServerPlayer> collection) {
        this.action = action;
        this.entries = Lists.newArrayListWithCapacity(collection.size());
        for (ServerPlayer serverPlayer : collection) {
            this.entries.add(new PlayerUpdate(serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName()));
        }
    }

    public ClientboundPlayerInfoPacket(FriendlyByteBuf friendlyByteBuf) {
        this.action = friendlyByteBuf.readEnum(Action.class);
        this.entries = friendlyByteBuf.readList(this.action::read);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.action);
        friendlyByteBuf.writeCollection(this.entries, this.action::write);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerInfo(this);
    }

    public List<PlayerUpdate> getEntries() {
        return this.entries;
    }

    public Action getAction() {
        return this.action;
    }

    @Nullable
    static Component readDisplayName(FriendlyByteBuf friendlyByteBuf) {
        return friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null;
    }

    static void writeDisplayName(FriendlyByteBuf friendlyByteBuf, @Nullable Component component) {
        if (component == null) {
            friendlyByteBuf.writeBoolean(false);
        } else {
            friendlyByteBuf.writeBoolean(true);
            friendlyByteBuf.writeComponent(component);
        }
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", (Object)this.action).add("entries", this.entries).toString();
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum Action {
        ADD_PLAYER{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = friendlyByteBuf.readGameProfile();
                GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
                int i = friendlyByteBuf.readVarInt();
                Component component = ClientboundPlayerInfoPacket.readDisplayName(friendlyByteBuf);
                return new PlayerUpdate(gameProfile, i, gameType, component);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf, PlayerUpdate playerUpdate) {
                friendlyByteBuf.writeGameProfile(playerUpdate.getProfile());
                friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
                friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
                ClientboundPlayerInfoPacket.writeDisplayName(friendlyByteBuf, playerUpdate.getDisplayName());
            }
        }
        ,
        UPDATE_GAME_MODE{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
                GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
                return new PlayerUpdate(gameProfile, 0, gameType, null);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf, PlayerUpdate playerUpdate) {
                friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
                friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
            }
        }
        ,
        UPDATE_LATENCY{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
                int i = friendlyByteBuf.readVarInt();
                return new PlayerUpdate(gameProfile, i, null, null);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf, PlayerUpdate playerUpdate) {
                friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
                friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
            }
        }
        ,
        UPDATE_DISPLAY_NAME{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
                Component component = ClientboundPlayerInfoPacket.readDisplayName(friendlyByteBuf);
                return new PlayerUpdate(gameProfile, 0, null, component);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf, PlayerUpdate playerUpdate) {
                friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
                ClientboundPlayerInfoPacket.writeDisplayName(friendlyByteBuf, playerUpdate.getDisplayName());
            }
        }
        ,
        REMOVE_PLAYER{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
                return new PlayerUpdate(gameProfile, 0, null, null);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf, PlayerUpdate playerUpdate) {
                friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
            }
        };


        protected abstract PlayerUpdate read(FriendlyByteBuf var1);

        protected abstract void write(FriendlyByteBuf var1, PlayerUpdate var2);
    }

    public static class PlayerUpdate {
        private final int latency;
        private final GameType gameMode;
        private final GameProfile profile;
        @Nullable
        private final Component displayName;

        public PlayerUpdate(GameProfile gameProfile, int i, @Nullable GameType gameType, @Nullable Component component) {
            this.profile = gameProfile;
            this.latency = i;
            this.gameMode = gameType;
            this.displayName = component;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public int getLatency() {
            return this.latency;
        }

        public GameType getGameMode() {
            return this.gameMode;
        }

        @Nullable
        public Component getDisplayName() {
            return this.displayName;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("latency", this.latency).add("gameMode", (Object)this.gameMode).add("profile", this.profile).add("displayName", this.displayName == null ? null : Component.Serializer.toJson(this.displayName)).toString();
        }
    }
}

