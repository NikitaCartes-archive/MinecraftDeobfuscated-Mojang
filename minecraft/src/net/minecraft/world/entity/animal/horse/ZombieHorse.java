package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class ZombieHorse extends AbstractHorse {
	public ZombieHorse(EntityType<? extends ZombieHorse> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	public static boolean checkZombieHorseSpawnRules(
		EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return !MobSpawnType.isSpawner(mobSpawnType)
			? Animal.checkAnimalSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource)
			: MobSpawnType.ignoresLightRequirements(mobSpawnType) || isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	@Override
	protected void randomizeAttributes(RandomSource randomSource) {
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength(randomSource::nextDouble));
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ZOMBIE_HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIE_HORSE_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ZOMBIE_HORSE_HURT;
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.ZOMBIE_HORSE.create(serverLevel);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		return !this.isTamed() ? InteractionResult.PASS : super.mobInteract(player, interactionHand);
	}

	@Override
	protected void addBehaviourGoals() {
	}

	@Override
	protected float getPassengersRidingOffsetY(EntityDimensions entityDimensions, float f) {
		return entityDimensions.height - (this.isBaby() ? 0.03125F : 0.28125F) * f;
	}
}
