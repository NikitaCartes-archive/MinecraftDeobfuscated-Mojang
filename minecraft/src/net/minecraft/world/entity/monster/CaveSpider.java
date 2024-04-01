package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class CaveSpider extends Spider {
	public CaveSpider(EntityType<? extends CaveSpider> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean hasPotatoVariant() {
		return false;
	}

	public static AttributeSupplier.Builder createCaveSpider() {
		return Spider.createAttributes().add(Attributes.MAX_HEALTH, 12.0);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		if (super.doHurtTarget(entity)) {
			poisonMethodThatSpidersUse(entity, this);
			return true;
		} else {
			return false;
		}
	}

	public static void poisonMethodThatSpidersUse(Entity entity, @Nullable Entity entity2) {
		if (entity instanceof LivingEntity livingEntity) {
			int i = 0;
			if (entity.level().getDifficulty() == Difficulty.NORMAL) {
				i = 7;
			} else if (entity.level().getDifficulty() == Difficulty.HARD) {
				i = 15;
			}

			if (i > 0) {
				livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), entity2);
			}
		}
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		return spawnGroupData;
	}

	@Override
	public Vec3 getVehicleAttachmentPoint(Entity entity) {
		return entity.getBbWidth() <= this.getBbWidth() ? new Vec3(0.0, 0.21875 * (double)this.getScale(), 0.0) : super.getVehicleAttachmentPoint(entity);
	}
}
