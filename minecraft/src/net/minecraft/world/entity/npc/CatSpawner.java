package net.minecraft.world.entity.npc;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class CatSpawner implements CustomSpawner {
	private static final int TICK_DELAY = 1200;
	private int nextTick;

	@Override
	public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
		if (bl2 && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
			this.nextTick--;
			if (this.nextTick > 0) {
				return 0;
			} else {
				this.nextTick = 1200;
				Player player = serverLevel.getRandomPlayer();
				if (player == null) {
					return 0;
				} else {
					RandomSource randomSource = serverLevel.random;
					int i = (8 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
					int j = (8 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
					BlockPos blockPos = player.blockPosition().offset(i, 0, j);
					int k = 10;
					if (!serverLevel.hasChunksAt(blockPos.getX() - 10, blockPos.getZ() - 10, blockPos.getX() + 10, blockPos.getZ() + 10)) {
						return 0;
					} else {
						if (SpawnPlacements.isSpawnPositionOk(EntityType.CAT, serverLevel, blockPos)) {
							if (serverLevel.isCloseToVillage(blockPos, 2)) {
								return this.spawnInVillage(serverLevel, blockPos);
							}

							if (serverLevel.structureManager().getStructureWithPieceAt(blockPos, StructureTags.CATS_SPAWN_IN).isValid()) {
								return this.spawnInHut(serverLevel, blockPos);
							}
						}

						return 0;
					}
				}
			}
		} else {
			return 0;
		}
	}

	private int spawnInVillage(ServerLevel serverLevel, BlockPos blockPos) {
		int i = 48;
		if (serverLevel.getPoiManager().getCountInRange(holder -> holder.is(PoiTypes.HOME), blockPos, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L) {
			List<Cat> list = serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(48.0, 8.0, 48.0));
			if (list.size() < 5) {
				return this.spawnCat(blockPos, serverLevel);
			}
		}

		return 0;
	}

	private int spawnInHut(ServerLevel serverLevel, BlockPos blockPos) {
		int i = 16;
		List<Cat> list = serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(16.0, 8.0, 16.0));
		return list.size() < 1 ? this.spawnCat(blockPos, serverLevel) : 0;
	}

	private int spawnCat(BlockPos blockPos, ServerLevel serverLevel) {
		Cat cat = EntityType.CAT.create(serverLevel, EntitySpawnReason.NATURAL);
		if (cat == null) {
			return 0;
		} else {
			cat.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), EntitySpawnReason.NATURAL, null);
			cat.moveTo(blockPos, 0.0F, 0.0F);
			serverLevel.addFreshEntityWithPassengers(cat);
			return 1;
		}
	}
}
