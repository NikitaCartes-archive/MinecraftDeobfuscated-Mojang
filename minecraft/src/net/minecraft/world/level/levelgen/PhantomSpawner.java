package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class PhantomSpawner implements CustomSpawner {
	private int nextTick;

	@Override
	public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
		if (!bl) {
			return 0;
		} else if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
			return 0;
		} else {
			Random random = serverLevel.random;
			this.nextTick--;
			if (this.nextTick > 0) {
				return 0;
			} else {
				this.nextTick = this.nextTick + (60 + random.nextInt(60)) * 20;
				if (serverLevel.getSkyDarken() < 5 && serverLevel.dimensionType().hasSkyLight()) {
					return 0;
				} else {
					int i = 0;

					for (Player player : serverLevel.players()) {
						if (!player.isSpectator()) {
							BlockPos blockPos = player.blockPosition();
							if (!serverLevel.dimensionType().hasSkyLight() || blockPos.getY() >= serverLevel.getSeaLevel() && serverLevel.canSeeSky(blockPos)) {
								DifficultyInstance difficultyInstance = serverLevel.getCurrentDifficultyAt(blockPos);
								if (difficultyInstance.isHarderThan(random.nextFloat() * 3.0F)) {
									ServerStatsCounter serverStatsCounter = ((ServerPlayer)player).getStats();
									int j = Mth.clamp(serverStatsCounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
									int k = 24000;
									if (random.nextInt(j) >= 72000) {
										BlockPos blockPos2 = blockPos.above(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
										BlockState blockState = serverLevel.getBlockState(blockPos2);
										FluidState fluidState = serverLevel.getFluidState(blockPos2);
										if (NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos2, blockState, fluidState, EntityType.PHANTOM)) {
											SpawnGroupData spawnGroupData = null;
											int l = 1 + random.nextInt(difficultyInstance.getDifficulty().getId() + 1);

											for (int m = 0; m < l; m++) {
												Phantom phantom = EntityType.PHANTOM.create(serverLevel);
												phantom.moveTo(blockPos2, 0.0F, 0.0F);
												spawnGroupData = phantom.finalizeSpawn(serverLevel, difficultyInstance, MobSpawnType.NATURAL, spawnGroupData, null);
												serverLevel.addFreshEntityWithPassengers(phantom);
											}

											i += l;
										}
									}
								}
							}
						}
					}

					return i;
				}
			}
		}
	}
}
