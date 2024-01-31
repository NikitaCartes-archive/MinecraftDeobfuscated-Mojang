package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;

public class TrialSpawnerData {
	public static final String TAG_SPAWN_DATA = "spawn_data";
	private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
	public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					UUIDUtil.CODEC_SET.optionalFieldOf("registered_players", Sets.<UUID>newHashSet()).forGetter(trialSpawnerData -> trialSpawnerData.detectedPlayers),
					UUIDUtil.CODEC_SET.optionalFieldOf("current_mobs", Sets.<UUID>newHashSet()).forGetter(trialSpawnerData -> trialSpawnerData.currentMobs),
					Codec.LONG.optionalFieldOf("cooldown_ends_at", Long.valueOf(0L)).forGetter(trialSpawnerData -> trialSpawnerData.cooldownEndsAt),
					Codec.LONG.optionalFieldOf("next_mob_spawns_at", Long.valueOf(0L)).forGetter(trialSpawnerData -> trialSpawnerData.nextMobSpawnsAt),
					Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("total_mobs_spawned", 0).forGetter(trialSpawnerData -> trialSpawnerData.totalMobsSpawned),
					SpawnData.CODEC.optionalFieldOf("spawn_data").forGetter(trialSpawnerData -> trialSpawnerData.nextSpawnData),
					ResourceLocation.CODEC.optionalFieldOf("ejecting_loot_table").forGetter(trialSpawnerData -> trialSpawnerData.ejectingLootTable)
				)
				.apply(instance, TrialSpawnerData::new)
	);
	protected final Set<UUID> detectedPlayers = new HashSet();
	protected final Set<UUID> currentMobs = new HashSet();
	protected long cooldownEndsAt;
	protected long nextMobSpawnsAt;
	protected int totalMobsSpawned;
	protected Optional<SpawnData> nextSpawnData;
	protected Optional<ResourceLocation> ejectingLootTable;
	protected SimpleWeightedRandomList<SpawnData> spawnPotentials;
	@Nullable
	protected Entity displayEntity;
	protected double spin;
	protected double oSpin;

	public TrialSpawnerData() {
		this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
	}

	public TrialSpawnerData(Set<UUID> set, Set<UUID> set2, long l, long m, int i, Optional<SpawnData> optional, Optional<ResourceLocation> optional2) {
		this.detectedPlayers.addAll(set);
		this.currentMobs.addAll(set2);
		this.cooldownEndsAt = l;
		this.nextMobSpawnsAt = m;
		this.totalMobsSpawned = i;
		this.nextSpawnData = optional;
		this.ejectingLootTable = optional2;
	}

	public void setSpawnPotentialsFromConfig(TrialSpawnerConfig trialSpawnerConfig) {
		SimpleWeightedRandomList<SpawnData> simpleWeightedRandomList = trialSpawnerConfig.spawnPotentialsDefinition();
		if (simpleWeightedRandomList.isEmpty()) {
			this.spawnPotentials = SimpleWeightedRandomList.single((SpawnData)this.nextSpawnData.orElseGet(SpawnData::new));
		} else {
			this.spawnPotentials = simpleWeightedRandomList;
		}
	}

	public void reset() {
		this.detectedPlayers.clear();
		this.totalMobsSpawned = 0;
		this.nextMobSpawnsAt = 0L;
		this.cooldownEndsAt = 0L;
		this.currentMobs.clear();
	}

	public boolean hasMobToSpawn() {
		boolean bl = this.nextSpawnData.isPresent() && ((SpawnData)this.nextSpawnData.get()).getEntityToSpawn().contains("id", 8);
		return bl || !this.spawnPotentials.isEmpty();
	}

	public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig trialSpawnerConfig, int i) {
		return this.totalMobsSpawned >= trialSpawnerConfig.calculateTargetTotalMobs(i);
	}

	public boolean haveAllCurrentMobsDied() {
		return this.currentMobs.isEmpty();
	}

	public boolean isReadyToSpawnNextMob(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, int i) {
		return serverLevel.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < trialSpawnerConfig.calculateTargetSimultaneousMobs(i);
	}

	public int countAdditionalPlayers(BlockPos blockPos) {
		if (this.detectedPlayers.isEmpty()) {
			Util.logAndPauseIfInIde("Trial Spawner at " + blockPos + " has no detected players");
		}

		return Math.max(0, this.detectedPlayers.size() - 1);
	}

	public void tryDetectPlayers(ServerLevel serverLevel, BlockPos blockPos, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector, int i) {
		List<UUID> list = playerDetector.detect(serverLevel, entitySelector, blockPos, (double)i);
		boolean bl = this.detectedPlayers.addAll(list);
		if (bl) {
			this.nextMobSpawnsAt = Math.max(serverLevel.getGameTime() + 40L, this.nextMobSpawnsAt);
			serverLevel.levelEvent(3013, blockPos, this.detectedPlayers.size());
		}
	}

	public boolean isReadyToOpenShutter(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, float f) {
		long l = this.cooldownEndsAt - (long)trialSpawnerConfig.targetCooldownLength();
		return (float)serverLevel.getGameTime() >= (float)l + f;
	}

	public boolean isReadyToEjectItems(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, float f) {
		long l = this.cooldownEndsAt - (long)trialSpawnerConfig.targetCooldownLength();
		return (float)(serverLevel.getGameTime() - l) % f == 0.0F;
	}

	public boolean isCooldownFinished(ServerLevel serverLevel) {
		return serverLevel.getGameTime() >= this.cooldownEndsAt;
	}

	public void setEntityId(TrialSpawner trialSpawner, RandomSource randomSource, EntityType<?> entityType) {
		this.getOrCreateNextSpawnData(trialSpawner, randomSource).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
	}

	protected SpawnData getOrCreateNextSpawnData(TrialSpawner trialSpawner, RandomSource randomSource) {
		if (this.nextSpawnData.isPresent()) {
			return (SpawnData)this.nextSpawnData.get();
		} else {
			this.nextSpawnData = Optional.of((SpawnData)this.spawnPotentials.getRandom(randomSource).map(WeightedEntry.Wrapper::getData).orElseGet(SpawnData::new));
			trialSpawner.markUpdated();
			return (SpawnData)this.nextSpawnData.get();
		}
	}

	@Nullable
	public Entity getOrCreateDisplayEntity(TrialSpawner trialSpawner, Level level, TrialSpawnerState trialSpawnerState) {
		if (trialSpawner.canSpawnInLevel(level) && trialSpawnerState.hasSpinningMob()) {
			if (this.displayEntity == null) {
				CompoundTag compoundTag = this.getOrCreateNextSpawnData(trialSpawner, level.getRandom()).getEntityToSpawn();
				if (compoundTag.contains("id", 8)) {
					this.displayEntity = EntityType.loadEntityRecursive(compoundTag, level, Function.identity());
				}
			}

			return this.displayEntity;
		} else {
			return null;
		}
	}

	public CompoundTag getUpdateTag(TrialSpawnerState trialSpawnerState) {
		CompoundTag compoundTag = new CompoundTag();
		if (trialSpawnerState == TrialSpawnerState.ACTIVE) {
			compoundTag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
		}

		this.nextSpawnData
			.ifPresent(
				spawnData -> compoundTag.put(
						"spawn_data", (Tag)SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, spawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
					)
			);
		return compoundTag;
	}

	public double getSpin() {
		return this.spin;
	}

	public double getOSpin() {
		return this.oSpin;
	}
}
