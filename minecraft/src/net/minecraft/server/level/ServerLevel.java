package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
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
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLevel extends Level implements WorldGenLevel {
	public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<ServerPlayer> players = Lists.<ServerPlayer>newArrayList();
	private final ServerChunkCache chunkSource;
	private final MinecraftServer server;
	private final ServerLevelData serverLevelData;
	private final EntityTickList entityTickList = new EntityTickList();
	private final PersistentEntitySectionManager<Entity> entityManager;
	public boolean noSave;
	private boolean allPlayersSleeping;
	private int emptyTime;
	private final PortalForcer portalForcer;
	private final ServerTickList<Block> blockTicks = new ServerTickList<>(
		this, block -> block == null || block.defaultBlockState().isAir(), Registry.BLOCK::getKey, this::tickBlock
	);
	private final ServerTickList<Fluid> liquidTicks = new ServerTickList<>(
		this, fluid -> fluid == null || fluid == Fluids.EMPTY, Registry.FLUID::getKey, this::tickLiquid
	);
	private final Set<Mob> navigatingMobs = new ObjectOpenHashSet<>();
	protected final Raids raids;
	private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
	private boolean handlingTick;
	private final List<CustomSpawner> customSpawners;
	@Nullable
	private final EndDragonFight dragonFight;
	private final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap<>();
	private final StructureFeatureManager structureFeatureManager;
	private final boolean tickTime;

	public ServerLevel(
		MinecraftServer minecraftServer,
		Executor executor,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		ServerLevelData serverLevelData,
		ResourceKey<Level> resourceKey,
		DimensionType dimensionType,
		ChunkProgressListener chunkProgressListener,
		ChunkGenerator chunkGenerator,
		boolean bl,
		long l,
		List<CustomSpawner> list,
		boolean bl2
	) {
		super(serverLevelData, resourceKey, dimensionType, minecraftServer::getProfiler, false, bl, l);
		this.tickTime = bl2;
		this.server = minecraftServer;
		this.customSpawners = list;
		this.serverLevelData = serverLevelData;
		boolean bl3 = minecraftServer.forceSynchronousWrites();
		DataFixer dataFixer = minecraftServer.getFixerUpper();
		EntityPersistentStorage<Entity> entityPersistentStorage = new EntityStorage(
			this, new File(levelStorageAccess.getDimensionPath(resourceKey), "entities"), dataFixer, bl3, minecraftServer
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
			bl3,
			chunkProgressListener,
			this.entityManager::updateChunkStatus,
			() -> minecraftServer.overworld().getDataStorage()
		);
		this.portalForcer = new PortalForcer(this);
		this.updateSkyBrightness();
		this.prepareWeather();
		this.getWorldBorder().setAbsoluteMaxSize(minecraftServer.getAbsoluteMaxWorldSize());
		this.raids = this.getDataStorage()
			.computeIfAbsent(compoundTag -> Raids.load(this, compoundTag), () -> new Raids(this), Raids.getFileId(this.dimensionType()));
		if (!minecraftServer.isSingleplayer()) {
			serverLevelData.setGameType(minecraftServer.getDefaultGameType());
		}

		this.structureFeatureManager = new StructureFeatureManager(this, minecraftServer.getWorldData().worldGenSettings());
		if (this.dimensionType().createDragonFight()) {
			this.dragonFight = new EndDragonFight(this, minecraftServer.getWorldData().worldGenSettings().seed(), minecraftServer.getWorldData().endDragonFightData());
		} else {
			this.dragonFight = null;
		}
	}

	public void setWeatherParameters(int i, int j, boolean bl, boolean bl2) {
		this.serverLevelData.setClearWeatherTime(i);
		this.serverLevelData.setRainTime(j);
		this.serverLevelData.setThunderTime(j);
		this.serverLevelData.setRaining(bl);
		this.serverLevelData.setThundering(bl2);
	}

	@Override
	public Biome getUncachedNoiseBiome(int i, int j, int k) {
		return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(i, j, k);
	}

	public StructureFeatureManager structureFeatureManager() {
		return this.structureFeatureManager;
	}

	public void tick(BooleanSupplier booleanSupplier) {
		ProfilerFiller profilerFiller = this.getProfiler();
		this.handlingTick = true;
		profilerFiller.push("world border");
		this.getWorldBorder().tick();
		profilerFiller.popPush("weather");
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
						j = this.random.nextInt(12000) + 3600;
					} else {
						j = this.random.nextInt(168000) + 12000;
					}

					if (k > 0) {
						if (--k == 0) {
							bl3 = !bl3;
						}
					} else if (bl3) {
						k = this.random.nextInt(12000) + 12000;
					} else {
						k = this.random.nextInt(168000) + 12000;
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
				this.thunderLevel = (float)((double)this.thunderLevel + 0.01);
			} else {
				this.thunderLevel = (float)((double)this.thunderLevel - 0.01);
			}

			this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0F, 1.0F);
			this.oRainLevel = this.rainLevel;
			if (this.levelData.isRaining()) {
				this.rainLevel = (float)((double)this.rainLevel + 0.01);
			} else {
				this.rainLevel = (float)((double)this.rainLevel - 0.01);
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

		if (this.allPlayersSleeping && this.players.stream().noneMatch(serverPlayer -> !serverPlayer.isSpectator() && !serverPlayer.isSleepingLongEnough())) {
			this.allPlayersSleeping = false;
			if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
				long l = this.levelData.getDayTime() + 24000L;
				this.setDayTime(l - l % 24000L);
			}

			this.wakeUpAllPlayers();
			if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
				this.stopWeather();
			}
		}

		this.updateSkyBrightness();
		this.tickTime();
		profilerFiller.popPush("chunkSource");
		this.getChunkSource().tick(booleanSupplier);
		profilerFiller.popPush("tickPending");
		if (!this.isDebug()) {
			this.blockTicks.tick();
			this.liquidTicks.tick();
		}

		profilerFiller.popPush("raid");
		this.raids.tick();
		profilerFiller.popPush("blockEvents");
		this.runBlockEvents();
		this.handlingTick = false;
		profilerFiller.pop();
		boolean bl4 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
		if (bl4) {
			this.resetEmptyTime();
		}

		if (bl4 || this.emptyTime++ < 300) {
			profilerFiller.push("entities");
			if (this.dragonFight != null) {
				profilerFiller.push("dragonFight");
				this.dragonFight.tick();
				profilerFiller.pop();
			}

			this.entityTickList.forEach(entity -> {
				if (!entity.isRemoved()) {
					if (this.shouldDiscardEntity(entity)) {
						entity.discard();
					} else {
						profilerFiller.push("checkDespawn");
						entity.checkDespawn();
						profilerFiller.pop();
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
			});
			profilerFiller.pop();
			this.tickBlockEntities();
		}

		profilerFiller.push("entityManagement");
		this.entityManager.tick();
		profilerFiller.pop();
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
					&& this.random.nextDouble() < (double)difficultyInstance.getEffectiveDifficulty() * 0.01;
				if (bl2) {
					SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this);
					skeletonHorse.setTrap(true);
					skeletonHorse.setAge(0);
					skeletonHorse.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
					this.addFreshEntity(skeletonHorse);
				}

				LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this);
				lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
				lightningBolt.setVisualOnly(bl2);
				this.addFreshEntity(lightningBolt);
			}
		}

		profilerFiller.popPush("iceandsnow");
		if (this.random.nextInt(16) == 0) {
			BlockPos blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(j, 0, k, 15));
			BlockPos blockPos2 = blockPos.below();
			Biome biome = this.getBiome(blockPos);
			if (biome.shouldFreeze(this, blockPos2)) {
				this.setBlockAndUpdate(blockPos2, Blocks.ICE.defaultBlockState());
			}

			if (bl) {
				if (biome.shouldSnow(this, blockPos)) {
					this.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
				}

				BlockState blockState = this.getBlockState(blockPos2);
				Biome.Precipitation precipitation = this.getBiome(blockPos2).getPrecipitation();
				blockState.getBlock().handlePrecipitation(blockState, this, blockPos2, precipitation);
			}
		}

		profilerFiller.popPush("tickBlocks");
		if (i > 0) {
			for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
				if (levelChunkSection != LevelChunk.EMPTY_SECTION && levelChunkSection.isRandomlyTicking()) {
					int l = levelChunkSection.bottomBlockY();

					for (int m = 0; m < i; m++) {
						BlockPos blockPos3 = this.getBlockRandomPos(j, l, k, 15);
						profilerFiller.push("randomTick");
						BlockState blockState2 = levelChunkSection.getBlockState(blockPos3.getX() - j, blockPos3.getY() - l, blockPos3.getZ() - k);
						if (blockState2.isRandomlyTicking()) {
							blockState2.randomTick(this, blockPos3, this.random);
						}

						FluidState fluidState = blockState2.getFluidState();
						if (fluidState.isRandomlyTicking()) {
							fluidState.randomTick(this, blockPos3, this.random);
						}

						profilerFiller.pop();
					}
				}
			}
		}

		profilerFiller.pop();
	}

	private Optional<BlockPos> findLightningRod(BlockPos blockPos) {
		Optional<BlockPos> optional = this.getPoiManager().findClosest(poiType -> poiType == PoiType.LIGHTNING_ROD, blockPos, 128, PoiManager.Occupancy.ANY);
		if (optional.isPresent()) {
			BlockPos blockPos2 = (BlockPos)optional.get();
			int i = this.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, blockPos2.getX(), blockPos2.getZ()) - 1;
			if (blockPos2.getY() == i) {
				return Optional.of(blockPos2.above(1));
			}

			BlockPos blockPos3 = new BlockPos(blockPos2.getX(), i, blockPos2.getZ());
			if (this.getLevel().getBlockState(blockPos3).is(Blocks.LIGHTNING_ROD)) {
				return Optional.of(blockPos3.above(1));
			}
		}

		return Optional.empty();
	}

	protected BlockPos findLightningTargetAround(BlockPos blockPos) {
		BlockPos blockPos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos);
		Optional<BlockPos> optional = this.findLightningRod(blockPos2);
		if (optional.isPresent()) {
			return (BlockPos)optional.get();
		} else {
			AABB aABB = new AABB(blockPos2, new BlockPos(blockPos2.getX(), this.getMaxBuildHeight(), blockPos2.getZ())).inflate(3.0);
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

	public void updateSleepingPlayerList() {
		this.allPlayersSleeping = false;
		if (!this.players.isEmpty()) {
			int i = 0;
			int j = 0;

			for (ServerPlayer serverPlayer : this.players) {
				if (serverPlayer.isSpectator()) {
					i++;
				} else if (serverPlayer.isSleeping()) {
					j++;
				}
			}

			this.allPlayersSleeping = j > 0 && j >= this.players.size() - i;
		}
	}

	public ServerScoreboard getScoreboard() {
		return this.server.getScoreboard();
	}

	private void stopWeather() {
		this.serverLevelData.setRainTime(0);
		this.serverLevelData.setRaining(false);
		this.serverLevelData.setThunderTime(0);
		this.serverLevelData.setThundering(false);
	}

	public void resetEmptyTime() {
		this.emptyTime = 0;
	}

	private void tickLiquid(TickNextTickData<Fluid> tickNextTickData) {
		FluidState fluidState = this.getFluidState(tickNextTickData.pos);
		if (fluidState.getType() == tickNextTickData.getType()) {
			fluidState.tick(this, tickNextTickData.pos);
		}
	}

	private void tickBlock(TickNextTickData<Block> tickNextTickData) {
		BlockState blockState = this.getBlockState(tickNextTickData.pos);
		if (blockState.is(tickNextTickData.getType())) {
			blockState.tick(this, tickNextTickData.pos, this.random);
		}
	}

	public void tickNonPassenger(Entity entity) {
		entity.setPosAndOldPos(entity.getX(), entity.getY(), entity.getZ());
		entity.yRotO = entity.yRot;
		entity.xRotO = entity.xRot;
		ProfilerFiller profilerFiller = this.getProfiler();
		entity.tickCount++;
		this.getProfiler().push((Supplier<String>)(() -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString()));
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
			entity2.setPosAndOldPos(entity2.getX(), entity2.getY(), entity2.getZ());
			entity2.yRotO = entity2.yRot;
			entity2.xRotO = entity2.xRot;
			entity2.tickCount++;
			ProfilerFiller profilerFiller = this.getProfiler();
			profilerFiller.push((Supplier<String>)(() -> Registry.ENTITY_TYPE.getKey(entity2.getType()).toString()));
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
				progressListener.progressStartNoAbort(new TranslatableComponent("menu.savingLevel"));
			}

			this.saveLevelData();
			if (progressListener != null) {
				progressListener.progressStage(new TranslatableComponent("menu.savingChunks"));
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
		this.getEntities().get(entityTypeTest, entity -> {
			if (predicate.test(entity)) {
				list.add(entity);
			}
		});
		return list;
	}

	public List<? extends EnderDragon> getDragons() {
		return this.getEntities(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
	}

	public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate) {
		List<ServerPlayer> list = Lists.<ServerPlayer>newArrayList();

		for (ServerPlayer serverPlayer : this.players) {
			if (predicate.test(serverPlayer)) {
				list.add(serverPlayer);
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
			LOGGER.warn("Force-added player with duplicate UUID {}", serverPlayer.getUUID().toString());
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
		levelChunk.invalidateAllBlockEntities();
	}

	public void removePlayerImmediately(ServerPlayer serverPlayer, Entity.RemovalReason removalReason) {
		serverPlayer.remove(removalReason);
	}

	@Override
	public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
		for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
			if (serverPlayer != null && serverPlayer.level == this && serverPlayer.getId() != i) {
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
	public void playSound(@Nullable Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h) {
		this.server
			.getPlayerList()
			.broadcast(player, d, e, f, g > 1.0F ? (double)(16.0F * g) : 16.0, this.dimension(), new ClientboundSoundPacket(soundEvent, soundSource, d, e, f, g, h));
	}

	@Override
	public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
		this.server
			.getPlayerList()
			.broadcast(
				player,
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				f > 1.0F ? (double)(16.0F * f) : 16.0,
				this.dimension(),
				new ClientboundSoundEntityPacket(soundEvent, soundSource, entity, f, g)
			);
	}

	@Override
	public void globalLevelEvent(int i, BlockPos blockPos, int j) {
		this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(i, blockPos, j, true));
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

	@Override
	public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
		this.getChunkSource().blockChanged(blockPos);
		VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos);
		VoxelShape voxelShape2 = blockState2.getCollisionShape(this, blockPos);
		if (Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.NOT_SAME)) {
			for (Mob mob : this.navigatingMobs) {
				PathNavigation pathNavigation = mob.getNavigation();
				if (!pathNavigation.hasDelayedRecomputation()) {
					pathNavigation.recomputePath(blockPos);
				}
			}
		}
	}

	@Override
	public void broadcastEntityEvent(Entity entity, byte b) {
		this.getChunkSource().broadcastAndSend(entity, new ClientboundEntityEventPacket(entity, b));
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
		Explosion.BlockInteraction blockInteraction
	) {
		Explosion explosion = new Explosion(this, entity, damageSource, explosionDamageCalculator, d, e, f, g, bl, blockInteraction);
		explosion.explode();
		explosion.finalizeExplosion(false);
		if (blockInteraction == Explosion.BlockInteraction.NONE) {
			explosion.clearToBlow();
		}

		for (ServerPlayer serverPlayer : this.players) {
			if (serverPlayer.distanceToSqr(d, e, f) < 4096.0) {
				serverPlayer.connection.send(new ClientboundExplodePacket(d, e, f, g, explosion.getToBlow(), (Vec3)explosion.getHitPlayers().get(serverPlayer)));
			}
		}

		return explosion;
	}

	@Override
	public void blockEvent(BlockPos blockPos, Block block, int i, int j) {
		this.blockEvents.add(new BlockEventData(blockPos, block, i, j));
	}

	private void runBlockEvents() {
		while (!this.blockEvents.isEmpty()) {
			BlockEventData blockEventData = this.blockEvents.removeFirst();
			if (this.doBlockEvent(blockEventData)) {
				this.server
					.getPlayerList()
					.broadcast(
						null,
						(double)blockEventData.getPos().getX(),
						(double)blockEventData.getPos().getY(),
						(double)blockEventData.getPos().getZ(),
						64.0,
						this.dimension(),
						new ClientboundBlockEventPacket(blockEventData.getPos(), blockEventData.getBlock(), blockEventData.getParamA(), blockEventData.getParamB())
					);
			}
		}
	}

	private boolean doBlockEvent(BlockEventData blockEventData) {
		BlockState blockState = this.getBlockState(blockEventData.getPos());
		return blockState.is(blockEventData.getBlock())
			? blockState.triggerEvent(this, blockEventData.getPos(), blockEventData.getParamA(), blockEventData.getParamB())
			: false;
	}

	public ServerTickList<Block> getBlockTicks() {
		return this.blockTicks;
	}

	public ServerTickList<Fluid> getLiquidTicks() {
		return this.liquidTicks;
	}

	@Nonnull
	@Override
	public MinecraftServer getServer() {
		return this.server;
	}

	public PortalForcer getPortalForcer() {
		return this.portalForcer;
	}

	public StructureManager getStructureManager() {
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
		if (serverPlayer.getLevel() != this) {
			return false;
		} else {
			BlockPos blockPos = serverPlayer.blockPosition();
			if (blockPos.closerThan(new Vec3(d, e, f), bl ? 512.0 : 32.0)) {
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
	public BlockPos findNearestMapFeature(StructureFeature<?> structureFeature, BlockPos blockPos, int i, boolean bl) {
		return !this.server.getWorldData().worldGenSettings().generateFeatures()
			? null
			: this.getChunkSource().getGenerator().findNearestMapFeature(this, structureFeature, blockPos, i, bl);
	}

	@Nullable
	public BlockPos findNearestBiome(Biome biome, BlockPos blockPos, int i, int j) {
		return this.getChunkSource()
			.getGenerator()
			.getBiomeSource()
			.findBiomeHorizontal(blockPos.getX(), blockPos.getY(), blockPos.getZ(), i, j, biome2 -> biome2 == biome, this.random, true);
	}

	@Override
	public RecipeManager getRecipeManager() {
		return this.server.getRecipeManager();
	}

	@Override
	public TagContainer getTagManager() {
		return this.server.getTags();
	}

	@Override
	public boolean noSave() {
		return this.noSave;
	}

	@Override
	public RegistryAccess registryAccess() {
		return this.server.registryAccess();
	}

	public DimensionDataStorage getDataStorage() {
		return this.getChunkSource().getDataStorage();
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(String string) {
		return this.getServer().overworld().getDataStorage().get(MapItemSavedData::load, string);
	}

	@Override
	public void setMapData(String string, MapItemSavedData mapItemSavedData) {
		this.getServer().overworld().getDataStorage().set(string, mapItemSavedData);
	}

	@Override
	public int getFreeMapId() {
		return this.getServer().overworld().getDataStorage().<MapIndex>computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
	}

	public void setDefaultSpawnPos(BlockPos blockPos, float f) {
		ChunkPos chunkPos = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
		this.levelData.setSpawn(blockPos, f);
		this.getChunkSource().removeRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
		this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
		this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(blockPos, f));
	}

	public BlockPos getSharedSpawnPos() {
		BlockPos blockPos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
		if (!this.getWorldBorder().isWithinBounds(blockPos)) {
			blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
		}

		return blockPos;
	}

	public float getSharedSpawnAngle() {
		return this.levelData.getSpawnAngle();
	}

	public LongSet getForcedChunks() {
		ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
		return (LongSet)(forcedChunksSavedData != null ? LongSets.unmodifiable(forcedChunksSavedData.getChunks()) : LongSets.EMPTY_SET);
	}

	public boolean setChunkForced(int i, int j, boolean bl) {
		ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
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
		Optional<PoiType> optional = PoiType.forState(blockState);
		Optional<PoiType> optional2 = PoiType.forState(blockState2);
		if (!Objects.equals(optional, optional2)) {
			BlockPos blockPos2 = blockPos.immutable();
			optional.ifPresent(poiType -> this.getServer().execute(() -> {
					this.getPoiManager().remove(blockPos2);
					DebugPackets.sendPoiRemovedPacket(this, blockPos2);
				}));
			optional2.ifPresent(poiType -> this.getServer().execute(() -> {
					this.getPoiManager().add(blockPos2, poiType);
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
		Throwable path2 = null;

		try {
			writer.write(String.format("spawning_chunks: %d\n", chunkMap.getDistanceManager().getNaturalSpawnChunkCount()));
			NaturalSpawner.SpawnState spawnState = this.getChunkSource().getLastSpawnState();
			if (spawnState != null) {
				for (Entry<MobCategory> entry : spawnState.getMobCategoryCounts().object2IntEntrySet()) {
					writer.write(String.format("spawn_count.%s: %d\n", ((MobCategory)entry.getKey()).getName(), entry.getIntValue()));
				}
			}

			writer.write(String.format("entities: %s\n", this.entityManager.gatherStats()));
			writer.write(String.format("block_entity_tickers: %d\n", this.blockEntityTickers.size()));
			writer.write(String.format("block_ticks: %d\n", this.getBlockTicks().size()));
			writer.write(String.format("fluid_ticks: %d\n", this.getLiquidTicks().size()));
			writer.write("distance_manager: " + chunkMap.getDistanceManager().getDebugStatus() + "\n");
			writer.write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
		} catch (Throwable var165) {
			path2 = var165;
			throw var165;
		} finally {
			if (writer != null) {
				if (path2 != null) {
					try {
						writer.close();
					} catch (Throwable var154) {
						path2.addSuppressed(var154);
					}
				} else {
					writer.close();
				}
			}
		}

		CrashReport crashReport = new CrashReport("Level dump", new Exception("dummy"));
		this.fillReportDetails(crashReport);
		Writer writer2 = Files.newBufferedWriter(path.resolve("example_crash.txt"));
		Throwable var170 = null;

		try {
			writer2.write(crashReport.getFriendlyReport());
		} catch (Throwable var159) {
			var170 = var159;
			throw var159;
		} finally {
			if (writer2 != null) {
				if (var170 != null) {
					try {
						writer2.close();
					} catch (Throwable var153) {
						var170.addSuppressed(var153);
					}
				} else {
					writer2.close();
				}
			}
		}

		Path path2x = path.resolve("chunks.csv");
		Writer writer3 = Files.newBufferedWriter(path2x);
		Throwable var173 = null;

		try {
			chunkMap.dumpChunks(writer3);
		} catch (Throwable var158) {
			var173 = var158;
			throw var158;
		} finally {
			if (writer3 != null) {
				if (var173 != null) {
					try {
						writer3.close();
					} catch (Throwable var152) {
						var173.addSuppressed(var152);
					}
				} else {
					writer3.close();
				}
			}
		}

		Path path3 = path.resolve("entity_chunks.csv");
		Writer writer4 = Files.newBufferedWriter(path3);
		Throwable var176 = null;

		try {
			this.entityManager.dumpSections(writer4);
		} catch (Throwable var157) {
			var176 = var157;
			throw var157;
		} finally {
			if (writer4 != null) {
				if (var176 != null) {
					try {
						writer4.close();
					} catch (Throwable var151) {
						var176.addSuppressed(var151);
					}
				} else {
					writer4.close();
				}
			}
		}

		Path path4 = path.resolve("entities.csv");
		Writer writer5 = Files.newBufferedWriter(path4);
		Throwable writer6 = null;

		try {
			dumpEntities(writer5, this.getEntities().getAll());
		} catch (Throwable var156) {
			writer6 = var156;
			throw var156;
		} finally {
			if (writer5 != null) {
				if (writer6 != null) {
					try {
						writer5.close();
					} catch (Throwable var150) {
						writer6.addSuppressed(var150);
					}
				} else {
					writer5.close();
				}
			}
		}

		Path path5 = path.resolve("block_entities.csv");
		Writer writer6x = Files.newBufferedWriter(path5);
		Throwable var9 = null;

		try {
			this.dumpBlockEntityTickers(writer6x);
		} catch (Throwable var155) {
			var9 = var155;
			throw var155;
		} finally {
			if (writer6x != null) {
				if (var9 != null) {
					try {
						writer6x.close();
					} catch (Throwable var149) {
						var9.addSuppressed(var149);
					}
				} else {
					writer6x.close();
				}
			}
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
				Registry.ENTITY_TYPE.getKey(entity.getType()),
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
		this.blockEvents.removeIf(blockEventData -> boundingBox.isInside(blockEventData.getPos()));
	}

	@Override
	public void blockUpdated(BlockPos blockPos, Block block) {
		if (!this.isDebug()) {
			this.updateNeighborsAt(blockPos, block);
		}
	}

	@Environment(EnvType.CLIENT)
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
		return this.server.getWorldData().worldGenSettings().isFlatWorld();
	}

	@Override
	public long getSeed() {
		return this.server.getWorldData().worldGenSettings().seed();
	}

	@Nullable
	public EndDragonFight dragonFight() {
		return this.dragonFight;
	}

	@Override
	public Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
		return this.structureFeatureManager().startsForFeature(sectionPos, structureFeature);
	}

	@Override
	public ServerLevel getLevel() {
		return this;
	}

	@VisibleForTesting
	public String getWatchdogStats() {
		return String.format(
			"players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s",
			this.players.size(),
			this.entityManager.gatherStats(),
			getTypeCount(this.entityManager.getEntityGetter().getAll(), entity -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString()),
			this.blockEntityTickers.size(),
			getTypeCount(this.blockEntityTickers, TickingBlockEntity::getType),
			this.getBlockTicks().size(),
			this.getLiquidTicks().size(),
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

	@Override
	public void close() throws IOException {
		super.close();
		this.entityManager.close();
	}

	public String gatherChunkSourceStats() {
		return "Chunks[S] W: " + this.chunkSource.gatherStats() + " E: " + this.entityManager.gatherStats();
	}

	final class EntityCallbacks implements LevelCallback<Entity> {
		private EntityCallbacks() {
		}

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
			if (entity instanceof ServerPlayer) {
				ServerLevel.this.players.add((ServerPlayer)entity);
				ServerLevel.this.updateSleepingPlayerList();
			}

			if (entity instanceof Mob) {
				ServerLevel.this.navigatingMobs.add((Mob)entity);
			}

			if (entity instanceof EnderDragon) {
				for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
					ServerLevel.this.dragonParts.put(enderDragonPart.getId(), enderDragonPart);
				}
			}
		}

		public void onTrackingEnd(Entity entity) {
			ServerLevel.this.getChunkSource().removeEntity(entity);
			if (entity instanceof ServerPlayer) {
				ServerPlayer serverPlayer = (ServerPlayer)entity;
				ServerLevel.this.players.remove(serverPlayer);
				ServerLevel.this.updateSleepingPlayerList();
			}

			if (entity instanceof Mob) {
				ServerLevel.this.navigatingMobs.remove(entity);
			}

			if (entity instanceof EnderDragon) {
				for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
					ServerLevel.this.dragonParts.remove(enderDragonPart.getId());
				}
			}
		}
	}
}
