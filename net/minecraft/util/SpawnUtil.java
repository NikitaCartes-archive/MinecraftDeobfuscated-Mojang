/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
    public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> entityType, MobSpawnType mobSpawnType, ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k, Strategy strategy) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int l = 0; l < i; ++l) {
            Mob mob;
            int m = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
            int n = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
            mutableBlockPos.setWithOffset(blockPos, m, k, n);
            if (!serverLevel.getWorldBorder().isWithinBounds(mutableBlockPos) || !SpawnUtil.moveToPossibleSpawnPosition(serverLevel, k, mutableBlockPos, strategy) || (mob = (Mob)entityType.create(serverLevel, null, null, null, mutableBlockPos, mobSpawnType, false, false)) == null) continue;
            if (mob.checkSpawnRules(serverLevel, mobSpawnType) && mob.checkSpawnObstruction(serverLevel)) {
                serverLevel.addFreshEntityWithPassengers(mob);
                return Optional.of(mob);
            }
            mob.discard();
        }
        return Optional.empty();
    }

    private static boolean moveToPossibleSpawnPosition(ServerLevel serverLevel, int i, BlockPos.MutableBlockPos mutableBlockPos, Strategy strategy) {
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos().set(mutableBlockPos);
        BlockState blockState = serverLevel.getBlockState(mutableBlockPos2);
        for (int j = i; j >= -i; --j) {
            mutableBlockPos.move(Direction.DOWN);
            mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, Direction.UP);
            BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
            if (strategy.canSpawnOn(serverLevel, mutableBlockPos, blockState2, mutableBlockPos2, blockState)) {
                mutableBlockPos.move(Direction.UP);
                return true;
            }
            blockState = blockState2;
        }
        return false;
    }

    public static interface Strategy {
        public static final Strategy LEGACY_IRON_GOLEM = (serverLevel, blockPos, blockState, blockPos2, blockState2) -> (blockState2.isAir() || blockState2.getMaterial().isLiquid()) && blockState.getMaterial().isSolidBlocking();
        public static final Strategy ON_TOP_OF_COLLIDER = (serverLevel, blockPos, blockState, blockPos2, blockState2) -> blockState2.getCollisionShape(serverLevel, blockPos2).isEmpty() && Block.isFaceFull(blockState.getCollisionShape(serverLevel, blockPos), Direction.UP);

        public boolean canSpawnOn(ServerLevel var1, BlockPos var2, BlockState var3, BlockPos var4, BlockState var5);
    }
}

