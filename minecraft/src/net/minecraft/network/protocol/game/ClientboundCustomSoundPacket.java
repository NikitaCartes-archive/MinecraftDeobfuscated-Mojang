package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class ClientboundCustomSoundPacket implements Packet<ClientGamePacketListener> {
	private ResourceLocation name;
	private SoundSource source;
	private int x;
	private int y = Integer.MAX_VALUE;
	private int z;
	private float volume;
	private float pitch;

	public ClientboundCustomSoundPacket() {
	}

	public ClientboundCustomSoundPacket(ResourceLocation resourceLocation, SoundSource soundSource, Vec3 vec3, float f, float g) {
		this.name = resourceLocation;
		this.source = soundSource;
		this.x = (int)(vec3.x * 8.0);
		this.y = (int)(vec3.y * 8.0);
		this.z = (int)(vec3.z * 8.0);
		this.volume = f;
		this.pitch = g;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.name = friendlyByteBuf.readResourceLocation();
		this.source = friendlyByteBuf.readEnum(SoundSource.class);
		this.x = friendlyByteBuf.readInt();
		this.y = friendlyByteBuf.readInt();
		this.z = friendlyByteBuf.readInt();
		this.volume = friendlyByteBuf.readFloat();
		this.pitch = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeResourceLocation(this.name);
		friendlyByteBuf.writeEnum(this.source);
		friendlyByteBuf.writeInt(this.x);
		friendlyByteBuf.writeInt(this.y);
		friendlyByteBuf.writeInt(this.z);
		friendlyByteBuf.writeFloat(this.volume);
		friendlyByteBuf.writeFloat(this.pitch);
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getName() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	public SoundSource getSource() {
		return this.source;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return (double)((float)this.x / 8.0F);
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return (double)((float)this.y / 8.0F);
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return (double)((float)this.z / 8.0F);
	}

	@Environment(EnvType.CLIENT)
	public float getVolume() {
		return this.volume;
	}

	@Environment(EnvType.CLIENT)
	public float getPitch() {
		return this.pitch;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCustomSoundEvent(this);
	}
}
