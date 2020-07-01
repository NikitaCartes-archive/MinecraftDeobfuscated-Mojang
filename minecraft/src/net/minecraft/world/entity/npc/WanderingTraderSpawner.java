package net.minecraft.world.entity.npc;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

public class WanderingTraderSpawner implements CustomSpawner {
	private final Random random = new Random();
	private final ServerLevelData serverLevelData;
	private int tickDelay;
	private int spawnDelay;
	private int spawnChance;

	public WanderingTraderSpawner(ServerLevelData serverLevelData) {
		this.serverLevelData = serverLevelData;
		this.tickDelay = 1200;
		this.spawnDelay = serverLevelData.getWanderingTraderSpawnDelay();
		this.spawnChance = serverLevelData.getWanderingTraderSpawnChance();
		if (this.spawnDelay == 0 && this.spawnChance == 0) {
			this.spawnDelay = 24000;
			serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
			this.spawnChance = 25;
			serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
		}
	}

	@Override
	public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
		if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
			return 0;
		} else if (--this.tickDelay > 0) {
			return 0;
		} else {
			this.tickDelay = 1200;
			this.spawnDelay -= 1200;
			this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
			if (this.spawnDelay > 0) {
				return 0;
			} else {
				this.spawnDelay = 24000;
				if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
					return 0;
				} else {
					int i = this.spawnChance;
					this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
					this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
					if (this.random.nextInt(100) > i) {
						return 0;
					} else if (this.spawn(serverLevel)) {
						this.spawnChance = 25;
						return 1;
					} else {
						return 0;
					}
				}
			}
		}
	}

	private boolean spawn(ServerLevel serverLevel) {
		Player player = serverLevel.getRandomPlayer();
		if (player == null) {
			return true;
		} else if (this.random.nextInt(10) != 0) {
			return false;
		} else {
			BlockPos blockPos = player.blockPosition();
			int i = 48;
			PoiManager poiManager = serverLevel.getPoiManager();
			Optional<BlockPos> optional = poiManager.find(PoiType.MEETING.getPredicate(), blockPosx -> true, blockPos, 48, PoiManager.Occupancy.ANY);
			BlockPos blockPos2 = (BlockPos)optional.orElse(blockPos);
			BlockPos blockPos3 = this.findSpawnPositionNear(serverLevel, blockPos2, 48);
			if (blockPos3 != null && this.hasEnoughSpace(serverLevel, blockPos3)) {
				if (serverLevel.getBiome(blockPos3) == Biomes.THE_VOID) {
					return false;
				}

				WanderingTrader wanderingTrader = EntityType.WANDERING_TRADER.spawn(serverLevel, null, null, null, blockPos3, MobSpawnType.EVENT, false, false);
				if (wanderingTrader != null) {
					for (int j = 0; j < 2; j++) {
						this.tryToSpawnLlamaFor(serverLevel, wanderingTrader, 4);
					}

					this.serverLevelData.setWanderingTraderId(wanderingTrader.getUUID());
					wanderingTrader.setDespawnDelay(48000);
					wanderingTrader.setWanderTarget(blockPos2);
					wanderingTrader.restrictTo(blockPos2, 16);
					return true;
				}
			}

			return false;
		}
	}

	private void tryToSpawnLlamaFor(ServerLevel serverLevel, WanderingTrader wanderingTrader, int i) {
		BlockPos blockPos = this.findSpawnPositionNear(serverLevel, wanderingTrader.blockPosition(), i);
		if (blockPos != null) {
			TraderLlama traderLlama = EntityType.TRADER_LLAMA.spawn(serverLevel, null, null, null, blockPos, MobSpawnType.EVENT, false, false);
			if (traderLlama != null) {
				traderLlama.setLeashedTo(wanderingTrader, true);
			}
		}
	}

	@Nullable
	private BlockPos findSpawnPositionNear(LevelReader levelReader, BlockPos blockPos, int i) {
		BlockPos blockPos2 = null;

		for (int j = 0; j < 10; j++) {
			int k = blockPos.getX() + this.random.nextInt(i * 2) - i;
			int l = blockPos.getZ() + this.random.nextInt(i * 2) - i;
			int m = levelReader.getHeight(Heightmap.Types.WORLD_SURFACE, k, l);
			BlockPos blockPos3 = new BlockPos(k, m, l);
			if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, levelReader, blockPos3, EntityType.WANDERING_TRADER)) {
				blockPos2 = blockPos3;
				break;
			}
		}

		return blockPos2;
	}

	private boolean hasEnoughSpace(BlockGetter blockGetter, BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(1, 2, 1))) {
			if (!blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2).isEmpty()) {
				return false;
			}
		}

		return true;
	}
}
