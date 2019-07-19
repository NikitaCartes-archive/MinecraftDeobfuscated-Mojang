/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundPlayerPositionPacket
implements Packet<ClientGamePacketListener> {
    private double x;
    private double y;
    private double z;
    private float yRot;
    private float xRot;
    private Set<RelativeArgument> relativeArguments;
    private int id;

    public ClientboundPlayerPositionPacket() {
    }

    public ClientboundPlayerPositionPacket(double d, double e, double f, float g, float h, Set<RelativeArgument> set, int i) {
        this.x = d;
        this.y = e;
        this.z = f;
        this.yRot = g;
        this.xRot = h;
        this.relativeArguments = set;
        this.id = i;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.x = friendlyByteBuf.readDouble();
        this.y = friendlyByteBuf.readDouble();
        this.z = friendlyByteBuf.readDouble();
        this.yRot = friendlyByteBuf.readFloat();
        this.xRot = friendlyByteBuf.readFloat();
        this.relativeArguments = RelativeArgument.unpack(friendlyByteBuf.readUnsignedByte());
        this.id = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeDouble(this.x);
        friendlyByteBuf.writeDouble(this.y);
        friendlyByteBuf.writeDouble(this.z);
        friendlyByteBuf.writeFloat(this.yRot);
        friendlyByteBuf.writeFloat(this.xRot);
        friendlyByteBuf.writeByte(RelativeArgument.pack(this.relativeArguments));
        friendlyByteBuf.writeVarInt(this.id);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMovePlayer(this);
    }

    @Environment(value=EnvType.CLIENT)
    public double getX() {
        return this.x;
    }

    @Environment(value=EnvType.CLIENT)
    public double getY() {
        return this.y;
    }

    @Environment(value=EnvType.CLIENT)
    public double getZ() {
        return this.z;
    }

    @Environment(value=EnvType.CLIENT)
    public float getYRot() {
        return this.yRot;
    }

    @Environment(value=EnvType.CLIENT)
    public float getXRot() {
        return this.xRot;
    }

    @Environment(value=EnvType.CLIENT)
    public int getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    public Set<RelativeArgument> getRelativeArguments() {
        return this.relativeArguments;
    }

    public static enum RelativeArgument {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int bit;

        private RelativeArgument(int j) {
            this.bit = j;
        }

        private int getMask() {
            return 1 << this.bit;
        }

        private boolean isSet(int i) {
            return (i & this.getMask()) == this.getMask();
        }

        public static Set<RelativeArgument> unpack(int i) {
            EnumSet<RelativeArgument> set = EnumSet.noneOf(RelativeArgument.class);
            for (RelativeArgument relativeArgument : RelativeArgument.values()) {
                if (!relativeArgument.isSet(i)) continue;
                set.add(relativeArgument);
            }
            return set;
        }

        public static int pack(Set<RelativeArgument> set) {
            int i = 0;
            for (RelativeArgument relativeArgument : set) {
                i |= relativeArgument.getMask();
            }
            return i;
        }
    }
}

