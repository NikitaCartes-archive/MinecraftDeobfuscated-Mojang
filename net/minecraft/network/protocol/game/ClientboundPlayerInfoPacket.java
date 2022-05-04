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
import net.minecraft.world.entity.player.ProfilePublicKey;
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
            this.entries.add(ClientboundPlayerInfoPacket.createPlayerUpdate(serverPlayer));
        }
    }

    public ClientboundPlayerInfoPacket(Action action, Collection<ServerPlayer> collection) {
        this.action = action;
        this.entries = Lists.newArrayListWithCapacity(collection.size());
        for (ServerPlayer serverPlayer : collection) {
            this.entries.add(ClientboundPlayerInfoPacket.createPlayerUpdate(serverPlayer));
        }
    }

    public ClientboundPlayerInfoPacket(FriendlyByteBuf friendlyByteBuf) {
        this.action = friendlyByteBuf.readEnum(Action.class);
        this.entries = friendlyByteBuf.readList(this.action::read);
    }

    private static PlayerUpdate createPlayerUpdate(ServerPlayer serverPlayer) {
        ProfilePublicKey profilePublicKey = serverPlayer.getProfilePublicKey();
        ProfilePublicKey.Data data = profilePublicKey != null ? profilePublicKey.data() : null;
        return new PlayerUpdate(serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName(), data);
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

    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", (Object)this.action).add("entries", this.entries).toString();
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum Action {
        ADD_PLAYER{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf2) {
                GameProfile gameProfile = friendlyByteBuf2.readGameProfile();
                GameType gameType = GameType.byId(friendlyByteBuf2.readVarInt());
                int i = friendlyByteBuf2.readVarInt();
                Component component = (Component)friendlyByteBuf2.readNullable(FriendlyByteBuf::readComponent);
                ProfilePublicKey.Data data = (ProfilePublicKey.Data)friendlyByteBuf2.readNullable(friendlyByteBuf -> friendlyByteBuf.readWithCodec(ProfilePublicKey.Data.CODEC));
                return new PlayerUpdate(gameProfile, i, gameType, component, data);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf2, PlayerUpdate playerUpdate) {
                friendlyByteBuf2.writeGameProfile(playerUpdate.getProfile());
                friendlyByteBuf2.writeVarInt(playerUpdate.getGameMode().getId());
                friendlyByteBuf2.writeVarInt(playerUpdate.getLatency());
                friendlyByteBuf2.writeNullable(playerUpdate.getDisplayName(), FriendlyByteBuf::writeComponent);
                friendlyByteBuf2.writeNullable(playerUpdate.getProfilePublicKey(), (friendlyByteBuf, data) -> friendlyByteBuf.writeWithCodec(ProfilePublicKey.Data.CODEC, data));
            }
        }
        ,
        UPDATE_GAME_MODE{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
                GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
                return new PlayerUpdate(gameProfile, 0, gameType, null, null);
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
                return new PlayerUpdate(gameProfile, i, null, null, null);
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
                Component component = (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
                return new PlayerUpdate(gameProfile, 0, null, component, null);
            }

            @Override
            protected void write(FriendlyByteBuf friendlyByteBuf, PlayerUpdate playerUpdate) {
                friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
                friendlyByteBuf.writeNullable(playerUpdate.getDisplayName(), FriendlyByteBuf::writeComponent);
            }
        }
        ,
        REMOVE_PLAYER{

            @Override
            protected PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
                GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
                return new PlayerUpdate(gameProfile, 0, null, null, null);
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
        @Nullable
        private final ProfilePublicKey.Data profilePublicKey;

        public PlayerUpdate(GameProfile gameProfile, int i, @Nullable GameType gameType, @Nullable Component component, @Nullable ProfilePublicKey.Data data) {
            this.profile = gameProfile;
            this.latency = i;
            this.gameMode = gameType;
            this.displayName = component;
            this.profilePublicKey = data;
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

        @Nullable
        public ProfilePublicKey.Data getProfilePublicKey() {
            return this.profilePublicKey;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this).add("latency", this.latency).add("gameMode", (Object)this.gameMode).add("profile", this.profile).add("displayName", this.displayName == null ? null : Component.Serializer.toJson(this.displayName)).add("profilePublicKey", this.profilePublicKey).toString();
        }
    }
}

