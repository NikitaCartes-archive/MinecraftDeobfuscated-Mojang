package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientLevel extends Level {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final double FLUID_PARTICLE_SPAWN_OFFSET = 0.05;
	private static final int NORMAL_LIGHT_UPDATES_PER_FRAME = 10;
	private static final int LIGHT_UPDATE_QUEUE_SIZE_THRESHOLD = 1000;
	final EntityTickList tickingEntities = new EntityTickList();
	private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new ClientLevel.EntityCallbacks());
	private final ClientPacketListener connection;
	private final LevelRenderer levelRenderer;
	private final ClientLevel.ClientLevelData clientLevelData;
	private final DimensionSpecialEffects effects;
	private final Minecraft minecraft = Minecraft.getInstance();
	final List<AbstractClientPlayer> players = Lists.<AbstractClientPlayer>newArrayList();
	private Scoreboard scoreboard = new Scoreboard();
	private final Map<String, MapItemSavedData> mapData = Maps.<String, MapItemSavedData>newHashMap();
	private static final long CLOUD_COLOR = 16777215L;
	private int skyFlashTime;
	private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(
		new Object2ObjectArrayMap<>(3),
		object2ObjectArrayMap -> {
			object2ObjectArrayMap.put(
				BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache(blockPos -> this.calculateBlockTint(blockPos, BiomeColors.GRASS_COLOR_RESOLVER))
			);
			object2ObjectArrayMap.put(
				BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache(blockPos -> this.calculateBlockTint(blockPos, BiomeColors.FOLIAGE_COLOR_RESOLVER))
			);
			object2ObjectArrayMap.put(
				BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache(blockPos -> this.calculateBlockTint(blockPos, BiomeColors.WATER_COLOR_RESOLVER))
			);
		}
	);
	private final ClientChunkCache chunkSource;
	private final Deque<Runnable> lightUpdateQueue = Queues.<Runnable>newArrayDeque();
	private int serverSimulationDistance;
	private final BlockStatePredictionHandler blockStatePredictionHandler = new BlockStatePredictionHandler();
	private static final Set<Item> MARKER_PARTICLE_ITEMS = Set.of(Items.BARRIER, Items.LIGHT);

	public void handleBlockChangedAck(int i) {
		this.blockStatePredictionHandler.endPredictionsUpTo(i, this);
	}

	public void setServerVerifiedBlockState(BlockPos blockPos, BlockState blockState, int i) {
		if (!this.blockStatePredictionHandler.updateKnownServerState(blockPos, blockState)) {
			super.setBlock(blockPos, blockState, i, 512);
		}
	}

	public void syncBlockState(BlockPos blockPos, BlockState blockState, Vec3 vec3) {
		BlockState blockState2 = this.getBlockState(blockPos);
		if (blockState2 != blockState) {
			this.setBlock(blockPos, blockState, 19);
			Player player = this.minecraft.player;
			if (this == player.level && player.isColliding(blockPos, blockState)) {
				player.absMoveTo(vec3.x, vec3.y, vec3.z);
			}
		}
	}

	BlockStatePredictionHandler getBlockStatePredictionHandler() {
		return this.blockStatePredictionHandler;
	}

	@Override
	public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
		if (this.blockStatePredictionHandler.isPredicting()) {
			BlockState blockState2 = this.getBlockState(blockPos);
			boolean bl = super.setBlock(blockPos, blockState, i, j);
			if (bl) {
				this.blockStatePredictionHandler.retainKnownServerState(blockPos, blockState2, this.minecraft.player);
			}

			return bl;
		} else {
			return super.setBlock(blockPos, blockState, i, j);
		}
	}

	public ClientLevel(
		ClientPacketListener clientPacketListener,
		ClientLevel.ClientLevelData clientLevelData,
		ResourceKey<Level> resourceKey,
		Holder<DimensionType> holder,
		int i,
		int j,
		Supplier<ProfilerFiller> supplier,
		LevelRenderer levelRenderer,
		boolean bl,
		long l
	) {
		super(clientLevelData, resourceKey, holder, supplier, true, bl, l, 1000000);
		this.connection = clientPacketListener;
		this.chunkSource = new ClientChunkCache(this, i);
		this.clientLevelData = clientLevelData;
		this.levelRenderer = levelRenderer;
		this.effects = DimensionSpecialEffects.forType(holder.value());
		this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
		this.serverSimulationDistance = j;
		this.updateSkyBrightness();
		this.prepareWeather();
	}

	public void queueLightUpdate(Runnable runnable) {
		this.lightUpdateQueue.add(runnable);
	}

	public void pollLightUpdates() {
		int i = this.lightUpdateQueue.size();
		int j = i < 1000 ? Math.max(10, i / 10) : i;

		for (int k = 0; k < j; k++) {
			Runnable runnable = (Runnable)this.lightUpdateQueue.poll();
			if (runnable == null) {
				break;
			}

			runnable.run();
		}
	}

	public boolean isLightUpdateQueueEmpty() {
		return this.lightUpdateQueue.isEmpty();
	}

	public DimensionSpecialEffects effects() {
		return this.effects;
	}

	public void tick(BooleanSupplier booleanSupplier) {
		this.getWorldBorder().tick();
		this.tickTime();
		this.getProfiler().push("blocks");
		this.chunkSource.tick(booleanSupplier, true);
		this.getProfiler().pop();
	}

	private void tickTime() {
		this.setGameTime(this.levelData.getGameTime() + 1L);
		if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
			this.setDayTime(this.levelData.getDayTime() + 1L);
		}
	}

	public void setGameTime(long l) {
		this.clientLevelData.setGameTime(l);
	}

	public void setDayTime(long l) {
		if (l < 0L) {
			l = -l;
			this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, null);
		} else {
			this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, null);
		}

		this.clientLevelData.setDayTime(l);
	}

	public Iterable<Entity> entitiesForRendering() {
		return this.getEntities().getAll();
	}

	public void tickEntities() {
		ProfilerFiller profilerFiller = this.getProfiler();
		profilerFiller.push("entities");
		this.tickingEntities.forEach(entity -> {
			if (!entity.isRemoved() && !entity.isPassenger()) {
				this.guardEntityTick(this::tickNonPassenger, entity);
			}
		});
		profilerFiller.pop();
		this.tickBlockEntities();
	}

	@Override
	public boolean shouldTickDeath(Entity entity) {
		return entity.chunkPosition().getChessboardDistance(this.minecraft.player.chunkPosition()) <= this.serverSimulationDistance;
	}

	public void tickNonPassenger(Entity entity) {
		entity.setOldPosAndRot();
		entity.tickCount++;
		this.getProfiler().push((Supplier<String>)(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()));
		entity.tick();
		this.getProfiler().pop();

		for (Entity entity2 : entity.getPassengers()) {
			this.tickPassenger(entity, entity2);
		}
	}

	private void tickPassenger(Entity entity, Entity entity2) {
		if (entity2.isRemoved() || entity2.getVehicle() != entity) {
			entity2.stopRiding();
		} else if (entity2 instanceof Player || this.tickingEntities.contains(entity2)) {
			entity2.setOldPosAndRot();
			entity2.tickCount++;
			entity2.rideTick();

			for (Entity entity3 : entity2.getPassengers()) {
				this.tickPassenger(entity2, entity3);
			}
		}
	}

	public void unload(LevelChunk levelChunk) {
		levelChunk.clearAllBlockEntities();
		this.chunkSource.getLightEngine().enableLightSources(levelChunk.getPos(), false);
		this.entityStorage.stopTicking(levelChunk.getPos());
	}

	public void onChunkLoaded(ChunkPos chunkPos) {
		this.tintCaches.forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(chunkPos.x, chunkPos.z));
		this.entityStorage.startTicking(chunkPos);
	}

	public void clearTintCaches() {
		this.tintCaches.forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateAll());
	}

	@Override
	public boolean hasChunk(int i, int j) {
		return true;
	}

	public int getEntityCount() {
		return this.entityStorage.count();
	}

	public void addPlayer(int i, AbstractClientPlayer abstractClientPlayer) {
		this.addEntity(i, abstractClientPlayer);
	}

	public void putNonPlayerEntity(int i, Entity entity) {
		this.addEntity(i, entity);
	}

	private void addEntity(int i, Entity entity) {
		this.removeEntity(i, Entity.RemovalReason.DISCARDED);
		this.entityStorage.addEntity(entity);
	}

	public void removeEntity(int i, Entity.RemovalReason removalReason) {
		Entity entity = this.getEntities().get(i);
		if (entity != null) {
			entity.setRemoved(removalReason);
			entity.onClientRemoval();
		}
	}

	@Nullable
	@Override
	public Entity getEntity(int i) {
		return this.getEntities().get(i);
	}

	@Override
	public void disconnect() {
		this.connection.getConnection().disconnect(Component.translatable("multiplayer.status.quitting"));
	}

	public void animateTick(int i, int j, int k) {
		int l = 32;
		RandomSource randomSource = RandomSource.create();
		Block block = this.getMarkerParticleTarget();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int m = 0; m < 667; m++) {
			this.doAnimateTick(i, j, k, 16, randomSource, block, mutableBlockPos);
			this.doAnimateTick(i, j, k, 32, randomSource, block, mutableBlockPos);
		}
	}

	@Nullable
	private Block getMarkerParticleTarget() {
		if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
			ItemStack itemStack = this.minecraft.player.getMainHandItem();
			Item item = itemStack.getItem();
			if (MARKER_PARTICLE_ITEMS.contains(item) && item instanceof BlockItem blockItem) {
				return blockItem.getBlock();
			}
		}

		return null;
	}

	public void doAnimateTick(int i, int j, int k, int l, RandomSource randomSource, @Nullable Block block, BlockPos.MutableBlockPos mutableBlockPos) {
		int m = i + this.random.nextInt(l) - this.random.nextInt(l);
		int n = j + this.random.nextInt(l) - this.random.nextInt(l);
		int o = k + this.random.nextInt(l) - this.random.nextInt(l);
		mutableBlockPos.set(m, n, o);
		BlockState blockState = this.getBlockState(mutableBlockPos);
		blockState.getBlock().animateTick(blockState, this, mutableBlockPos, randomSource);
		FluidState fluidState = this.getFluidState(mutableBlockPos);
		if (!fluidState.isEmpty()) {
			fluidState.animateTick(this, mutableBlockPos, randomSource);
			ParticleOptions particleOptions = fluidState.getDripParticle();
			if (particleOptions != null && this.random.nextInt(10) == 0) {
				boolean bl = blockState.isFaceSturdy(this, mutableBlockPos, Direction.DOWN);
				BlockPos blockPos = mutableBlockPos.below();
				this.trySpawnDripParticles(blockPos, this.getBlockState(blockPos), particleOptions, bl);
			}
		}

		if (block == blockState.getBlock()) {
			this.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, blockState), (double)m + 0.5, (double)n + 0.5, (double)o + 0.5, 0.0, 0.0, 0.0);
		}

		if (!blockState.isCollisionShapeFullBlock(this, mutableBlockPos)) {
			this.getBiome(mutableBlockPos)
				.value()
				.getAmbientParticle()
				.ifPresent(
					ambientParticleSettings -> {
						if (ambientParticleSettings.canSpawn(this.random)) {
							this.addParticle(
								ambientParticleSettings.getOptions(),
								(double)mutableBlockPos.getX() + this.random.nextDouble(),
								(double)mutableBlockPos.getY() + this.random.nextDouble(),
								(double)mutableBlockPos.getZ() + this.random.nextDouble(),
								0.0,
								0.0,
								0.0
							);
						}
					}
				);
		}
	}

	private void trySpawnDripParticles(BlockPos blockPos, BlockState blockState, ParticleOptions particleOptions, boolean bl) {
		if (blockState.getFluidState().isEmpty()) {
			VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos);
			double d = voxelShape.max(Direction.Axis.Y);
			if (d < 1.0) {
				if (bl) {
					this.spawnFluidParticle(
						(double)blockPos.getX(),
						(double)(blockPos.getX() + 1),
						(double)blockPos.getZ(),
						(double)(blockPos.getZ() + 1),
						(double)(blockPos.getY() + 1) - 0.05,
						particleOptions
					);
				}
			} else if (!blockState.is(BlockTags.IMPERMEABLE)) {
				double e = voxelShape.min(Direction.Axis.Y);
				if (e > 0.0) {
					this.spawnParticle(blockPos, particleOptions, voxelShape, (double)blockPos.getY() + e - 0.05);
				} else {
					BlockPos blockPos2 = blockPos.below();
					BlockState blockState2 = this.getBlockState(blockPos2);
					VoxelShape voxelShape2 = blockState2.getCollisionShape(this, blockPos2);
					double f = voxelShape2.max(Direction.Axis.Y);
					if (f < 1.0 && blockState2.getFluidState().isEmpty()) {
						this.spawnParticle(blockPos, particleOptions, voxelShape, (double)blockPos.getY() - 0.05);
					}
				}
			}
		}
	}

	private void spawnParticle(BlockPos blockPos, ParticleOptions particleOptions, VoxelShape voxelShape, double d) {
		this.spawnFluidParticle(
			(double)blockPos.getX() + voxelShape.min(Direction.Axis.X),
			(double)blockPos.getX() + voxelShape.max(Direction.Axis.X),
			(double)blockPos.getZ() + voxelShape.min(Direction.Axis.Z),
			(double)blockPos.getZ() + voxelShape.max(Direction.Axis.Z),
			d,
			particleOptions
		);
	}

	private void spawnFluidParticle(double d, double e, double f, double g, double h, ParticleOptions particleOptions) {
		this.addParticle(particleOptions, Mth.lerp(this.random.nextDouble(), d, e), h, Mth.lerp(this.random.nextDouble(), f, g), 0.0, 0.0, 0.0);
	}

	@Override
	public CrashReportCategory fillReportDetails(CrashReport crashReport) {
		CrashReportCategory crashReportCategory = super.fillReportDetails(crashReport);
		crashReportCategory.setDetail("Server brand", (CrashReportDetail<String>)(() -> this.minecraft.player.getServerBrand()));
		crashReportCategory.setDetail(
			"Server type",
			(CrashReportDetail<String>)(() -> this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server")
		);
		return crashReportCategory;
	}

	@Override
	public void playSeededSound(
		@Nullable Player player, double d, double e, double f, Holder<SoundEvent> holder, SoundSource soundSource, float g, float h, long l
	) {
		if (player == this.minecraft.player) {
			this.playSound(d, e, f, holder.value(), soundSource, g, h, false, l);
		}
	}

	@Override
	public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float f, float g, long l) {
		if (player == this.minecraft.player) {
			this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(holder.value(), soundSource, f, g, entity, l));
		}
	}

	@Override
	public void playLocalSound(double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h, boolean bl) {
		this.playSound(d, e, f, soundEvent, soundSource, g, h, bl, this.random.nextLong());
	}

	private void playSound(double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h, boolean bl, long l) {
		double i = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(d, e, f);
		SimpleSoundInstance simpleSoundInstance = new SimpleSoundInstance(soundEvent, soundSource, g, h, RandomSource.create(l), d, e, f);
		if (bl && i > 100.0) {
			double j = Math.sqrt(i) / 40.0;
			this.minecraft.getSoundManager().playDelayed(simpleSoundInstance, (int)(j * 20.0));
		} else {
			this.minecraft.getSoundManager().play(simpleSoundInstance);
		}
	}

	@Override
	public void createFireworks(double d, double e, double f, double g, double h, double i, @Nullable CompoundTag compoundTag) {
		this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, d, e, f, g, h, i, this.minecraft.particleEngine, compoundTag));
	}

	@Override
	public void sendPacketToServer(Packet<?> packet) {
		this.connection.send(packet);
	}

	@Override
	public RecipeManager getRecipeManager() {
		return this.connection.getRecipeManager();
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return BlackholeTickAccess.emptyLevelList();
	}

	public ClientChunkCache getChunkSource() {
		return this.chunkSource;
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(String string) {
		return (MapItemSavedData)this.mapData.get(string);
	}

	public void overrideMapData(String string, MapItemSavedData mapItemSavedData) {
		this.mapData.put(string, mapItemSavedData);
	}

	@Override
	public void setMapData(String string, MapItemSavedData mapItemSavedData) {
	}

	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	@Override
	public RegistryAccess registryAccess() {
		return this.connection.registryAccess();
	}

	@Override
	public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
		this.levelRenderer.blockChanged(this, blockPos, blockState, blockState2, i);
	}

	@Override
	public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		this.levelRenderer.setBlockDirty(blockPos, blockState, blockState2);
	}

	public void setSectionDirtyWithNeighbors(int i, int j, int k) {
		this.levelRenderer.setSectionDirtyWithNeighbors(i, j, k);
	}

	public void setLightReady(int i, int j) {
		LevelChunk levelChunk = this.chunkSource.getChunk(i, j, false);
		if (levelChunk != null) {
			levelChunk.setClientLightReady(true);
		}
	}

	@Override
	public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
		this.levelRenderer.destroyBlockProgress(i, blockPos, j);
	}

	@Override
	public void globalLevelEvent(int i, BlockPos blockPos, int j) {
		this.levelRenderer.globalLevelEvent(i, blockPos, j);
	}

	@Override
	public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
		try {
			this.levelRenderer.levelEvent(i, blockPos, j);
		} catch (Throwable var8) {
			CrashReport crashReport = CrashReport.forThrowable(var8, "Playing level event");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Level event being played");
			crashReportCategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, blockPos));
			crashReportCategory.setDetail("Event source", player);
			crashReportCategory.setDetail("Event type", i);
			crashReportCategory.setDetail("Event data", j);
			throw new ReportedException(crashReport);
		}
	}

	@Override
	public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
		this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter(), d, e, f, g, h, i);
	}

	@Override
	public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
		this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter() || bl, d, e, f, g, h, i);
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
		this.levelRenderer.addParticle(particleOptions, false, true, d, e, f, g, h, i);
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
		this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter() || bl, true, d, e, f, g, h, i);
	}

	@Override
	public List<AbstractClientPlayer> players() {
		return this.players;
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
		return this.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
	}

	public float getSkyDarken(float f) {
		float g = this.getTimeOfDay(f);
		float h = 1.0F - (Mth.cos(g * (float) (Math.PI * 2)) * 2.0F + 0.2F);
		h = Mth.clamp(h, 0.0F, 1.0F);
		h = 1.0F - h;
		h *= 1.0F - this.getRainLevel(f) * 5.0F / 16.0F;
		h *= 1.0F - this.getThunderLevel(f) * 5.0F / 16.0F;
		return h * 0.8F + 0.2F;
	}

	public Vec3 getSkyColor(Vec3 vec3, float f) {
		float g = this.getTimeOfDay(f);
		Vec3 vec32 = vec3.subtract(2.0, 2.0, 2.0).scale(0.25);
		BiomeManager biomeManager = this.getBiomeManager();
		Vec3 vec33 = CubicSampler.gaussianSampleVec3(vec32, (ix, jx, kx) -> Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(ix, jx, kx).value().getSkyColor()));
		float h = Mth.cos(g * (float) (Math.PI * 2)) * 2.0F + 0.5F;
		h = Mth.clamp(h, 0.0F, 1.0F);
		float i = (float)vec33.x * h;
		float j = (float)vec33.y * h;
		float k = (float)vec33.z * h;
		float l = this.getRainLevel(f);
		if (l > 0.0F) {
			float m = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.6F;
			float n = 1.0F - l * 0.75F;
			i = i * n + m * (1.0F - n);
			j = j * n + m * (1.0F - n);
			k = k * n + m * (1.0F - n);
		}

		float m = this.getThunderLevel(f);
		if (m > 0.0F) {
			float n = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.2F;
			float o = 1.0F - m * 0.75F;
			i = i * o + n * (1.0F - o);
			j = j * o + n * (1.0F - o);
			k = k * o + n * (1.0F - o);
		}

		if (!this.minecraft.options.hideLightningFlash().get() && this.skyFlashTime > 0) {
			float n = (float)this.skyFlashTime - f;
			if (n > 1.0F) {
				n = 1.0F;
			}

			n *= 0.45F;
			i = i * (1.0F - n) + 0.8F * n;
			j = j * (1.0F - n) + 0.8F * n;
			k = k * (1.0F - n) + 1.0F * n;
		}

		return new Vec3((double)i, (double)j, (double)k);
	}

	public Vec3 getCloudColor(float f) {
		float g = this.getTimeOfDay(f);
		float h = Mth.cos(g * (float) (Math.PI * 2)) * 2.0F + 0.5F;
		h = Mth.clamp(h, 0.0F, 1.0F);
		float i = 1.0F;
		float j = 1.0F;
		float k = 1.0F;
		float l = this.getRainLevel(f);
		if (l > 0.0F) {
			float m = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.6F;
			float n = 1.0F - l * 0.95F;
			i = i * n + m * (1.0F - n);
			j = j * n + m * (1.0F - n);
			k = k * n + m * (1.0F - n);
		}

		i *= h * 0.9F + 0.1F;
		j *= h * 0.9F + 0.1F;
		k *= h * 0.85F + 0.15F;
		float m = this.getThunderLevel(f);
		if (m > 0.0F) {
			float n = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.2F;
			float o = 1.0F - m * 0.95F;
			i = i * o + n * (1.0F - o);
			j = j * o + n * (1.0F - o);
			k = k * o + n * (1.0F - o);
		}

		return new Vec3((double)i, (double)j, (double)k);
	}

	public float getStarBrightness(float f) {
		float g = this.getTimeOfDay(f);
		float h = 1.0F - (Mth.cos(g * (float) (Math.PI * 2)) * 2.0F + 0.25F);
		h = Mth.clamp(h, 0.0F, 1.0F);
		return h * h * 0.5F;
	}

	public int getSkyFlashTime() {
		return this.skyFlashTime;
	}

	@Override
	public void setSkyFlashTime(int i) {
		this.skyFlashTime = i;
	}

	@Override
	public float getShade(Direction direction, boolean bl) {
		boolean bl2 = this.effects().constantAmbientLight();
		if (!bl) {
			return bl2 ? 0.9F : 1.0F;
		} else {
			switch (direction) {
				case DOWN:
					return bl2 ? 0.9F : 0.5F;
				case UP:
					return bl2 ? 0.9F : 1.0F;
				case NORTH:
				case SOUTH:
					return 0.8F;
				case WEST:
				case EAST:
					return 0.6F;
				default:
					return 1.0F;
			}
		}
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		BlockTintCache blockTintCache = this.tintCaches.get(colorResolver);
		return blockTintCache.getColor(blockPos);
	}

	public int calculateBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		int i = Minecraft.getInstance().options.biomeBlendRadius().get();
		if (i == 0) {
			return colorResolver.getColor(this.getBiome(blockPos).value(), (double)blockPos.getX(), (double)blockPos.getZ());
		} else {
			int j = (i * 2 + 1) * (i * 2 + 1);
			int k = 0;
			int l = 0;
			int m = 0;
			Cursor3D cursor3D = new Cursor3D(blockPos.getX() - i, blockPos.getY(), blockPos.getZ() - i, blockPos.getX() + i, blockPos.getY(), blockPos.getZ() + i);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			while (cursor3D.advance()) {
				mutableBlockPos.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
				int n = colorResolver.getColor(this.getBiome(mutableBlockPos).value(), (double)mutableBlockPos.getX(), (double)mutableBlockPos.getZ());
				k += (n & 0xFF0000) >> 16;
				l += (n & 0xFF00) >> 8;
				m += n & 0xFF;
			}

			return (k / j & 0xFF) << 16 | (l / j & 0xFF) << 8 | m / j & 0xFF;
		}
	}

	public void setDefaultSpawnPos(BlockPos blockPos, float f) {
		this.levelData.setSpawn(blockPos, f);
	}

	public String toString() {
		return "ClientLevel";
	}

	public ClientLevel.ClientLevelData getLevelData() {
		return this.clientLevelData;
	}

	@Override
	public void gameEvent(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context) {
	}

	protected Map<String, MapItemSavedData> getAllMapData() {
		return ImmutableMap.copyOf(this.mapData);
	}

	protected void addMapData(Map<String, MapItemSavedData> map) {
		this.mapData.putAll(map);
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return this.entityStorage.getEntityGetter();
	}

	@Override
	public String gatherChunkSourceStats() {
		return "Chunks[C] W: " + this.chunkSource.gatherStats() + " E: " + this.entityStorage.gatherStats();
	}

	@Override
	public void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState) {
		this.minecraft.particleEngine.destroy(blockPos, blockState);
	}

	public void setServerSimulationDistance(int i) {
		this.serverSimulationDistance = i;
	}

	public int getServerSimulationDistance() {
		return this.serverSimulationDistance;
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return this.connection.enabledFeatures();
	}

	@Environment(EnvType.CLIENT)
	public static class ClientLevelData implements WritableLevelData {
		private final boolean hardcore;
		private final GameRules gameRules;
		private final boolean isFlat;
		private int xSpawn;
		private int ySpawn;
		private int zSpawn;
		private float spawnAngle;
		private long gameTime;
		private long dayTime;
		private boolean raining;
		private Difficulty difficulty;
		private boolean difficultyLocked;

		public ClientLevelData(Difficulty difficulty, boolean bl, boolean bl2) {
			this.difficulty = difficulty;
			this.hardcore = bl;
			this.isFlat = bl2;
			this.gameRules = new GameRules();
		}

		@Override
		public int getXSpawn() {
			return this.xSpawn;
		}

		@Override
		public int getYSpawn() {
			return this.ySpawn;
		}

		@Override
		public int getZSpawn() {
			return this.zSpawn;
		}

		@Override
		public float getSpawnAngle() {
			return this.spawnAngle;
		}

		@Override
		public long getGameTime() {
			return this.gameTime;
		}

		@Override
		public long getDayTime() {
			return this.dayTime;
		}

		@Override
		public void setXSpawn(int i) {
			this.xSpawn = i;
		}

		@Override
		public void setYSpawn(int i) {
			this.ySpawn = i;
		}

		@Override
		public void setZSpawn(int i) {
			this.zSpawn = i;
		}

		@Override
		public void setSpawnAngle(float f) {
			this.spawnAngle = f;
		}

		public void setGameTime(long l) {
			this.gameTime = l;
		}

		public void setDayTime(long l) {
			this.dayTime = l;
		}

		@Override
		public void setSpawn(BlockPos blockPos, float f) {
			this.xSpawn = blockPos.getX();
			this.ySpawn = blockPos.getY();
			this.zSpawn = blockPos.getZ();
			this.spawnAngle = f;
		}

		@Override
		public boolean isThundering() {
			return false;
		}

		@Override
		public boolean isRaining() {
			return this.raining;
		}

		@Override
		public void setRaining(boolean bl) {
			this.raining = bl;
		}

		@Override
		public boolean isHardcore() {
			return this.hardcore;
		}

		@Override
		public GameRules getGameRules() {
			return this.gameRules;
		}

		@Override
		public Difficulty getDifficulty() {
			return this.difficulty;
		}

		@Override
		public boolean isDifficultyLocked() {
			return this.difficultyLocked;
		}

		@Override
		public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
			WritableLevelData.super.fillCrashReportCategory(crashReportCategory, levelHeightAccessor);
		}

		public void setDifficulty(Difficulty difficulty) {
			this.difficulty = difficulty;
		}

		public void setDifficultyLocked(boolean bl) {
			this.difficultyLocked = bl;
		}

		public double getHorizonHeight(LevelHeightAccessor levelHeightAccessor) {
			return this.isFlat ? (double)levelHeightAccessor.getMinBuildHeight() : 63.0;
		}

		public float getClearColorScale() {
			return this.isFlat ? 1.0F : 0.03125F;
		}
	}

	@Environment(EnvType.CLIENT)
	final class EntityCallbacks implements LevelCallback<Entity> {
		public void onCreated(Entity entity) {
		}

		public void onDestroyed(Entity entity) {
		}

		public void onTickingStart(Entity entity) {
			ClientLevel.this.tickingEntities.add(entity);
		}

		public void onTickingEnd(Entity entity) {
			ClientLevel.this.tickingEntities.remove(entity);
		}

		public void onTrackingStart(Entity entity) {
			if (entity instanceof AbstractClientPlayer) {
				ClientLevel.this.players.add((AbstractClientPlayer)entity);
			}
		}

		public void onTrackingEnd(Entity entity) {
			entity.unRide();
			ClientLevel.this.players.remove(entity);
		}

		public void onSectionChange(Entity entity) {
		}
	}
}
