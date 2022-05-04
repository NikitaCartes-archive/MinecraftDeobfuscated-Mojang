package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class Monster extends PathfinderMob implements Enemy {
	protected Monster(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	public void aiStep() {
		this.updateSwingTime();
		this.updateNoActionTime();
		super.aiStep();
	}

	protected void updateNoActionTime() {
		float f = this.getLightLevelDependentMagicValue();
		if (f > 0.5F) {
			this.noActionTime += 2;
		}
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.HOSTILE_SPLASH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.HOSTILE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.HOSTILE_DEATH;
	}

	@Override
	public LivingEntity.Fallsounds getFallSounds() {
		return new LivingEntity.Fallsounds(SoundEvents.HOSTILE_SMALL_FALL, SoundEvents.HOSTILE_BIG_FALL);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return -levelReader.getPathfindingCostFromLightLevels(blockPos);
	}

	public static boolean isDarkEnoughToSpawn(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevelAccessor.getBrightness(LightLayer.SKY, blockPos) > randomSource.nextInt(32)) {
			return false;
		} else if (serverLevelAccessor.getBrightness(LightLayer.BLOCK, blockPos) > 0) {
			return false;
		} else {
			int i = serverLevelAccessor.getLevel().isThundering()
				? serverLevelAccessor.getMaxLocalRawBrightness(blockPos, 10)
				: serverLevelAccessor.getMaxLocalRawBrightness(blockPos);
			return i <= randomSource.nextInt(8);
		}
	}

	public static boolean checkMonsterSpawnRules(
		EntityType<? extends Monster> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return serverLevelAccessor.getDifficulty() != Difficulty.PEACEFUL
			&& isDarkEnoughToSpawn(serverLevelAccessor, blockPos, randomSource)
			&& checkMobSpawnRules(entityType, serverLevelAccessor, mobSpawnType, blockPos, randomSource);
	}

	public static boolean checkAnyLightMonsterSpawnRules(
		EntityType<? extends Monster> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
	}

	public static AttributeSupplier.Builder createMonsterAttributes() {
		return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
	}

	@Override
	public boolean shouldDropExperience() {
		return true;
	}

	@Override
	protected boolean shouldDropLoot() {
		return true;
	}

	public boolean isPreventingPlayerRest(Player player) {
		return true;
	}

	@Override
	public ItemStack getProjectile(ItemStack itemStack) {
		if (itemStack.getItem() instanceof ProjectileWeaponItem) {
			Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemStack.getItem()).getSupportedHeldProjectiles();
			ItemStack itemStack2 = ProjectileWeaponItem.getHeldProjectile(this, predicate);
			return itemStack2.isEmpty() ? new ItemStack(Items.ARROW) : itemStack2;
		} else {
			return ItemStack.EMPTY;
		}
	}
}
