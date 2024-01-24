package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTicks;
import org.slf4j.Logger;

public class ServerLevel extends Level implements WorldGenLevel {
	public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
	public static final IntProvider RAIN_DELAY = UniformInt.of(12000, 180000);
	public static final IntProvider RAIN_DURATION = UniformInt.of(12000, 24000);
	private static final IntProvider THUNDER_DELAY = UniformInt.of(12000, 180000);
	public static final IntProvider THUNDER_DURATION = UniformInt.of(3600, 15600);
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int EMPTY_TIME_NO_TICK = 300;
	private static final int MAX_SCHEDULED_TICKS_PER_TICK = 65536;
	final List<ServerPlayer> players = Lists.<ServerPlayer>newArrayList();
	private final ServerChunkCache chunkSource;
	private final MinecraftServer server;
	private final ServerLevelData serverLevelData;
	private int lastSpawnChunkRadius;
	final EntityTickList entityTickList = new EntityTickList();
	private final PersistentEntitySectionManager<Entity> entityManager;
	private final GameEventDispatcher gameEventDispatcher;
	public boolean noSave;
	private final SleepStatus sleepStatus;
	private int emptyTime;
	private final PortalForcer portalForcer;
	private final LevelTicks<Block> blockTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
	private final LevelTicks<Fluid> fluidTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
	final Set<Mob> navigatingMobs = new ObjectOpenHashSet<>();
	volatile boolean isUpdatingNavigations;
	protected final Raids raids;
	private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
	private final List<BlockEventData> blockEventsToReschedule = new ArrayList(64);
	private boolean handlingTick;
	private final List<CustomSpawner> customSpawners;
	@Nullable
	private EndDragonFight dragonFight;
	final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap<>();
	private final StructureManager structureManager;
	private final StructureCheck structureCheck;
	private final boolean tickTime;
	private final RandomSequences randomSequences;

	public ServerLevel(
		MinecraftServer minecraftServer,
		Executor executor,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		ServerLevelData serverLevelData,
		ResourceKey<Level> resourceKey,
		LevelStem levelStem,
		ChunkProgressListener chunkProgressListener,
		boolean bl,
		long l,
		List<CustomSpawner> list,
		boolean bl2,
		@Nullable RandomSequences randomSequences
	) {
		super(
			serverLevelData,
			resourceKey,
			minecraftServer.registryAccess(),
			levelStem.type(),
			minecraftServer::getProfiler,
			false,
			bl,
			l,
			minecraftServer.getMaxChainedNeighborUpdates()
		);
		this.tickTime = bl2;
		this.server = minecraftServer;
		this.customSpawners = list;
		this.serverLevelData = serverLevelData;
		ChunkGenerator chunkGenerator = levelStem.generator();
		boolean bl3 = minecraftServer.forceSynchronousWrites();
		DataFixer dataFixer = minecraftServer.getFixerUpper();
		EntityPersistentStorage<Entity> entityPersistentStorage = new EntityStorage(
			new SimpleRegionStorage(levelStorageAccess.getDimensionPath(resourceKey).resolve("entities"), dataFixer, bl3, "entities", DataFixTypes.ENTITY_CHUNK),
			this,
			minecraftServer
		);
		this.entityManager = new PersistentEntitySectionManager<>(Entity.class, new ServerLevel.EntityCallbacks(), entityPersistentStorage);
		this.chunkSource = new ServerChunkCache(
			this,
			levelStorageAccess,
			dataFixer,
			minecraftServer.getStructureManager(),
			executor,
			chunkGenerator,
			minecraftServer.getPlayerList().getViewDistance(),
			minecraftServer.getPlayerList().getSimulationDistance(),
			bl3,
			chunkProgressListener,
			this.entityManager::updateChunkStatus,
			() -> minecraftServer.overworld().getDataStorage()
		);
		this.chunkSource.getGeneratorState().ensureStructuresGenerated();
		this.portalForcer = new PortalForcer(this);
		this.updateSkyBrightness();
		this.prepareWeather();
		this.getWorldBorder().setAbsoluteMaxSize(minecraftServer.getAbsoluteMaxWorldSize());
		this.raids = this.getDataStorage().computeIfAbsent(Raids.factory(this), Raids.getFileId(this.dimensionTypeRegistration()));
		if (!minecraftServer.isSingleplayer()) {
			serverLevelData.setGameType(minecraftServer.getDefaultGameType());
		}

		long m = minecraftServer.getWorldData().worldGenOptions().seed();
		this.structureCheck = new StructureCheck(
			this.chunkSource.chunkScanner(),
			this.registryAccess(),
			minecraftServer.getStructureManager(),
			resourceKey,
			chunkGenerator,
			this.chunkSource.randomState(),
			this,
			chunkGenerator.getBiomeSource(),
			m,
			dataFixer
		);
		this.structureManager = new StructureManager(this, minecraftServer.getWorldData().worldGenOptions(), this.structureCheck);
		if (this.dimension() == Level.END && this.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
			this.dragonFight = new EndDragonFight(this, m, minecraftServer.getWorldData().endDragonFightData());
		} else {
			this.dragonFight = null;
		}

		this.sleepStatus = new SleepStatus();
		this.gameEventDispatcher = new GameEventDispatcher(this);
		this.randomSequences = (RandomSequences)Objects.requireNonNullElseGet(
			randomSequences, () -> this.getDataStorage().computeIfAbsent(RandomSequences.factory(m), "random_sequences")
		);
	}

	@Deprecated
	@VisibleForTesting
	public void setDragonFight(@Nullable EndDragonFight endDragonFight) {
		this.dragonFight = endDragonFight;
	}

	public void setWeatherParameters(int i, int j, boolean bl, boolean bl2) {
		this.serverLevelData.setClearWeatherTime(i);
		this.serverLevelData.setRainTime(j);
		this.serverLevelData.setThunderTime(j);
		this.serverLevelData.setRaining(bl);
		this.serverLevelData.setThundering(bl2);
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
		return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(i, j, k, this.getChunkSource().randomState().sampler());
	}

	public StructureManager structureManager() {
		return this.structureManager;
	}

	public void tick(BooleanSupplier booleanSupplier) {
		ProfilerFiller profilerFiller = this.getProfiler();
		this.handlingTick = true;
		TickRateManager tickRateManager = this.tickRateManager();
		boolean bl = tickRateManager.runsNormally();
		if (bl) {
			profilerFiller.push("world border");
			this.getWorldBorder().tick();
			profilerFiller.popPush("weather");
			this.advanceWeatherCycle();
		}

		int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
		if (this.sleepStatus.areEnoughSleeping(i) && this.sleepStatus.areEnoughDeepSleeping(i, this.players)) {
			if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
				long l = this.levelData.getDayTime() + 24000L;
				this.setDayTime(l - l % 24000L);
			}

			this.wakeUpAllPlayers();
			if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && this.isRaining()) {
				this.resetWeatherCycle();
			}
		}

		this.updateSkyBrightness();
		if (bl) {
			this.tickTime();
		}

		profilerFiller.popPush("tickPending");
		if (!this.isDebug() && bl) {
			long l = this.getGameTime();
			profilerFiller.push("blockTicks");
			this.blockTicks.tick(l, 65536, this::tickBlock);
			profilerFiller.popPush("fluidTicks");
			this.fluidTicks.tick(l, 65536, this::tickFluid);
			profilerFiller.pop();
		}

		profilerFiller.popPush("raid");
		if (bl) {
			this.raids.tick();
		}

		profilerFiller.popPush("chunkSource");
		this.getChunkSource().tick(booleanSupplier, true);
		profilerFiller.popPush("blockEvents");
		if (bl) {
			this.runBlockEvents();
		}

		this.handlingTick = false;
		profilerFiller.pop();
		boolean bl2 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
		if (bl2) {
			this.resetEmptyTime();
		}

		if (bl2 || this.emptyTime++ < 300) {
			profilerFiller.push("entities");
			if (this.dragonFight != null && bl) {
				profilerFiller.push("dragonFight");
				this.dragonFight.tick();
				profilerFiller.pop();
			}

			this.entityTickList.forEach(entity -> {
				if (!entity.isRemoved()) {
					if (this.shouldDiscardEntity(entity)) {
						entity.discard();
					} else if (!tickRateManager.isEntityFrozen(entity)) {
						profilerFiller.push("checkDespawn");
						entity.checkDespawn();
						profilerFiller.pop();
						if (this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong())) {
							Entity entity2 = entity.getVehicle();
							if (entity2 != null) {
								if (!entity2.isRemoved() && entity2.hasPassenger(entity)) {
									return;
								}

								entity.stopRiding();
							}

							profilerFiller.push("tick");
							this.guardEntityTick(this::tickNonPassenger, entity);
							profilerFiller.pop();
						}
					}
				}
			});
			profilerFiller.pop();
			this.tickBlockEntities();
		}

		profilerFiller.push("entityManagement");
		this.entityManager.tick();
		profilerFiller.pop();
	}

	@Override
	public boolean shouldTickBlocksAt(long l) {
		return this.chunkSource.chunkMap.getDistanceManager().inBlockTickingRange(l);
	}

	protected void tickTime() {
		if (this.tickTime) {
			long l = this.levelData.getGameTime() + 1L;
			this.serverLevelData.setGameTime(l);
			this.serverLevelData.getScheduledEvents().tick(this.server, l);
			if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
				this.setDayTime(this.levelData.getDayTime() + 1L);
			}
		}
	}

	public void setDayTime(long l) {
		this.serverLevelData.setDayTime(l);
	}

	public void tickCustomSpawners(boolean bl, boolean bl2) {
		for (CustomSpawner customSpawner : this.customSpawners) {
			customSpawner.tick(this, bl, bl2);
		}
	}

	private boolean shouldDiscardEntity(Entity entity) {
		return this.server.isSpawningAnimals() || !(entity instanceof Animal) && !(entity instanceof WaterAnimal)
			? !this.server.areNpcsEnabled() && entity instanceof Npc
			: true;
	}

	private void wakeUpAllPlayers() {
		this.sleepStatus.removeAllSleepers();
		((List)this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()))
			.forEach(serverPlayer -> serverPlayer.stopSleepInBed(false, false));
	}

	public void tickChunk(LevelChunk levelChunk, int i) {
		ChunkPos chunkPos = levelChunk.getPos();
		boolean bl = this.isRaining();
		int j = chunkPos.getMinBlockX();
		int k = chunkPos.getMinBlockZ();
		ProfilerFiller profilerFiller = this.getProfiler();
		profilerFiller.push("thunder");
		if (bl && this.isThundering() && this.random.nextInt(100000) == 0) {
			BlockPos blockPos = this.findLightningTargetAround(this.getBlockRandomPos(j, 0, k, 15));
			if (this.isRainingAt(blockPos)) {
				DifficultyInstance difficultyInstance = this.getCurrentDifficultyAt(blockPos);
				boolean bl2 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
					&& this.random.nextDouble() < (double)difficultyInstance.getEffectiveDifficulty() * 0.01
					&& !this.getBlockState(blockPos.below()).is(Blocks.LIGHTNING_ROD);
				if (bl2) {
					SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this);
					if (skeletonHorse != null) {
						skeletonHorse.setTrap(true);
						skeletonHorse.setAge(0);
						skeletonHorse.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
						this.addFreshEntity(skeletonHorse);
					}
				}

				LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this);
				if (lightningBolt != null) {
					lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
					lightningBolt.setVisualOnly(bl2);
					this.addFreshEntity(lightningBolt);
				}
			}
		}

		profilerFiller.popPush("iceandsnow");

		for (int l = 0; l < i; l++) {
			if (this.random.nextInt(48) == 0) {
				this.tickPrecipitation(this.getBlockRandomPos(j, 0, k, 15));
			}
		}

		profilerFiller.popPush("tickBlocks");
		if (i > 0) {
			LevelChunkSection[] levelChunkSections = levelChunk.getSections();

			for (int m = 0; m < levelChunkSections.length; m++) {
				LevelChunkSection levelChunkSection = levelChunkSections[m];
				if (levelChunkSection.isRandomlyTicking()) {
					int n = levelChunk.getSectionYFromSectionIndex(m);
					int o = SectionPos.sectionToBlockCoord(n);

					for (int p = 0; p < i; p++) {
						BlockPos blockPos2 = this.getBlockRandomPos(j, o, k, 15);
						profilerFiller.push("randomTick");
						BlockState blockState = levelChunkSection.getBlockState(blockPos2.getX() - j, blockPos2.getY() - o, blockPos2.getZ() - k);
						if (blockState.isRandomlyTicking()) {
							blockState.randomTick(this, blockPos2, this.random);
						}

						FluidState fluidState = blockState.getFluidState();
						if (fluidState.isRandomlyTicking()) {
							fluidState.randomTick(this, blockPos2, this.random);
						}

						profilerFiller.pop();
					}
				}
			}
		}

		profilerFiller.pop();
	}

	@VisibleForTesting
	public void tickPrecipitation(BlockPos blockPos) {
		BlockPos blockPos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos);
		BlockPos blockPos3 = blockPos2.below();
		Biome biome = this.getBiome(blockPos2).value();
		if (biome.shouldFreeze(this, blockPos3)) {
			this.setBlockAndUpdate(blockPos3, Blocks.ICE.defaultBlockState());
		}

		if (this.isRaining()) {
			int i = this.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT);
			if (i > 0 && biome.shouldSnow(this, blockPos2)) {
				BlockState blockState = this.getBlockState(blockPos2);
				if (blockState.is(Blocks.SNOW)) {
					int j = (Integer)blockState.getValue(SnowLayerBlock.LAYERS);
					if (j < Math.min(i, 8)) {
						BlockState blockState2 = blockState.setValue(SnowLayerBlock.LAYERS, Integer.valueOf(j + 1));
						Block.pushEntitiesUp(blockState, blockState2, this, blockPos2);
						this.setBlockAndUpdate(blockPos2, blockState2);
					}
				} else {
					this.setBlockAndUpdate(blockPos2, Blocks.SNOW.defaultBlockState());
				}
			}

			Biome.Precipitation precipitation = biome.getPrecipitationAt(blockPos3);
			if (precipitation != Biome.Precipitation.NONE) {
				BlockState blockState3 = this.getBlockState(blockPos3);
				blockState3.getBlock().handlePrecipitation(blockState3, this, blockPos3, precipitation);
			}
		}
	}

	private Optional<BlockPos> findLightningRod(BlockPos blockPos) {
		Optional<BlockPos> optional = this.getPoiManager()
			.findClosest(
				holder -> holder.is(PoiTypes.LIGHTNING_ROD),
				blockPosx -> blockPosx.getY() == this.getHeight(Heightmap.Types.WORLD_SURFACE, blockPosx.getX(), blockPosx.getZ()) - 1,
				blockPos,
				128,
				PoiManager.Occupancy.ANY
			);
		return optional.map(blockPosx -> blockPosx.above(1));
	}

	protected BlockPos findLightningTargetAround(BlockPos blockPos) {
		BlockPos blockPos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos);
		Optional<BlockPos> optional = this.findLightningRod(blockPos2);
		if (optional.isPresent()) {
			return (BlockPos)optional.get();
		} else {
			AABB aABB = AABB.encapsulatingFullBlocks(blockPos2, new BlockPos(blockPos2.atY(this.getMaxBuildHeight()))).inflate(3.0);
			List<LivingEntity> list = this.getEntitiesOfClass(
				LivingEntity.class, aABB, livingEntity -> livingEntity != null && livingEntity.isAlive() && this.canSeeSky(livingEntity.blockPosition())
			);
			if (!list.isEmpty()) {
				return ((LivingEntity)list.get(this.random.nextInt(list.size()))).blockPosition();
			} else {
				if (blockPos2.getY() == this.getMinBuildHeight() - 1) {
					blockPos2 = blockPos2.above(2);
				}

				return blockPos2;
			}
		}
	}

	public boolean isHandlingTick() {
		return this.handlingTick;
	}

	public boolean canSleepThroughNights() {
		return this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) <= 100;
	}

	private void announceSleepStatus() {
		if (this.canSleepThroughNights()) {
			if (!this.getServer().isSingleplayer() || this.getServer().isPublished()) {
				int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
				Component component;
				if (this.sleepStatus.areEnoughSleeping(i)) {
					component = Component.translatable("sleep.skipping_night");
				} else {
					component = Component.translatable("sleep.players_sleeping", this.sleepStatus.amountSleeping(), this.sleepStatus.sleepersNeeded(i));
				}

				for (ServerPlayer serverPlayer : this.players) {
					serverPlayer.displayClientMessage(component, true);
				}
			}
		}
	}

	public void updateSleepingPlayerList() {
		if (!this.players.isEmpty() && this.sleepStatus.update(this.players)) {
			this.announceSleepStatus();
		}
	}

	public ServerScoreboard getScoreboard() {
		return this.server.getScoreboard();
	}

	private void advanceWeatherCycle() {
		boolean bl = this.isRaining();
		if (this.dimensionType().hasSkyLight()) {
			if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
				int i = this.serverLevelData.getClearWeatherTime();
				int j = this.serverLevelData.getThunderTime();
				int k = this.serverLevelData.getRainTime();
				boolean bl2 = this.levelData.isThundering();
				boolean bl3 = this.levelData.isRaining();
				if (i > 0) {
					i--;
					j = bl2 ? 0 : 1;
					k = bl3 ? 0 : 1;
					bl2 = false;
					bl3 = false;
				} else {
					if (j > 0) {
						if (--j == 0) {
							bl2 = !bl2;
						}
					} else if (bl2) {
						j = THUNDER_DURATION.sample(this.random);
					} else {
						j = THUNDER_DELAY.sample(this.random);
					}

					if (k > 0) {
						if (--k == 0) {
							bl3 = !bl3;
						}
					} else if (bl3) {
						k = RAIN_DURATION.sample(this.random);
					} else {
						k = RAIN_DELAY.sample(this.random);
					}
				}

				this.serverLevelData.setThunderTime(j);
				this.serverLevelData.setRainTime(k);
				this.serverLevelData.setClearWeatherTime(i);
				this.serverLevelData.setThundering(bl2);
				this.serverLevelData.setRaining(bl3);
			}

			this.oThunderLevel = this.thunderLevel;
			if (this.levelData.isThundering()) {
				this.thunderLevel += 0.01F;
			} else {
				this.thunderLevel -= 0.01F;
			}

			this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0F, 1.0F);
			this.oRainLevel = this.rainLevel;
			if (this.levelData.isRaining()) {
				this.rainLevel += 0.01F;
			} else {
				this.rainLevel -= 0.01F;
			}

			this.rainLevel = Mth.clamp(this.rainLevel, 0.0F, 1.0F);
		}

		if (this.oRainLevel != this.rainLevel) {
			this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
		}

		if (this.oThunderLevel != this.thunderLevel) {
			this.server
				.getPlayerList()
				.broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
		}

		if (bl != this.isRaining()) {
			if (bl) {
				this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
			} else {
				this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
			}

			this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
			this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
		}
	}

	@VisibleForTesting
	public void resetWeatherCycle() {
		this.serverLevelData.setRainTime(0);
		this.serverLevelData.setRaining(false);
		this.serverLevelData.setThunderTime(0);
		this.serverLevelData.setThundering(false);
	}

	public void resetEmptyTime() {
		this.emptyTime = 0;
	}

	private void tickFluid(BlockPos blockPos, Fluid fluid) {
		FluidState fluidState = this.getFluidState(blockPos);
		if (fluidState.is(fluid)) {
			fluidState.tick(this, blockPos);
		}
	}

	private void tickBlock(BlockPos blockPos, Block block) {
		BlockState blockState = this.getBlockState(blockPos);
		if (blockState.is(block)) {
			blockState.tick(this, blockPos, this.random);
		}
	}

	public void tickNonPassenger(Entity entity) {
		entity.setOldPosAndRot();
		ProfilerFiller profilerFiller = this.getProfiler();
		entity.tickCount++;
		this.getProfiler().push((Supplier<String>)(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()));
		profilerFiller.incrementCounter("tickNonPassenger");
		entity.tick();
		this.getProfiler().pop();

		for (Entity entity2 : entity.getPassengers()) {
			this.tickPassenger(entity, entity2);
		}
	}

	private void tickPassenger(Entity entity, Entity entity2) {
		if (entity2.isRemoved() || entity2.getVehicle() != entity) {
			entity2.stopRiding();
		} else if (entity2 instanceof Player || this.entityTickList.contains(entity2)) {
			entity2.setOldPosAndRot();
			entity2.tickCount++;
			ProfilerFiller profilerFiller = this.getProfiler();
			profilerFiller.push((Supplier<String>)(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity2.getType()).toString()));
			profilerFiller.incrementCounter("tickPassenger");
			entity2.rideTick();
			profilerFiller.pop();

			for (Entity entity3 : entity2.getPassengers()) {
				this.tickPassenger(entity2, entity3);
			}
		}
	}

	@Override
	public boolean mayInteract(Player player, BlockPos blockPos) {
		return !this.server.isUnderSpawnProtection(this, blockPos, player) && this.getWorldBorder().isWithinBounds(blockPos);
	}

	public void save(@Nullable ProgressListener progressListener, boolean bl, boolean bl2) {
		ServerChunkCache serverChunkCache = this.getChunkSource();
		if (!bl2) {
			if (progressListener != null) {
				progressListener.progressStartNoAbort(Component.translatable("menu.savingLevel"));
			}

			this.saveLevelData();
			if (progressListener != null) {
				progressListener.progressStage(Component.translatable("menu.savingChunks"));
			}

			serverChunkCache.save(bl);
			if (bl) {
				this.entityManager.saveAll();
			} else {
				this.entityManager.autoSave();
			}
		}
	}

	private void saveLevelData() {
		if (this.dragonFight != null) {
			this.server.getWorldData().setEndDragonFightData(this.dragonFight.saveData());
		}

		this.getChunkSource().getDataStorage().save();
	}

	public <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, Predicate<? super T> predicate) {
		List<T> list = Lists.<T>newArrayList();
		this.getEntities(entityTypeTest, predicate, list);
		return list;
	}

	public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entityTypeTest, Predicate<? super T> predicate, List<? super T> list) {
		this.getEntities(entityTypeTest, predicate, list, Integer.MAX_VALUE);
	}

	public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entityTypeTest, Predicate<? super T> predicate, List<? super T> list, int i) {
		this.getEntities().get(entityTypeTest, entity -> {
			if (predicate.test(entity)) {
				list.add(entity);
				if (list.size() >= i) {
					return AbortableIterationConsumer.Continuation.ABORT;
				}
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
		});
	}

	public List<? extends EnderDragon> getDragons() {
		return this.getEntities(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
	}

	public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate) {
		return this.getPlayers(predicate, Integer.MAX_VALUE);
	}

	public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate, int i) {
		List<ServerPlayer> list = Lists.<ServerPlayer>newArrayList();

		for (ServerPlayer serverPlayer : this.players) {
			if (predicate.test(serverPlayer)) {
				list.add(serverPlayer);
				if (list.size() >= i) {
					return list;
				}
			}
		}

		return list;
	}

	@Nullable
	public ServerPlayer getRandomPlayer() {
		List<ServerPlayer> list = this.getPlayers(LivingEntity::isAlive);
		return list.isEmpty() ? null : (ServerPlayer)list.get(this.random.nextInt(list.size()));
	}

	@Override
	public boolean addFreshEntity(Entity entity) {
		return this.addEntity(entity);
	}

	public boolean addWithUUID(Entity entity) {
		return this.addEntity(entity);
	}

	public void addDuringTeleport(Entity entity) {
		this.addEntity(entity);
	}

	public void addDuringCommandTeleport(ServerPlayer serverPlayer) {
		this.addPlayer(serverPlayer);
	}

	public void addDuringPortalTeleport(ServerPlayer serverPlayer) {
		this.addPlayer(serverPlayer);
	}

	public void addNewPlayer(ServerPlayer serverPlayer) {
		this.addPlayer(serverPlayer);
	}

	public void addRespawnedPlayer(ServerPlayer serverPlayer) {
		this.addPlayer(serverPlayer);
	}

	private void addPlayer(ServerPlayer serverPlayer) {
		Entity entity = this.getEntities().get(serverPlayer.getUUID());
		if (entity != null) {
			LOGGER.warn("Force-added player with duplicate UUID {}", serverPlayer.getUUID());
			entity.unRide();
			this.removePlayerImmediately((ServerPlayer)entity, Entity.RemovalReason.DISCARDED);
		}

		this.entityManager.addNewEntity(serverPlayer);
	}

	private boolean addEntity(Entity entity) {
		if (entity.isRemoved()) {
			LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityType.getKey(entity.getType()));
			return false;
		} else {
			return this.entityManager.addNewEntity(entity);
		}
	}

	public boolean tryAddFreshEntityWithPassengers(Entity entity) {
		if (entity.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded)) {
			return false;
		} else {
			this.addFreshEntityWithPassengers(entity);
			return true;
		}
	}

	public void unload(LevelChunk levelChunk) {
		levelChunk.clearAllBlockEntities();
		levelChunk.unregisterTickContainerFromLevel(this);
	}

	public void removePlayerImmediately(ServerPlayer serverPlayer, Entity.RemovalReason removalReason) {
		serverPlayer.remove(removalReason);
	}

	@Override
	public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
		for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
			if (serverPlayer != null && serverPlayer.level() == this && serverPlayer.getId() != i) {
				double d = (double)blockPos.getX() - serverPlayer.getX();
				double e = (double)blockPos.getY() - serverPlayer.getY();
				double f = (double)blockPos.getZ() - serverPlayer.getZ();
				if (d * d + e * e + f * f < 1024.0) {
					serverPlayer.connection.send(new ClientboundBlockDestructionPacket(i, blockPos, j));
				}
			}
		}
	}

	@Override
	public void playSeededSound(
		@Nullable Player player, double d, double e, double f, Holder<SoundEvent> holder, SoundSource soundSource, float g, float h, long l
	) {
		this.server
			.getPlayerList()
			.broadcast(player, d, e, f, (double)holder.value().getRange(g), this.dimension(), new ClientboundSoundPacket(holder, soundSource, d, e, f, g, h, l));
	}

	@Override
	public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float f, float g, long l) {
		this.server
			.getPlayerList()
			.broadcast(
				player,
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				(double)holder.value().getRange(f),
				this.dimension(),
				new ClientboundSoundEntityPacket(holder, soundSource, entity, f, g, l)
			);
	}

	@Override
	public void globalLevelEvent(int i, BlockPos blockPos, int j) {
		if (this.getGameRules().getBoolean(GameRules.RULE_GLOBAL_SOUND_EVENTS)) {
			this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(i, blockPos, j, true));
		} else {
			this.levelEvent(null, i, blockPos, j);
		}
	}

	@Override
	public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
		this.server
			.getPlayerList()
			.broadcast(
				player,
				(double)blockPos.getX(),
				(double)blockPos.getY(),
				(double)blockPos.getZ(),
				64.0,
				this.dimension(),
				new ClientboundLevelEventPacket(i, blockPos, j, false)
			);
	}

	public int getLogicalHeight() {
		return this.dimensionType().logicalHeight();
	}

	@Override
	public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
		this.gameEventDispatcher.post(holder, vec3, context);
	}

	@Override
	public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
		if (this.isUpdatingNavigations) {
			String string = "recursive call to sendBlockUpdated";
			Util.logAndPauseIfInIde("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
		}

		this.getChunkSource().blockChanged(blockPos);
		VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos);
		VoxelShape voxelShape2 = blockState2.getCollisionShape(this, blockPos);
		if (Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.NOT_SAME)) {
			List<PathNavigation> list = new ObjectArrayList<>();

			for (Mob mob : this.navigatingMobs) {
				PathNavigation pathNavigation = mob.getNavigation();
				if (pathNavigation.shouldRecomputePath(blockPos)) {
					list.add(pathNavigation);
				}
			}

			try {
				this.isUpdatingNavigations = true;

				for (PathNavigation pathNavigation2 : list) {
					pathNavigation2.recomputePath();
				}
			} finally {
				this.isUpdatingNavigations = false;
			}
		}
	}

	@Override
	public void updateNeighborsAt(BlockPos blockPos, Block block) {
		this.neighborUpdater.updateNeighborsAtExceptFromFacing(blockPos, block, null);
	}

	@Override
	public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction) {
		this.neighborUpdater.updateNeighborsAtExceptFromFacing(blockPos, block, direction);
	}

	@Override
	public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
		this.neighborUpdater.neighborChanged(blockPos, block, blockPos2);
	}

	@Override
	public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		this.neighborUpdater.neighborChanged(blockState, blockPos, block, blockPos2, bl);
	}

	@Override
	public void broadcastEntityEvent(Entity entity, byte b) {
		this.getChunkSource().broadcastAndSend(entity, new ClientboundEntityEventPacket(entity, b));
	}

	@Override
	public void broadcastDamageEvent(Entity entity, DamageSource damageSource) {
		this.getChunkSource().broadcastAndSend(entity, new ClientboundDamageEventPacket(entity, damageSource));
	}

	public ServerChunkCache getChunkSource() {
		return this.chunkSource;
	}

	@Override
	public Explosion explode(
		@Nullable Entity entity,
		@Nullable DamageSource damageSource,
		@Nullable ExplosionDamageCalculator explosionDamageCalculator,
		double d,
		double e,
		double f,
		float g,
		boolean bl,
		Level.ExplosionInteraction explosionInteraction,
		ParticleOptions particleOptions,
		ParticleOptions particleOptions2,
		Holder<SoundEvent> holder
	) {
		Explosion explosion = this.explode(
			entity, damageSource, explosionDamageCalculator, d, e, f, g, bl, explosionInteraction, false, particleOptions, particleOptions2, holder
		);
		if (!explosion.interactsWithBlocks()) {
			explosion.clearToBlow();
		}

		for (ServerPlayer serverPlayer : this.players) {
			if (serverPlayer.distanceToSqr(d, e, f) < 4096.0) {
				serverPlayer.connection
					.send(
						new ClientboundExplodePacket(
							d,
							e,
							f,
							g,
							explosion.getToBlow(),
							(Vec3)explosion.getHitPlayers().get(serverPlayer),
							explosion.getBlockInteraction(),
							explosion.getSmallExplosionParticles(),
							explosion.getLargeExplosionParticles(),
							explosion.getExplosionSound()
						)
					);
			}
		}

		return explosion;
	}

	@Override
	public void blockEvent(BlockPos blockPos, Block block, int i, int j) {
		this.blockEvents.add(new BlockEventData(blockPos, block, i, j));
	}

	private void runBlockEvents() {
		this.blockEventsToReschedule.clear();

		while (!this.blockEvents.isEmpty()) {
			BlockEventData blockEventData = this.blockEvents.removeFirst();
			if (this.shouldTickBlocksAt(blockEventData.pos())) {
				if (this.doBlockEvent(blockEventData)) {
					this.server
						.getPlayerList()
						.broadcast(
							null,
							(double)blockEventData.pos().getX(),
							(double)blockEventData.pos().getY(),
							(double)blockEventData.pos().getZ(),
							64.0,
							this.dimension(),
							new ClientboundBlockEventPacket(blockEventData.pos(), blockEventData.block(), blockEventData.paramA(), blockEventData.paramB())
						);
				}
			} else {
				this.blockEventsToReschedule.add(blockEventData);
			}
		}

		this.blockEvents.addAll(this.blockEventsToReschedule);
	}

	private boolean doBlockEvent(BlockEventData blockEventData) {
		BlockState blockState = this.getBlockState(blockEventData.pos());
		return blockState.is(blockEventData.block()) ? blockState.triggerEvent(this, blockEventData.pos(), blockEventData.paramA(), blockEventData.paramB()) : false;
	}

	public LevelTicks<Block> getBlockTicks() {
		return this.blockTicks;
	}

	public LevelTicks<Fluid> getFluidTicks() {
		return this.fluidTicks;
	}

	@Nonnull
	@Override
	public MinecraftServer getServer() {
		return this.server;
	}

	public PortalForcer getPortalForcer() {
		return this.portalForcer;
	}

	public StructureTemplateManager getStructureManager() {
		return this.server.getStructureManager();
	}

	public <T extends ParticleOptions> int sendParticles(T particleOptions, double d, double e, double f, int i, double g, double h, double j, double k) {
		ClientboundLevelParticlesPacket clientboundLevelParticlesPacket = new ClientboundLevelParticlesPacket(
			particleOptions, false, d, e, f, (float)g, (float)h, (float)j, (float)k, i
		);
		int l = 0;

		for (int m = 0; m < this.players.size(); m++) {
			ServerPlayer serverPlayer = (ServerPlayer)this.players.get(m);
			if (this.sendParticles(serverPlayer, false, d, e, f, clientboundLevelParticlesPacket)) {
				l++;
			}
		}

		return l;
	}

	public <T extends ParticleOptions> boolean sendParticles(
		ServerPlayer serverPlayer, T particleOptions, boolean bl, double d, double e, double f, int i, double g, double h, double j, double k
	) {
		Packet<?> packet = new ClientboundLevelParticlesPacket(particleOptions, bl, d, e, f, (float)g, (float)h, (float)j, (float)k, i);
		return this.sendParticles(serverPlayer, bl, d, e, f, packet);
	}

	private boolean sendParticles(ServerPlayer serverPlayer, boolean bl, double d, double e, double f, Packet<?> packet) {
		if (serverPlayer.level() != this) {
			return false;
		} else {
			BlockPos blockPos = serverPlayer.blockPosition();
			if (blockPos.closerToCenterThan(new Vec3(d, e, f), bl ? 512.0 : 32.0)) {
				serverPlayer.connection.send(packet);
				return true;
			} else {
				return false;
			}
		}
	}

	@Nullable
	@Override
	public Entity getEntity(int i) {
		return this.getEntities().get(i);
	}

	@Deprecated
	@Nullable
	public Entity getEntityOrPart(int i) {
		Entity entity = this.getEntities().get(i);
		return entity != null ? entity : this.dragonParts.get(i);
	}

	@Nullable
	public Entity getEntity(UUID uUID) {
		return this.getEntities().get(uUID);
	}

	@Nullable
	public BlockPos findNearestMapStructure(TagKey<Structure> tagKey, BlockPos blockPos, int i, boolean bl) {
		if (!this.server.getWorldData().worldGenOptions().generateStructures()) {
			return null;
		} else {
			Optional<HolderSet.Named<Structure>> optional = this.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(tagKey);
			if (optional.isEmpty()) {
				return null;
			} else {
				Pair<BlockPos, Holder<Structure>> pair = this.getChunkSource()
					.getGenerator()
					.findNearestMapStructure(this, (HolderSet<Structure>)optional.get(), blockPos, i, bl);
				return pair != null ? pair.getFirst() : null;
			}
		}
	}

	@Nullable
	public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(Predicate<Holder<Biome>> predicate, BlockPos blockPos, int i, int j, int k) {
		return this.getChunkSource()
			.getGenerator()
			.getBiomeSource()
			.findClosestBiome3d(blockPos, i, j, k, predicate, this.getChunkSource().randomState().sampler(), this);
	}

	@Override
	public RecipeManager getRecipeManager() {
		return this.server.getRecipeManager();
	}

	@Override
	public TickRateManager tickRateManager() {
		return this.server.tickRateManager();
	}

	@Override
	public boolean noSave() {
		return this.noSave;
	}

	public DimensionDataStorage getDataStorage() {
		return this.getChunkSource().getDataStorage();
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(String string) {
		return this.getServer().overworld().getDataStorage().get(MapItemSavedData.factory(), string);
	}

	@Override
	public void setMapData(String string, MapItemSavedData mapItemSavedData) {
		this.getServer().overworld().getDataStorage().set(string, mapItemSavedData);
	}

	@Override
	public int getFreeMapId() {
		return this.getServer().overworld().getDataStorage().computeIfAbsent(MapIndex.factory(), "idcounts").getFreeAuxValueForMap();
	}

	public void setDefaultSpawnPos(BlockPos blockPos, float f) {
		BlockPos blockPos2 = this.levelData.getSpawnPos();
		float g = this.levelData.getSpawnAngle();
		if (!blockPos2.equals(blockPos) || g != f) {
			this.levelData.setSpawn(blockPos, f);
			this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(blockPos, f));
		}

		if (this.lastSpawnChunkRadius > 1) {
			this.getChunkSource().removeRegionTicket(TicketType.START, new ChunkPos(blockPos2), this.lastSpawnChunkRadius, Unit.INSTANCE);
		}

		int i = this.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS) + 1;
		if (i > 1) {
			this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockPos), i, Unit.INSTANCE);
		}

		this.lastSpawnChunkRadius = i;
	}

	public LongSet getForcedChunks() {
		ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
		return (LongSet)(forcedChunksSavedData != null ? LongSets.unmodifiable(forcedChunksSavedData.getChunks()) : LongSets.EMPTY_SET);
	}

	public boolean setChunkForced(int i, int j, boolean bl) {
		ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData.factory(), "chunks");
		ChunkPos chunkPos = new ChunkPos(i, j);
		long l = chunkPos.toLong();
		boolean bl2;
		if (bl) {
			bl2 = forcedChunksSavedData.getChunks().add(l);
			if (bl2) {
				this.getChunk(i, j);
			}
		} else {
			bl2 = forcedChunksSavedData.getChunks().remove(l);
		}

		forcedChunksSavedData.setDirty(bl2);
		if (bl2) {
			this.getChunkSource().updateChunkForced(chunkPos, bl);
		}

		return bl2;
	}

	@Override
	public List<ServerPlayer> players() {
		return this.players;
	}

	@Override
	public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		Optional<Holder<PoiType>> optional = PoiTypes.forState(blockState);
		Optional<Holder<PoiType>> optional2 = PoiTypes.forState(blockState2);
		if (!Objects.equals(optional, optional2)) {
			BlockPos blockPos2 = blockPos.immutable();
			optional.ifPresent(holder -> this.getServer().execute(() -> {
					this.getPoiManager().remove(blockPos2);
					DebugPackets.sendPoiRemovedPacket(this, blockPos2);
				}));
			optional2.ifPresent(holder -> this.getServer().execute(() -> {
					this.getPoiManager().add(blockPos2, holder);
					DebugPackets.sendPoiAddedPacket(this, blockPos2);
				}));
		}
	}

	public PoiManager getPoiManager() {
		return this.getChunkSource().getPoiManager();
	}

	public boolean isVillage(BlockPos blockPos) {
		return this.isCloseToVillage(blockPos, 1);
	}

	public boolean isVillage(SectionPos sectionPos) {
		return this.isVillage(sectionPos.center());
	}

	public boolean isCloseToVillage(BlockPos blockPos, int i) {
		return i > 6 ? false : this.sectionsToVillage(SectionPos.of(blockPos)) <= i;
	}

	public int sectionsToVillage(SectionPos sectionPos) {
		return this.getPoiManager().sectionsToVillage(sectionPos);
	}

	public Raids getRaids() {
		return this.raids;
	}

	@Nullable
	public Raid getRaidAt(BlockPos blockPos) {
		return this.raids.getNearbyRaid(blockPos, 9216);
	}

	public boolean isRaided(BlockPos blockPos) {
		return this.getRaidAt(blockPos) != null;
	}

	public void onReputationEvent(ReputationEventType reputationEventType, Entity entity, ReputationEventHandler reputationEventHandler) {
		reputationEventHandler.onReputationEventFrom(reputationEventType, entity);
	}

	public void saveDebugReport(Path path) throws IOException {
		ChunkMap chunkMap = this.getChunkSource().chunkMap;
		Writer writer = Files.newBufferedWriter(path.resolve("stats.txt"));

		try {
			writer.write(String.format(Locale.ROOT, "spawning_chunks: %d\n", chunkMap.getDistanceManager().getNaturalSpawnChunkCount()));
			NaturalSpawner.SpawnState spawnState = this.getChunkSource().getLastSpawnState();
			if (spawnState != null) {
				for (Entry<MobCategory> entry : spawnState.getMobCategoryCounts().object2IntEntrySet()) {
					writer.write(String.format(Locale.ROOT, "spawn_count.%s: %d\n", ((MobCategory)entry.getKey()).getName(), entry.getIntValue()));
				}
			}

			writer.write(String.format(Locale.ROOT, "entities: %s\n", this.entityManager.gatherStats()));
			writer.write(String.format(Locale.ROOT, "block_entity_tickers: %d\n", this.blockEntityTickers.size()));
			writer.write(String.format(Locale.ROOT, "block_ticks: %d\n", this.getBlockTicks().count()));
			writer.write(String.format(Locale.ROOT, "fluid_ticks: %d\n", this.getFluidTicks().count()));
			writer.write("distance_manager: " + chunkMap.getDistanceManager().getDebugStatus() + "\n");
			writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
		} catch (Throwable var22) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable var16) {
					var22.addSuppressed(var16);
				}
			}

			throw var22;
		}

		if (writer != null) {
			writer.close();
		}

		CrashReport crashReport = new CrashReport("Level dump", new Exception("dummy"));
		this.fillReportDetails(crashReport);
		Writer writer2 = Files.newBufferedWriter(path.resolve("example_crash.txt"));

		try {
			writer2.write(crashReport.getFriendlyReport());
		} catch (Throwable var21) {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch (Throwable var15) {
					var21.addSuppressed(var15);
				}
			}

			throw var21;
		}

		if (writer2 != null) {
			writer2.close();
		}

		Path path2 = path.resolve("chunks.csv");
		Writer writer3 = Files.newBufferedWriter(path2);

		try {
			chunkMap.dumpChunks(writer3);
		} catch (Throwable var20) {
			if (writer3 != null) {
				try {
					writer3.close();
				} catch (Throwable var14) {
					var20.addSuppressed(var14);
				}
			}

			throw var20;
		}

		if (writer3 != null) {
			writer3.close();
		}

		Path path3 = path.resolve("entity_chunks.csv");
		Writer writer4 = Files.newBufferedWriter(path3);

		try {
			this.entityManager.dumpSections(writer4);
		} catch (Throwable var19) {
			if (writer4 != null) {
				try {
					writer4.close();
				} catch (Throwable var13) {
					var19.addSuppressed(var13);
				}
			}

			throw var19;
		}

		if (writer4 != null) {
			writer4.close();
		}

		Path path4 = path.resolve("entities.csv");
		Writer writer5 = Files.newBufferedWriter(path4);

		try {
			dumpEntities(writer5, this.getEntities().getAll());
		} catch (Throwable var18) {
			if (writer5 != null) {
				try {
					writer5.close();
				} catch (Throwable var12) {
					var18.addSuppressed(var12);
				}
			}

			throw var18;
		}

		if (writer5 != null) {
			writer5.close();
		}

		Path path5 = path.resolve("block_entities.csv");
		Writer writer6 = Files.newBufferedWriter(path5);

		try {
			this.dumpBlockEntityTickers(writer6);
		} catch (Throwable var17) {
			if (writer6 != null) {
				try {
					writer6.close();
				} catch (Throwable var11) {
					var17.addSuppressed(var11);
				}
			}

			throw var17;
		}

		if (writer6 != null) {
			writer6.close();
		}
	}

	private static void dumpEntities(Writer writer, Iterable<Entity> iterable) throws IOException {
		CsvOutput csvOutput = CsvOutput.builder()
			.addColumn("x")
			.addColumn("y")
			.addColumn("z")
			.addColumn("uuid")
			.addColumn("type")
			.addColumn("alive")
			.addColumn("display_name")
			.addColumn("custom_name")
			.build(writer);

		for (Entity entity : iterable) {
			Component component = entity.getCustomName();
			Component component2 = entity.getDisplayName();
			csvOutput.writeRow(
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				entity.getUUID(),
				BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
				entity.isAlive(),
				component2.getString(),
				component != null ? component.getString() : null
			);
		}
	}

	private void dumpBlockEntityTickers(Writer writer) throws IOException {
		CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(writer);

		for (TickingBlockEntity tickingBlockEntity : this.blockEntityTickers) {
			BlockPos blockPos = tickingBlockEntity.getPos();
			csvOutput.writeRow(blockPos.getX(), blockPos.getY(), blockPos.getZ(), tickingBlockEntity.getType());
		}
	}

	@VisibleForTesting
	public void clearBlockEvents(BoundingBox boundingBox) {
		this.blockEvents.removeIf(blockEventData -> boundingBox.isInside(blockEventData.pos()));
	}

	@Override
	public void blockUpdated(BlockPos blockPos, Block block) {
		if (!this.isDebug()) {
			this.updateNeighborsAt(blockPos, block);
		}
	}

	@Override
	public float getShade(Direction direction, boolean bl) {
		return 1.0F;
	}

	public Iterable<Entity> getAllEntities() {
		return this.getEntities().getAll();
	}

	public String toString() {
		return "ServerLevel[" + this.serverLevelData.getLevelName() + "]";
	}

	public boolean isFlat() {
		return this.server.getWorldData().isFlatWorld();
	}

	@Override
	public long getSeed() {
		return this.server.getWorldData().worldGenOptions().seed();
	}

	@Nullable
	public EndDragonFight getDragonFight() {
		return this.dragonFight;
	}

	@Override
	public ServerLevel getLevel() {
		return this;
	}

	@VisibleForTesting
	public String getWatchdogStats() {
		return String.format(
			Locale.ROOT,
			"players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s",
			this.players.size(),
			this.entityManager.gatherStats(),
			getTypeCount(this.entityManager.getEntityGetter().getAll(), entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()),
			this.blockEntityTickers.size(),
			getTypeCount(this.blockEntityTickers, TickingBlockEntity::getType),
			this.getBlockTicks().count(),
			this.getFluidTicks().count(),
			this.gatherChunkSourceStats()
		);
	}

	private static <T> String getTypeCount(Iterable<T> iterable, Function<T, String> function) {
		try {
			Object2IntOpenHashMap<String> object2IntOpenHashMap = new Object2IntOpenHashMap<>();

			for (T object : iterable) {
				String string = (String)function.apply(object);
				object2IntOpenHashMap.addTo(string, 1);
			}

			return (String)object2IntOpenHashMap.object2IntEntrySet()
				.stream()
				.sorted(Comparator.comparing(Entry::getIntValue).reversed())
				.limit(5L)
				.map(entry -> (String)entry.getKey() + ":" + entry.getIntValue())
				.collect(Collectors.joining(","));
		} catch (Exception var6) {
			return "";
		}
	}

	public static void makeObsidianPlatform(ServerLevel serverLevel) {
		BlockPos blockPos = END_SPAWN_POINT;
		int i = blockPos.getX();
		int j = blockPos.getY() - 2;
		int k = blockPos.getZ();
		BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2)
			.forEach(blockPosx -> serverLevel.setBlockAndUpdate(blockPosx, Blocks.AIR.defaultBlockState()));
		BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach(blockPosx -> serverLevel.setBlockAndUpdate(blockPosx, Blocks.OBSIDIAN.defaultBlockState()));
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return this.entityManager.getEntityGetter();
	}

	public void addLegacyChunkEntities(Stream<Entity> stream) {
		this.entityManager.addLegacyChunkEntities(stream);
	}

	public void addWorldGenChunkEntities(Stream<Entity> stream) {
		this.entityManager.addWorldGenChunkEntities(stream);
	}

	public void startTickingChunk(LevelChunk levelChunk) {
		levelChunk.unpackTicks(this.getLevelData().getGameTime());
	}

	public void onStructureStartsAvailable(ChunkAccess chunkAccess) {
		this.server.execute(() -> this.structureCheck.onStructureLoad(chunkAccess.getPos(), chunkAccess.getAllStarts()));
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.entityManager.close();
	}

	@Override
	public String gatherChunkSourceStats() {
		return "Chunks[S] W: " + this.chunkSource.gatherStats() + " E: " + this.entityManager.gatherStats();
	}

	public boolean areEntitiesLoaded(long l) {
		return this.entityManager.areEntitiesLoaded(l);
	}

	private boolean isPositionTickingWithEntitiesLoaded(long l) {
		return this.areEntitiesLoaded(l) && this.chunkSource.isPositionTicking(l);
	}

	public boolean isPositionEntityTicking(BlockPos blockPos) {
		return this.entityManager.canPositionTick(blockPos) && this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(ChunkPos.asLong(blockPos));
	}

	public boolean isNaturalSpawningAllowed(BlockPos blockPos) {
		return this.entityManager.canPositionTick(blockPos);
	}

	public boolean isNaturalSpawningAllowed(ChunkPos chunkPos) {
		return this.entityManager.canPositionTick(chunkPos);
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return this.server.getWorldData().enabledFeatures();
	}

	public RandomSource getRandomSequence(ResourceLocation resourceLocation) {
		return this.randomSequences.get(resourceLocation);
	}

	public RandomSequences getRandomSequences() {
		return this.randomSequences;
	}

	@Override
	public CrashReportCategory fillReportDetails(CrashReport crashReport) {
		CrashReportCategory crashReportCategory = super.fillReportDetails(crashReport);
		crashReportCategory.setDetail("Loaded entity count", (CrashReportDetail<String>)(() -> String.valueOf(this.entityManager.count())));
		return crashReportCategory;
	}

	final class EntityCallbacks implements LevelCallback<Entity> {
		public void onCreated(Entity entity) {
		}

		public void onDestroyed(Entity entity) {
			ServerLevel.this.getScoreboard().entityRemoved(entity);
		}

		public void onTickingStart(Entity entity) {
			ServerLevel.this.entityTickList.add(entity);
		}

		public void onTickingEnd(Entity entity) {
			ServerLevel.this.entityTickList.remove(entity);
		}

		public void onTrackingStart(Entity entity) {
			ServerLevel.this.getChunkSource().addEntity(entity);
			if (entity instanceof ServerPlayer serverPlayer) {
				ServerLevel.this.players.add(serverPlayer);
				ServerLevel.this.updateSleepingPlayerList();
			}

			if (entity instanceof Mob mob) {
				if (ServerLevel.this.isUpdatingNavigations) {
					String string = "onTrackingStart called during navigation iteration";
					Util.logAndPauseIfInIde(
						"onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration")
					);
				}

				ServerLevel.this.navigatingMobs.add(mob);
			}

			if (entity instanceof EnderDragon enderDragon) {
				for (EnderDragonPart enderDragonPart : enderDragon.getSubEntities()) {
					ServerLevel.this.dragonParts.put(enderDragonPart.getId(), enderDragonPart);
				}
			}

			entity.updateDynamicGameEventListener(DynamicGameEventListener::add);
		}

		public void onTrackingEnd(Entity entity) {
			ServerLevel.this.getChunkSource().removeEntity(entity);
			if (entity instanceof ServerPlayer serverPlayer) {
				ServerLevel.this.players.remove(serverPlayer);
				ServerLevel.this.updateSleepingPlayerList();
			}

			if (entity instanceof Mob mob) {
				if (ServerLevel.this.isUpdatingNavigations) {
					String string = "onTrackingStart called during navigation iteration";
					Util.logAndPauseIfInIde(
						"onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration")
					);
				}

				ServerLevel.this.navigatingMobs.remove(mob);
			}

			if (entity instanceof EnderDragon enderDragon) {
				for (EnderDragonPart enderDragonPart : enderDragon.getSubEntities()) {
					ServerLevel.this.dragonParts.remove(enderDragonPart.getId());
				}
			}

			entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
		}

		public void onSectionChange(Entity entity) {
			entity.updateDynamicGameEventListener(DynamicGameEventListener::move);
		}
	}
}
