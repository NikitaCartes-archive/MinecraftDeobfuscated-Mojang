package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class MoonCow extends Cow {
	public MoonCow(EntityType<? extends MoonCow> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new MoonCow.MoonWalkControl(this);
	}

	public static boolean checkMoonCowSpawnRules(
		EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getBlockState(blockPos.below()).is(Blocks.CHEESE);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return levelReader.getBlockState(blockPos.below()).is(Blocks.CHEESE) ? 10.0F : super.getWalkTargetValue(blockPos, levelReader);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.95F;
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.GLASS));
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Nullable
	@Override
	public Cow getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Cow cow = (Cow)this.getType().create(serverLevel);
		cow.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.GLASS));
		return cow;
	}

	static class MoonWalkControl extends MoveControl {
		public MoonWalkControl(Mob mob) {
			super(mob);
		}

		@Override
		public void tick() {
			MoveControl.Operation operation = this.operation;
			super.tick();
			if (operation == MoveControl.Operation.MOVE_TO || operation == MoveControl.Operation.JUMPING) {
				this.mob.setZza(-this.mob.getSpeed());
			}
		}

		@Override
		protected float targetYRot(double d, double e) {
			return super.targetYRot(d, e) - 180.0F;
		}
	}
}
