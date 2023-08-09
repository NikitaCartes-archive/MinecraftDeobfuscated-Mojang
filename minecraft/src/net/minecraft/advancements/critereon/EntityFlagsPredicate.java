package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record EntityFlagsPredicate(
	Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isBaby
) {
	public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(Codec.BOOL, "is_on_fire").forGetter(EntityFlagsPredicate::isOnFire),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "is_sneaking").forGetter(EntityFlagsPredicate::isCrouching),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "is_sprinting").forGetter(EntityFlagsPredicate::isSprinting),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "is_swimming").forGetter(EntityFlagsPredicate::isSwimming),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "is_baby").forGetter(EntityFlagsPredicate::isBaby)
				)
				.apply(instance, EntityFlagsPredicate::new)
	);

	static Optional<EntityFlagsPredicate> of(
		Optional<Boolean> optional, Optional<Boolean> optional2, Optional<Boolean> optional3, Optional<Boolean> optional4, Optional<Boolean> optional5
	) {
		return optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty() && optional4.isEmpty() && optional5.isEmpty()
			? Optional.empty()
			: Optional.of(new EntityFlagsPredicate(optional, optional2, optional3, optional4, optional5));
	}

	public boolean matches(Entity entity) {
		if (this.isOnFire.isPresent() && entity.isOnFire() != (Boolean)this.isOnFire.get()) {
			return false;
		} else if (this.isCrouching.isPresent() && entity.isCrouching() != (Boolean)this.isCrouching.get()) {
			return false;
		} else if (this.isSprinting.isPresent() && entity.isSprinting() != (Boolean)this.isSprinting.get()) {
			return false;
		} else if (this.isSwimming.isPresent() && entity.isSwimming() != (Boolean)this.isSwimming.get()) {
			return false;
		} else {
			if (this.isBaby.isPresent() && entity instanceof LivingEntity livingEntity && livingEntity.isBaby() != (Boolean)this.isBaby.get()) {
				return false;
			}

			return true;
		}
	}

	public static class Builder {
		private Optional<Boolean> isOnFire = Optional.empty();
		private Optional<Boolean> isCrouching = Optional.empty();
		private Optional<Boolean> isSprinting = Optional.empty();
		private Optional<Boolean> isSwimming = Optional.empty();
		private Optional<Boolean> isBaby = Optional.empty();

		public static EntityFlagsPredicate.Builder flags() {
			return new EntityFlagsPredicate.Builder();
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

		public EntityFlagsPredicate.Builder setIsBaby(Boolean boolean_) {
			this.isBaby = Optional.of(boolean_);
			return this;
		}

		public Optional<EntityFlagsPredicate> build() {
			return EntityFlagsPredicate.of(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
		}
	}
}
