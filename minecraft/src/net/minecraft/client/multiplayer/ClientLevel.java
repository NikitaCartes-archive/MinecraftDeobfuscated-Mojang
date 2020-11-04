package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;

@Environment(EnvType.CLIENT)
public class ClientLevel extends Level {
	private final EntityTickList tickingEntities = new EntityTickList();
	private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new ClientLevel.EntityCallbacks());
	private final ClientPacketListener connection;
	private final LevelRenderer levelRenderer;
	private final ClientLevel.ClientLevelData clientLevelData;
	private final DimensionSpecialEffects effects;
	private final Minecraft minecraft = Minecraft.getInstance();
	private final List<AbstractClientPlayer> players = Lists.<AbstractClientPlayer>newArrayList();
	private Scoreboard scoreboard = new Scoreboard();
	private final Map<String, MapItemSavedData> mapData = Maps.<String, MapItemSavedData>newHashMap();
	private int skyFlashTime;
	private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap<>(3), object2ObjectArrayMap -> {
		object2ObjectArrayMap.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache());
		object2ObjectArrayMap.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache());
		object2ObjectArrayMap.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache());
	});
	private final ClientChunkCache chunkSource;

	public ClientLevel(
		ClientPacketListener clientPacketListener,
		ClientLevel.ClientLevelData clientLevelData,
		ResourceKey<Level> resourceKey,
		DimensionType dimensionType,
		int i,
		Supplier<ProfilerFiller> supplier,
		LevelRenderer levelRenderer,
		boolean bl,
		long l
	) {
		super(clientLevelData, resourceKey, dimensionType, supplier, true, bl, l);
		this.connection = clientPacketListener;
		this.chunkSource = new ClientChunkCache(this, i);
		this.clientLevelData = clientLevelData;
		this.levelRenderer = levelRenderer;
		this.effects = DimensionSpecialEffects.forType(dimensionType);
		this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
		this.updateSkyBrightness();
		this.prepareWeather();
	}

	public DimensionSpecialEffects effects() {
		return this.effects;
	}

	public void tick(BooleanSupplier booleanSupplier) {
		this.getWorldBorder().tick();
		this.tickTime();
		this.getProfiler().push("blocks");
		this.chunkSource.tick(booleanSupplier);
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

	public void tickNonPassenger(Entity entity) {
		entity.setPosAndOldPos(entity.getX(), entity.getY(), entity.getZ());
		entity.yRotO = entity.yRot;
		entity.xRotO = entity.xRot;
		entity.tickCount++;
		this.getProfiler().push((Supplier<String>)(() -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString()));
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
			entity2.setPosAndOldPos(entity2.getX(), entity2.getY(), entity2.getZ());
			entity2.yRotO = entity2.yRot;
			entity2.xRotO = entity2.xRot;
			entity2.tickCount++;
			entity2.rideTick();

			for (Entity entity3 : entity2.getPassengers()) {
				this.tickPassenger(entity2, entity3);
			}
		}
	}

	public void unload(LevelChunk levelChunk) {
		levelChunk.invalidateAllBlockEntities();
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
		}
	}

	@Nullable
	@Override
	public Entity getEntity(int i) {
		return this.getEntities().get(i);
	}

	public void setKnownState(BlockPos blockPos, BlockState blockState) {
		this.setBlock(blockPos, blockState, 19);
	}

	@Override
	public void disconnect() {
		this.connection.getConnection().disconnect(new TranslatableComponent("multiplayer.status.quitting"));
	}

	public void animateTick(int i, int j, int k) {
		int l = 32;
		Random random = new Random();
		boolean bl = false;
		if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
			for (ItemStack itemStack : this.minecraft.player.getHandSlots()) {
				if (itemStack.is(Blocks.BARRIER.asItem())) {
					bl = true;
					break;
				}
			}
		}

		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int m = 0; m < 667; m++) {
			this.doAnimateTick(i, j, k, 16, random, bl, mutableBlockPos);
			this.doAnimateTick(i, j, k, 32, random, bl, mutableBlockPos);
		}
	}

	public void doAnimateTick(int i, int j, int k, int l, Random random, boolean bl, BlockPos.MutableBlockPos mutableBlockPos) {
		int m = i + this.random.nextInt(l) - this.random.nextInt(l);
		int n = j + this.random.nextInt(l) - this.random.nextInt(l);
		int o = k + this.random.nextInt(l) - this.random.nextInt(l);
		mutableBlockPos.set(m, n, o);
		BlockState blockState = this.getBlockState(mutableBlockPos);
		blockState.getBlock().animateTick(blockState, this, mutableBlockPos, random);
		FluidState fluidState = this.getFluidState(mutableBlockPos);
		if (!fluidState.isEmpty()) {
			fluidState.animateTick(this, mutableBlockPos, random);
			ParticleOptions particleOptions = fluidState.getDripParticle();
			if (particleOptions != null && this.random.nextInt(10) == 0) {
				boolean bl2 = blockState.isFaceSturdy(this, mutableBlockPos, Direction.DOWN);
				BlockPos blockPos = mutableBlockPos.below();
				this.trySpawnDripParticles(blockPos, this.getBlockState(blockPos), particleOptions, bl2);
			}
		}

		if (bl && blockState.is(Blocks.BARRIER)) {
			this.addParticle(ParticleTypes.BARRIER, (double)m + 0.5, (double)n + 0.5, (double)o + 0.5, 0.0, 0.0, 0.0);
		}

		if (!blockState.isCollisionShapeFullBlock(this, mutableBlockPos)) {
			this.getBiome(mutableBlockPos)
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
	public void playSound(@Nullable Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h) {
		if (player == this.minecraft.player) {
			this.playLocalSound(d, e, f, soundEvent, soundSource, g, h, false);
		}
	}

	@Override
	public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
		if (player == this.minecraft.player) {
			this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(soundEvent, soundSource, entity));
		}
	}

	public void playLocalSound(BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g, boolean bl) {
		this.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, soundSource, f, g, bl);
	}

	@Override
	public void playLocalSound(double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h, boolean bl) {
		double i = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(d, e, f);
		SimpleSoundInstance simpleSoundInstance = new SimpleSoundInstance(soundEvent, soundSource, g, h, d, e, f);
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
	public TickList<Block> getBlockTicks() {
		return EmptyTickList.empty();
	}

	@Override
	public TickList<Fluid> getLiquidTicks() {
		return EmptyTickList.empty();
	}

	public ClientChunkCache getChunkSource() {
		return this.chunkSource;
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(String string) {
		return (MapItemSavedData)this.mapData.get(string);
	}

	@Override
	public void setMapData(MapItemSavedData mapItemSavedData) {
		this.mapData.put(mapItemSavedData.getId(), mapItemSavedData);
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
	public TagContainer getTagManager() {
		return this.connection.getTags();
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
			this.levelRenderer.levelEvent(player, i, blockPos, j);
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
	public Biome getUncachedNoiseBiome(int i, int j, int k) {
		return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
	}

	public float getSkyDarken(float f) {
		float g = this.getTimeOfDay(f);
		float h = 1.0F - (Mth.cos(g * (float) (Math.PI * 2)) * 2.0F + 0.2F);
		h = Mth.clamp(h, 0.0F, 1.0F);
		h = 1.0F - h;
		h = (float)((double)h * (1.0 - (double)(this.getRainLevel(f) * 5.0F) / 16.0));
		h = (float)((double)h * (1.0 - (double)(this.getThunderLevel(f) * 5.0F) / 16.0));
		return h * 0.8F + 0.2F;
	}

	public Vec3 getSkyColor(BlockPos blockPos, float f) {
		float g = this.getTimeOfDay(f);
		float h = Mth.cos(g * (float) (Math.PI * 2)) * 2.0F + 0.5F;
		h = Mth.clamp(h, 0.0F, 1.0F);
		Biome biome = this.getBiome(blockPos);
		int i = biome.getSkyColor();
		float j = (float)(i >> 16 & 0xFF) / 255.0F;
		float k = (float)(i >> 8 & 0xFF) / 255.0F;
		float l = (float)(i & 0xFF) / 255.0F;
		j *= h;
		k *= h;
		l *= h;
		float m = this.getRainLevel(f);
		if (m > 0.0F) {
			float n = (j * 0.3F + k * 0.59F + l * 0.11F) * 0.6F;
			float o = 1.0F - m * 0.75F;
			j = j * o + n * (1.0F - o);
			k = k * o + n * (1.0F - o);
			l = l * o + n * (1.0F - o);
		}

		float n = this.getThunderLevel(f);
		if (n > 0.0F) {
			float o = (j * 0.3F + k * 0.59F + l * 0.11F) * 0.2F;
			float p = 1.0F - n * 0.75F;
			j = j * p + o * (1.0F - p);
			k = k * p + o * (1.0F - p);
			l = l * p + o * (1.0F - p);
		}

		if (this.skyFlashTime > 0) {
			float o = (float)this.skyFlashTime - f;
			if (o > 1.0F) {
				o = 1.0F;
			}

			o *= 0.45F;
			j = j * (1.0F - o) + 0.8F * o;
			k = k * (1.0F - o) + 0.8F * o;
			l = l * (1.0F - o) + 1.0F * o;
		}

		return new Vec3((double)j, (double)k, (double)l);
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
		return blockTintCache.getColor(blockPos, () -> this.calculateBlockTint(blockPos, colorResolver));
	}

	public int calculateBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		int i = Minecraft.getInstance().options.biomeBlendRadius;
		if (i == 0) {
			return colorResolver.getColor(this.getBiome(blockPos), (double)blockPos.getX(), (double)blockPos.getZ());
		} else {
			int j = (i * 2 + 1) * (i * 2 + 1);
			int k = 0;
			int l = 0;
			int m = 0;
			Cursor3D cursor3D = new Cursor3D(blockPos.getX() - i, blockPos.getY(), blockPos.getZ() - i, blockPos.getX() + i, blockPos.getY(), blockPos.getZ() + i);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			while (cursor3D.advance()) {
				mutableBlockPos.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
				int n = colorResolver.getColor(this.getBiome(mutableBlockPos), (double)mutableBlockPos.getX(), (double)mutableBlockPos.getZ());
				k += (n & 0xFF0000) >> 16;
				l += (n & 0xFF00) >> 8;
				m += n & 0xFF;
			}

			return (k / j & 0xFF) << 16 | (l / j & 0xFF) << 8 | m / j & 0xFF;
		}
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
	protected LevelEntityGetter<Entity> getEntities() {
		return this.entityStorage.getEntityGetter();
	}

	public String gatherChunkSourceStats() {
		return "Chunks[C] W: " + this.chunkSource.gatherStats() + " E: " + this.entityStorage.gatherStats();
	}

	@Override
	public void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState) {
		this.minecraft.particleEngine.destroy(blockPos, blockState);
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

		public double getHorizonHeight() {
			return this.isFlat ? 0.0 : 63.0;
		}

		public double getClearColorScale() {
			return this.isFlat ? 1.0 : 0.03125;
		}
	}

	@Environment(EnvType.CLIENT)
	final class EntityCallbacks implements LevelCallback<Entity> {
		private EntityCallbacks() {
		}

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
	}
}
