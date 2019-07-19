/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundSetEntityMotionPacket
implements Packet<ClientGamePacketListener> {
    private int id;
    private int xa;
    private int ya;
    private int za;

    public ClientboundSetEntityMotionPacket() {
    }

    public ClientboundSetEntityMotionPacket(Entity entity) {
        this(entity.getId(), entity.getDeltaMovement());
    }

    public ClientboundSetEntityMotionPacket(int i, Vec3 vec3) {
        this.id = i;
        double d = 3.9;
        double e = Mth.clamp(vec3.x, -3.9, 3.9);
        double f = Mth.clamp(vec3.y, -3.9, 3.9);
        double g = Mth.clamp(vec3.z, -3.9, 3.9);
        this.xa = (int)(e * 8000.0);
        this.ya = (int)(f * 8000.0);
        this.za = (int)(g * 8000.0);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.id = friendlyByteBuf.readVarInt();
        this.xa = friendlyByteBuf.readShort();
        this.ya = friendlyByteBuf.readShort();
        this.za = friendlyByteBuf.readShort();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeShort(this.xa);
        friendlyByteBuf.writeShort(this.ya);
        friendlyByteBuf.writeShort(this.za);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEntityMotion(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    public int getXa() {
        return this.xa;
    }

    @Environment(value=EnvType.CLIENT)
    public int getYa() {
        return this.ya;
    }

    @Environment(value=EnvType.CLIENT)
    public int getZa() {
        return this.za;
    }
}

