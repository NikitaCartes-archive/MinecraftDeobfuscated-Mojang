package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
	private double x;
	private double y;
	private double z;
	private float power;
	private List<BlockPos> toBlow;
	private float knockbackX;
	private float knockbackY;
	private float knockbackZ;

	public ClientboundExplodePacket() {
	}

	public ClientboundExplodePacket(double d, double e, double f, float g, List<BlockPos> list, Vec3 vec3) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.power = g;
		this.toBlow = Lists.<BlockPos>newArrayList(list);
		if (vec3 != null) {
			this.knockbackX = (float)vec3.x;
			this.knockbackY = (float)vec3.y;
			this.knockbackZ = (float)vec3.z;
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.x = (double)friendlyByteBuf.readFloat();
		this.y = (double)friendlyByteBuf.readFloat();
		this.z = (double)friendlyByteBuf.readFloat();
		this.power = friendlyByteBuf.readFloat();
		int i = friendlyByteBuf.readInt();
		this.toBlow = Lists.<BlockPos>newArrayListWithCapacity(i);
		int j = Mth.floor(this.x);
		int k = Mth.floor(this.y);
		int l = Mth.floor(this.z);

		for (int m = 0; m < i; m++) {
			int n = friendlyByteBuf.readByte() + j;
			int o = friendlyByteBuf.readByte() + k;
			int p = friendlyByteBuf.readByte() + l;
			this.toBlow.add(new BlockPos(n, o, p));
		}

		this.knockbackX = friendlyByteBuf.readFloat();
		this.knockbackY = friendlyByteBuf.readFloat();
		this.knockbackZ = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeFloat((float)this.x);
		friendlyByteBuf.writeFloat((float)this.y);
		friendlyByteBuf.writeFloat((float)this.z);
		friendlyByteBuf.writeFloat(this.power);
		friendlyByteBuf.writeInt(this.toBlow.size());
		int i = Mth.floor(this.x);
		int j = Mth.floor(this.y);
		int k = Mth.floor(this.z);

		for (BlockPos blockPos : this.toBlow) {
			int l = blockPos.getX() - i;
			int m = blockPos.getY() - j;
			int n = blockPos.getZ() - k;
			friendlyByteBuf.writeByte(l);
			friendlyByteBuf.writeByte(m);
			friendlyByteBuf.writeByte(n);
		}

		friendlyByteBuf.writeFloat(this.knockbackX);
		friendlyByteBuf.writeFloat(this.knockbackY);
		friendlyByteBuf.writeFloat(this.knockbackZ);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleExplosion(this);
	}

	@Environment(EnvType.CLIENT)
	public float getKnockbackX() {
		return this.knockbackX;
	}

	@Environment(EnvType.CLIENT)
	public float getKnockbackY() {
		return this.knockbackY;
	}

	@Environment(EnvType.CLIENT)
	public float getKnockbackZ() {
		return this.knockbackZ;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return this.y;
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return this.z;
	}

	@Environment(EnvType.CLIENT)
	public float getPower() {
		return this.power;
	}

	@Environment(EnvType.CLIENT)
	public List<BlockPos> getToBlow() {
		return this.toBlow;
	}
}
