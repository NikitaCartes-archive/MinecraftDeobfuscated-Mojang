package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class TrialSpawnerData {
	public static final String TAG_SPAWN_DATA = "spawn_data";
	private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
	private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
	private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
	public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Sets.<UUID>newHashSet()).forGetter(trialSpawnerData -> trialSpawnerData.detectedPlayers),
					UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Sets.<UUID>newHashSet()).forGetter(trialSpawnerData -> trialSpawnerData.currentMobs),
					Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", Long.valueOf(0L)).forGetter(trialSpawnerData -> trialSpawnerData.cooldownEndsAt),
					Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", Long.valueOf(0L)).forGetter(trialSpawnerData -> trialSpawnerData.nextMobSpawnsAt),
					Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter(trialSpawnerData -> trialSpawnerData.totalMobsSpawned),
					SpawnData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter(trialSpawnerData -> trialSpawnerData.nextSpawnData),
					ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter(trialSpawnerData -> trialSpawnerData.ejectingLootTable)
				)
				.apply(instance, TrialSpawnerData::new)
	);
	protected final Set<UUID> detectedPlayers = new HashSet();
	protected final Set<UUID> currentMobs = new HashSet();
	protected long cooldownEndsAt;
	protected long nextMobSpawnsAt;
	protected int totalMobsSpawned;
	protected Optional<SpawnData> nextSpawnData;
	protected Optional<ResourceKey<LootTable>> ejectingLootTable;
	@Nullable
	protected Entity displayEntity;
	@Nullable
	private SimpleWeightedRandomList<ItemStack> dispensing;
	protected double spin;
	protected double oSpin;

	public TrialSpawnerData() {
		this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
	}

	public TrialSpawnerData(Set<UUID> set, Set<UUID> set2, long l, long m, int i, Optional<SpawnData> optional, Optional<ResourceKey<LootTable>> optional2) {
		this.detectedPlayers.addAll(set);
		this.currentMobs.addAll(set2);
		this.cooldownEndsAt = l;
		this.nextMobSpawnsAt = m;
		this.totalMobsSpawned = i;
		this.nextSpawnData = optional;
		this.ejectingLootTable = optional2;
	}

	public void reset() {
		this.currentMobs.clear();
		this.nextSpawnData = Optional.empty();
		this.resetStatistics();
	}

	public void resetStatistics() {
		this.detectedPlayers.clear();
		this.totalMobsSpawned = 0;
		this.nextMobSpawnsAt = 0L;
		this.cooldownEndsAt = 0L;
	}

	public boolean hasMobToSpawn(TrialSpawner trialSpawner, RandomSource randomSource) {
		boolean bl = this.getOrCreateNextSpawnData(trialSpawner, randomSource).getEntityToSpawn().contains("id", 8);
		return bl || !trialSpawner.getConfig().spawnPotentialsDefinition().isEmpty();
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

	public void tryDetectPlayers(ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner) {
		boolean bl = (blockPos.asLong() + serverLevel.getGameTime()) % 20L != 0L;
		if (!bl) {
			if (!trialSpawner.getState().equals(TrialSpawnerState.COOLDOWN) || !trialSpawner.isOminous()) {
				List<UUID> list = trialSpawner.getPlayerDetector()
					.detect(serverLevel, trialSpawner.getEntitySelector(), blockPos, (double)trialSpawner.getRequiredPlayerRange(), true);
				boolean bl2;
				if (!trialSpawner.isOminous() && !list.isEmpty()) {
					Optional<Pair<Player, Holder<MobEffect>>> optional = findPlayerWithOminousEffect(serverLevel, list);
					optional.ifPresent(pair -> {
						Player player = (Player)pair.getFirst();
						if (pair.getSecond() == MobEffects.BAD_OMEN) {
							transformBadOmenIntoTrialOmen(player);
						}

						serverLevel.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
						trialSpawner.applyOminous(serverLevel, blockPos);
					});
					bl2 = optional.isPresent();
				} else {
					bl2 = false;
				}

				if (!trialSpawner.getState().equals(TrialSpawnerState.COOLDOWN) || bl2) {
					boolean bl3 = trialSpawner.getData().detectedPlayers.isEmpty();
					List<UUID> list2 = bl3
						? list
						: trialSpawner.getPlayerDetector().detect(serverLevel, trialSpawner.getEntitySelector(), blockPos, (double)trialSpawner.getRequiredPlayerRange(), false);
					if (this.detectedPlayers.addAll(list2)) {
						this.nextMobSpawnsAt = Math.max(serverLevel.getGameTime() + 40L, this.nextMobSpawnsAt);
						if (!bl2) {
							int i = trialSpawner.isOminous() ? 3019 : 3013;
							serverLevel.levelEvent(i, blockPos, this.detectedPlayers.size());
						}
					}
				}
			}
		}
	}

	private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel serverLevel, List<UUID> list) {
		Player player = null;

		for (UUID uUID : list) {
			Player player2 = serverLevel.getPlayerByUUID(uUID);
			if (player2 != null) {
				Holder<MobEffect> holder = MobEffects.TRIAL_OMEN;
				if (player2.hasEffect(holder)) {
					return Optional.of(Pair.of(player2, holder));
				}

				if (player2.hasEffect(MobEffects.BAD_OMEN)) {
					player = player2;
				}
			}
		}

		return Optional.ofNullable(player).map(playerx -> Pair.of(playerx, MobEffects.BAD_OMEN));
	}

	public void resetAfterBecomingOminous(TrialSpawner trialSpawner, ServerLevel serverLevel) {
		this.currentMobs.stream().map(serverLevel::getEntity).forEach(entity -> {
			if (entity != null) {
				serverLevel.levelEvent(3012, entity.blockPosition(), TrialSpawner.FlameParticle.NORMAL.encode());
				if (entity instanceof Mob mob) {
					mob.dropPreservedEquipment(serverLevel);
				}

				entity.remove(Entity.RemovalReason.DISCARDED);
			}
		});
		if (!trialSpawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
			this.nextSpawnData = Optional.empty();
		}

		this.totalMobsSpawned = 0;
		this.currentMobs.clear();
		this.nextMobSpawnsAt = serverLevel.getGameTime() + (long)trialSpawner.getOminousConfig().ticksBetweenSpawn();
		trialSpawner.markUpdated();
		this.cooldownEndsAt = serverLevel.getGameTime() + trialSpawner.getOminousConfig().ticksBetweenItemSpawners();
	}

	private static void transformBadOmenIntoTrialOmen(Player player) {
		MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.BAD_OMEN);
		if (mobEffectInstance != null) {
			int i = mobEffectInstance.getAmplifier() + 1;
			int j = 18000 * i;
			player.removeEffect(MobEffects.BAD_OMEN);
			player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
		}
	}

	public boolean isReadyToOpenShutter(ServerLevel serverLevel, float f, int i) {
		long l = this.cooldownEndsAt - (long)i;
		return (float)serverLevel.getGameTime() >= (float)l + f;
	}

	public boolean isReadyToEjectItems(ServerLevel serverLevel, float f, int i) {
		long l = this.cooldownEndsAt - (long)i;
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
			SimpleWeightedRandomList<SpawnData> simpleWeightedRandomList = trialSpawner.getConfig().spawnPotentialsDefinition();
			Optional<SpawnData> optional = simpleWeightedRandomList.isEmpty()
				? this.nextSpawnData
				: simpleWeightedRandomList.getRandom(randomSource).map(WeightedEntry.Wrapper::data);
			this.nextSpawnData = Optional.of((SpawnData)optional.orElseGet(SpawnData::new));
			trialSpawner.markUpdated();
			return (SpawnData)this.nextSpawnData.get();
		}
	}

	@Nullable
	public Entity getOrCreateDisplayEntity(TrialSpawner trialSpawner, Level level, TrialSpawnerState trialSpawnerState) {
		if (!trialSpawnerState.hasSpinningMob()) {
			return null;
		} else {
			if (this.displayEntity == null) {
				CompoundTag compoundTag = this.getOrCreateNextSpawnData(trialSpawner, level.getRandom()).getEntityToSpawn();
				if (compoundTag.contains("id", 8)) {
					this.displayEntity = EntityType.loadEntityRecursive(compoundTag, level, EntitySpawnReason.TRIAL_SPAWNER, Function.identity());
				}
			}

			return this.displayEntity;
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

	SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, BlockPos blockPos) {
		if (this.dispensing != null) {
			return this.dispensing;
		} else {
			LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(trialSpawnerConfig.itemsToDropWhenOminous());
			LootParams lootParams = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY);
			long l = lowResolutionPosition(serverLevel, blockPos);
			ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams, l);
			if (objectArrayList.isEmpty()) {
				return SimpleWeightedRandomList.empty();
			} else {
				SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder<>();

				for (ItemStack itemStack : objectArrayList) {
					builder.add(itemStack.copyWithCount(1), itemStack.getCount());
				}

				this.dispensing = builder.build();
				return this.dispensing;
			}
		}
	}

	private static long lowResolutionPosition(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos blockPos2 = new BlockPos(
			Mth.floor((float)blockPos.getX() / 30.0F), Mth.floor((float)blockPos.getY() / 20.0F), Mth.floor((float)blockPos.getZ() / 30.0F)
		);
		return serverLevel.getSeed() + blockPos2.asLong();
	}
}
