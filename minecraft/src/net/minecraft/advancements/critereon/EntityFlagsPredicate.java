package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EntityFlagsPredicate(
	Optional<Boolean> isOnGround,
	Optional<Boolean> isOnFire,
	Optional<Boolean> isCrouching,
	Optional<Boolean> isSprinting,
	Optional<Boolean> isSwimming,
	Optional<Boolean> isFlying,
	Optional<Boolean> isBaby
) {
	public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagsPredicate::isOnGround),
					Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagsPredicate::isOnFire),
					Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagsPredicate::isCrouching),
					Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagsPredicate::isSprinting),
					Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagsPredicate::isSwimming),
					Codec.BOOL.optionalFieldOf("is_flying").forGetter(EntityFlagsPredicate::isFlying),
					Codec.BOOL.optionalFieldOf("is_baby").forGetter(EntityFlagsPredicate::isBaby)
				)
				.apply(instance, EntityFlagsPredicate::new)
	);

	public boolean matches(Entity entity) {
		if (this.isOnGround.isPresent() && entity.onGround() != (Boolean)this.isOnGround.get()) {
			return false;
		} else if (this.isOnFire.isPresent() && entity.isOnFire() != (Boolean)this.isOnFire.get()) {
			return false;
		} else if (this.isCrouching.isPresent() && entity.isCrouching() != (Boolean)this.isCrouching.get()) {
			return false;
		} else if (this.isSprinting.isPresent() && entity.isSprinting() != (Boolean)this.isSprinting.get()) {
			return false;
		} else if (this.isSwimming.isPresent() && entity.isSwimming() != (Boolean)this.isSwimming.get()) {
			return false;
		} else {
			if (this.isFlying.isPresent()) {
				boolean var10000;
				label53: {
					if (entity instanceof LivingEntity livingEntity && (livingEntity.isFallFlying() || livingEntity instanceof Player player && player.getAbilities().flying)) {
						var10000 = true;
						break label53;
					}

					var10000 = false;
				}

				boolean bl = var10000;
				if (bl != (Boolean)this.isFlying.get()) {
					return false;
				}
			}

			if (this.isBaby.isPresent() && entity instanceof LivingEntity livingEntity2 && livingEntity2.isBaby() != (Boolean)this.isBaby.get()) {
				return false;
			}

			return true;
		}
	}

	public static class Builder {
		private Optional<Boolean> isOnGround = Optional.empty();
		private Optional<Boolean> isOnFire = Optional.empty();
		private Optional<Boolean> isCrouching = Optional.empty();
		private Optional<Boolean> isSprinting = Optional.empty();
		private Optional<Boolean> isSwimming = Optional.empty();
		private Optional<Boolean> isFlying = Optional.empty();
		private Optional<Boolean> isBaby = Optional.empty();

		public static EntityFlagsPredicate.Builder flags() {
			return new EntityFlagsPredicate.Builder();
		}

		public EntityFlagsPredicate.Builder setOnGround(Boolean boolean_) {
			this.isOnGround = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate.Builder setOnFire(Boolean boolean_) {
			this.isOnFire = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate.Builder setCrouching(Boolean boolean_) {
			this.isCrouching = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate.Builder setSprinting(Boolean boolean_) {
			this.isSprinting = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate.Builder setSwimming(Boolean boolean_) {
			this.isSwimming = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate.Builder setIsFlying(Boolean boolean_) {
			this.isFlying = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate.Builder setIsBaby(Boolean boolean_) {
			this.isBaby = Optional.of(boolean_);
			return this;
		}

		public EntityFlagsPredicate build() {
			return new EntityFlagsPredicate(this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby);
		}
	}
}
