/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ClientboundExplodePacket
implements Packet<ClientGamePacketListener> {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;

    public ClientboundExplodePacket(double d, double e, double f, float g, List<BlockPos> list, @Nullable Vec3 vec3) {
        this.x = d;
        this.y = e;
        this.z = f;
        this.power = g;
        this.toBlow = Lists.newArrayList(list);
        if (vec3 != null) {
            this.knockbackX = (float)vec3.x;
            this.knockbackY = (float)vec3.y;
            this.knockbackZ = (float)vec3.z;
        } else {
            this.knockbackX = 0.0f;
            this.knockbackY = 0.0f;
            this.knockbackZ = 0.0f;
        }
    }

    public ClientboundExplodePacket(FriendlyByteBuf friendlyByteBuf2) {
        this.x = friendlyByteBuf2.readDouble();
        this.y = friendlyByteBuf2.readDouble();
        this.z = friendlyByteBuf2.readDouble();
        this.power = friendlyByteBuf2.readFloat();
        int i = Mth.floor(this.x);
        int j = Mth.floor(this.y);
        int k = Mth.floor(this.z);
        this.toBlow = friendlyByteBuf2.readList(friendlyByteBuf -> {
            int l = friendlyByteBuf.readByte() + i;
            int m = friendlyByteBuf.readByte() + j;
            int n = friendlyByteBuf.readByte() + k;
            return new BlockPos(l, m, n);
        });
        this.knockbackX = friendlyByteBuf2.readFloat();
        this.knockbackY = friendlyByteBuf2.readFloat();
        this.knockbackZ = friendlyByteBuf2.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeDouble(this.x);
        friendlyByteBuf2.writeDouble(this.y);
        friendlyByteBuf2.writeDouble(this.z);
        friendlyByteBuf2.writeFloat(this.power);
        int i = Mth.floor(this.x);
        int j = Mth.floor(this.y);
        int k = Mth.floor(this.z);
        friendlyByteBuf2.writeCollection(this.toBlow, (friendlyByteBuf, blockPos) -> {
            int l = blockPos.getX() - i;
            int m = blockPos.getY() - j;
            int n = blockPos.getZ() - k;
            friendlyByteBuf.writeByte(l);
            friendlyByteBuf.writeByte(m);
            friendlyByteBuf.writeByte(n);
        });
        friendlyByteBuf2.writeFloat(this.knockbackX);
        friendlyByteBuf2.writeFloat(this.knockbackY);
        friendlyByteBuf2.writeFloat(this.knockbackZ);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }
}

