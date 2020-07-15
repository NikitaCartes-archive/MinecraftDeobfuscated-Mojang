package net.minecraft.world.entity;

import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExperienceOrb extends Entity {
	public int tickCount;
	public int age;
	public int throwTime;
	private int health = 5;
	private int value;
	private Player followingPlayer;
	private int followingTime;

	public ExperienceOrb(Level level, double d, double e, double f, int i) {
		this(EntityType.EXPERIENCE_ORB, level);
		this.setPos(d, e, f);
		this.yRot = (float)(this.random.nextDouble() * 360.0);
		this.setDeltaMovement((this.random.nextDouble() * 0.2F - 0.1F) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2F - 0.1F) * 2.0);
		this.value = i;
	}

	public ExperienceOrb(EntityType<? extends ExperienceOrb> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	public void tick() {
		super.tick();
		if (this.throwTime > 0) {
			this.throwTime--;
		}

		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		if (this.isEyeInFluid(FluidTags.WATER)) {
			this.setUnderwaterMovement();
		} else if (!this.isNoGravity()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
		}

		if (this.level.getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
			this.setDeltaMovement(
				(double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), 0.2F, (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F)
			);
			this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
		}

		if (!this.level.noCollision(this.getBoundingBox())) {
			this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
		}

		double d = 8.0;
		if (this.followingTime < this.tickCount - 20 + this.getId() % 100) {
			if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0) {
				this.followingPlayer = this.level.getNearestPlayer(this, 8.0);
			}

			this.followingTime = this.tickCount;
		}

		if (this.followingPlayer != null && this.followingPlayer.isSpectator()) {
			this.followingPlayer = null;
		}

		if (this.followingPlayer != null) {
			Vec3 vec3 = new Vec3(
				this.followingPlayer.getX() - this.getX(),
				this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(),
				this.followingPlayer.getZ() - this.getZ()
			);
			double e = vec3.lengthSqr();
			if (e < 64.0) {
				double f = 1.0 - Math.sqrt(e) / 8.0;
				this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(f * f * 0.1)));
			}
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		float g = 0.98F;
		if (this.onGround) {
			g = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.98F;
		}

		this.setDeltaMovement(this.getDeltaMovement().multiply((double)g, 0.98, (double)g));
		if (this.onGround) {
			this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));
		}

		this.tickCount++;
		this.age++;
		if (this.age >= 6000) {
			this.remove();
		}
	}

	private void setUnderwaterMovement() {
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.x * 0.99F, Math.min(vec3.y + 5.0E-4F, 0.06F), vec3.z * 0.99F);
	}

	@Override
	protected void doWaterSplashEffect() {
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.markHurt();
			this.health = (int)((float)this.health - f);
			if (this.health <= 0) {
				this.remove();
			}

			return false;
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putShort("Health", (short)this.health);
		compoundTag.putShort("Age", (short)this.age);
		compoundTag.putShort("Value", (short)this.value);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.health = compoundTag.getShort("Health");
		this.age = compoundTag.getShort("Age");
		this.value = compoundTag.getShort("Value");
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.level.isClientSide) {
			if (this.throwTime == 0 && player.takeXpDelay == 0) {
				player.takeXpDelay = 2;
				player.take(this, 1);
				Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
				if (entry != null) {
					ItemStack itemStack = (ItemStack)entry.getValue();
					if (!itemStack.isEmpty() && itemStack.isDamaged()) {
						int i = Math.min(this.xpToDurability(this.value), itemStack.getDamageValue());
						this.value = this.value - this.durabilityToXp(i);
						itemStack.setDamageValue(itemStack.getDamageValue() - i);
					}
				}

				if (this.value > 0) {
					player.giveExperiencePoints(this.value);
				}

				this.remove();
			}
		}
	}

	private int durabilityToXp(int i) {
		return i / 2;
	}

	private int xpToDurability(int i) {
		return i * 2;
	}

	public int getValue() {
		return this.value;
	}

	@Environment(EnvType.CLIENT)
	public int getIcon() {
		if (this.value >= 2477) {
			return 10;
		} else if (this.value >= 1237) {
			return 9;
		} else if (this.value >= 617) {
			return 8;
		} else if (this.value >= 307) {
			return 7;
		} else if (this.value >= 149) {
			return 6;
		} else if (this.value >= 73) {
			return 5;
		} else if (this.value >= 37) {
			return 4;
		} else if (this.value >= 17) {
			return 3;
		} else if (this.value >= 7) {
			return 2;
		} else {
			return this.value >= 3 ? 1 : 0;
		}
	}

	public static int getExperienceValue(int i) {
		if (i >= 2477) {
			return 2477;
		} else if (i >= 1237) {
			return 1237;
		} else if (i >= 617) {
			return 617;
		} else if (i >= 307) {
			return 307;
		} else if (i >= 149) {
			return 149;
		} else if (i >= 73) {
			return 73;
		} else if (i >= 37) {
			return 37;
		} else if (i >= 17) {
			return 17;
		} else if (i >= 7) {
			return 7;
		} else {
			return i >= 3 ? 3 : 1;
		}
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddExperienceOrbPacket(this);
	}
}
