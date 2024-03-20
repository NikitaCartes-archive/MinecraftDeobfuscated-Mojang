package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

public abstract class Animal extends AgeableMob {
	protected static final int PARENT_AGE_AFTER_BREEDING = 6000;
	private int inLove;
	@Nullable
	private UUID loveCause;

	protected Animal(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
	}

	@Override
	protected void customServerAiStep() {
		if (this.getAge() != 0) {
			this.inLove = 0;
		}

		super.customServerAiStep();
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.getAge() != 0) {
			this.inLove = 0;
		}

		if (this.inLove > 0) {
			this.inLove--;
			if (this.inLove % 10 == 0) {
				double d = this.random.nextGaussian() * 0.02;
				double e = this.random.nextGaussian() * 0.02;
				double f = this.random.nextGaussian() * 0.02;
				this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
			}
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.inLove = 0;
			return super.hurt(damageSource, f);
		}
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return levelReader.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : levelReader.getPathfindingCostFromLightLevels(blockPos);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("InLove", this.inLove);
		if (this.loveCause != null) {
			compoundTag.putUUID("LoveCause", this.loveCause);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.inLove = compoundTag.getInt("InLove");
		this.loveCause = compoundTag.hasUUID("LoveCause") ? compoundTag.getUUID("LoveCause") : null;
	}

	public static boolean checkAnimalSpawnRules(
		EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		boolean bl = MobSpawnType.ignoresLightRequirements(mobSpawnType) || isBrightEnoughToSpawn(levelAccessor, blockPos);
		return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && bl;
	}

	protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		return blockAndTintGetter.getRawBrightness(blockPos, 0) > 8;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 120;
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return false;
	}

	@Override
	public int getExperienceReward() {
		return 1 + this.level().random.nextInt(3);
	}

	public abstract boolean isFood(ItemStack itemStack);

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (this.isFood(itemStack)) {
			int i = this.getAge();
			if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
				this.usePlayerItem(player, interactionHand, itemStack);
				this.setInLove(player);
				return InteractionResult.SUCCESS;
			}

			if (this.isBaby()) {
				this.usePlayerItem(player, interactionHand, itemStack);
				this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}

			if (this.level().isClientSide) {
				return InteractionResult.CONSUME;
			}
		}

		return super.mobInteract(player, interactionHand);
	}

	protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
		itemStack.consume(1, player);
	}

	public boolean canFallInLove() {
		return this.inLove <= 0;
	}

	public void setInLove(@Nullable Player player) {
		this.inLove = 600;
		if (player != null) {
			this.loveCause = player.getUUID();
		}

		this.level().broadcastEntityEvent(this, (byte)18);
	}

	public void setInLoveTime(int i) {
		this.inLove = i;
	}

	public int getInLoveTime() {
		return this.inLove;
	}

	@Nullable
	public ServerPlayer getLoveCause() {
		if (this.loveCause == null) {
			return null;
		} else {
			Player player = this.level().getPlayerByUUID(this.loveCause);
			return player instanceof ServerPlayer ? (ServerPlayer)player : null;
		}
	}

	public boolean isInLove() {
		return this.inLove > 0;
	}

	public void resetLove() {
		this.inLove = 0;
	}

	public boolean canMate(Animal animal) {
		if (animal == this) {
			return false;
		} else {
			return animal.getClass() != this.getClass() ? false : this.isInLove() && animal.isInLove();
		}
	}

	public void spawnChildFromBreeding(ServerLevel serverLevel, Animal animal) {
		AgeableMob ageableMob = this.getBreedOffspring(serverLevel, animal);
		if (ageableMob != null) {
			ageableMob.setBaby(true);
			ageableMob.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
			this.finalizeSpawnChildFromBreeding(serverLevel, animal, ageableMob);
			serverLevel.addFreshEntityWithPassengers(ageableMob);
		}
	}

	public void finalizeSpawnChildFromBreeding(ServerLevel serverLevel, Animal animal, @Nullable AgeableMob ageableMob) {
		Optional.ofNullable(this.getLoveCause()).or(() -> Optional.ofNullable(animal.getLoveCause())).ifPresent(serverPlayer -> {
			serverPlayer.awardStat(Stats.ANIMALS_BRED);
			CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayer, this, animal, ageableMob);
		});
		this.setAge(6000);
		animal.setAge(6000);
		this.resetLove();
		animal.resetLove();
		serverLevel.broadcastEntityEvent(this, (byte)18);
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 18) {
			for (int i = 0; i < 7; i++) {
				double d = this.random.nextGaussian() * 0.02;
				double e = this.random.nextGaussian() * 0.02;
				double f = this.random.nextGaussian() * 0.02;
				this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
			}
		} else {
			super.handleEntityEvent(b);
		}
	}
}
