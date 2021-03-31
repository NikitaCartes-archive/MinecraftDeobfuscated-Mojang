/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket
implements Packet<ClientGamePacketListener> {
    private static final int FLAG_DARKEN = 1;
    private static final int FLAG_MUSIC = 2;
    private static final int FLAG_FOG = 4;
    private final UUID id;
    private final Operation operation;
    private static final Operation REMOVE_OPERATION = new Operation(){

        @Override
        public OperationType getType() {
            return OperationType.REMOVE;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.remove(uUID);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
        }
    };

    private ClientboundBossEventPacket(UUID uUID, Operation operation) {
        this.id = uUID;
        this.operation = operation;
    }

    public ClientboundBossEventPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readUUID();
        OperationType operationType = friendlyByteBuf.readEnum(OperationType.class);
        this.operation = (Operation)operationType.reader.apply(friendlyByteBuf);
    }

    public static ClientboundBossEventPacket createAddPacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new AddOperation(bossEvent));
    }

    public static ClientboundBossEventPacket createRemovePacket(UUID uUID) {
        return new ClientboundBossEventPacket(uUID, REMOVE_OPERATION);
    }

    public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdateProgressOperation(bossEvent.getProgress()));
    }

    public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdateNameOperation(bossEvent.getName()));
    }

    public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdateStyleOperation(bossEvent.getColor(), bossEvent.getOverlay()));
    }

    public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent bossEvent) {
        return new ClientboundBossEventPacket(bossEvent.getId(), new UpdatePropertiesOperation(bossEvent.shouldDarkenScreen(), bossEvent.shouldPlayBossMusic(), bossEvent.shouldCreateWorldFog()));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.id);
        friendlyByteBuf.writeEnum(this.operation.getType());
        this.operation.write(friendlyByteBuf);
    }

    private static int encodeProperties(boolean bl, boolean bl2, boolean bl3) {
        int i = 0;
        if (bl) {
            i |= 1;
        }
        if (bl2) {
            i |= 2;
        }
        if (bl3) {
            i |= 4;
        }
        return i;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBossUpdate(this);
    }

    public void dispatch(Handler handler) {
        this.operation.dispatch(this.id, handler);
    }

    static class UpdatePropertiesOperation
    implements Operation {
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        private UpdatePropertiesOperation(boolean bl, boolean bl2, boolean bl3) {
            this.darkenScreen = bl;
            this.playMusic = bl2;
            this.createWorldFog = bl3;
        }

        private UpdatePropertiesOperation(FriendlyByteBuf friendlyByteBuf) {
            short i = friendlyByteBuf.readUnsignedByte();
            this.darkenScreen = (i & 1) > 0;
            this.playMusic = (i & 2) > 0;
            this.createWorldFog = (i & 4) > 0;
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_PROPERTIES;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateProperties(uUID, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    static class UpdateStyleOperation
    implements Operation {
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;

        private UpdateStyleOperation(BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
            this.color = bossBarColor;
            this.overlay = bossBarOverlay;
        }

        private UpdateStyleOperation(FriendlyByteBuf friendlyByteBuf) {
            this.color = friendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
            this.overlay = friendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_STYLE;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateStyle(uUID, this.color, this.overlay);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeEnum(this.color);
            friendlyByteBuf.writeEnum(this.overlay);
        }
    }

    static class UpdateNameOperation
    implements Operation {
        private final Component name;

        private UpdateNameOperation(Component component) {
            this.name = component;
        }

        private UpdateNameOperation(FriendlyByteBuf friendlyByteBuf) {
            this.name = friendlyByteBuf.readComponent();
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_NAME;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateName(uUID, this.name);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeComponent(this.name);
        }
    }

    static class UpdateProgressOperation
    implements Operation {
        private final float progress;

        private UpdateProgressOperation(float f) {
            this.progress = f;
        }

        private UpdateProgressOperation(FriendlyByteBuf friendlyByteBuf) {
            this.progress = friendlyByteBuf.readFloat();
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_PROGRESS;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.updateProgress(uUID, this.progress);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeFloat(this.progress);
        }
    }

    static class AddOperation
    implements Operation {
        private final Component name;
        private final float progress;
        private final BossEvent.BossBarColor color;
        private final BossEvent.BossBarOverlay overlay;
        private final boolean darkenScreen;
        private final boolean playMusic;
        private final boolean createWorldFog;

        private AddOperation(BossEvent bossEvent) {
            this.name = bossEvent.getName();
            this.progress = bossEvent.getProgress();
            this.color = bossEvent.getColor();
            this.overlay = bossEvent.getOverlay();
            this.darkenScreen = bossEvent.shouldDarkenScreen();
            this.playMusic = bossEvent.shouldPlayBossMusic();
            this.createWorldFog = bossEvent.shouldCreateWorldFog();
        }

        private AddOperation(FriendlyByteBuf friendlyByteBuf) {
            this.name = friendlyByteBuf.readComponent();
            this.progress = friendlyByteBuf.readFloat();
            this.color = friendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
            this.overlay = friendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
            short i = friendlyByteBuf.readUnsignedByte();
            this.darkenScreen = (i & 1) > 0;
            this.playMusic = (i & 2) > 0;
            this.createWorldFog = (i & 4) > 0;
        }

        @Override
        public OperationType getType() {
            return OperationType.ADD;
        }

        @Override
        public void dispatch(UUID uUID, Handler handler) {
            handler.add(uUID, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeComponent(this.name);
            friendlyByteBuf.writeFloat(this.progress);
            friendlyByteBuf.writeEnum(this.color);
            friendlyByteBuf.writeEnum(this.overlay);
            friendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
        }
    }

    static interface Operation {
        public OperationType getType();

        public void dispatch(UUID var1, Handler var2);

        public void write(FriendlyByteBuf var1);
    }

    public static interface Handler {
        default public void add(UUID uUID, Component component, float f, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl, boolean bl2, boolean bl3) {
        }

        default public void remove(UUID uUID) {
        }

        default public void updateProgress(UUID uUID, float f) {
        }

        default public void updateName(UUID uUID, Component component) {
        }

        default public void updateStyle(UUID uUID, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
        }

        default public void updateProperties(UUID uUID, boolean bl, boolean bl2, boolean bl3) {
        }
    }

    static enum OperationType {
        ADD(friendlyByteBuf -> new AddOperation((FriendlyByteBuf)friendlyByteBuf)),
        REMOVE(friendlyByteBuf -> ClientboundBossEventPacket.method_34093()),
        UPDATE_PROGRESS(friendlyByteBuf -> new UpdateProgressOperation((FriendlyByteBuf)friendlyByteBuf)),
        UPDATE_NAME(friendlyByteBuf -> new UpdateNameOperation((FriendlyByteBuf)friendlyByteBuf)),
        UPDATE_STYLE(friendlyByteBuf -> new UpdateStyleOperation((FriendlyByteBuf)friendlyByteBuf)),
        UPDATE_PROPERTIES(friendlyByteBuf -> new UpdatePropertiesOperation((FriendlyByteBuf)friendlyByteBuf));

        private final Function<FriendlyByteBuf, Operation> reader;

        private OperationType(Function<FriendlyByteBuf, Operation> function) {
            this.reader = function;
        }
    }
}

