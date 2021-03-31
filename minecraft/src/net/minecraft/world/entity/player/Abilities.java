package net.minecraft.world.entity.player;

import net.minecraft.nbt.CompoundTag;

public class Abilities {
	public boolean invulnerable;
	public boolean flying;
	public boolean mayfly;
	public boolean instabuild;
	public boolean mayBuild = true;
	private float flyingSpeed = 0.05F;
	private float walkingSpeed = 0.1F;

	public void addSaveData(CompoundTag compoundTag) {
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.putBoolean("invulnerable", this.invulnerable);
		compoundTag2.putBoolean("flying", this.flying);
		compoundTag2.putBoolean("mayfly", this.mayfly);
		compoundTag2.putBoolean("instabuild", this.instabuild);
		compoundTag2.putBoolean("mayBuild", this.mayBuild);
		compoundTag2.putFloat("flySpeed", this.flyingSpeed);
		compoundTag2.putFloat("walkSpeed", this.walkingSpeed);
		compoundTag.put("abilities", compoundTag2);
	}

	public void loadSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("abilities", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("abilities");
			this.invulnerable = compoundTag2.getBoolean("invulnerable");
			this.flying = compoundTag2.getBoolean("flying");
			this.mayfly = compoundTag2.getBoolean("mayfly");
			this.instabuild = compoundTag2.getBoolean("instabuild");
			if (compoundTag2.contains("flySpeed", 99)) {
				this.flyingSpeed = compoundTag2.getFloat("flySpeed");
				this.walkingSpeed = compoundTag2.getFloat("walkSpeed");
			}

			if (compoundTag2.contains("mayBuild", 1)) {
				this.mayBuild = compoundTag2.getBoolean("mayBuild");
			}
		}
	}

	public float getFlyingSpeed() {
		return this.flyingSpeed;
	}

	public void setFlyingSpeed(float f) {
		this.flyingSpeed = f;
	}

	public float getWalkingSpeed() {
		return this.walkingSpeed;
	}

	public void setWalkingSpeed(float f) {
		this.walkingSpeed = f;
	}
}
