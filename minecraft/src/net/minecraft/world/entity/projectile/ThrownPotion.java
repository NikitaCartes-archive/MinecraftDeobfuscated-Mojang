package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EnvironmentInterfaces({@EnvironmentInterface(
		value = EnvType.CLIENT,
		itf = ItemSupplier.class
	)})
public class ThrownPotion extends ThrowableProjectile implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrownPotion.class, EntityDataSerializers.ITEM_STACK);
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Predicate<LivingEntity> WATER_SENSITIVE = ThrownPotion::isWaterSensitiveEntity;

	public ThrownPotion(EntityType<? extends ThrownPotion> entityType, Level level) {
		super(entityType, level);
	}

	public ThrownPotion(Level level, LivingEntity livingEntity) {
		super(EntityType.POTION, livingEntity, level);
	}

	public ThrownPotion(Level level, double d, double e, double f) {
		super(EntityType.POTION, d, e, f, level);
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemStack = this.getEntityData().get(DATA_ITEM_STACK);
		if (itemStack.getItem() != Items.SPLASH_POTION && itemStack.getItem() != Items.LINGERING_POTION) {
			if (this.level != null) {
				LOGGER.error("ThrownPotion entity {} has no item?!", this.getId());
			}

			return new ItemStack(Items.SPLASH_POTION);
		} else {
			return itemStack;
		}
	}

	public void setItem(ItemStack itemStack) {
		this.getEntityData().set(DATA_ITEM_STACK, itemStack.copy());
	}

	@Override
	protected float getGravity() {
		return 0.05F;
	}

	@Override
	protected void onHit(HitResult hitResult) {
		if (!this.level.isClientSide) {
			ItemStack itemStack = this.getItem();
			Potion potion = PotionUtils.getPotion(itemStack);
			List<MobEffectInstance> list = PotionUtils.getMobEffects(itemStack);
			boolean bl = potion == Potions.WATER && list.isEmpty();
			if (hitResult.getType() == HitResult.Type.BLOCK && bl) {
				BlockHitResult blockHitResult = (BlockHitResult)hitResult;
				Direction direction = blockHitResult.getDirection();
				BlockPos blockPos = blockHitResult.getBlockPos().relative(direction);
				this.dowseFire(blockPos, direction);
				this.dowseFire(blockPos.relative(direction.getOpposite()), direction);

				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					this.dowseFire(blockPos.relative(direction2), direction2);
				}
			}

			if (bl) {
				this.applyWater();
			} else if (!list.isEmpty()) {
				if (this.isLingering()) {
					this.makeAreaOfEffectCloud(itemStack, potion);
				} else {
					this.applySplash(list, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitResult).getEntity() : null);
				}
			}

			int i = potion.hasInstantEffects() ? 2007 : 2002;
			this.level.levelEvent(i, new BlockPos(this), PotionUtils.getColor(itemStack));
			this.remove();
		}
	}

	private void applyWater() {
		AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
		List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, aABB, WATER_SENSITIVE);
		if (!list.isEmpty()) {
			for (LivingEntity livingEntity : list) {
				double d = this.distanceToSqr(livingEntity);
				if (d < 16.0 && isWaterSensitiveEntity(livingEntity)) {
					livingEntity.hurt(DamageSource.indirectMagic(livingEntity, this.getOwner()), 1.0F);
				}
			}
		}
	}

	private void applySplash(List<MobEffectInstance> list, @Nullable Entity entity) {
		AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
		List<LivingEntity> list2 = this.level.getEntitiesOfClass(LivingEntity.class, aABB);
		if (!list2.isEmpty()) {
			for (LivingEntity livingEntity : list2) {
				if (livingEntity.isAffectedByPotions()) {
					double d = this.distanceToSqr(livingEntity);
					if (d < 16.0) {
						double e = 1.0 - Math.sqrt(d) / 4.0;
						if (livingEntity == entity) {
							e = 1.0;
						}

						for (MobEffectInstance mobEffectInstance : list) {
							MobEffect mobEffect = mobEffectInstance.getEffect();
							if (mobEffect.isInstantenous()) {
								mobEffect.applyInstantenousEffect(this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), e);
							} else {
								int i = (int)(e * (double)mobEffectInstance.getDuration() + 0.5);
								if (i > 20) {
									livingEntity.addEffect(
										new MobEffectInstance(mobEffect, i, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible())
									);
								}
							}
						}
					}
				}
			}
		}
	}

	private void makeAreaOfEffectCloud(ItemStack itemStack, Potion potion) {
		AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level, this.x, this.y, this.z);
		areaEffectCloud.setOwner(this.getOwner());
		areaEffectCloud.setRadius(3.0F);
		areaEffectCloud.setRadiusOnUse(-0.5F);
		areaEffectCloud.setWaitTime(10);
		areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration());
		areaEffectCloud.setPotion(potion);

		for (MobEffectInstance mobEffectInstance : PotionUtils.getCustomEffects(itemStack)) {
			areaEffectCloud.addEffect(new MobEffectInstance(mobEffectInstance));
		}

		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("CustomPotionColor", 99)) {
			areaEffectCloud.setFixedColor(compoundTag.getInt("CustomPotionColor"));
		}

		this.level.addFreshEntity(areaEffectCloud);
	}

	private boolean isLingering() {
		return this.getItem().getItem() == Items.LINGERING_POTION;
	}

	private void dowseFire(BlockPos blockPos, Direction direction) {
		BlockState blockState = this.level.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block == Blocks.FIRE) {
			this.level.extinguishFire(null, blockPos.relative(direction), direction.getOpposite());
		} else if (block == Blocks.CAMPFIRE && (Boolean)blockState.getValue(CampfireBlock.LIT)) {
			this.level.levelEvent(null, 1009, blockPos, 0);
			this.level.setBlockAndUpdate(blockPos, blockState.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		ItemStack itemStack = ItemStack.of(compoundTag.getCompound("Potion"));
		if (itemStack.isEmpty()) {
			this.remove();
		} else {
			this.setItem(itemStack);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		ItemStack itemStack = this.getItem();
		if (!itemStack.isEmpty()) {
			compoundTag.put("Potion", itemStack.save(new CompoundTag()));
		}
	}

	private static boolean isWaterSensitiveEntity(LivingEntity livingEntity) {
		return livingEntity instanceof EnderMan || livingEntity instanceof Blaze;
	}
}
