package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class WaterAnimal extends PathfinderMob {
	protected WaterAnimal(EntityType<? extends WaterAnimal> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public MobType getMobType() {
		return MobType.WATER;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.isUnobstructed(this);
	}

	@Override
	public int getAmbientSoundInterval() {
		return 120;
	}

	@Override
	protected int getExperienceReward(Player player) {
		return 1 + this.level.random.nextInt(3);
	}

	protected void handleAirSupply(int i) {
		if (this.isAlive() && !this.isInWaterOrBubble()) {
			this.setAirSupply(i - 1);
			if (this.getAirSupply() == -20) {
				this.setAirSupply(0);
				this.hurt(DamageSource.DROWN, 2.0F);
			}
		} else {
			this.setAirSupply(300);
		}
	}

	@Override
	public void baseTick() {
		int i = this.getAirSupply();
		super.baseTick();
		this.handleAirSupply(i);
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return false;
	}

	public static boolean checkUndergroundWaterCreatureSpawnRules(
		EntityType<? extends LivingEntity> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return blockPos.getY() < serverLevelAccessor.getSeaLevel()
			&& blockPos.getY() < serverLevelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ())
			&& isDarkEnoughToSpawn(serverLevelAccessor, blockPos)
			&& isBaseStoneBelow(blockPos, serverLevelAccessor);
	}

	public static boolean isBaseStoneBelow(BlockPos blockPos, ServerLevelAccessor serverLevelAccessor) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 0; i < 5; i++) {
			mutableBlockPos.move(Direction.DOWN);
			BlockState blockState = serverLevelAccessor.getBlockState(mutableBlockPos);
			if (blockState.is(BlockTags.BASE_STONE_OVERWORLD)) {
				return true;
			}

			if (!blockState.is(Blocks.WATER)) {
				return false;
			}
		}

		return false;
	}

	public static boolean isDarkEnoughToSpawn(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos) {
		int i = serverLevelAccessor.getLevel().isThundering()
			? serverLevelAccessor.getMaxLocalRawBrightness(blockPos, 10)
			: serverLevelAccessor.getMaxLocalRawBrightness(blockPos);
		return i == 0;
	}
}
