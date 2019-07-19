package net.minecraft.world.entity.monster;

import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;

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
		float f = this.getBrightness();
		if (f > 0.5F) {
			this.noActionTime += 2;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level.isClientSide && this.level.getDifficulty() == Difficulty.PEACEFUL) {
			this.remove();
		}
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
	public boolean hurt(DamageSource damageSource, float f) {
		return this.isInvulnerableTo(damageSource) ? false : super.hurt(damageSource, f);
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
	protected SoundEvent getFallDamageSound(int i) {
		return i > 4 ? SoundEvents.HOSTILE_BIG_FALL : SoundEvents.HOSTILE_SMALL_FALL;
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return 0.5F - levelReader.getBrightness(blockPos);
	}

	public static boolean isDarkEnoughToSpawn(LevelAccessor levelAccessor, BlockPos blockPos, Random random) {
		if (levelAccessor.getBrightness(LightLayer.SKY, blockPos) > random.nextInt(32)) {
			return false;
		} else {
			int i = levelAccessor.getLevel().isThundering() ? levelAccessor.getMaxLocalRawBrightness(blockPos, 10) : levelAccessor.getMaxLocalRawBrightness(blockPos);
			return i <= random.nextInt(8);
		}
	}

	public static boolean checkMonsterSpawnRules(
		EntityType<? extends Monster> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getDifficulty() != Difficulty.PEACEFUL
			&& isDarkEnoughToSpawn(levelAccessor, blockPos, random)
			&& checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
	}

	public static boolean checkAnyLightMonsterSpawnRules(
		EntityType<? extends Monster> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
	}

	@Override
	protected boolean shouldDropExperience() {
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
