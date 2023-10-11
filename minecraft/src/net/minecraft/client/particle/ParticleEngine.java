package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ParticleEngine implements PreparableReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
	private static final ResourceLocation PARTICLES_ATLAS_INFO = new ResourceLocation("particles");
	private static final int MAX_PARTICLES_PER_LAYER = 16384;
	private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(
		ParticleRenderType.TERRAIN_SHEET,
		ParticleRenderType.PARTICLE_SHEET_OPAQUE,
		ParticleRenderType.PARTICLE_SHEET_LIT,
		ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT,
		ParticleRenderType.CUSTOM
	);
	protected ClientLevel level;
	private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.<ParticleRenderType, Queue<Particle>>newIdentityHashMap();
	private final Queue<TrackingEmitter> trackingEmitters = Queues.<TrackingEmitter>newArrayDeque();
	private final TextureManager textureManager;
	private final RandomSource random = RandomSource.create();
	private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap<>();
	private final Queue<Particle> particlesToAdd = Queues.<Particle>newArrayDeque();
	private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.<ResourceLocation, ParticleEngine.MutableSpriteSet>newHashMap();
	private final TextureAtlas textureAtlas;
	private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

	public ParticleEngine(ClientLevel clientLevel, TextureManager textureManager) {
		this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
		textureManager.register(this.textureAtlas.location(), this.textureAtlas);
		this.level = clientLevel;
		this.textureManager = textureManager;
		this.registerProviders();
	}

	private void registerProviders() {
		this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
		this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
		this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
		this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
		this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
		this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
		this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
		this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
		this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
		this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
		this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
		this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
		this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
		this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
		this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
		this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
		this.register(ParticleTypes.DRIPPING_LAVA, DripParticle::createLavaHangParticle);
		this.register(ParticleTypes.FALLING_LAVA, DripParticle::createLavaFallParticle);
		this.register(ParticleTypes.LANDING_LAVA, DripParticle::createLavaLandParticle);
		this.register(ParticleTypes.DRIPPING_WATER, DripParticle::createWaterHangParticle);
		this.register(ParticleTypes.FALLING_WATER, DripParticle::createWaterFallParticle);
		this.register(ParticleTypes.DUST, DustParticle.Provider::new);
		this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
		this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
		this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
		this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
		this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
		this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
		this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
		this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
		this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
		this.register(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Provider::new);
		this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
		this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
		this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
		this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
		this.register(ParticleTypes.SCULK_SOUL, SoulParticle.EmissiveProvider::new);
		this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
		this.register(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Provider::new);
		this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
		this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
		this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
		this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
		this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
		this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
		this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
		this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
		this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
		this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
		this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
		this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
		this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
		this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
		this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
		this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
		this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
		this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
		this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
		this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
		this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
		this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
		this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
		this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
		this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
		this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
		this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
		this.register(ParticleTypes.DRIPPING_HONEY, DripParticle::createHoneyHangParticle);
		this.register(ParticleTypes.FALLING_HONEY, DripParticle::createHoneyFallParticle);
		this.register(ParticleTypes.LANDING_HONEY, DripParticle::createHoneyLandParticle);
		this.register(ParticleTypes.FALLING_NECTAR, DripParticle::createNectarFallParticle);
		this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle::createSporeBlossomFallParticle);
		this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
		this.register(ParticleTypes.ASH, AshParticle.Provider::new);
		this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
		this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
		this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle::createObsidianTearHangParticle);
		this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle::createObsidianTearFallParticle);
		this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle::createObsidianTearLandParticle);
		this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
		this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
		this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
		this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterHangParticle);
		this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterFallParticle);
		this.register(
			ParticleTypes.CHERRY_LEAVES, spriteSet -> (simpleParticleType, clientLevel, d, e, f, g, h, i) -> new CherryParticle(clientLevel, d, e, f, spriteSet)
		);
		this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaHangParticle);
		this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaFallParticle);
		this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
		this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
		this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
		this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
		this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
		this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
		this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
		this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
		this.register(ParticleTypes.EGG_CRACK, SuspendedTownParticle.EggCrackProvider::new);
		this.register(ParticleTypes.DUST_PLUME, DustPlumeParticle.Provider::new);
	}

	private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider<T> particleProvider) {
		this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(particleType), particleProvider);
	}

	private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider.Sprite<T> sprite) {
		this.register(particleType, spriteSet -> (particleOptions, clientLevel, d, e, f, g, h, i) -> {
				TextureSheetParticle textureSheetParticle = sprite.createParticle(particleOptions, clientLevel, d, e, f, g, h, i);
				if (textureSheetParticle != null) {
					textureSheetParticle.pickSprite(spriteSet);
				}

				return textureSheetParticle;
			});
	}

	private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> spriteParticleRegistration) {
		ParticleEngine.MutableSpriteSet mutableSpriteSet = new ParticleEngine.MutableSpriteSet();
		this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType), mutableSpriteSet);
		this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(particleType), spriteParticleRegistration.create(mutableSpriteSet));
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		@Environment(EnvType.CLIENT)
		record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
		}

		CompletableFuture<List<ParticleDefinition>> completableFuture = CompletableFuture.supplyAsync(
				() -> PARTICLE_LISTER.listMatchingResources(resourceManager), executor
			)
			.thenCompose(
				map -> {
					List<CompletableFuture<ParticleDefinition>> list = new ArrayList(map.size());
					map.forEach(
						(resourceLocation, resource) -> {
							ResourceLocation resourceLocation2 = PARTICLE_LISTER.fileToId(resourceLocation);
							list.add(
								CompletableFuture.supplyAsync(() -> new ParticleDefinition(resourceLocation2, this.loadParticleDescription(resourceLocation2, resource)), executor)
							);
						}
					);
					return Util.sequence(list);
				}
			);
		CompletableFuture<SpriteLoader.Preparations> completableFuture2 = SpriteLoader.create(this.textureAtlas)
			.loadAndStitch(resourceManager, PARTICLES_ATLAS_INFO, 0, executor)
			.thenCompose(SpriteLoader.Preparations::waitForUpload);
		return CompletableFuture.allOf(completableFuture2, completableFuture).thenCompose(preparationBarrier::wait).thenAcceptAsync(void_ -> {
			this.clearParticles();
			profilerFiller2.startTick();
			profilerFiller2.push("upload");
			SpriteLoader.Preparations preparations = (SpriteLoader.Preparations)completableFuture2.join();
			this.textureAtlas.upload(preparations);
			profilerFiller2.popPush("bindSpriteSets");
			Set<ResourceLocation> set = new HashSet();
			TextureAtlasSprite textureAtlasSprite = preparations.missing();
			((List)completableFuture.join()).forEach(arg -> {
				Optional<List<ResourceLocation>> optional = arg.sprites();
				if (!optional.isEmpty()) {
					List<TextureAtlasSprite> list = new ArrayList();

					for (ResourceLocation resourceLocation : (List)optional.get()) {
						TextureAtlasSprite textureAtlasSprite2 = (TextureAtlasSprite)preparations.regions().get(resourceLocation);
						if (textureAtlasSprite2 == null) {
							set.add(resourceLocation);
							list.add(textureAtlasSprite);
						} else {
							list.add(textureAtlasSprite2);
						}
					}

					if (list.isEmpty()) {
						list.add(textureAtlasSprite);
					}

					((ParticleEngine.MutableSpriteSet)this.spriteSets.get(arg.id())).rebind(list);
				}
			});
			if (!set.isEmpty()) {
				LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
			}

			profilerFiller2.pop();
			profilerFiller2.endTick();
		}, executor2);
	}

	public void close() {
		this.textureAtlas.clearTextureData();
	}

	private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation resourceLocation, Resource resource) {
		if (!this.spriteSets.containsKey(resourceLocation)) {
			LOGGER.debug("Redundant texture list for particle: {}", resourceLocation);
			return Optional.empty();
		} else {
			try {
				Reader reader = resource.openAsReader();

				Optional var5;
				try {
					ParticleDescription particleDescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
					var5 = Optional.of(particleDescription.getTextures());
				} catch (Throwable var7) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var6) {
							var7.addSuppressed(var6);
						}
					}

					throw var7;
				}

				if (reader != null) {
					reader.close();
				}

				return var5;
			} catch (IOException var8) {
				throw new IllegalStateException("Failed to load description for particle " + resourceLocation, var8);
			}
		}
	}

	public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions) {
		this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleOptions));
	}

	public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions, int i) {
		this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleOptions, i));
	}

	@Nullable
	public Particle createParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
		Particle particle = this.makeParticle(particleOptions, d, e, f, g, h, i);
		if (particle != null) {
			this.add(particle);
			return particle;
		} else {
			return null;
		}
	}

	@Nullable
	private <T extends ParticleOptions> Particle makeParticle(T particleOptions, double d, double e, double f, double g, double h, double i) {
		ParticleProvider<T> particleProvider = (ParticleProvider<T>)this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getId(particleOptions.getType()));
		return particleProvider == null ? null : particleProvider.createParticle(particleOptions, this.level, d, e, f, g, h, i);
	}

	public void add(Particle particle) {
		Optional<ParticleGroup> optional = particle.getParticleGroup();
		if (optional.isPresent()) {
			if (this.hasSpaceInParticleLimit((ParticleGroup)optional.get())) {
				this.particlesToAdd.add(particle);
				this.updateCount((ParticleGroup)optional.get(), 1);
			}
		} else {
			this.particlesToAdd.add(particle);
		}
	}

	public void tick() {
		this.particles.forEach((particleRenderType, queue) -> {
			this.level.getProfiler().push(particleRenderType.toString());
			this.tickParticleList(queue);
			this.level.getProfiler().pop();
		});
		if (!this.trackingEmitters.isEmpty()) {
			List<TrackingEmitter> list = Lists.<TrackingEmitter>newArrayList();

			for (TrackingEmitter trackingEmitter : this.trackingEmitters) {
				trackingEmitter.tick();
				if (!trackingEmitter.isAlive()) {
					list.add(trackingEmitter);
				}
			}

			this.trackingEmitters.removeAll(list);
		}

		Particle particle;
		if (!this.particlesToAdd.isEmpty()) {
			while ((particle = (Particle)this.particlesToAdd.poll()) != null) {
				((Queue)this.particles.computeIfAbsent(particle.getRenderType(), particleRenderType -> EvictingQueue.create(16384))).add(particle);
			}
		}
	}

	private void tickParticleList(Collection<Particle> collection) {
		if (!collection.isEmpty()) {
			Iterator<Particle> iterator = collection.iterator();

			while (iterator.hasNext()) {
				Particle particle = (Particle)iterator.next();
				this.tickParticle(particle);
				if (!particle.isAlive()) {
					particle.getParticleGroup().ifPresent(particleGroup -> this.updateCount(particleGroup, -1));
					iterator.remove();
				}
			}
		}
	}

	private void updateCount(ParticleGroup particleGroup, int i) {
		this.trackedParticleCounts.addTo(particleGroup, i);
	}

	private void tickParticle(Particle particle) {
		try {
			particle.tick();
		} catch (Throwable var5) {
			CrashReport crashReport = CrashReport.forThrowable(var5, "Ticking Particle");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being ticked");
			crashReportCategory.setDetail("Particle", particle::toString);
			crashReportCategory.setDetail("Particle Type", particle.getRenderType()::toString);
			throw new ReportedException(crashReport);
		}
	}

	public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float f) {
		lightTexture.turnOnLightLayer();
		RenderSystem.enableDepthTest();
		PoseStack poseStack2 = RenderSystem.getModelViewStack();
		poseStack2.pushPose();
		poseStack2.mulPoseMatrix(poseStack.last().pose());
		RenderSystem.applyModelViewMatrix();

		for (ParticleRenderType particleRenderType : RENDER_ORDER) {
			Iterable<Particle> iterable = (Iterable<Particle>)this.particles.get(particleRenderType);
			if (iterable != null) {
				RenderSystem.setShader(GameRenderer::getParticleShader);
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				particleRenderType.begin(bufferBuilder, this.textureManager);

				for (Particle particle : iterable) {
					try {
						particle.render(bufferBuilder, camera, f);
					} catch (Throwable var17) {
						CrashReport crashReport = CrashReport.forThrowable(var17, "Rendering Particle");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
						crashReportCategory.setDetail("Particle", particle::toString);
						crashReportCategory.setDetail("Particle Type", particleRenderType::toString);
						throw new ReportedException(crashReport);
					}
				}

				particleRenderType.end(tesselator);
			}
		}

		poseStack2.popPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		lightTexture.turnOffLightLayer();
	}

	public void setLevel(@Nullable ClientLevel clientLevel) {
		this.level = clientLevel;
		this.clearParticles();
		this.trackingEmitters.clear();
	}

	public void destroy(BlockPos blockPos, BlockState blockState) {
		if (!blockState.isAir() && blockState.shouldSpawnTerrainParticles()) {
			VoxelShape voxelShape = blockState.getShape(this.level, blockPos);
			double d = 0.25;
			voxelShape.forAllBoxes(
				(dx, e, f, g, h, i) -> {
					double j = Math.min(1.0, g - dx);
					double k = Math.min(1.0, h - e);
					double l = Math.min(1.0, i - f);
					int m = Math.max(2, Mth.ceil(j / 0.25));
					int n = Math.max(2, Mth.ceil(k / 0.25));
					int o = Math.max(2, Mth.ceil(l / 0.25));

					for (int p = 0; p < m; p++) {
						for (int q = 0; q < n; q++) {
							for (int r = 0; r < o; r++) {
								double s = ((double)p + 0.5) / (double)m;
								double t = ((double)q + 0.5) / (double)n;
								double u = ((double)r + 0.5) / (double)o;
								double v = s * j + dx;
								double w = t * k + e;
								double x = u * l + f;
								this.add(
									new TerrainParticle(
										this.level, (double)blockPos.getX() + v, (double)blockPos.getY() + w, (double)blockPos.getZ() + x, s - 0.5, t - 0.5, u - 0.5, blockState, blockPos
									)
								);
							}
						}
					}
				}
			);
		}
	}

	public void crack(BlockPos blockPos, Direction direction) {
		BlockState blockState = this.level.getBlockState(blockPos);
		if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.shouldSpawnTerrainParticles()) {
			int i = blockPos.getX();
			int j = blockPos.getY();
			int k = blockPos.getZ();
			float f = 0.1F;
			AABB aABB = blockState.getShape(this.level, blockPos).bounds();
			double d = (double)i + this.random.nextDouble() * (aABB.maxX - aABB.minX - 0.2F) + 0.1F + aABB.minX;
			double e = (double)j + this.random.nextDouble() * (aABB.maxY - aABB.minY - 0.2F) + 0.1F + aABB.minY;
			double g = (double)k + this.random.nextDouble() * (aABB.maxZ - aABB.minZ - 0.2F) + 0.1F + aABB.minZ;
			if (direction == Direction.DOWN) {
				e = (double)j + aABB.minY - 0.1F;
			}

			if (direction == Direction.UP) {
				e = (double)j + aABB.maxY + 0.1F;
			}

			if (direction == Direction.NORTH) {
				g = (double)k + aABB.minZ - 0.1F;
			}

			if (direction == Direction.SOUTH) {
				g = (double)k + aABB.maxZ + 0.1F;
			}

			if (direction == Direction.WEST) {
				d = (double)i + aABB.minX - 0.1F;
			}

			if (direction == Direction.EAST) {
				d = (double)i + aABB.maxX + 0.1F;
			}

			this.add(new TerrainParticle(this.level, d, e, g, 0.0, 0.0, 0.0, blockState, blockPos).setPower(0.2F).scale(0.6F));
		}
	}

	public String countParticles() {
		return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
	}

	private boolean hasSpaceInParticleLimit(ParticleGroup particleGroup) {
		return this.trackedParticleCounts.getInt(particleGroup) < particleGroup.getLimit();
	}

	private void clearParticles() {
		this.particles.clear();
		this.particlesToAdd.clear();
		this.trackingEmitters.clear();
		this.trackedParticleCounts.clear();
	}

	@Environment(EnvType.CLIENT)
	static class MutableSpriteSet implements SpriteSet {
		private List<TextureAtlasSprite> sprites;

		@Override
		public TextureAtlasSprite get(int i, int j) {
			return (TextureAtlasSprite)this.sprites.get(i * (this.sprites.size() - 1) / j);
		}

		@Override
		public TextureAtlasSprite get(RandomSource randomSource) {
			return (TextureAtlasSprite)this.sprites.get(randomSource.nextInt(this.sprites.size()));
		}

		public void rebind(List<TextureAtlasSprite> list) {
			this.sprites = ImmutableList.copyOf(list);
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	interface SpriteParticleRegistration<T extends ParticleOptions> {
		ParticleProvider<T> create(SpriteSet spriteSet);
	}
}
