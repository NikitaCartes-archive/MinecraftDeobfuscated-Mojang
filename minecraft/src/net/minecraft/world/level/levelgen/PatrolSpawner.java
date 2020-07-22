package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PatrolSpawner implements CustomSpawner {
	private int nextTick;

	@Override
	public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
		if (!bl) {
			return 0;
		} else if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
			return 0;
		} else {
			Random random = serverLevel.random;
			this.nextTick--;
			if (this.nextTick > 0) {
				return 0;
			} else {
				this.nextTick = this.nextTick + 12000 + random.nextInt(1200);
				long l = serverLevel.getDayTime() / 24000L;
				if (l < 5L || !serverLevel.isDay()) {
					return 0;
				} else if (random.nextInt(5) != 0) {
					return 0;
				} else {
					int i = serverLevel.players().size();
					if (i < 1) {
						return 0;
					} else {
						Player player = (Player)serverLevel.players().get(random.nextInt(i));
						if (player.isSpectator()) {
							return 0;
						} else if (serverLevel.isCloseToVillage(player.blockPosition(), 2)) {
							return 0;
						} else {
							int j = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
							int k = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
							BlockPos.MutableBlockPos mutableBlockPos = player.blockPosition().mutable().move(j, 0, k);
							if (!serverLevel.hasChunksAt(
								mutableBlockPos.getX() - 10,
								mutableBlockPos.getY() - 10,
								mutableBlockPos.getZ() - 10,
								mutableBlockPos.getX() + 10,
								mutableBlockPos.getY() + 10,
								mutableBlockPos.getZ() + 10
							)) {
								return 0;
							} else {
								Biome biome = serverLevel.getBiome(mutableBlockPos);
								Biome.BiomeCategory biomeCategory = biome.getBiomeCategory();
								if (biomeCategory == Biome.BiomeCategory.MUSHROOM) {
									return 0;
								} else {
									int m = 0;
									int n = (int)Math.ceil((double)serverLevel.getCurrentDifficultyAt(mutableBlockPos).getEffectiveDifficulty()) + 1;

									for (int o = 0; o < n; o++) {
										m++;
										mutableBlockPos.setY(serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY());
										if (o == 0) {
											if (!this.spawnPatrolMember(serverLevel, mutableBlockPos, random, true)) {
												break;
											}
										} else {
											this.spawnPatrolMember(serverLevel, mutableBlockPos, random, false);
										}

										mutableBlockPos.setX(mutableBlockPos.getX() + random.nextInt(5) - random.nextInt(5));
										mutableBlockPos.setZ(mutableBlockPos.getZ() + random.nextInt(5) - random.nextInt(5));
									}

									return m;
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean spawnPatrolMember(ServerLevel serverLevel, BlockPos blockPos, Random random, boolean bl) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		if (!NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos, blockState, blockState.getFluidState(), EntityType.PILLAGER)) {
			return false;
		} else if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, serverLevel, MobSpawnType.PATROL, blockPos, random)) {
			return false;
		} else {
			PatrollingMonster patrollingMonster = EntityType.PILLAGER.create(serverLevel);
			if (patrollingMonster != null) {
				if (bl) {
					patrollingMonster.setPatrolLeader(true);
					patrollingMonster.findPatrolTarget();
				}

				patrollingMonster.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
				patrollingMonster.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), MobSpawnType.PATROL, null, null);
				serverLevel.addFreshEntityWithPassengers(patrollingMonster);
				return true;
			} else {
				return false;
			}
		}
	}
}
