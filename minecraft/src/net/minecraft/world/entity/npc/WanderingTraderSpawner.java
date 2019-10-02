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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

public class WanderingTraderSpawner {
	private final Random random = new Random();
	private final ServerLevel level;
	private int tickDelay;
	private int spawnDelay;
	private int spawnChance;

	public WanderingTraderSpawner(ServerLevel serverLevel) {
		this.level = serverLevel;
		this.tickDelay = 1200;
		LevelData levelData = serverLevel.getLevelData();
		this.spawnDelay = levelData.getWanderingTraderSpawnDelay();
		this.spawnChance = levelData.getWanderingTraderSpawnChance();
		if (this.spawnDelay == 0 && this.spawnChance == 0) {
			this.spawnDelay = 24000;
			levelData.setWanderingTraderSpawnDelay(this.spawnDelay);
			this.spawnChance = 25;
			levelData.setWanderingTraderSpawnChance(this.spawnChance);
		}
	}

	public void tick() {
		if (--this.tickDelay <= 0) {
			this.tickDelay = 1200;
			LevelData levelData = this.level.getLevelData();
			this.spawnDelay -= 1200;
			levelData.setWanderingTraderSpawnDelay(this.spawnDelay);
			if (this.spawnDelay <= 0) {
				this.spawnDelay = 24000;
				if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
					int i = this.spawnChance;
					this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
					levelData.setWanderingTraderSpawnChance(this.spawnChance);
					if (this.random.nextInt(100) <= i) {
						if (this.spawn()) {
							this.spawnChance = 25;
						}
					}
				}
			}
		}
	}

	private boolean spawn() {
		Player player = this.level.getRandomPlayer();
		if (player == null) {
			return true;
		} else if (this.random.nextInt(10) != 0) {
			return false;
		} else {
			BlockPos blockPos = player.getCommandSenderBlockPosition();
			int i = 48;
			PoiManager poiManager = this.level.getPoiManager();
			Optional<BlockPos> optional = poiManager.find(PoiType.MEETING.getPredicate(), blockPosx -> true, blockPos, 48, PoiManager.Occupancy.ANY);
			BlockPos blockPos2 = (BlockPos)optional.orElse(blockPos);
			BlockPos blockPos3 = this.findSpawnPositionNear(blockPos2, 48);
			if (blockPos3 != null && this.hasEnoughSpace(blockPos3)) {
				if (this.level.getBiome(blockPos3) == Biomes.THE_VOID) {
					return false;
				}

				WanderingTrader wanderingTrader = EntityType.WANDERING_TRADER.spawn(this.level, null, null, null, blockPos3, MobSpawnType.EVENT, false, false);
				if (wanderingTrader != null) {
					for (int j = 0; j < 2; j++) {
						this.tryToSpawnLlamaFor(wanderingTrader, 4);
					}

					this.level.getLevelData().setWanderingTraderId(wanderingTrader.getUUID());
					wanderingTrader.setDespawnDelay(48000);
					wanderingTrader.setWanderTarget(blockPos2);
					wanderingTrader.restrictTo(blockPos2, 16);
					return true;
				}
			}

			return false;
		}
	}

	private void tryToSpawnLlamaFor(WanderingTrader wanderingTrader, int i) {
		BlockPos blockPos = this.findSpawnPositionNear(new BlockPos(wanderingTrader), i);
		if (blockPos != null) {
			TraderLlama traderLlama = EntityType.TRADER_LLAMA.spawn(this.level, null, null, null, blockPos, MobSpawnType.EVENT, false, false);
			if (traderLlama != null) {
				traderLlama.setLeashedTo(wanderingTrader, true);
			}
		}
	}

	@Nullable
	private BlockPos findSpawnPositionNear(BlockPos blockPos, int i) {
		BlockPos blockPos2 = null;

		for (int j = 0; j < 10; j++) {
			int k = blockPos.getX() + this.random.nextInt(i * 2) - i;
			int l = blockPos.getZ() + this.random.nextInt(i * 2) - i;
			int m = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, k, l);
			BlockPos blockPos3 = new BlockPos(k, m, l);
			if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, this.level, blockPos3, EntityType.WANDERING_TRADER)) {
				blockPos2 = blockPos3;
				break;
			}
		}

		return blockPos2;
	}

	private boolean hasEnoughSpace(BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(1, 2, 1))) {
			if (!this.level.getBlockState(blockPos2).getCollisionShape(this.level, blockPos2).isEmpty()) {
				return false;
			}
		}

		return true;
	}
}
