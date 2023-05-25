package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
	public static <T extends Mob> Optional<T> trySpawnMob(
		EntityType<T> entityType, MobSpawnType mobSpawnType, ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k, SpawnUtil.Strategy strategy
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int l = 0; l < i; l++) {
			int m = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
			int n = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
			mutableBlockPos.setWithOffset(blockPos, m, k, n);
			if (serverLevel.getWorldBorder().isWithinBounds(mutableBlockPos) && moveToPossibleSpawnPosition(serverLevel, k, mutableBlockPos, strategy)) {
				T mob = (T)entityType.create(serverLevel, null, null, mutableBlockPos, mobSpawnType, false, false);
				if (mob != null) {
					if (mob.checkSpawnRules(serverLevel, mobSpawnType) && mob.checkSpawnObstruction(serverLevel)) {
						serverLevel.addFreshEntityWithPassengers(mob);
						return Optional.of(mob);
					}

					mob.discard();
				}
			}
		}

		return Optional.empty();
	}

	private static boolean moveToPossibleSpawnPosition(ServerLevel serverLevel, int i, BlockPos.MutableBlockPos mutableBlockPos, SpawnUtil.Strategy strategy) {
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos().set(mutableBlockPos);
		BlockState blockState = serverLevel.getBlockState(mutableBlockPos2);

		for (int j = i; j >= -i; j--) {
			mutableBlockPos.move(Direction.DOWN);
			mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.UP);
			BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
			if (strategy.canSpawnOn(serverLevel, mutableBlockPos, blockState2, mutableBlockPos2, blockState)) {
				mutableBlockPos.move(Direction.UP);
				return true;
			}

			blockState = blockState2;
		}

		return false;
	}

	public interface Strategy {
		@Deprecated
		SpawnUtil.Strategy LEGACY_IRON_GOLEM = (serverLevel, blockPos, blockState, blockPos2, blockState2) -> !blockState.is(Blocks.COBWEB)
					&& !blockState.is(Blocks.CACTUS)
					&& !blockState.is(Blocks.GLASS_PANE)
					&& !(blockState.getBlock() instanceof StainedGlassPaneBlock)
					&& !(blockState.getBlock() instanceof StainedGlassBlock)
					&& !(blockState.getBlock() instanceof LeavesBlock)
					&& !blockState.is(Blocks.CONDUIT)
					&& !blockState.is(Blocks.ICE)
					&& !blockState.is(Blocks.TNT)
					&& !blockState.is(Blocks.GLOWSTONE)
					&& !blockState.is(Blocks.BEACON)
					&& !blockState.is(Blocks.SEA_LANTERN)
					&& !blockState.is(Blocks.FROSTED_ICE)
					&& !blockState.is(Blocks.TINTED_GLASS)
					&& !blockState.is(Blocks.GLASS)
				? (blockState2.isAir() || blockState2.liquid()) && (blockState.isSolid() || blockState.is(Blocks.POWDER_SNOW))
				: false;
		SpawnUtil.Strategy ON_TOP_OF_COLLIDER = (serverLevel, blockPos, blockState, blockPos2, blockState2) -> blockState2.getCollisionShape(serverLevel, blockPos2)
					.isEmpty()
				&& Block.isFaceFull(blockState.getCollisionShape(serverLevel, blockPos), Direction.UP);

		boolean canSpawnOn(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2);
	}
}
