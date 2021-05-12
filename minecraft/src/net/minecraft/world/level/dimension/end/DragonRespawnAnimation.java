package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;

public enum DragonRespawnAnimation {
	START {
		@Override
		public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
			BlockPos blockPos2 = new BlockPos(0, 128, 0);

			for (EndCrystal endCrystal : list) {
				endCrystal.setBeamTarget(blockPos2);
			}

			endDragonFight.setRespawnStage(PREPARING_TO_SUMMON_PILLARS);
		}
	},
	PREPARING_TO_SUMMON_PILLARS {
		@Override
		public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
			if (i < 100) {
				if (i == 0 || i == 50 || i == 51 || i == 52 || i >= 95) {
					serverLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
				}
			} else {
				endDragonFight.setRespawnStage(SUMMONING_PILLARS);
			}
		}
	},
	SUMMONING_PILLARS {
		@Override
		public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
			int j = 40;
			boolean bl = i % 40 == 0;
			boolean bl2 = i % 40 == 39;
			if (bl || bl2) {
				List<SpikeFeature.EndSpike> list2 = SpikeFeature.getSpikesForLevel(serverLevel);
				int k = i / 40;
				if (k < list2.size()) {
					SpikeFeature.EndSpike endSpike = (SpikeFeature.EndSpike)list2.get(k);
					if (bl) {
						for (EndCrystal endCrystal : list) {
							endCrystal.setBeamTarget(new BlockPos(endSpike.getCenterX(), endSpike.getHeight() + 1, endSpike.getCenterZ()));
						}
					} else {
						int l = 10;

						for (BlockPos blockPos2 : BlockPos.betweenClosed(
							new BlockPos(endSpike.getCenterX() - 10, endSpike.getHeight() - 10, endSpike.getCenterZ() - 10),
							new BlockPos(endSpike.getCenterX() + 10, endSpike.getHeight() + 10, endSpike.getCenterZ() + 10)
						)) {
							serverLevel.removeBlock(blockPos2, false);
						}

						serverLevel.explode(
							null,
							(double)((float)endSpike.getCenterX() + 0.5F),
							(double)endSpike.getHeight(),
							(double)((float)endSpike.getCenterZ() + 0.5F),
							5.0F,
							Explosion.BlockInteraction.DESTROY
						);
						SpikeConfiguration spikeConfiguration = new SpikeConfiguration(true, ImmutableList.of(endSpike), new BlockPos(0, 128, 0));
						Feature.END_SPIKE
							.configured(spikeConfiguration)
							.place(serverLevel, serverLevel.getChunkSource().getGenerator(), new Random(), new BlockPos(endSpike.getCenterX(), 45, endSpike.getCenterZ()));
					}
				} else if (bl) {
					endDragonFight.setRespawnStage(SUMMONING_DRAGON);
				}
			}
		}
	},
	SUMMONING_DRAGON {
		@Override
		public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
			if (i >= 100) {
				endDragonFight.setRespawnStage(END);
				endDragonFight.resetSpikeCrystals();

				for (EndCrystal endCrystal : list) {
					endCrystal.setBeamTarget(null);
					serverLevel.explode(endCrystal, endCrystal.getX(), endCrystal.getY(), endCrystal.getZ(), 6.0F, Explosion.BlockInteraction.NONE);
					endCrystal.discard();
				}
			} else if (i >= 80) {
				serverLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
			} else if (i == 0) {
				for (EndCrystal endCrystal : list) {
					endCrystal.setBeamTarget(new BlockPos(0, 128, 0));
				}
			} else if (i < 5) {
				serverLevel.levelEvent(3001, new BlockPos(0, 128, 0), 0);
			}
		}
	},
	END {
		@Override
		public void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos) {
		}
	};

	public abstract void tick(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos);
}
