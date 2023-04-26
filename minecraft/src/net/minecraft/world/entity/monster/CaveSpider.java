package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class CaveSpider extends Spider {
	public CaveSpider(EntityType<? extends CaveSpider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createCaveSpider() {
		return Spider.createAttributes().add(Attributes.MAX_HEALTH, 12.0);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		if (super.doHurtTarget(entity)) {
			if (entity instanceof LivingEntity) {
				int i = 0;
				if (this.level().getDifficulty() == Difficulty.NORMAL) {
					i = 7;
				} else if (this.level().getDifficulty() == Difficulty.HARD) {
					i = 15;
				}

				if (i > 0) {
					((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), this);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		return spawnGroupData;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.45F;
	}
}
