package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
			RandomSource randomSource = serverLevel.random;
			this.nextTick--;
			if (this.nextTick > 0) {
				return 0;
			} else {
				this.nextTick = this.nextTick + 12000 + randomSource.nextInt(1200);
				long l = serverLevel.getDayTime() / 24000L;
				if (l < 5L || !serverLevel.isDay()) {
					return 0;
				} else if (randomSource.nextInt(5) != 0) {
					return 0;
				} else {
					int i = serverLevel.players().size();
					if (i < 1) {
						return 0;
					} else {
						Player player = (Player)serverLevel.players().get(randomSource.nextInt(i));
						if (player.isSpectator()) {
							return 0;
						} else if (serverLevel.isCloseToVillage(player.blockPosition(), 2)) {
							return 0;
						} else {
							int j = (24 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
							int k = (24 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
							BlockPos.MutableBlockPos mutableBlockPos = player.blockPosition().mutable().move(j, 0, k);
							int m = 10;
							if (!serverLevel.hasChunksAt(mutableBlockPos.getX() - 10, mutableBlockPos.getZ() - 10, mutableBlockPos.getX() + 10, mutableBlockPos.getZ() + 10)) {
								return 0;
							} else {
								Holder<Biome> holder = serverLevel.getBiome(mutableBlockPos);
								if (holder.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
									return 0;
								} else {
									int n = 0;
									int o = (int)Math.ceil((double)serverLevel.getCurrentDifficultyAt(mutableBlockPos).getEffectiveDifficulty()) + 1;

									for (int p = 0; p < o; p++) {
										n++;
										mutableBlockPos.setY(serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY());
										if (p == 0) {
											if (!this.spawnPatrolMember(serverLevel, mutableBlockPos, randomSource, true)) {
												break;
											}
										} else {
											this.spawnPatrolMember(serverLevel, mutableBlockPos, randomSource, false);
										}

										mutableBlockPos.setX(mutableBlockPos.getX() + randomSource.nextInt(5) - randomSource.nextInt(5));
										mutableBlockPos.setZ(mutableBlockPos.getZ() + randomSource.nextInt(5) - randomSource.nextInt(5));
									}

									return n;
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean spawnPatrolMember(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, boolean bl) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		if (!NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos, blockState, blockState.getFluidState(), EntityType.PILLAGER)) {
			return false;
		} else if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, serverLevel, EntitySpawnReason.PATROL, blockPos, randomSource)) {
			return false;
		} else {
			PatrollingMonster patrollingMonster = EntityType.PILLAGER.create(serverLevel, EntitySpawnReason.PATROL);
			if (patrollingMonster != null) {
				if (bl) {
					patrollingMonster.setPatrolLeader(true);
					patrollingMonster.findPatrolTarget();
				}

				patrollingMonster.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
				patrollingMonster.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), EntitySpawnReason.PATROL, null);
				serverLevel.addFreshEntityWithPassengers(patrollingMonster);
				return true;
			} else {
				return false;
			}
		}
	}
}
