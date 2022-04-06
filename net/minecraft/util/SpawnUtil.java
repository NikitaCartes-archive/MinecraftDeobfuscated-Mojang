/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
    public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> entityType, MobSpawnType mobSpawnType, ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int l = 0; l < i; ++l) {
            Mob mob;
            int n;
            int m = Mth.randomBetweenInclusive(serverLevel.random, -j, j);
            if (!SpawnUtil.moveToPossibleSpawnPosition(serverLevel, k, mutableBlockPos.setWithOffset(blockPos, m, k, n = Mth.randomBetweenInclusive(serverLevel.random, -j, j))) || (mob = (Mob)entityType.create(serverLevel, null, null, null, mutableBlockPos, mobSpawnType, false, false)) == null) continue;
            if (mob.checkSpawnRules(serverLevel, mobSpawnType) && mob.checkSpawnObstruction(serverLevel)) {
                serverLevel.addFreshEntityWithPassengers(mob);
                return Optional.of(mob);
            }
            mob.discard();
        }
        return Optional.empty();
    }

    private static boolean moveToPossibleSpawnPosition(ServerLevel serverLevel, int i, BlockPos.MutableBlockPos mutableBlockPos) {
        BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
        for (int j = i; j >= -i; --j) {
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

