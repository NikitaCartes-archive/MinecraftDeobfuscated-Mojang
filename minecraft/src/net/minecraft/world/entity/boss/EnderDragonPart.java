package net.minecraft.world.entity.boss;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;

public class EnderDragonPart extends Entity {
	public final EnderDragon parentMob;
	public final String name;
	private final EntityDimensions size;

	public EnderDragonPart(EnderDragon enderDragon, String string, float f, float g) {
		super(enderDragon.getType(), enderDragon.level());
		this.size = EntityDimensions.scalable(f, g);
		this.refreshDimensions();
		this.parentMob = enderDragon;
		this.name = string;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Nullable
	@Override
	public ItemStack getPickResult() {
		return this.parentMob.getPickResult();
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		return this.isInvulnerableTo(damageSource) ? false : this.parentMob.hurt(this, damageSource, f);
	}

	@Override
	public boolean is(Entity entity) {
		return this == entity || this.parentMob == entity;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return this.size;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}
}
