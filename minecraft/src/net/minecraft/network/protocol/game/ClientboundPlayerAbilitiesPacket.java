package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ClientboundPlayerAbilitiesPacket implements Packet<ClientGamePacketListener> {
	private final boolean invulnerable;
	private final boolean isFlying;
	private final boolean canFly;
	private final boolean instabuild;
	private final float flyingSpeed;
	private final float walkingSpeed;

	public ClientboundPlayerAbilitiesPacket(Abilities abilities) {
		this.invulnerable = abilities.invulnerable;
		this.isFlying = abilities.flying;
		this.canFly = abilities.mayfly;
		this.instabuild = abilities.instabuild;
		this.flyingSpeed = abilities.getFlyingSpeed();
		this.walkingSpeed = abilities.getWalkingSpeed();
	}

	public ClientboundPlayerAbilitiesPacket(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		this.invulnerable = (b & 1) != 0;
		this.isFlying = (b & 2) != 0;
		this.canFly = (b & 4) != 0;
		this.instabuild = (b & 8) != 0;
		this.flyingSpeed = friendlyByteBuf.readFloat();
		this.walkingSpeed = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		byte b = 0;
		if (this.invulnerable) {
			b = (byte)(b | 1);
		}

		if (this.isFlying) {
			b = (byte)(b | 2);
		}

		if (this.canFly) {
			b = (byte)(b | 4);
		}

		if (this.instabuild) {
			b = (byte)(b | 8);
		}

		friendlyByteBuf.writeByte(b);
		friendlyByteBuf.writeFloat(this.flyingSpeed);
		friendlyByteBuf.writeFloat(this.walkingSpeed);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerAbilities(this);
	}

	@Environment(EnvType.CLIENT)
	public boolean isInvulnerable() {
		return this.invulnerable;
	}

	@Environment(EnvType.CLIENT)
	public boolean isFlying() {
		return this.isFlying;
	}

	@Environment(EnvType.CLIENT)
	public boolean canFly() {
		return this.canFly;
	}

	@Environment(EnvType.CLIENT)
	public boolean canInstabuild() {
		return this.instabuild;
	}

	@Environment(EnvType.CLIENT)
	public float getFlyingSpeed() {
		return this.flyingSpeed;
	}

	@Environment(EnvType.CLIENT)
	public float getWalkingSpeed() {
		return this.walkingSpeed;
	}
}
