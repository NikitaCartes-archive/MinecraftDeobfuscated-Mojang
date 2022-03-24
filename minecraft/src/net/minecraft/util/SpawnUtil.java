package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
	public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> entityType, ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int l = 0; l < i; l++) {
			int m = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
			int n = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
			if (moveToPossibleSpawnPosition(serverLevel, k, mutableBlockPos.setWithOffset(blockPos, m, k, n))) {
				T mob = (T)entityType.create(serverLevel, null, null, null, mutableBlockPos, MobSpawnType.MOB_SUMMONED, false, false);
				if (mob != null) {
					if (mob.checkSpawnRules(serverLevel, MobSpawnType.MOB_SUMMONED) && mob.checkSpawnObstruction(serverLevel)) {
						serverLevel.addFreshEntityWithPassengers(mob);
						return Optional.of(mob);
					}

					mob.discard();
				}
			}
		}

		return Optional.empty();
	}

	private static boolean moveToPossibleSpawnPosition(ServerLevel serverLevel, int i, BlockPos.MutableBlockPos mutableBlockPos) {
		BlockState blockState = serverLevel.getBlockState(mutableBlockPos);

		for (int j = i; j >= -i; j--) {
			mutableBlockPos.move(Direction.DOWN);
			BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
			if ((blockState.isAir() || blockState.getMaterial().isLiquid()) && blockState2.getMaterial().isSolidBlocking()) {
				mutableBlockPos.move(Direction.UP);
				return true;
			}

			blockState = blockState2;
		}

		return false;
	}
}
