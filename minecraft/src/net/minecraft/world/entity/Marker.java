package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

public class Marker extends Entity {
	private static final String DATA_TAG = "data";
	private CompoundTag data = new CompoundTag();

	public Marker(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
	}

	@Override
	public void tick() {
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.data = compoundTag.getCompound("data");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.put("data", this.data.copy());
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		throw new IllegalStateException("Markers should never be sent");
	}

	@Override
	protected void addPassenger(Entity entity) {
		entity.stopRiding();
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}
}
