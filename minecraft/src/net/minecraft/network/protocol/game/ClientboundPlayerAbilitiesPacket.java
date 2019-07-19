package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ClientboundPlayerAbilitiesPacket implements Packet<ClientGamePacketListener> {
	private boolean invulnerable;
	private boolean isFlying;
	private boolean canFly;
	private boolean instabuild;
	private float flyingSpeed;
	private float walkingSpeed;

	public ClientboundPlayerAbilitiesPacket() {
	}

	public ClientboundPlayerAbilitiesPacket(Abilities abilities) {
		this.setInvulnerable(abilities.invulnerable);
		this.setFlying(abilities.flying);
		this.setCanFly(abilities.mayfly);
		this.setInstabuild(abilities.instabuild);
		this.setFlyingSpeed(abilities.getFlyingSpeed());
		this.setWalkingSpeed(abilities.getWalkingSpeed());
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		byte b = friendlyByteBuf.readByte();
		this.setInvulnerable((b & 1) > 0);
		this.setFlying((b & 2) > 0);
		this.setCanFly((b & 4) > 0);
		this.setInstabuild((b & 8) > 0);
		this.setFlyingSpeed(friendlyByteBuf.readFloat());
		this.setWalkingSpeed(friendlyByteBuf.readFloat());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		byte b = 0;
		if (this.isInvulnerable()) {
			b = (byte)(b | 1);
		}

		if (this.isFlying()) {
			b = (byte)(b | 2);
		}

		if (this.canFly()) {
			b = (byte)(b | 4);
		}

		if (this.canInstabuild()) {
			b = (byte)(b | 8);
		}

		friendlyByteBuf.writeByte(b);
		friendlyByteBuf.writeFloat(this.flyingSpeed);
		friendlyByteBuf.writeFloat(this.walkingSpeed);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerAbilities(this);
	}

	public boolean isInvulnerable() {
		return this.invulnerable;
	}

	public void setInvulnerable(boolean bl) {
		this.invulnerable = bl;
	}

	public boolean isFlying() {
		return this.isFlying;
	}

	public void setFlying(boolean bl) {
		this.isFlying = bl;
	}

	public boolean canFly() {
		return this.canFly;
	}

	public void setCanFly(boolean bl) {
		this.canFly = bl;
	}

	public boolean canInstabuild() {
		return this.instabuild;
	}

	public void setInstabuild(boolean bl) {
		this.instabuild = bl;
	}

	@Environment(EnvType.CLIENT)
	public float getFlyingSpeed() {
		return this.flyingSpeed;
	}

	public void setFlyingSpeed(float f) {
		this.flyingSpeed = f;
	}

	@Environment(EnvType.CLIENT)
	public float getWalkingSpeed() {
		return this.walkingSpeed;
	}

	public void setWalkingSpeed(float f) {
		this.walkingSpeed = f;
	}
}
