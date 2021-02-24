/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundGameEventPacket
implements Packet<ClientGamePacketListener> {
    public static final Type NO_RESPAWN_BLOCK_AVAILABLE = new Type(0);
    public static final Type START_RAINING = new Type(1);
    public static final Type STOP_RAINING = new Type(2);
    public static final Type CHANGE_GAME_MODE = new Type(3);
    public static final Type WIN_GAME = new Type(4);
    public static final Type DEMO_EVENT = new Type(5);
    public static final Type ARROW_HIT_PLAYER = new Type(6);
    public static final Type RAIN_LEVEL_CHANGE = new Type(7);
    public static final Type THUNDER_LEVEL_CHANGE = new Type(8);
    public static final Type PUFFER_FISH_STING = new Type(9);
    public static final Type GUARDIAN_ELDER_EFFECT = new Type(10);
    public static final Type IMMEDIATE_RESPAWN = new Type(11);
    private final Type event;
    private final float param;

    public ClientboundGameEventPacket(Type type, float f) {
        this.event = type;
        this.param = f;
    }

    public ClientboundGameEventPacket(FriendlyByteBuf friendlyByteBuf) {
        this.event = (Type)Type.TYPES.get(friendlyByteBuf.readUnsignedByte());
        this.param = friendlyByteBuf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.event.id);
        friendlyByteBuf.writeFloat(this.param);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleGameEvent(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Type getEvent() {
        return this.event;
    }

    @Environment(value=EnvType.CLIENT)
    public float getParam() {
        return this.param;
    }

    public static class Type {
        private static final Int2ObjectMap<Type> TYPES = new Int2ObjectOpenHashMap<Type>();
        private final int id;

        public Type(int i) {
            this.id = i;
            TYPES.put(i, this);
        }
    }
}

