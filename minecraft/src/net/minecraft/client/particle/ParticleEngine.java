package net.minecraft.client.particle;

import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class ParticleEngine implements PreparableReloadListener {
	private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(
		ParticleRenderType.TERRAIN_SHEET,
		ParticleRenderType.PARTICLE_SHEET_OPAQUE,
		ParticleRenderType.PARTICLE_SHEET_LIT,
		ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT,
		ParticleRenderType.CUSTOM
	);
	protected Level level;
	private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.<ParticleRenderType, Queue<Particle>>newIdentityHashMap();
	private final Queue<TrackingEmitter> trackingEmitters = Queues.<TrackingEmitter>newArrayDeque();
	private final TextureManager textureManager;
	private final Random random = new Random();
	private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap<>();
	private final Queue<Particle> particlesToAdd = Queues.<Particle>newArrayDeque();
	private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.<ResourceLocation, ParticleEngine.MutableSpriteSet>newHashMap();
	private final TextureAtlas textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);

	public ParticleEngine(Level level, TextureManager textureManager) {
		textureManager.register(this.textureAtlas.location(), this.textureAtlas);
		this.level = level;
		this.textureManager = textureManager;
		this.registerProviders();
	}

	private void registerProviders() {
		this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
		this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
		this.register(ParticleTypes.BARRIER, new BarrierParticle.Provider());
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
		this.register(ParticleTypes.DRIPPING_LAVA, DripParticle.LavaHangProvider::new);
		this.register(ParticleTypes.FALLING_LAVA, DripParticle.LavaFallProvider::new);
		this.register(ParticleTypes.LANDING_LAVA, DripParticle.LavaLandProvider::new);
		this.register(ParticleTypes.DRIPPING_WATER, DripParticle.WaterHangProvider::new);
		this.register(ParticleTypes.FALLING_WATER, DripParticle.WaterFallProvider::new);
		this.register(ParticleTypes.DUST, DustParticle.Provider::new);
		this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
		this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
		this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
		this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
		this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
		this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
		this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
		this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
		this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
		this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
		this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
		this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
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
		this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
		this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
		this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
		this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
		this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
		this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
		this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
		this.register(ParticleTypes.DRIPPING_HONEY, DripParticle.HoneyHangProvider::new);
		this.register(ParticleTypes.FALLING_HONEY, DripParticle.HoneyFallProvider::new);
		this.register(ParticleTypes.LANDING_HONEY, DripParticle.HoneyLandProvider::new);
		this.register(ParticleTypes.FALLING_NECTAR, DripParticle.NectarFallProvider::new);
		this.register(ParticleTypes.ASH, AshParticle.Provider::new);
		this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
		this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
	}

	private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider<T> particleProvider) {
		this.providers.put(Registry.PARTICLE_TYPE.getId(particleType), particleProvider);
	}

	private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> spriteParticleRegistration) {
		ParticleEngine.MutableSpriteSet mutableSpriteSet = new ParticleEngine.MutableSpriteSet();
		this.spriteSets.put(Registry.PARTICLE_TYPE.getKey(particleType), mutableSpriteSet);
		this.providers.put(Registry.PARTICLE_TYPE.getId(particleType), spriteParticleRegistration.create(mutableSpriteSet));
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
		Map<ResourceLocation, List<ResourceLocation>> map = Maps.<ResourceLocation, List<ResourceLocation>>newConcurrentMap();
		CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[])Registry.PARTICLE_TYPE
			.keySet()
			.stream()
			.map(resourceLocation -> CompletableFuture.runAsync(() -> this.loadParticleDescription(resourceManager, resourceLocation, map), executor))
			.toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(completableFutures)
			.thenApplyAsync(
				void_ -> {
					profilerFiller.startTick();
					profilerFiller.push("stitching");
					TextureAtlas.Preparations preparations = this.textureAtlas
						.prepareToStitch(resourceManager, map.values().stream().flatMap(Collection::stream), profilerFiller, 0);
					profilerFiller.pop();
					profilerFiller.endTick();
					return preparations;
				},
				executor
			)
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(
				preparations -> {
					this.particles.clear();
					profilerFiller2.startTick();
					profilerFiller2.push("upload");
					this.textureAtlas.reload(preparations);
					profilerFiller2.popPush("bindSpriteSets");
					TextureAtlasSprite textureAtlasSprite = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
					map.forEach(
						(resourceLocation, list) -> {
							ImmutableList<TextureAtlasSprite> immutableList = list.isEmpty()
								? ImmutableList.of(textureAtlasSprite)
								: (ImmutableList)list.stream().map(this.textureAtlas::getSprite).collect(ImmutableList.toImmutableList());
							((ParticleEngine.MutableSpriteSet)this.spriteSets.get(resourceLocation)).rebind(immutableList);
						}
					);
					profilerFiller2.pop();
					profilerFiller2.endTick();
				},
				executor2
			);
	}

	public void close() {
		this.textureAtlas.clearTextureData();
	}

	private void loadParticleDescription(ResourceManager resourceManager, ResourceLocation resourceLocation, Map<ResourceLocation, List<ResourceLocation>> map) {
		ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), "particles/" + resourceLocation.getPath() + ".json");

		try {
			Resource resource = resourceManager.getResource(resourceLocation2);
			Throwable var6 = null;

			try {
				Reader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
				Throwable var8 = null;

				try {
					ParticleDescription particleDescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
					List<ResourceLocation> list = particleDescription.getTextures();
					boolean bl = this.spriteSets.containsKey(resourceLocation);
					if (list == null) {
						if (bl) {
							throw new IllegalStateException("Missing texture list for particle " + resourceLocation);
						}
					} else {
						if (!bl) {
							throw new IllegalStateException("Redundant texture list for particle " + resourceLocation);
						}

						map.put(
							resourceLocation,
							list.stream()
								.map(resourceLocationx -> new ResourceLocation(resourceLocationx.getNamespace(), "particle/" + resourceLocationx.getPath()))
								.collect(Collectors.toList())
						);
					}
				} catch (Throwable var35) {
					var8 = var35;
					throw var35;
				} finally {
					if (reader != null) {
						if (var8 != null) {
							try {
								reader.close();
							} catch (Throwable var34) {
								var8.addSuppressed(var34);
							}
						} else {
							reader.close();
						}
					}
				}
			} catch (Throwable var37) {
				var6 = var37;
				throw var37;
			} finally {
				if (resource != null) {
					if (var6 != null) {
						try {
							resource.close();
						} catch (Throwable var33) {
							var6.addSuppressed(var33);
						}
					} else {
						resource.close();
					}
				}
			}
		} catch (IOException var39) {
			throw new IllegalStateException("Failed to load description for particle " + resourceLocation, var39);
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
		ParticleProvider<T> particleProvider = (ParticleProvider<T>)this.providers
			.get(Registry.PARTICLE_TYPE.getId((ParticleType<? extends ParticleOptions>)particleOptions.getType()));
		return particleProvider == null ? null : particleProvider.createParticle(particleOptions, this.level, d, e, f, g, h, i);
	}

	public void add(Particle particle) {
		this.particlesToAdd.add(particle);
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
					iterator.remove();
				}
			}
		}
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
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.enableFog();
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(poseStack.last().pose());

		for (ParticleRenderType particleRenderType : RENDER_ORDER) {
			Iterable<Particle> iterable = (Iterable<Particle>)this.particles.get(particleRenderType);
			if (iterable != null) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				particleRenderType.begin(bufferBuilder, this.textureManager);

				for (Particle particle : iterable) {
					try {
						particle.render(bufferBuilder, camera, f);
					} catch (Throwable var16) {
						CrashReport crashReport = CrashReport.forThrowable(var16, "Rendering Particle");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
						crashReportCategory.setDetail("Particle", particle::toString);
						crashReportCategory.setDetail("Particle Type", particleRenderType::toString);
						throw new ReportedException(crashReport);
					}
				}

				particleRenderType.end(tesselator);
			}
		}

		RenderSystem.popMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.defaultAlphaFunc();
		lightTexture.turnOffLightLayer();
		RenderSystem.disableFog();
	}

	public void setLevel(@Nullable Level level) {
		this.level = level;
		this.particles.clear();
		this.trackingEmitters.clear();
	}

	public void destroy(BlockPos blockPos, BlockState blockState) {
		if (!blockState.isAir()) {
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
											this.level, (double)blockPos.getX() + v, (double)blockPos.getY() + w, (double)blockPos.getZ() + x, s - 0.5, t - 0.5, u - 0.5, blockState
										)
										.init(blockPos)
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
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
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

			this.add(new TerrainParticle(this.level, d, e, g, 0.0, 0.0, 0.0, blockState).init(blockPos).setPower(0.2F).scale(0.6F));
		}
	}

	public String countParticles() {
		return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
	}

	@Environment(EnvType.CLIENT)
	class MutableSpriteSet implements SpriteSet {
		private List<TextureAtlasSprite> sprites;

		private MutableSpriteSet() {
		}

		@Override
		public TextureAtlasSprite get(int i, int j) {
			return (TextureAtlasSprite)this.sprites.get(i * (this.sprites.size() - 1) / j);
		}

		@Override
		public TextureAtlasSprite get(Random random) {
			return (TextureAtlasSprite)this.sprites.get(random.nextInt(this.sprites.size()));
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
