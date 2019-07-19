/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket
implements Packet<ClientGamePacketListener> {
    private UUID id;
    private Operation operation;
    private Component name;
    private float pct;
    private BossEvent.BossBarColor color;
    private BossEvent.BossBarOverlay overlay;
    private boolean darkenScreen;
    private boolean playMusic;
    private boolean createWorldFog;

    public ClientboundBossEventPacket() {
    }

    public ClientboundBossEventPacket(Operation operation, BossEvent bossEvent) {
        this.operation = operation;
        this.id = bossEvent.getId();
        this.name = bossEvent.getName();
        this.pct = bossEvent.getPercent();
        this.color = bossEvent.getColor();
        this.overlay = bossEvent.getOverlay();
        this.darkenScreen = bossEvent.shouldDarkenScreen();
        this.playMusic = bossEvent.shouldPlayBossMusic();
        this.createWorldFog = bossEvent.shouldCreateWorldFog();
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.id = friendlyByteBuf.readUUID();
        this.operation = friendlyByteBuf.readEnum(Operation.class);
        switch (this.operation) {
            case ADD: {
                this.name = friendlyByteBuf.readComponent();
                this.pct = friendlyByteBuf.readFloat();
                this.color = friendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
                this.overlay = friendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
                this.decodeProperties(friendlyByteBuf.readUnsignedByte());
                break;
            }
            case REMOVE: {
                break;
            }
            case UPDATE_PCT: {
                this.pct = friendlyByteBuf.readFloat();
                break;
            }
            case UPDATE_NAME: {
                this.name = friendlyByteBuf.readComponent();
                break;
            }
            case UPDATE_STYLE: {
                this.color = friendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
                this.overlay = friendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
                break;
            }
            case UPDATE_PROPERTIES: {
                this.decodeProperties(friendlyByteBuf.readUnsignedByte());
            }
        }
    }

    private void decodeProperties(int i) {
        this.darkenScreen = (i & 1) > 0;
        this.playMusic = (i & 2) > 0;
        this.createWorldFog = (i & 4) > 0;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeUUID(this.id);
        friendlyByteBuf.writeEnum(this.operation);
        switch (this.operation) {
            case ADD: {
                friendlyByteBuf.writeComponent(this.name);
                friendlyByteBuf.writeFloat(this.pct);
                friendlyByteBuf.writeEnum(this.color);
                friendlyByteBuf.writeEnum(this.overlay);
                friendlyByteBuf.writeByte(this.encodeProperties());
                break;
            }
            case REMOVE: {
                break;
            }
            case UPDATE_PCT: {
                friendlyByteBuf.writeFloat(this.pct);
                break;
            }
            case UPDATE_NAME: {
                friendlyByteBuf.writeComponent(this.name);
                break;
            }
            case UPDATE_STYLE: {
                friendlyByteBuf.writeEnum(this.color);
                friendlyByteBuf.writeEnum(this.overlay);
                break;
            }
            case UPDATE_PROPERTIES: {
                friendlyByteBuf.writeByte(this.encodeProperties());
            }
        }
    }

    private int encodeProperties() {
        int i = 0;
        if (this.darkenScreen) {
            i |= 1;
        }
        if (this.playMusic) {
            i |= 2;
        }
        if (this.createWorldFog) {
            i |= 4;
        }
        return i;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBossUpdate(this);
    }

    @Environment(value=EnvType.CLIENT)
    public UUID getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    public Operation getOperation() {
        return this.operation;
    }

    @Environment(value=EnvType.CLIENT)
    public Component getName() {
        return this.name;
    }

    @Environment(value=EnvType.CLIENT)
    public float getPercent() {
        return this.pct;
    }

    @Environment(value=EnvType.CLIENT)
    public BossEvent.BossBarColor getColor() {
        return this.color;
    }

    @Environment(value=EnvType.CLIENT)
    public BossEvent.BossBarOverlay getOverlay() {
        return this.overlay;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldPlayMusic() {
        return this.playMusic;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldCreateWorldFog() {
        return this.createWorldFog;
    }

    public static enum Operation {
        ADD,
        REMOVE,
        UPDATE_PCT,
        UPDATE_NAME,
        UPDATE_STYLE,
        UPDATE_PROPERTIES;

    }
}

