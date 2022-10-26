/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public class ClientboundPlayerInfoUpdatePacket
implements Packet<ClientGamePacketListener> {
    private final EnumSet<Action> actions;
    private final List<Entry> entries;

    public ClientboundPlayerInfoUpdatePacket(EnumSet<Action> enumSet, Collection<ServerPlayer> collection) {
        this.actions = enumSet;
        this.entries = collection.stream().map(Entry::new).toList();
    }

    public ClientboundPlayerInfoUpdatePacket(Action action, ServerPlayer serverPlayer) {
        this.actions = EnumSet.of(action);
        this.entries = List.of(new Entry(serverPlayer));
    }

    public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> collection) {
        EnumSet<Action[]> enumSet = EnumSet.of(Action.ADD_PLAYER, new Action[]{Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME});
        return new ClientboundPlayerInfoUpdatePacket(enumSet, collection);
    }

    public ClientboundPlayerInfoUpdatePacket(FriendlyByteBuf friendlyByteBuf2) {
        this.actions = friendlyByteBuf2.readEnumSet(Action.class);
        this.entries = friendlyByteBuf2.readList(friendlyByteBuf -> {
            EntryBuilder entryBuilder = new EntryBuilder(friendlyByteBuf.readUUID());
            for (Action action : this.actions) {
                action.reader.read(entryBuilder, (FriendlyByteBuf)friendlyByteBuf);
            }
            return entryBuilder.build();
        });
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeEnumSet(this.actions, Action.class);
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> {
            friendlyByteBuf.writeUUID(entry.profileId());
            for (Action action : this.actions) {
                action.writer.write((FriendlyByteBuf)friendlyByteBuf, (Entry)entry);
            }
        });
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerInfoUpdate(this);
    }

    public EnumSet<Action> actions() {
        return this.actions;
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public List<Entry> newEntries() {
        return this.actions.contains((Object)Action.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public record Entry(UUID profileId, GameProfile profile, boolean listed, int latency, GameType gameMode, @Nullable Component displayName, @Nullable RemoteChatSession.Data chatSession) {
        Entry(ServerPlayer serverPlayer) {
            this(serverPlayer.getUUID(), serverPlayer.getGameProfile(), true, serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName(), Util.mapNullable(serverPlayer.getChatSession(), RemoteChatSession::asData));
        }

        @Nullable
        public Component displayName() {
            return this.displayName;
        }

        @Nullable
        public RemoteChatSession.Data chatSession() {
            return this.chatSession;
        }
    }

    public static enum Action {
        ADD_PLAYER((entryBuilder, friendlyByteBuf) -> {
            GameProfile gameProfile = new GameProfile(entryBuilder.profileId, friendlyByteBuf.readUtf(16));
            gameProfile.getProperties().putAll(friendlyByteBuf.readGameProfileProperties());
            entryBuilder.profile = gameProfile;
        }, (friendlyByteBuf, entry) -> {
            friendlyByteBuf.writeUtf(entry.profile().getName(), 16);
            friendlyByteBuf.writeGameProfileProperties(entry.profile().getProperties());
        }),
        INITIALIZE_CHAT((entryBuilder, friendlyByteBuf) -> {
            entryBuilder.chatSession = (RemoteChatSession.Data)friendlyByteBuf.readNullable(RemoteChatSession.Data::read);
        }, (friendlyByteBuf, entry) -> friendlyByteBuf.writeNullable(entry.chatSession, RemoteChatSession.Data::write)),
        UPDATE_GAME_MODE((entryBuilder, friendlyByteBuf) -> {
            entryBuilder.gameMode = GameType.byId(friendlyByteBuf.readVarInt());
        }, (friendlyByteBuf, entry) -> friendlyByteBuf.writeVarInt(entry.gameMode().getId())),
        UPDATE_LISTED((entryBuilder, friendlyByteBuf) -> {
            entryBuilder.listed = friendlyByteBuf.readBoolean();
        }, (friendlyByteBuf, entry) -> friendlyByteBuf.writeBoolean(entry.listed())),
        UPDATE_LATENCY((entryBuilder, friendlyByteBuf) -> {
            entryBuilder.latency = friendlyByteBuf.readVarInt();
        }, (friendlyByteBuf, entry) -> friendlyByteBuf.writeVarInt(entry.latency())),
        UPDATE_DISPLAY_NAME((entryBuilder, friendlyByteBuf) -> {
            entryBuilder.displayName = (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
        }, (friendlyByteBuf, entry) -> friendlyByteBuf.writeNullable(entry.displayName(), FriendlyByteBuf::writeComponent));

        final Reader reader;
        final Writer writer;

        private Action(Reader reader, Writer writer) {
            this.reader = reader;
            this.writer = writer;
        }

        public static interface Reader {
            public void read(EntryBuilder var1, FriendlyByteBuf var2);
        }

        public static interface Writer {
            public void write(FriendlyByteBuf var1, Entry var2);
        }
    }

    static class EntryBuilder {
        final UUID profileId;
        GameProfile profile;
        boolean listed;
        int latency;
        GameType gameMode = GameType.DEFAULT_MODE;
        @Nullable
        Component displayName;
        @Nullable
        RemoteChatSession.Data chatSession;

        EntryBuilder(UUID uUID) {
            this.profileId = uUID;
            this.profile = new GameProfile(uUID, null);
        }

        Entry build() {
            return new Entry(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession);
        }
    }
}

