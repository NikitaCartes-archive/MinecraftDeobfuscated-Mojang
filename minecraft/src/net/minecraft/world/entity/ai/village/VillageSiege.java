package net.minecraft.world.entity.ai.village;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class VillageSiege {
	private boolean hasSetupSiege;
	private VillageSiege.State siegeState = VillageSiege.State.SIEGE_DONE;
	private int zombiesToSpawn;
	private int nextSpawnTime;
	private int spawnX;
	private int spawnY;
	private int spawnZ;

	public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
		if (!serverLevel.isDay() && bl) {
			float f = serverLevel.getTimeOfDay(0.0F);
			if ((double)f == 0.5) {
				this.siegeState = serverLevel.random.nextInt(10) == 0 ? VillageSiege.State.SIEGE_TONIGHT : VillageSiege.State.SIEGE_DONE;
			}

			if (this.siegeState == VillageSiege.State.SIEGE_DONE) {
				return 0;
			} else {
				if (!this.hasSetupSiege) {
					if (!this.tryToSetupSiege(serverLevel)) {
						return 0;
					}

					this.hasSetupSiege = true;
				}

				if (this.nextSpawnTime > 0) {
					this.nextSpawnTime--;
					return 0;
				} else {
					this.nextSpawnTime = 2;
					if (this.zombiesToSpawn > 0) {
						this.trySpawn(serverLevel);
						this.zombiesToSpawn--;
					} else {
						this.siegeState = VillageSiege.State.SIEGE_DONE;
					}

					return 1;
				}
			}
		} else {
			this.siegeState = VillageSiege.State.SIEGE_DONE;
			this.hasSetupSiege = false;
			return 0;
		}
	}

	private boolean tryToSetupSiege(ServerLevel serverLevel) {
		for (Player player : serverLevel.players()) {
			if (!player.isSpectator()) {
				BlockPos blockPos = player.getCommandSenderBlockPosition();
				if (serverLevel.isVillage(blockPos) && serverLevel.getBiome(blockPos).getBiomeCategory() != Biome.BiomeCategory.MUSHROOM) {
					for (int i = 0; i < 10; i++) {
						float f = serverLevel.random.nextFloat() * (float) (Math.PI * 2);
						this.spawnX = blockPos.getX() + Mth.floor(Mth.cos(f) * 32.0F);
						this.spawnY = blockPos.getY();
						this.spawnZ = blockPos.getZ() + Mth.floor(Mth.sin(f) * 32.0F);
						if (this.findRandomSpawnPos(serverLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ)) != null) {
							this.nextSpawnTime = 0;
							this.zombiesToSpawn = 20;
							break;
						}
					}

					return true;
				}
			}
		}

		return false;
	}

	private void trySpawn(ServerLevel serverLevel) {
		Vec3 vec3 = this.findRandomSpawnPos(serverLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
		if (vec3 != null) {
			Zombie zombie;
			try {
				zombie = new Zombie(serverLevel);
				zombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(new BlockPos(zombie)), MobSpawnType.EVENT, null, null);
			} catch (Exception var5) {
				var5.printStackTrace();
				return;
			}

			zombie.moveTo(vec3.x, vec3.y, vec3.z, serverLevel.random.nextFloat() * 360.0F, 0.0F);
			serverLevel.addFreshEntity(zombie);
		}
	}

	@Nullable
	private Vec3 findRandomSpawnPos(ServerLevel serverLevel, BlockPos blockPos) {
		for (int i = 0; i < 10; i++) {
			int j = blockPos.getX() + serverLevel.random.nextInt(16) - 8;
			int k = blockPos.getZ() + serverLevel.random.nextInt(16) - 8;
			int l = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, j, k);
			BlockPos blockPos2 = new BlockPos(j, l, k);
			if (serverLevel.isVillage(blockPos2) && Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, serverLevel, MobSpawnType.EVENT, blockPos2, serverLevel.random)) {
				return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
			}
		}

		return null;
	}

	static enum State {
		SIEGE_CAN_ACTIVATE,
		SIEGE_TONIGHT,
		SIEGE_DONE;
	}
}
