package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int SECTION_SIZE = 16;
	public static final int HALF_SECTION_SIZE = 8;
	private static final float SKY_DISC_RADIUS = 512.0F;
	private static final int MIN_FOG_DISTANCE = 32;
	private static final int RAIN_RADIUS = 10;
	private static final int RAIN_DIAMETER = 21;
	private static final int TRANSPARENT_SORT_COUNT = 15;
	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
	private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
	public static final Direction[] DIRECTIONS = Direction.values();
	private final Minecraft minecraft;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	private final RenderBuffers renderBuffers;
	@Nullable
	private ClientLevel level;
	private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
	private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);
	private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
	@Nullable
	private ViewArea viewArea;
	@Nullable
	private VertexBuffer starBuffer;
	@Nullable
	private VertexBuffer skyBuffer;
	@Nullable
	private VertexBuffer darkBuffer;
	private boolean generateClouds = true;
	@Nullable
	private VertexBuffer cloudBuffer;
	private final RunningTrimmedMean frameTimes = new RunningTrimmedMean(100);
	private int ticks;
	private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
	private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
	private final Map<BlockPos, SoundInstance> playingRecords = Maps.<BlockPos, SoundInstance>newHashMap();
	@Nullable
	private RenderTarget entityTarget;
	@Nullable
	private PostChain entityEffect;
	@Nullable
	private RenderTarget translucentTarget;
	@Nullable
	private RenderTarget itemEntityTarget;
	@Nullable
	private RenderTarget particlesTarget;
	@Nullable
	private RenderTarget weatherTarget;
	@Nullable
	private RenderTarget cloudsTarget;
	@Nullable
	private PostChain transparencyChain;
	private int lastCameraSectionX = Integer.MIN_VALUE;
	private int lastCameraSectionY = Integer.MIN_VALUE;
	private int lastCameraSectionZ = Integer.MIN_VALUE;
	private double prevCamX = Double.MIN_VALUE;
	private double prevCamY = Double.MIN_VALUE;
	private double prevCamZ = Double.MIN_VALUE;
	private double prevCamRotX = Double.MIN_VALUE;
	private double prevCamRotY = Double.MIN_VALUE;
	private int prevCloudX = Integer.MIN_VALUE;
	private int prevCloudY = Integer.MIN_VALUE;
	private int prevCloudZ = Integer.MIN_VALUE;
	private Vec3 prevCloudColor = Vec3.ZERO;
	@Nullable
	private CloudStatus prevCloudsType;
	@Nullable
	private SectionRenderDispatcher sectionRenderDispatcher;
	private int lastViewDistance = -1;
	private int renderedEntities;
	private int culledEntities;
	private Frustum cullingFrustum;
	private boolean captureFrustum;
	@Nullable
	private Frustum capturedFrustum;
	private final Vector4f[] frustumPoints = new Vector4f[8];
	private final Vector3d frustumPos = new Vector3d(0.0, 0.0, 0.0);
	private double xTransparentOld;
	private double yTransparentOld;
	private double zTransparentOld;
	private int rainSoundTime;
	private final float[] rainSizeX = new float[1024];
	private final float[] rainSizeZ = new float[1024];

	public LevelRenderer(
		Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers
	) {
		this.minecraft = minecraft;
		this.entityRenderDispatcher = entityRenderDispatcher;
		this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
		this.renderBuffers = renderBuffers;

		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				float f = (float)(j - 16);
				float g = (float)(i - 16);
				float h = Mth.sqrt(f * f + g * g);
				this.rainSizeX[i << 5 | j] = -g / h;
				this.rainSizeZ[i << 5 | j] = f / h;
			}
		}

		this.createStars();
		this.createLightSky();
		this.createDarkSky();
	}

	private void renderSnowAndRain(LightTexture lightTexture, float f, double d, double e, double g) {
		float h = this.minecraft.level.getRainLevel(f);
		if (!(h <= 0.0F)) {
			lightTexture.turnOnLightLayer();
			Level level = this.minecraft.level;
			int i = Mth.floor(d);
			int j = Mth.floor(e);
			int k = Mth.floor(g);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			int l = 5;
			if (Minecraft.useFancyGraphics()) {
				l = 10;
			}

			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			int m = -1;
			float n = (float)this.ticks + f;
			RenderSystem.setShader(GameRenderer::getParticleShader);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int o = k - l; o <= k + l; o++) {
				for (int p = i - l; p <= i + l; p++) {
					int q = (o - k + 16) * 32 + p - i + 16;
					double r = (double)this.rainSizeX[q] * 0.5;
					double s = (double)this.rainSizeZ[q] * 0.5;
					mutableBlockPos.set((double)p, e, (double)o);
					Biome biome = level.getBiome(mutableBlockPos).value();
					if (biome.hasPrecipitation()) {
						int t = level.getHeight(Heightmap.Types.MOTION_BLOCKING, p, o);
						int u = j - l;
						int v = j + l;
						if (u < t) {
							u = t;
						}

						if (v < t) {
							v = t;
						}

						int w = t;
						if (t < j) {
							w = j;
						}

						if (u != v) {
							RandomSource randomSource = RandomSource.create((long)(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761));
							mutableBlockPos.set(p, u, o);
							Biome.Precipitation precipitation = biome.getPrecipitationAt(mutableBlockPos);
							if (precipitation == Biome.Precipitation.RAIN) {
								if (m != 0) {
									if (m >= 0) {
										tesselator.end();
									}

									m = 0;
									RenderSystem.setShaderTexture(0, RAIN_LOCATION);
									bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
								}

								int x = this.ticks & 131071;
								int y = p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 0xFF;
								float z = 3.0F + randomSource.nextFloat();
								float aa = -((float)(x + y) + f) / 32.0F * z;
								float ab = aa % 32.0F;
								double ac = (double)p + 0.5 - d;
								double ad = (double)o + 0.5 - g;
								float ae = (float)Math.sqrt(ac * ac + ad * ad) / (float)l;
								float af = ((1.0F - ae * ae) * 0.5F + 0.5F) * h;
								mutableBlockPos.set(p, w, o);
								int ag = getLightColor(level, mutableBlockPos);
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5)
									.uv(0.0F, (float)u * 0.25F + ab)
									.color(1.0F, 1.0F, 1.0F, af)
									.uv2(ag)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5)
									.uv(1.0F, (float)u * 0.25F + ab)
									.color(1.0F, 1.0F, 1.0F, af)
									.uv2(ag)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5)
									.uv(1.0F, (float)v * 0.25F + ab)
									.color(1.0F, 1.0F, 1.0F, af)
									.uv2(ag)
									.endVertex();
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5)
									.uv(0.0F, (float)v * 0.25F + ab)
									.color(1.0F, 1.0F, 1.0F, af)
									.uv2(ag)
									.endVertex();
							} else if (precipitation == Biome.Precipitation.SNOW) {
								if (m != 1) {
									if (m >= 0) {
										tesselator.end();
									}

									m = 1;
									RenderSystem.setShaderTexture(0, SNOW_LOCATION);
									bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
								}

								float ah = -((float)(this.ticks & 511) + f) / 512.0F;
								float ai = (float)(randomSource.nextDouble() + (double)n * 0.01 * (double)((float)randomSource.nextGaussian()));
								float z = (float)(randomSource.nextDouble() + (double)(n * (float)randomSource.nextGaussian()) * 0.001);
								double aj = (double)p + 0.5 - d;
								double ac = (double)o + 0.5 - g;
								float ak = (float)Math.sqrt(aj * aj + ac * ac) / (float)l;
								float al = ((1.0F - ak * ak) * 0.3F + 0.5F) * h;
								mutableBlockPos.set(p, w, o);
								int am = getLightColor(level, mutableBlockPos);
								int an = am >> 16 & 65535;
								int ag = am & 65535;
								int ao = (an * 3 + 240) / 4;
								int ap = (ag * 3 + 240) / 4;
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5)
									.uv(0.0F + ai, (float)u * 0.25F + ah + z)
									.color(1.0F, 1.0F, 1.0F, al)
									.uv2(ap, ao)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5)
									.uv(1.0F + ai, (float)u * 0.25F + ah + z)
									.color(1.0F, 1.0F, 1.0F, al)
									.uv2(ap, ao)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5)
									.uv(1.0F + ai, (float)v * 0.25F + ah + z)
									.color(1.0F, 1.0F, 1.0F, al)
									.uv2(ap, ao)
									.endVertex();
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5)
									.uv(0.0F + ai, (float)v * 0.25F + ah + z)
									.color(1.0F, 1.0F, 1.0F, al)
									.uv2(ap, ao)
									.endVertex();
							}
						}
					}
				}
			}

			if (m >= 0) {
				tesselator.end();
			}

			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			lightTexture.turnOffLightLayer();
		}
	}

	public void tickRain(Camera camera) {
		float f = this.minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
		if (!(f <= 0.0F)) {
			RandomSource randomSource = RandomSource.create((long)this.ticks * 312987231L);
			LevelReader levelReader = this.minecraft.level;
			BlockPos blockPos = BlockPos.containing(camera.getPosition());
			BlockPos blockPos2 = null;
			int i = (int)(100.0F * f * f) / (this.minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

			for (int j = 0; j < i; j++) {
				int k = randomSource.nextInt(21) - 10;
				int l = randomSource.nextInt(21) - 10;
				BlockPos blockPos3 = levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(k, 0, l));
				if (blockPos3.getY() > levelReader.getMinBuildHeight() && blockPos3.getY() <= blockPos.getY() + 10 && blockPos3.getY() >= blockPos.getY() - 10) {
					Biome biome = levelReader.getBiome(blockPos3).value();
					if (biome.getPrecipitationAt(blockPos3) == Biome.Precipitation.RAIN) {
						blockPos2 = blockPos3.below();
						if (this.minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
							break;
						}

						double d = randomSource.nextDouble();
						double e = randomSource.nextDouble();
						BlockState blockState = levelReader.getBlockState(blockPos2);
						FluidState fluidState = levelReader.getFluidState(blockPos2);
						VoxelShape voxelShape = blockState.getCollisionShape(levelReader, blockPos2);
						double g = voxelShape.max(Direction.Axis.Y, d, e);
						double h = (double)fluidState.getHeight(levelReader, blockPos2);
						double m = Math.max(g, h);
						ParticleOptions particleOptions = !fluidState.is(FluidTags.LAVA) && !blockState.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockState)
							? ParticleTypes.RAIN
							: ParticleTypes.SMOKE;
						this.minecraft
							.level
							.addParticle(particleOptions, (double)blockPos2.getX() + d, (double)blockPos2.getY() + m, (double)blockPos2.getZ() + e, 0.0, 0.0, 0.0);
					}
				}
			}

			if (blockPos2 != null && randomSource.nextInt(3) < this.rainSoundTime++) {
				this.rainSoundTime = 0;
				if (blockPos2.getY() > blockPos.getY() + 1
					&& levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float)blockPos.getY())) {
					this.minecraft.level.playLocalSound(blockPos2, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
				} else {
					this.minecraft.level.playLocalSound(blockPos2, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
				}
			}
		}
	}

	public void close() {
		if (this.entityEffect != null) {
			this.entityEffect.close();
		}

		if (this.transparencyChain != null) {
			this.transparencyChain.close();
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.initOutline();
		if (Minecraft.useShaderTransparency()) {
			this.initTransparency();
		}
	}

	public void initOutline() {
		if (this.entityEffect != null) {
			this.entityEffect.close();
		}

		ResourceLocation resourceLocation = new ResourceLocation("shaders/post/entity_outline.json");

		try {
			this.entityEffect = new PostChain(
				this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourceLocation
			);
			this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
			this.entityTarget = this.entityEffect.getTempTarget("final");
		} catch (IOException var3) {
			LOGGER.warn("Failed to load shader: {}", resourceLocation, var3);
			this.entityEffect = null;
			this.entityTarget = null;
		} catch (JsonSyntaxException var4) {
			LOGGER.warn("Failed to parse shader: {}", resourceLocation, var4);
			this.entityEffect = null;
			this.entityTarget = null;
		}
	}

	private void initTransparency() {
		this.deinitTransparency();
		ResourceLocation resourceLocation = new ResourceLocation("shaders/post/transparency.json");

		try {
			PostChain postChain = new PostChain(
				this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourceLocation
			);
			postChain.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
			RenderTarget renderTarget = postChain.getTempTarget("translucent");
			RenderTarget renderTarget2 = postChain.getTempTarget("itemEntity");
			RenderTarget renderTarget3 = postChain.getTempTarget("particles");
			RenderTarget renderTarget4 = postChain.getTempTarget("weather");
			RenderTarget renderTarget5 = postChain.getTempTarget("clouds");
			this.transparencyChain = postChain;
			this.translucentTarget = renderTarget;
			this.itemEntityTarget = renderTarget2;
			this.particlesTarget = renderTarget3;
			this.weatherTarget = renderTarget4;
			this.cloudsTarget = renderTarget5;
		} catch (Exception var8) {
			String string = var8 instanceof JsonSyntaxException ? "parse" : "load";
			String string2 = "Failed to " + string + " shader: " + resourceLocation;
			LevelRenderer.TransparencyShaderException transparencyShaderException = new LevelRenderer.TransparencyShaderException(string2, var8);
			if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1) {
				Component component = (Component)this.minecraft
					.getResourceManager()
					.listPacks()
					.findFirst()
					.map(packResources -> Component.literal(packResources.packId()))
					.orElse(null);
				this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
				this.minecraft.clearResourcePacksOnError(transparencyShaderException, component, null);
			} else {
				this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
				this.minecraft.options.save();
				LOGGER.error(LogUtils.FATAL_MARKER, string2, (Throwable)transparencyShaderException);
				this.minecraft.emergencySaveAndCrash(new CrashReport(string2, transparencyShaderException));
			}
		}
	}

	private void deinitTransparency() {
		if (this.transparencyChain != null) {
			this.transparencyChain.close();
			this.translucentTarget.destroyBuffers();
			this.itemEntityTarget.destroyBuffers();
			this.particlesTarget.destroyBuffers();
			this.weatherTarget.destroyBuffers();
			this.cloudsTarget.destroyBuffers();
			this.transparencyChain = null;
			this.translucentTarget = null;
			this.itemEntityTarget = null;
			this.particlesTarget = null;
			this.weatherTarget = null;
			this.cloudsTarget = null;
		}
	}

	public void doEntityOutline() {
		if (this.shouldShowEntityOutlines()) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			this.entityTarget.blitToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), false);
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	}

	protected boolean shouldShowEntityOutlines() {
		return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
	}

	private void createDarkSky() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.darkBuffer != null) {
			this.darkBuffer.close();
		}

		this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		BufferBuilder.RenderedBuffer renderedBuffer = buildSkyDisc(bufferBuilder, -16.0F);
		this.darkBuffer.bind();
		this.darkBuffer.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	private void createLightSky() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.skyBuffer != null) {
			this.skyBuffer.close();
		}

		this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		BufferBuilder.RenderedBuffer renderedBuffer = buildSkyDisc(bufferBuilder, 16.0F);
		this.skyBuffer.bind();
		this.skyBuffer.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	private static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder bufferBuilder, float f) {
		float g = Math.signum(f) * 512.0F;
		float h = 512.0F;
		RenderSystem.setShader(GameRenderer::getPositionShader);
		bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex(0.0, (double)f, 0.0).endVertex();

		for (int i = -180; i <= 180; i += 45) {
			bufferBuilder.vertex(
					(double)(g * Mth.cos((float)i * (float) (Math.PI / 180.0))), (double)f, (double)(512.0F * Mth.sin((float)i * (float) (Math.PI / 180.0)))
				)
				.endVertex();
		}

		return bufferBuilder.end();
	}

	private void createStars() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		if (this.starBuffer != null) {
			this.starBuffer.close();
		}

		this.starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		BufferBuilder.RenderedBuffer renderedBuffer = this.drawStars(bufferBuilder);
		this.starBuffer.bind();
		this.starBuffer.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	private BufferBuilder.RenderedBuffer drawStars(BufferBuilder bufferBuilder) {
		RandomSource randomSource = RandomSource.create(10842L);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		for (int i = 0; i < 1500; i++) {
			double d = (double)(randomSource.nextFloat() * 2.0F - 1.0F);
			double e = (double)(randomSource.nextFloat() * 2.0F - 1.0F);
			double f = (double)(randomSource.nextFloat() * 2.0F - 1.0F);
			double g = (double)(0.15F + randomSource.nextFloat() * 0.1F);
			double h = d * d + e * e + f * f;
			if (h < 1.0 && h > 0.01) {
				h = 1.0 / Math.sqrt(h);
				d *= h;
				e *= h;
				f *= h;
				double j = d * 100.0;
				double k = e * 100.0;
				double l = f * 100.0;
				double m = Math.atan2(d, f);
				double n = Math.sin(m);
				double o = Math.cos(m);
				double p = Math.atan2(Math.sqrt(d * d + f * f), e);
				double q = Math.sin(p);
				double r = Math.cos(p);
				double s = randomSource.nextDouble() * Math.PI * 2.0;
				double t = Math.sin(s);
				double u = Math.cos(s);

				for (int v = 0; v < 4; v++) {
					double w = 0.0;
					double x = (double)((v & 2) - 1) * g;
					double y = (double)((v + 1 & 2) - 1) * g;
					double z = 0.0;
					double aa = x * u - y * t;
					double ab = y * u + x * t;
					double ad = aa * q + 0.0 * r;
					double ae = 0.0 * q - aa * r;
					double af = ae * n - ab * o;
					double ah = ab * n + ae * o;
					bufferBuilder.vertex(j + af, k + ad, l + ah).endVertex();
				}
			}
		}

		return bufferBuilder.end();
	}

	public void setLevel(@Nullable ClientLevel clientLevel) {
		this.lastCameraSectionX = Integer.MIN_VALUE;
		this.lastCameraSectionY = Integer.MIN_VALUE;
		this.lastCameraSectionZ = Integer.MIN_VALUE;
		this.entityRenderDispatcher.setLevel(clientLevel);
		this.level = clientLevel;
		if (clientLevel != null) {
			this.allChanged();
		} else {
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
				this.viewArea = null;
			}

			if (this.sectionRenderDispatcher != null) {
				this.sectionRenderDispatcher.dispose();
			}

			this.sectionRenderDispatcher = null;
			this.globalBlockEntities.clear();
			this.sectionOcclusionGraph.waitAndReset(null);
			this.visibleSections.clear();
		}
	}

	public void graphicsChanged() {
		if (Minecraft.useShaderTransparency()) {
			this.initTransparency();
		} else {
			this.deinitTransparency();
		}
	}

	public void allChanged() {
		if (this.level != null) {
			this.graphicsChanged();
			this.level.clearTintCaches();
			if (this.sectionRenderDispatcher == null) {
				this.sectionRenderDispatcher = new SectionRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.renderBuffers);
			} else {
				this.sectionRenderDispatcher.setLevel(this.level);
			}

			this.generateClouds = true;
			ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
			this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
			}

			this.sectionRenderDispatcher.blockUntilClear();
			synchronized (this.globalBlockEntities) {
				this.globalBlockEntities.clear();
			}

			this.viewArea = new ViewArea(this.sectionRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
			this.sectionOcclusionGraph.waitAndReset(this.viewArea);
			this.visibleSections.clear();
			Entity entity = this.minecraft.getCameraEntity();
			if (entity != null) {
				this.viewArea.repositionCamera(entity.getX(), entity.getZ());
			}
		}
	}

	public void resize(int i, int j) {
		this.needsUpdate();
		if (this.entityEffect != null) {
			this.entityEffect.resize(i, j);
		}

		if (this.transparencyChain != null) {
			this.transparencyChain.resize(i, j);
		}
	}

	public String getSectionStatistics() {
		int i = this.viewArea.sections.length;
		int j = this.countRenderedSections();
		return String.format(
			Locale.ROOT,
			"C: %d/%d %sD: %d, %s",
			j,
			i,
			this.minecraft.smartCull ? "(s) " : "",
			this.lastViewDistance,
			this.sectionRenderDispatcher == null ? "null" : this.sectionRenderDispatcher.getStats()
		);
	}

	public SectionRenderDispatcher getSectionRenderDispatcher() {
		return this.sectionRenderDispatcher;
	}

	public double getTotalSections() {
		return (double)this.viewArea.sections.length;
	}

	public double getLastViewDistance() {
		return (double)this.lastViewDistance;
	}

	public int countRenderedSections() {
		int i = 0;

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			if (!renderSection.getCompiled().hasNoRenderableLayers()) {
				i++;
			}
		}

		return i;
	}

	public String getEntityStatistics() {
		return "E: "
			+ this.renderedEntities
			+ "/"
			+ this.level.getEntityCount()
			+ ", B: "
			+ this.culledEntities
			+ ", SD: "
			+ this.level.getServerSimulationDistance();
	}

	private void setupRender(Camera camera, Frustum frustum, boolean bl, boolean bl2) {
		Vec3 vec3 = camera.getPosition();
		if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
			this.allChanged();
		}

		this.level.getProfiler().push("camera");
		double d = this.minecraft.player.getX();
		double e = this.minecraft.player.getY();
		double f = this.minecraft.player.getZ();
		int i = SectionPos.posToSectionCoord(d);
		int j = SectionPos.posToSectionCoord(e);
		int k = SectionPos.posToSectionCoord(f);
		if (this.lastCameraSectionX != i || this.lastCameraSectionY != j || this.lastCameraSectionZ != k) {
			this.lastCameraSectionX = i;
			this.lastCameraSectionY = j;
			this.lastCameraSectionZ = k;
			this.viewArea.repositionCamera(d, f);
		}

		this.sectionRenderDispatcher.setCamera(vec3);
		this.level.getProfiler().popPush("cull");
		this.minecraft.getProfiler().popPush("culling");
		BlockPos blockPos = camera.getBlockPosition();
		double g = Math.floor(vec3.x / 8.0);
		double h = Math.floor(vec3.y / 8.0);
		double l = Math.floor(vec3.z / 8.0);
		if (g != this.prevCamX || h != this.prevCamY || l != this.prevCamZ) {
			this.sectionOcclusionGraph.invalidate();
		}

		this.prevCamX = g;
		this.prevCamY = h;
		this.prevCamZ = l;
		this.minecraft.getProfiler().popPush("update");
		if (!bl) {
			boolean bl3 = this.minecraft.smartCull;
			if (bl2 && this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos)) {
				bl3 = false;
			}

			Entity.setViewScale(
				Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get()
			);
			this.minecraft.getProfiler().push("section_occlusion_graph");
			this.sectionOcclusionGraph.update(bl3, camera, frustum, this.visibleSections);
			this.minecraft.getProfiler().pop();
			double m = Math.floor((double)(camera.getXRot() / 2.0F));
			double n = Math.floor((double)(camera.getYRot() / 2.0F));
			if (this.sectionOcclusionGraph.consumeFrustumUpdate() || m != this.prevCamRotX || n != this.prevCamRotY) {
				this.applyFrustum(offsetFrustum(frustum));
				this.prevCamRotX = m;
				this.prevCamRotY = n;
			}
		}

		this.minecraft.getProfiler().pop();
	}

	public static Frustum offsetFrustum(Frustum frustum) {
		return new Frustum(frustum).offsetToFullyIncludeCameraCube(8);
	}

	private void applyFrustum(Frustum frustum) {
		if (!Minecraft.getInstance().isSameThread()) {
			throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
		} else {
			this.minecraft.getProfiler().push("apply_frustum");
			this.visibleSections.clear();
			this.sectionOcclusionGraph.addSectionsInFrustum(frustum, this.visibleSections);
			this.minecraft.getProfiler().pop();
		}
	}

	public void addRecentlyCompiledSection(SectionRenderDispatcher.RenderSection renderSection) {
		this.sectionOcclusionGraph.onSectionCompiled(renderSection);
	}

	private void captureFrustum(Matrix4f matrix4f, Matrix4f matrix4f2, double d, double e, double f, Frustum frustum) {
		this.capturedFrustum = frustum;
		Matrix4f matrix4f3 = new Matrix4f(matrix4f2);
		matrix4f3.mul(matrix4f);
		matrix4f3.invert();
		this.frustumPos.x = d;
		this.frustumPos.y = e;
		this.frustumPos.z = f;
		this.frustumPoints[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
		this.frustumPoints[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
		this.frustumPoints[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
		this.frustumPoints[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
		this.frustumPoints[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
		this.frustumPoints[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
		this.frustumPoints[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.frustumPoints[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

		for (int i = 0; i < 8; i++) {
			matrix4f3.transform(this.frustumPoints[i]);
			this.frustumPoints[i].div(this.frustumPoints[i].w());
		}
	}

	public void prepareCullFrustum(PoseStack poseStack, Vec3 vec3, Matrix4f matrix4f) {
		Matrix4f matrix4f2 = poseStack.last().pose();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		this.cullingFrustum = new Frustum(matrix4f2, matrix4f);
		this.cullingFrustum.prepare(d, e, f);
	}

	public void renderLevel(
		PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f
	) {
		TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
		float g = tickRateManager.runsNormally() ? f : 1.0F;
		RenderSystem.setShaderGameTime(this.level.getGameTime(), g);
		this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
		this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
		ProfilerFiller profilerFiller = this.level.getProfiler();
		profilerFiller.popPush("light_update_queue");
		this.level.pollLightUpdates();
		profilerFiller.popPush("light_updates");
		this.level.getChunkSource().getLightEngine().runLightUpdates();
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double h = vec3.z();
		Matrix4f matrix4f2 = poseStack.last().pose();
		profilerFiller.popPush("culling");
		boolean bl2 = this.capturedFrustum != null;
		Frustum frustum;
		if (bl2) {
			frustum = this.capturedFrustum;
			frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
		} else {
			frustum = this.cullingFrustum;
		}

		this.minecraft.getProfiler().popPush("captureFrustum");
		if (this.captureFrustum) {
			this.captureFrustum(matrix4f2, matrix4f, vec3.x, vec3.y, vec3.z, bl2 ? new Frustum(matrix4f2, matrix4f) : frustum);
			this.captureFrustum = false;
		}

		profilerFiller.popPush("clear");
		FogRenderer.setupColor(camera, g, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), gameRenderer.getDarkenWorldAmount(g));
		FogRenderer.levelFogColor();
		RenderSystem.clear(16640, Minecraft.ON_OSX);
		float i = gameRenderer.getRenderDistance();
		boolean bl3 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d), Mth.floor(e)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
		profilerFiller.popPush("sky");
		RenderSystem.setShader(GameRenderer::getPositionShader);
		this.renderSky(poseStack, matrix4f, g, camera, bl3, () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, i, bl3, g));
		profilerFiller.popPush("fog");
		FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(i, 32.0F), bl3, g);
		profilerFiller.popPush("terrain_setup");
		this.setupRender(camera, frustum, bl2, this.minecraft.player.isSpectator());
		profilerFiller.popPush("compile_sections");
		this.compileSections(camera);
		profilerFiller.popPush("terrain");
		this.renderSectionLayer(RenderType.solid(), poseStack, d, e, h, matrix4f);
		this.renderSectionLayer(RenderType.cutoutMipped(), poseStack, d, e, h, matrix4f);
		this.renderSectionLayer(RenderType.cutout(), poseStack, d, e, h, matrix4f);
		if (this.level.effects().constantAmbientLight()) {
			Lighting.setupNetherLevel(poseStack.last().pose());
		} else {
			Lighting.setupLevel(poseStack.last().pose());
		}

		profilerFiller.popPush("entities");
		this.renderedEntities = 0;
		this.culledEntities = 0;
		if (this.itemEntityTarget != null) {
			this.itemEntityTarget.clear(Minecraft.ON_OSX);
			this.itemEntityTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
			this.minecraft.getMainRenderTarget().bindWrite(false);
		}

		if (this.weatherTarget != null) {
			this.weatherTarget.clear(Minecraft.ON_OSX);
		}

		if (this.shouldShowEntityOutlines()) {
			this.entityTarget.clear(Minecraft.ON_OSX);
			this.minecraft.getMainRenderTarget().bindWrite(false);
		}

		boolean bl4 = false;
		MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();

		for (Entity entity : this.level.entitiesForRendering()) {
			if (this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, h) || entity.hasIndirectPassenger(this.minecraft.player)) {
				BlockPos blockPos = entity.blockPosition();
				if ((this.level.isOutsideBuildHeight(blockPos.getY()) || this.isSectionCompiled(blockPos))
					&& (entity != camera.getEntity() || camera.isDetached() || camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping())
					&& (!(entity instanceof LocalPlayer) || camera.getEntity() == entity)) {
					this.renderedEntities++;
					if (entity.tickCount == 0) {
						entity.xOld = entity.getX();
						entity.yOld = entity.getY();
						entity.zOld = entity.getZ();
					}

					MultiBufferSource multiBufferSource;
					if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
						bl4 = true;
						OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
						multiBufferSource = outlineBufferSource;
						int j = entity.getTeamColor();
						outlineBufferSource.setColor(FastColor.ARGB32.red(j), FastColor.ARGB32.green(j), FastColor.ARGB32.blue(j), 255);
					} else {
						multiBufferSource = bufferSource;
					}

					float k = tickRateManager.isEntityFrozen(entity) ? g : f;
					this.renderEntity(entity, d, e, h, k, poseStack, multiBufferSource);
				}
			}
		}

		bufferSource.endLastBatch();
		this.checkPoseStack(poseStack);
		bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
		bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
		bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
		bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
		profilerFiller.popPush("blockentities");

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			List<BlockEntity> list = renderSection.getCompiled().getRenderableBlockEntities();
			if (!list.isEmpty()) {
				for (BlockEntity blockEntity : list) {
					BlockPos blockPos2 = blockEntity.getBlockPos();
					MultiBufferSource multiBufferSource2 = bufferSource;
					poseStack.pushPose();
					poseStack.translate((double)blockPos2.getX() - d, (double)blockPos2.getY() - e, (double)blockPos2.getZ() - h);
					SortedSet<BlockDestructionProgress> sortedSet = this.destructionProgress.get(blockPos2.asLong());
					if (sortedSet != null && !sortedSet.isEmpty()) {
						int m = ((BlockDestructionProgress)sortedSet.last()).getProgress();
						if (m >= 0) {
							PoseStack.Pose pose = poseStack.last();
							VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(
								this.renderBuffers.crumblingBufferSource().getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(m)), pose.pose(), pose.normal(), 1.0F
							);
							multiBufferSource2 = renderType -> {
								VertexConsumer vertexConsumer2x = bufferSource.getBuffer(renderType);
								return renderType.affectsCrumbling() ? VertexMultiConsumer.create(vertexConsumer, vertexConsumer2x) : vertexConsumer2x;
							};
						}
					}

					this.blockEntityRenderDispatcher.render(blockEntity, g, poseStack, multiBufferSource2);
					poseStack.popPose();
				}
			}
		}

		synchronized (this.globalBlockEntities) {
			for (BlockEntity blockEntity2 : this.globalBlockEntities) {
				BlockPos blockPos3 = blockEntity2.getBlockPos();
				poseStack.pushPose();
				poseStack.translate((double)blockPos3.getX() - d, (double)blockPos3.getY() - e, (double)blockPos3.getZ() - h);
				this.blockEntityRenderDispatcher.render(blockEntity2, g, poseStack, bufferSource);
				poseStack.popPose();
			}
		}

		this.checkPoseStack(poseStack);
		bufferSource.endBatch(RenderType.solid());
		bufferSource.endBatch(RenderType.endPortal());
		bufferSource.endBatch(RenderType.endGateway());
		bufferSource.endBatch(Sheets.solidBlockSheet());
		bufferSource.endBatch(Sheets.cutoutBlockSheet());
		bufferSource.endBatch(Sheets.bedSheet());
		bufferSource.endBatch(Sheets.shulkerBoxSheet());
		bufferSource.endBatch(Sheets.signSheet());
		bufferSource.endBatch(Sheets.hangingSignSheet());
		bufferSource.endBatch(Sheets.chestSheet());
		this.renderBuffers.outlineBufferSource().endOutlineBatch();
		if (bl4) {
			this.entityEffect.process(g);
			this.minecraft.getMainRenderTarget().bindWrite(false);
		}

		profilerFiller.popPush("destroyProgress");

		for (Entry<SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.long2ObjectEntrySet()) {
			BlockPos blockPos = BlockPos.of(entry.getLongKey());
			double n = (double)blockPos.getX() - d;
			double o = (double)blockPos.getY() - e;
			double p = (double)blockPos.getZ() - h;
			if (!(n * n + o * o + p * p > 1024.0)) {
				SortedSet<BlockDestructionProgress> sortedSet2 = (SortedSet<BlockDestructionProgress>)entry.getValue();
				if (sortedSet2 != null && !sortedSet2.isEmpty()) {
					int q = ((BlockDestructionProgress)sortedSet2.last()).getProgress();
					poseStack.pushPose();
					poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - h);
					PoseStack.Pose pose2 = poseStack.last();
					VertexConsumer vertexConsumer2 = new SheetedDecalTextureGenerator(
						this.renderBuffers.crumblingBufferSource().getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(q)), pose2.pose(), pose2.normal(), 1.0F
					);
					this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos), blockPos, this.level, poseStack, vertexConsumer2);
					poseStack.popPose();
				}
			}
		}

		this.checkPoseStack(poseStack);
		HitResult hitResult = this.minecraft.hitResult;
		if (bl && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
			profilerFiller.popPush("outline");
			BlockPos blockPos4 = ((BlockHitResult)hitResult).getBlockPos();
			BlockState blockState = this.level.getBlockState(blockPos4);
			if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos4)) {
				VertexConsumer vertexConsumer3 = bufferSource.getBuffer(RenderType.lines());
				this.renderHitOutline(poseStack, vertexConsumer3, camera.getEntity(), d, e, h, blockPos4, blockState);
			}
		}

		this.minecraft.debugRenderer.render(poseStack, bufferSource, d, e, h);
		bufferSource.endLastBatch();
		PoseStack poseStack2 = RenderSystem.getModelViewStack();
		RenderSystem.applyModelViewMatrix();
		bufferSource.endBatch(Sheets.translucentCullBlockSheet());
		bufferSource.endBatch(Sheets.bannerSheet());
		bufferSource.endBatch(Sheets.shieldSheet());
		bufferSource.endBatch(RenderType.armorGlint());
		bufferSource.endBatch(RenderType.armorEntityGlint());
		bufferSource.endBatch(RenderType.glint());
		bufferSource.endBatch(RenderType.glintDirect());
		bufferSource.endBatch(RenderType.glintTranslucent());
		bufferSource.endBatch(RenderType.entityGlint());
		bufferSource.endBatch(RenderType.entityGlintDirect());
		bufferSource.endBatch(RenderType.waterMask());
		this.renderBuffers.crumblingBufferSource().endBatch();
		if (this.transparencyChain != null) {
			bufferSource.endBatch(RenderType.lines());
			bufferSource.endBatch();
			this.translucentTarget.clear(Minecraft.ON_OSX);
			this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
			profilerFiller.popPush("translucent");
			this.renderSectionLayer(RenderType.translucent(), poseStack, d, e, h, matrix4f);
			profilerFiller.popPush("string");
			this.renderSectionLayer(RenderType.tripwire(), poseStack, d, e, h, matrix4f);
			this.particlesTarget.clear(Minecraft.ON_OSX);
			this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
			RenderStateShard.PARTICLES_TARGET.setupRenderState();
			profilerFiller.popPush("particles");
			this.minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, g);
			RenderStateShard.PARTICLES_TARGET.clearRenderState();
		} else {
			profilerFiller.popPush("translucent");
			if (this.translucentTarget != null) {
				this.translucentTarget.clear(Minecraft.ON_OSX);
			}

			this.renderSectionLayer(RenderType.translucent(), poseStack, d, e, h, matrix4f);
			bufferSource.endBatch(RenderType.lines());
			bufferSource.endBatch();
			profilerFiller.popPush("string");
			this.renderSectionLayer(RenderType.tripwire(), poseStack, d, e, h, matrix4f);
			profilerFiller.popPush("particles");
			this.minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, g);
		}

		poseStack2.pushPose();
		poseStack2.mulPoseMatrix(poseStack.last().pose());
		RenderSystem.applyModelViewMatrix();
		if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
			if (this.transparencyChain != null) {
				this.cloudsTarget.clear(Minecraft.ON_OSX);
				RenderStateShard.CLOUDS_TARGET.setupRenderState();
				profilerFiller.popPush("clouds");
				this.renderClouds(poseStack, matrix4f, g, d, e, h);
				RenderStateShard.CLOUDS_TARGET.clearRenderState();
			} else {
				profilerFiller.popPush("clouds");
				RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
				this.renderClouds(poseStack, matrix4f, g, d, e, h);
			}
		}

		if (this.transparencyChain != null) {
			RenderStateShard.WEATHER_TARGET.setupRenderState();
			profilerFiller.popPush("weather");
			this.renderSnowAndRain(lightTexture, g, d, e, h);
			this.renderWorldBorder(camera);
			RenderStateShard.WEATHER_TARGET.clearRenderState();
			this.transparencyChain.process(g);
			this.minecraft.getMainRenderTarget().bindWrite(false);
		} else {
			RenderSystem.depthMask(false);
			profilerFiller.popPush("weather");
			this.renderSnowAndRain(lightTexture, g, d, e, h);
			this.renderWorldBorder(camera);
			RenderSystem.depthMask(true);
		}

		poseStack2.popPose();
		RenderSystem.applyModelViewMatrix();
		this.renderDebug(poseStack, bufferSource, camera);
		bufferSource.endLastBatch();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		FogRenderer.setupNoFog();
	}

	private void checkPoseStack(PoseStack poseStack) {
		if (!poseStack.clear()) {
			throw new IllegalStateException("Pose stack not empty");
		}
	}

	private void renderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		double h = Mth.lerp((double)g, entity.xOld, entity.getX());
		double i = Mth.lerp((double)g, entity.yOld, entity.getY());
		double j = Mth.lerp((double)g, entity.zOld, entity.getZ());
		float k = Mth.lerp(g, entity.yRotO, entity.getYRot());
		this.entityRenderDispatcher
			.render(entity, h - d, i - e, j - f, k, g, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, g));
	}

	private void renderSectionLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f matrix4f) {
		RenderSystem.assertOnRenderThread();
		renderType.setupRenderState();
		if (renderType == RenderType.translucent()) {
			this.minecraft.getProfiler().push("translucent_sort");
			double g = d - this.xTransparentOld;
			double h = e - this.yTransparentOld;
			double i = f - this.zTransparentOld;
			if (g * g + h * h + i * i > 1.0) {
				int j = SectionPos.posToSectionCoord(d);
				int k = SectionPos.posToSectionCoord(e);
				int l = SectionPos.posToSectionCoord(f);
				boolean bl = j != SectionPos.posToSectionCoord(this.xTransparentOld)
					|| l != SectionPos.posToSectionCoord(this.zTransparentOld)
					|| k != SectionPos.posToSectionCoord(this.yTransparentOld);
				this.xTransparentOld = d;
				this.yTransparentOld = e;
				this.zTransparentOld = f;
				int m = 0;

				for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
					if (m < 15 && (bl || renderSection.isAxisAlignedWith(j, k, l)) && renderSection.resortTransparency(renderType, this.sectionRenderDispatcher)) {
						m++;
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}

		this.minecraft.getProfiler().push("filterempty");
		this.minecraft.getProfiler().popPush((Supplier<String>)(() -> "render_" + renderType));
		boolean bl2 = renderType != RenderType.translucent();
		ObjectListIterator<SectionRenderDispatcher.RenderSection> objectListIterator = this.visibleSections.listIterator(bl2 ? 0 : this.visibleSections.size());
		ShaderInstance shaderInstance = RenderSystem.getShader();

		for (int n = 0; n < 12; n++) {
			int o = RenderSystem.getShaderTexture(n);
			shaderInstance.setSampler("Sampler" + n, o);
		}

		if (shaderInstance.MODEL_VIEW_MATRIX != null) {
			shaderInstance.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
		}

		if (shaderInstance.PROJECTION_MATRIX != null) {
			shaderInstance.PROJECTION_MATRIX.set(matrix4f);
		}

		if (shaderInstance.COLOR_MODULATOR != null) {
			shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		}

		if (shaderInstance.GLINT_ALPHA != null) {
			shaderInstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
		}

		if (shaderInstance.FOG_START != null) {
			shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
		}

		if (shaderInstance.FOG_END != null) {
			shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
		}

		if (shaderInstance.FOG_COLOR != null) {
			shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		}

		if (shaderInstance.FOG_SHAPE != null) {
			shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		}

		if (shaderInstance.TEXTURE_MATRIX != null) {
			shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
		}

		if (shaderInstance.GAME_TIME != null) {
			shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
		}

		RenderSystem.setupShaderLights(shaderInstance);
		shaderInstance.apply();
		Uniform uniform = shaderInstance.CHUNK_OFFSET;

		while (bl2 ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
			SectionRenderDispatcher.RenderSection renderSection2 = bl2
				? (SectionRenderDispatcher.RenderSection)objectListIterator.next()
				: objectListIterator.previous();
			if (!renderSection2.getCompiled().isEmpty(renderType)) {
				VertexBuffer vertexBuffer = renderSection2.getBuffer(renderType);
				BlockPos blockPos = renderSection2.getOrigin();
				if (uniform != null) {
					uniform.set((float)((double)blockPos.getX() - d), (float)((double)blockPos.getY() - e), (float)((double)blockPos.getZ() - f));
					uniform.upload();
				}

				vertexBuffer.bind();
				vertexBuffer.draw();
			}
		}

		if (uniform != null) {
			uniform.set(0.0F, 0.0F, 0.0F);
		}

		shaderInstance.clear();
		VertexBuffer.unbind();
		this.minecraft.getProfiler().pop();
		renderType.clearRenderState();
	}

	private void renderDebug(PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera) {
		if (this.minecraft.sectionPath || this.minecraft.sectionVisibility) {
			double d = camera.getPosition().x();
			double e = camera.getPosition().y();
			double f = camera.getPosition().z();

			for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
				SectionOcclusionGraph.Node node = this.sectionOcclusionGraph.getNode(renderSection);
				if (node != null) {
					BlockPos blockPos = renderSection.getOrigin();
					poseStack.pushPose();
					poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
					Matrix4f matrix4f = poseStack.last().pose();
					if (this.minecraft.sectionPath) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
						int i = node.step == 0 ? 0 : Mth.hsvToRgb((float)node.step / 50.0F, 0.9F, 0.9F);
						int j = i >> 16 & 0xFF;
						int k = i >> 8 & 0xFF;
						int l = i & 0xFF;

						for (int m = 0; m < DIRECTIONS.length; m++) {
							if (node.hasSourceDirection(m)) {
								Direction direction = DIRECTIONS[m];
								vertexConsumer.vertex(matrix4f, 8.0F, 8.0F, 8.0F)
									.color(j, k, l, 255)
									.normal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ())
									.endVertex();
								vertexConsumer.vertex(matrix4f, (float)(8 - 16 * direction.getStepX()), (float)(8 - 16 * direction.getStepY()), (float)(8 - 16 * direction.getStepZ()))
									.color(j, k, l, 255)
									.normal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ())
									.endVertex();
							}
						}
					}

					if (this.minecraft.sectionVisibility && !renderSection.getCompiled().hasNoRenderableLayers()) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
						int i = 0;

						for (Direction direction2 : DIRECTIONS) {
							for (Direction direction3 : DIRECTIONS) {
								boolean bl = renderSection.getCompiled().facesCanSeeEachother(direction2, direction3);
								if (!bl) {
									i++;
									vertexConsumer.vertex(matrix4f, (float)(8 + 8 * direction2.getStepX()), (float)(8 + 8 * direction2.getStepY()), (float)(8 + 8 * direction2.getStepZ()))
										.color(255, 0, 0, 255)
										.normal((float)direction2.getStepX(), (float)direction2.getStepY(), (float)direction2.getStepZ())
										.endVertex();
									vertexConsumer.vertex(matrix4f, (float)(8 + 8 * direction3.getStepX()), (float)(8 + 8 * direction3.getStepY()), (float)(8 + 8 * direction3.getStepZ()))
										.color(255, 0, 0, 255)
										.normal((float)direction3.getStepX(), (float)direction3.getStepY(), (float)direction3.getStepZ())
										.endVertex();
								}
							}
						}

						if (i > 0) {
							VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.debugQuads());
							float g = 0.5F;
							float h = 0.2F;
							vertexConsumer2.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
							vertexConsumer2.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						}
					}

					poseStack.popPose();
				}
			}
		}

		if (this.capturedFrustum != null) {
			poseStack.pushPose();
			poseStack.translate(
				(float)(this.frustumPos.x - camera.getPosition().x),
				(float)(this.frustumPos.y - camera.getPosition().y),
				(float)(this.frustumPos.z - camera.getPosition().z)
			);
			Matrix4f matrix4f2 = poseStack.last().pose();
			VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.debugQuads());
			this.addFrustumQuad(vertexConsumer3, matrix4f2, 0, 1, 2, 3, 0, 1, 1);
			this.addFrustumQuad(vertexConsumer3, matrix4f2, 4, 5, 6, 7, 1, 0, 0);
			this.addFrustumQuad(vertexConsumer3, matrix4f2, 0, 1, 5, 4, 1, 1, 0);
			this.addFrustumQuad(vertexConsumer3, matrix4f2, 2, 3, 7, 6, 0, 0, 1);
			this.addFrustumQuad(vertexConsumer3, matrix4f2, 0, 4, 7, 3, 0, 1, 0);
			this.addFrustumQuad(vertexConsumer3, matrix4f2, 1, 5, 6, 2, 1, 0, 1);
			VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.lines());
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 0);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 1);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 1);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 2);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 2);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 3);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 3);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 0);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 4);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 5);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 5);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 6);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 6);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 7);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 7);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 4);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 0);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 4);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 1);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 5);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 2);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 6);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 3);
			this.addFrustumVertex(vertexConsumer4, matrix4f2, 7);
			poseStack.popPose();
		}
	}

	private void addFrustumVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i) {
		vertexConsumer.vertex(matrix4f, this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z())
			.color(0, 0, 0, 255)
			.normal(0.0F, 0.0F, -1.0F)
			.endVertex();
	}

	private void addFrustumQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, int j, int k, int l, int m, int n, int o) {
		float f = 0.25F;
		vertexConsumer.vertex(matrix4f, this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
		vertexConsumer.vertex(matrix4f, this.frustumPoints[j].x(), this.frustumPoints[j].y(), this.frustumPoints[j].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
		vertexConsumer.vertex(matrix4f, this.frustumPoints[k].x(), this.frustumPoints[k].y(), this.frustumPoints[k].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
		vertexConsumer.vertex(matrix4f, this.frustumPoints[l].x(), this.frustumPoints[l].y(), this.frustumPoints[l].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
	}

	public void captureFrustum() {
		this.captureFrustum = true;
	}

	public void killFrustum() {
		this.capturedFrustum = null;
	}

	public void tick() {
		if (this.level.tickRateManager().runsNormally()) {
			this.ticks++;
		}

		if (this.ticks % 20 == 0) {
			Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

			while (iterator.hasNext()) {
				BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)iterator.next();
				int i = blockDestructionProgress.getUpdatedRenderTick();
				if (this.ticks - i > 400) {
					iterator.remove();
					this.removeProgress(blockDestructionProgress);
				}
			}
		}
	}

	private void removeProgress(BlockDestructionProgress blockDestructionProgress) {
		long l = blockDestructionProgress.getPos().asLong();
		Set<BlockDestructionProgress> set = (Set<BlockDestructionProgress>)this.destructionProgress.get(l);
		set.remove(blockDestructionProgress);
		if (set.isEmpty()) {
			this.destructionProgress.remove(l);
		}
	}

	private void renderEndSky(PoseStack poseStack) {
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();

		for (int i = 0; i < 6; i++) {
			poseStack.pushPose();
			if (i == 1) {
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			}

			if (i == 2) {
				poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			}

			if (i == 3) {
				poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			}

			if (i == 4) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			}

			if (i == 5) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
			}

			Matrix4f matrix4f = poseStack.last().pose();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			tesselator.end();
			poseStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}

	public void renderSky(PoseStack poseStack, Matrix4f matrix4f, float f, Camera camera, boolean bl, Runnable runnable) {
		runnable.run();
		if (!bl) {
			FogType fogType = camera.getFluidInCamera();
			if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
				if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
					this.renderEndSky(poseStack);
				} else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
					Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), f);
					float g = (float)vec3.x;
					float h = (float)vec3.y;
					float i = (float)vec3.z;
					FogRenderer.levelFogColor();
					BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
					RenderSystem.depthMask(false);
					RenderSystem.setShaderColor(g, h, i, 1.0F);
					ShaderInstance shaderInstance = RenderSystem.getShader();
					this.skyBuffer.bind();
					this.skyBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
					VertexBuffer.unbind();
					RenderSystem.enableBlend();
					float[] fs = this.level.effects().getSunriseColor(this.level.getTimeOfDay(f), f);
					if (fs != null) {
						RenderSystem.setShader(GameRenderer::getPositionColorShader);
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						poseStack.pushPose();
						poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
						float j = Mth.sin(this.level.getSunAngle(f)) < 0.0F ? 180.0F : 0.0F;
						poseStack.mulPose(Axis.ZP.rotationDegrees(j));
						poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
						float k = fs[0];
						float l = fs[1];
						float m = fs[2];
						Matrix4f matrix4f2 = poseStack.last().pose();
						bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
						bufferBuilder.vertex(matrix4f2, 0.0F, 100.0F, 0.0F).color(k, l, m, fs[3]).endVertex();
						int n = 16;

						for (int o = 0; o <= 16; o++) {
							float p = (float)o * (float) (Math.PI * 2) / 16.0F;
							float q = Mth.sin(p);
							float r = Mth.cos(p);
							bufferBuilder.vertex(matrix4f2, q * 120.0F, r * 120.0F, -r * 40.0F * fs[3]).color(fs[0], fs[1], fs[2], 0.0F).endVertex();
						}

						BufferUploader.drawWithShader(bufferBuilder.end());
						poseStack.popPose();
					}

					RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
					);
					poseStack.pushPose();
					float j = 1.0F - this.level.getRainLevel(f);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, j);
					poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
					poseStack.mulPose(Axis.XP.rotationDegrees(this.level.getTimeOfDay(f) * 360.0F));
					Matrix4f matrix4f3 = poseStack.last().pose();
					float l = 30.0F;
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderTexture(0, SUN_LOCATION);
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
					bufferBuilder.vertex(matrix4f3, -l, 100.0F, -l).uv(0.0F, 0.0F).endVertex();
					bufferBuilder.vertex(matrix4f3, l, 100.0F, -l).uv(1.0F, 0.0F).endVertex();
					bufferBuilder.vertex(matrix4f3, l, 100.0F, l).uv(1.0F, 1.0F).endVertex();
					bufferBuilder.vertex(matrix4f3, -l, 100.0F, l).uv(0.0F, 1.0F).endVertex();
					BufferUploader.drawWithShader(bufferBuilder.end());
					l = 20.0F;
					RenderSystem.setShaderTexture(0, MOON_LOCATION);
					int s = this.level.getMoonPhase();
					int t = s % 4;
					int n = s / 4 % 2;
					float u = (float)(t + 0) / 4.0F;
					float p = (float)(n + 0) / 2.0F;
					float q = (float)(t + 1) / 4.0F;
					float r = (float)(n + 1) / 2.0F;
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
					bufferBuilder.vertex(matrix4f3, -l, -100.0F, l).uv(q, r).endVertex();
					bufferBuilder.vertex(matrix4f3, l, -100.0F, l).uv(u, r).endVertex();
					bufferBuilder.vertex(matrix4f3, l, -100.0F, -l).uv(u, p).endVertex();
					bufferBuilder.vertex(matrix4f3, -l, -100.0F, -l).uv(q, p).endVertex();
					BufferUploader.drawWithShader(bufferBuilder.end());
					float v = this.level.getStarBrightness(f) * j;
					if (v > 0.0F) {
						RenderSystem.setShaderColor(v, v, v, v);
						FogRenderer.setupNoFog();
						this.starBuffer.bind();
						this.starBuffer.drawWithShader(poseStack.last().pose(), matrix4f, GameRenderer.getPositionShader());
						VertexBuffer.unbind();
						runnable.run();
					}

					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.disableBlend();
					RenderSystem.defaultBlendFunc();
					poseStack.popPose();
					RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
					double d = this.minecraft.player.getEyePosition(f).y - this.level.getLevelData().getHorizonHeight(this.level);
					if (d < 0.0) {
						poseStack.pushPose();
						poseStack.translate(0.0F, 12.0F, 0.0F);
						this.darkBuffer.bind();
						this.darkBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
						VertexBuffer.unbind();
						poseStack.popPose();
					}

					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.depthMask(true);
				}
			}
		}
	}

	private boolean doesMobEffectBlockSky(Camera camera) {
		return !(camera.getEntity() instanceof LivingEntity livingEntity)
			? false
			: livingEntity.hasEffect(MobEffects.BLINDNESS) || livingEntity.hasEffect(MobEffects.DARKNESS);
	}

	public void renderClouds(PoseStack poseStack, Matrix4f matrix4f, float f, double d, double e, double g) {
		float h = this.level.effects().getCloudHeight();
		if (!Float.isNaN(h)) {
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
			);
			RenderSystem.depthMask(true);
			float i = 12.0F;
			float j = 4.0F;
			double k = 2.0E-4;
			double l = (double)(((float)this.ticks + f) * 0.03F);
			double m = (d + l) / 12.0;
			double n = (double)(h - (float)e + 0.33F);
			double o = g / 12.0 + 0.33F;
			m -= (double)(Mth.floor(m / 2048.0) * 2048);
			o -= (double)(Mth.floor(o / 2048.0) * 2048);
			float p = (float)(m - (double)Mth.floor(m));
			float q = (float)(n / 4.0 - (double)Mth.floor(n / 4.0)) * 4.0F;
			float r = (float)(o - (double)Mth.floor(o));
			Vec3 vec3 = this.level.getCloudColor(f);
			int s = (int)Math.floor(m);
			int t = (int)Math.floor(n / 4.0);
			int u = (int)Math.floor(o);
			if (s != this.prevCloudX
				|| t != this.prevCloudY
				|| u != this.prevCloudZ
				|| this.minecraft.options.getCloudsType() != this.prevCloudsType
				|| this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4) {
				this.prevCloudX = s;
				this.prevCloudY = t;
				this.prevCloudZ = u;
				this.prevCloudColor = vec3;
				this.prevCloudsType = this.minecraft.options.getCloudsType();
				this.generateClouds = true;
			}

			if (this.generateClouds) {
				this.generateClouds = false;
				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
				if (this.cloudBuffer != null) {
					this.cloudBuffer.close();
				}

				this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
				BufferBuilder.RenderedBuffer renderedBuffer = this.buildClouds(bufferBuilder, m, n, o, vec3);
				this.cloudBuffer.bind();
				this.cloudBuffer.upload(renderedBuffer);
				VertexBuffer.unbind();
			}

			RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
			RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
			FogRenderer.levelFogColor();
			poseStack.pushPose();
			poseStack.scale(12.0F, 1.0F, 12.0F);
			poseStack.translate(-p, q, -r);
			if (this.cloudBuffer != null) {
				this.cloudBuffer.bind();
				int v = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

				for (int w = v; w < 2; w++) {
					if (w == 0) {
						RenderSystem.colorMask(false, false, false, false);
					} else {
						RenderSystem.colorMask(true, true, true, true);
					}

					ShaderInstance shaderInstance = RenderSystem.getShader();
					this.cloudBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
				}

				VertexBuffer.unbind();
			}

			poseStack.popPose();
			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	}

	private BufferBuilder.RenderedBuffer buildClouds(BufferBuilder bufferBuilder, double d, double e, double f, Vec3 vec3) {
		float g = 4.0F;
		float h = 0.00390625F;
		int i = 8;
		int j = 4;
		float k = 9.765625E-4F;
		float l = (float)Mth.floor(d) * 0.00390625F;
		float m = (float)Mth.floor(f) * 0.00390625F;
		float n = (float)vec3.x;
		float o = (float)vec3.y;
		float p = (float)vec3.z;
		float q = n * 0.9F;
		float r = o * 0.9F;
		float s = p * 0.9F;
		float t = n * 0.7F;
		float u = o * 0.7F;
		float v = p * 0.7F;
		float w = n * 0.8F;
		float x = o * 0.8F;
		float y = p * 0.8F;
		RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
		float z = (float)Math.floor(e / 4.0) * 4.0F;
		if (this.prevCloudsType == CloudStatus.FANCY) {
			for (int aa = -3; aa <= 4; aa++) {
				for (int ab = -3; ab <= 4; ab++) {
					float ac = (float)(aa * 8);
					float ad = (float)(ab * 8);
					if (z > -5.0F) {
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + 8.0F))
							.uv((ac + 0.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + 8.0F))
							.uv((ac + 8.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + 0.0F))
							.uv((ac + 8.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + 0.0F))
							.uv((ac + 0.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
					}

					if (z <= 5.0F) {
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 8.0F))
							.uv((ac + 0.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 8.0F))
							.uv((ac + 8.0F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 0.0F))
							.uv((ac + 8.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 0.0F))
							.uv((ac + 0.0F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
					}

					if (aa > -1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 0.0F), (double)(ad + 8.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 4.0F), (double)(ad + 8.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 4.0F), (double)(ad + 0.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 0.0F), (double)(ad + 0.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
						}
					}

					if (aa <= 1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 0.0F), (double)(ad + 8.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 4.0F), (double)(ad + 8.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 8.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 4.0F), (double)(ad + 0.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 0.0F), (double)(ad + 0.0F))
								.uv((ac + (float)ae + 0.5F) * 0.00390625F + l, (ad + 0.0F) * 0.00390625F + m)
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
						}
					}

					if (ab > -1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 0.0F))
								.uv((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 0.0F))
								.uv((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 0.0F))
								.uv((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 0.0F))
								.uv((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
						}
					}

					if (ab <= 1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((ac + 8.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((ac + 0.0F) * 0.00390625F + l, (ad + (float)ae + 0.5F) * 0.00390625F + m)
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
						}
					}
				}
			}
		} else {
			int aa = 1;
			int ab = 32;

			for (int af = -32; af < 32; af += 32) {
				for (int ag = -32; ag < 32; ag += 32) {
					bufferBuilder.vertex((double)(af + 0), (double)z, (double)(ag + 32))
						.uv((float)(af + 0) * 0.00390625F + l, (float)(ag + 32) * 0.00390625F + m)
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
					bufferBuilder.vertex((double)(af + 32), (double)z, (double)(ag + 32))
						.uv((float)(af + 32) * 0.00390625F + l, (float)(ag + 32) * 0.00390625F + m)
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
					bufferBuilder.vertex((double)(af + 32), (double)z, (double)(ag + 0))
						.uv((float)(af + 32) * 0.00390625F + l, (float)(ag + 0) * 0.00390625F + m)
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
					bufferBuilder.vertex((double)(af + 0), (double)z, (double)(ag + 0))
						.uv((float)(af + 0) * 0.00390625F + l, (float)(ag + 0) * 0.00390625F + m)
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
				}
			}
		}

		return bufferBuilder.end();
	}

	private void compileSections(Camera camera) {
		this.minecraft.getProfiler().push("populate_sections_to_compile");
		LevelLightEngine levelLightEngine = this.level.getLightEngine();
		RenderRegionCache renderRegionCache = new RenderRegionCache();
		BlockPos blockPos = camera.getBlockPosition();
		List<SectionRenderDispatcher.RenderSection> list = Lists.<SectionRenderDispatcher.RenderSection>newArrayList();

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			SectionPos sectionPos = SectionPos.of(renderSection.getOrigin());
			if (renderSection.isDirty() && levelLightEngine.lightOnInSection(sectionPos)) {
				boolean bl = false;
				if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
					BlockPos blockPos2 = renderSection.getOrigin().offset(8, 8, 8);
					bl = blockPos2.distSqr(blockPos) < 768.0 || renderSection.isDirtyFromPlayer();
				} else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
					bl = renderSection.isDirtyFromPlayer();
				}

				if (bl) {
					this.minecraft.getProfiler().push("build_near_sync");
					this.sectionRenderDispatcher.rebuildSectionSync(renderSection, renderRegionCache);
					renderSection.setNotDirty();
					this.minecraft.getProfiler().pop();
				} else {
					list.add(renderSection);
				}
			}
		}

		this.minecraft.getProfiler().popPush("upload");
		this.sectionRenderDispatcher.uploadAllPendingUploads();
		this.minecraft.getProfiler().popPush("schedule_async_compile");

		for (SectionRenderDispatcher.RenderSection renderSectionx : list) {
			renderSectionx.rebuildSectionAsync(this.sectionRenderDispatcher, renderRegionCache);
			renderSectionx.setNotDirty();
		}

		this.minecraft.getProfiler().pop();
	}

	private void renderWorldBorder(Camera camera) {
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		WorldBorder worldBorder = this.level.getWorldBorder();
		double d = (double)(this.minecraft.options.getEffectiveRenderDistance() * 16);
		if (!(camera.getPosition().x < worldBorder.getMaxX() - d)
			|| !(camera.getPosition().x > worldBorder.getMinX() + d)
			|| !(camera.getPosition().z < worldBorder.getMaxZ() - d)
			|| !(camera.getPosition().z > worldBorder.getMinZ() + d)) {
			double e = 1.0 - worldBorder.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d;
			e = Math.pow(e, 4.0);
			e = Mth.clamp(e, 0.0, 1.0);
			double f = camera.getPosition().x;
			double g = camera.getPosition().z;
			double h = (double)this.minecraft.gameRenderer.getDepthFar();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			PoseStack poseStack = RenderSystem.getModelViewStack();
			poseStack.pushPose();
			RenderSystem.applyModelViewMatrix();
			int i = worldBorder.getStatus().getColor();
			float j = (float)(i >> 16 & 0xFF) / 255.0F;
			float k = (float)(i >> 8 & 0xFF) / 255.0F;
			float l = (float)(i & 0xFF) / 255.0F;
			RenderSystem.setShaderColor(j, k, l, (float)e);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.polygonOffset(-3.0F, -3.0F);
			RenderSystem.enablePolygonOffset();
			RenderSystem.disableCull();
			float m = (float)(Util.getMillis() % 3000L) / 3000.0F;
			float n = (float)(-Mth.frac(camera.getPosition().y * 0.5));
			float o = n + (float)h;
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			double p = Math.max((double)Mth.floor(g - d), worldBorder.getMinZ());
			double q = Math.min((double)Mth.ceil(g + d), worldBorder.getMaxZ());
			float r = (float)(Mth.floor(p) & 1) * 0.5F;
			if (f > worldBorder.getMaxX() - d) {
				float s = r;

				for (double t = p; t < q; s += 0.5F) {
					double u = Math.min(1.0, q - t);
					float v = (float)u * 0.5F;
					bufferBuilder.vertex(worldBorder.getMaxX() - f, -h, t - g).uv(m - s, m + o).endVertex();
					bufferBuilder.vertex(worldBorder.getMaxX() - f, -h, t + u - g).uv(m - (v + s), m + o).endVertex();
					bufferBuilder.vertex(worldBorder.getMaxX() - f, h, t + u - g).uv(m - (v + s), m + n).endVertex();
					bufferBuilder.vertex(worldBorder.getMaxX() - f, h, t - g).uv(m - s, m + n).endVertex();
					t++;
				}
			}

			if (f < worldBorder.getMinX() + d) {
				float s = r;

				for (double t = p; t < q; s += 0.5F) {
					double u = Math.min(1.0, q - t);
					float v = (float)u * 0.5F;
					bufferBuilder.vertex(worldBorder.getMinX() - f, -h, t - g).uv(m + s, m + o).endVertex();
					bufferBuilder.vertex(worldBorder.getMinX() - f, -h, t + u - g).uv(m + v + s, m + o).endVertex();
					bufferBuilder.vertex(worldBorder.getMinX() - f, h, t + u - g).uv(m + v + s, m + n).endVertex();
					bufferBuilder.vertex(worldBorder.getMinX() - f, h, t - g).uv(m + s, m + n).endVertex();
					t++;
				}
			}

			p = Math.max((double)Mth.floor(f - d), worldBorder.getMinX());
			q = Math.min((double)Mth.ceil(f + d), worldBorder.getMaxX());
			r = (float)(Mth.floor(p) & 1) * 0.5F;
			if (g > worldBorder.getMaxZ() - d) {
				float s = r;

				for (double t = p; t < q; s += 0.5F) {
					double u = Math.min(1.0, q - t);
					float v = (float)u * 0.5F;
					bufferBuilder.vertex(t - f, -h, worldBorder.getMaxZ() - g).uv(m + s, m + o).endVertex();
					bufferBuilder.vertex(t + u - f, -h, worldBorder.getMaxZ() - g).uv(m + v + s, m + o).endVertex();
					bufferBuilder.vertex(t + u - f, h, worldBorder.getMaxZ() - g).uv(m + v + s, m + n).endVertex();
					bufferBuilder.vertex(t - f, h, worldBorder.getMaxZ() - g).uv(m + s, m + n).endVertex();
					t++;
				}
			}

			if (g < worldBorder.getMinZ() + d) {
				float s = r;

				for (double t = p; t < q; s += 0.5F) {
					double u = Math.min(1.0, q - t);
					float v = (float)u * 0.5F;
					bufferBuilder.vertex(t - f, -h, worldBorder.getMinZ() - g).uv(m - s, m + o).endVertex();
					bufferBuilder.vertex(t + u - f, -h, worldBorder.getMinZ() - g).uv(m - (v + s), m + o).endVertex();
					bufferBuilder.vertex(t + u - f, h, worldBorder.getMinZ() - g).uv(m - (v + s), m + n).endVertex();
					bufferBuilder.vertex(t - f, h, worldBorder.getMinZ() - g).uv(m - s, m + n).endVertex();
					t++;
				}
			}

			BufferUploader.drawWithShader(bufferBuilder.end());
			RenderSystem.enableCull();
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			poseStack.popPose();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.depthMask(true);
		}
	}

	private void renderHitOutline(
		PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState
	) {
		renderShape(
			poseStack,
			vertexConsumer,
			blockState.getShape(this.level, blockPos, CollisionContext.of(entity)),
			(double)blockPos.getX() - d,
			(double)blockPos.getY() - e,
			(double)blockPos.getZ() - f,
			0.0F,
			0.0F,
			0.0F,
			0.4F
		);
	}

	private static Vec3 mixColor(float f) {
		float g = 5.99999F;
		int i = (int)(Mth.clamp(f, 0.0F, 1.0F) * 5.99999F);
		float h = f * 5.99999F - (float)i;

		return switch (i) {
			case 0 -> new Vec3(1.0, (double)h, 0.0);
			case 1 -> new Vec3((double)(1.0F - h), 1.0, 0.0);
			case 2 -> new Vec3(0.0, 1.0, (double)h);
			case 3 -> new Vec3(0.0, 1.0 - (double)h, 1.0);
			case 4 -> new Vec3((double)h, 0.0, 1.0);
			case 5 -> new Vec3(1.0, 0.0, 1.0 - (double)h);
			default -> throw new IllegalStateException("Unexpected value: " + i);
		};
	}

	private static Vec3 shiftHue(float f, float g, float h, float i) {
		Vec3 vec3 = mixColor(i).scale((double)f);
		Vec3 vec32 = mixColor((i + 0.33333334F) % 1.0F).scale((double)g);
		Vec3 vec33 = mixColor((i + 0.6666667F) % 1.0F).scale((double)h);
		Vec3 vec34 = vec3.add(vec32).add(vec33);
		double d = Math.max(Math.max(1.0, vec34.x), Math.max(vec34.y, vec34.z));
		return new Vec3(vec34.x / d, vec34.y / d, vec34.z / d);
	}

	public static void renderVoxelShape(
		PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j, boolean bl
	) {
		List<AABB> list = voxelShape.toAabbs();
		if (!list.isEmpty()) {
			int k = bl ? list.size() : list.size() * 8;
			renderShape(poseStack, vertexConsumer, Shapes.create((AABB)list.get(0)), d, e, f, g, h, i, j);

			for (int l = 1; l < list.size(); l++) {
				AABB aABB = (AABB)list.get(l);
				float m = (float)l / (float)k;
				Vec3 vec3 = shiftHue(g, h, i, m);
				renderShape(poseStack, vertexConsumer, Shapes.create(aABB), d, e, f, (float)vec3.x, (float)vec3.y, (float)vec3.z, j);
			}
		}
	}

	private static void renderShape(
		PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j
	) {
		PoseStack.Pose pose = poseStack.last();
		voxelShape.forAllEdges((k, l, m, n, o, p) -> {
			float q = (float)(n - k);
			float r = (float)(o - l);
			float s = (float)(p - m);
			float t = Mth.sqrt(q * q + r * r + s * s);
			q /= t;
			r /= t;
			s /= t;
			vertexConsumer.vertex(pose.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(pose.normal(), q, r, s).endVertex();
			vertexConsumer.vertex(pose.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(pose.normal(), q, r, s).endVertex();
		});
	}

	public static void renderLineBox(VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
		renderLineBox(new PoseStack(), vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
	}

	public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aABB, float f, float g, float h, float i) {
		renderLineBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i, f, g, h);
	}

	public static void renderLineBox(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		renderLineBox(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
	}

	public static void renderLineBox(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o,
		float p
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();
		float q = (float)d;
		float r = (float)e;
		float s = (float)f;
		float t = (float)g;
		float u = (float)h;
		float v = (float)i;
		vertexConsumer.vertex(matrix4f, q, r, s).color(j, o, p, m).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, s).color(j, o, p, m).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, s).color(n, k, p, m).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, s).color(n, k, p, m).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, s).color(n, o, l, m).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, v).color(n, o, l, m).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, s).color(j, k, l, m).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, s).color(j, k, l, m).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, s).color(j, k, l, m).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, v).color(j, k, l, m).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, v).color(j, k, l, m).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, s).color(j, k, l, m).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
	}

	public static void addChainedFilledBoxVertices(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		addChainedFilledBoxVertices(poseStack, vertexConsumer, (float)d, (float)e, (float)f, (float)g, (float)h, (float)i, j, k, l, m);
	}

	public static void addChainedFilledBoxVertices(
		PoseStack poseStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		vertexConsumer.vertex(matrix4f, f, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, g, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, f, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, h).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, k).color(l, m, n, o).endVertex();
		vertexConsumer.vertex(matrix4f, i, j, k).color(l, m, n, o).endVertex();
	}

	public void blockChanged(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
		this.setBlockDirty(blockPos, (i & 8) != 0);
	}

	private void setBlockDirty(BlockPos blockPos, boolean bl) {
		for (int i = blockPos.getZ() - 1; i <= blockPos.getZ() + 1; i++) {
			for (int j = blockPos.getX() - 1; j <= blockPos.getX() + 1; j++) {
				for (int k = blockPos.getY() - 1; k <= blockPos.getY() + 1; k++) {
					this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), bl);
				}
			}
		}
	}

	public void setBlocksDirty(int i, int j, int k, int l, int m, int n) {
		for (int o = k - 1; o <= n + 1; o++) {
			for (int p = i - 1; p <= l + 1; p++) {
				for (int q = j - 1; q <= m + 1; q++) {
					this.setSectionDirty(SectionPos.blockToSectionCoord(p), SectionPos.blockToSectionCoord(q), SectionPos.blockToSectionCoord(o));
				}
			}
		}
	}

	public void setBlockDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		if (this.minecraft.getModelManager().requiresRender(blockState, blockState2)) {
			this.setBlocksDirty(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
	}

	public void setSectionDirtyWithNeighbors(int i, int j, int k) {
		for (int l = k - 1; l <= k + 1; l++) {
			for (int m = i - 1; m <= i + 1; m++) {
				for (int n = j - 1; n <= j + 1; n++) {
					this.setSectionDirty(m, n, l);
				}
			}
		}
	}

	public void setSectionDirty(int i, int j, int k) {
		this.setSectionDirty(i, j, k, false);
	}

	private void setSectionDirty(int i, int j, int k, boolean bl) {
		this.viewArea.setDirty(i, j, k, bl);
	}

	public void playStreamingMusic(@Nullable SoundEvent soundEvent, BlockPos blockPos) {
		SoundInstance soundInstance = (SoundInstance)this.playingRecords.get(blockPos);
		if (soundInstance != null) {
			this.minecraft.getSoundManager().stop(soundInstance);
			this.playingRecords.remove(blockPos);
		}

		if (soundEvent != null) {
			RecordItem recordItem = RecordItem.getBySound(soundEvent);
			if (recordItem != null) {
				this.minecraft.gui.setNowPlaying(recordItem.getDisplayName());
			}

			SoundInstance var5 = SimpleSoundInstance.forRecord(soundEvent, Vec3.atCenterOf(blockPos));
			this.playingRecords.put(blockPos, var5);
			this.minecraft.getSoundManager().play(var5);
		}

		this.notifyNearbyEntities(this.level, blockPos, soundEvent != null);
	}

	private void notifyNearbyEntities(Level level, BlockPos blockPos, boolean bl) {
		for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(3.0))) {
			livingEntity.setRecordPlayingNearby(blockPos, bl);
		}
	}

	public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
		this.addParticle(particleOptions, bl, false, d, e, f, g, h, i);
	}

	public void addParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i) {
		try {
			this.addParticleInternal(particleOptions, bl, bl2, d, e, f, g, h, i);
		} catch (Throwable var19) {
			CrashReport crashReport = CrashReport.forThrowable(var19, "Exception while adding particle");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being added");
			crashReportCategory.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType()));
			crashReportCategory.setDetail("Parameters", particleOptions.writeToString());
			crashReportCategory.setDetail("Position", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.level, d, e, f)));
			throw new ReportedException(crashReport);
		}
	}

	private <T extends ParticleOptions> void addParticle(T particleOptions, double d, double e, double f, double g, double h, double i) {
		this.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter(), d, e, f, g, h, i);
	}

	@Nullable
	private Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
		return this.addParticleInternal(particleOptions, bl, false, d, e, f, g, h, i);
	}

	@Nullable
	private Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		ParticleStatus particleStatus = this.calculateParticleLevel(bl2);
		if (bl) {
			return this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
		} else if (camera.getPosition().distanceToSqr(d, e, f) > 1024.0) {
			return null;
		} else {
			return particleStatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
		}
	}

	private ParticleStatus calculateParticleLevel(boolean bl) {
		ParticleStatus particleStatus = this.minecraft.options.particles().get();
		if (bl && particleStatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
			particleStatus = ParticleStatus.DECREASED;
		}

		if (particleStatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
			particleStatus = ParticleStatus.MINIMAL;
		}

		return particleStatus;
	}

	public void clear() {
	}

	public void globalLevelEvent(int i, BlockPos blockPos, int j) {
		switch (i) {
			case 1023:
			case 1028:
			case 1038:
				Camera camera = this.minecraft.gameRenderer.getMainCamera();
				if (camera.isInitialized()) {
					double d = (double)blockPos.getX() - camera.getPosition().x;
					double e = (double)blockPos.getY() - camera.getPosition().y;
					double f = (double)blockPos.getZ() - camera.getPosition().z;
					double g = Math.sqrt(d * d + e * e + f * f);
					double h = camera.getPosition().x;
					double k = camera.getPosition().y;
					double l = camera.getPosition().z;
					if (g > 0.0) {
						h += d / g * 2.0;
						k += e / g * 2.0;
						l += f / g * 2.0;
					}

					if (i == 1023) {
						this.level.playLocalSound(h, k, l, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
					} else if (i == 1038) {
						this.level.playLocalSound(h, k, l, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
					} else {
						this.level.playLocalSound(h, k, l, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
					}
				}
		}
	}

	public void levelEvent(int i, BlockPos blockPos, int j) {
		RandomSource randomSource = this.level.random;
		switch (i) {
			case 1000:
				this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1001:
				this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
				break;
			case 1002:
				this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
				break;
			case 1003:
				this.level.playLocalSound(blockPos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
				break;
			case 1004:
				this.level.playLocalSound(blockPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
				break;
			case 1009:
				if (j == 0) {
					this.level
						.playLocalSound(
							blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false
						);
				} else if (j == 1) {
					this.level
						.playLocalSound(
							blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.4F, false
						);
				}
				break;
			case 1010:
				if (Item.byId(j) instanceof RecordItem recordItem) {
					this.playStreamingMusic(recordItem.getSound(), blockPos);
				}
				break;
			case 1011:
				this.playStreamingMusic(null, blockPos);
				break;
			case 1015:
				this.level
					.playLocalSound(blockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1016:
				this.level
					.playLocalSound(blockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1017:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1018:
				this.level
					.playLocalSound(blockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1019:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1020:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1021:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1022:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1024:
				this.level
					.playLocalSound(blockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1025:
				this.level
					.playLocalSound(blockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1026:
				this.level
					.playLocalSound(blockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1027:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1029:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1030:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1031:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1032:
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, randomSource.nextFloat() * 0.4F + 0.8F, 0.25F));
				break;
			case 1033:
				this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1034:
				this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1035:
				this.level.playLocalSound(blockPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1039:
				this.level.playLocalSound(blockPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1040:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1041:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1042:
				this.level.playLocalSound(blockPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1043:
				this.level.playLocalSound(blockPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1044:
				this.level.playLocalSound(blockPos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1045:
				this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1046:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false
					);
				break;
			case 1047:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false
					);
				break;
			case 1048:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1049:
				this.level.playLocalSound(blockPos, SoundEvents.CRAFTER_CRAFT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1050:
				this.level.playLocalSound(blockPos, SoundEvents.CRAFTER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1500:
				ComposterBlock.handleFill(this.level, blockPos, j > 0);
				break;
			case 1501:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false
					);

				for (int ox = 0; ox < 8; ox++) {
					this.level
						.addParticle(
							ParticleTypes.LARGE_SMOKE,
							(double)blockPos.getX() + randomSource.nextDouble(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + randomSource.nextDouble(),
							0.0,
							0.0,
							0.0
						);
				}
				break;
			case 1502:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false
					);

				for (int ox = 0; ox < 5; ox++) {
					double g = (double)blockPos.getX() + randomSource.nextDouble() * 0.6 + 0.2;
					double p = (double)blockPos.getY() + randomSource.nextDouble() * 0.6 + 0.2;
					double q = (double)blockPos.getZ() + randomSource.nextDouble() * 0.6 + 0.2;
					this.level.addParticle(ParticleTypes.SMOKE, g, p, q, 0.0, 0.0, 0.0);
				}
				break;
			case 1503:
				this.level.playLocalSound(blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

				for (int ox = 0; ox < 16; ox++) {
					double g = (double)blockPos.getX() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
					double p = (double)blockPos.getY() + 0.8125;
					double q = (double)blockPos.getZ() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
					this.level.addParticle(ParticleTypes.SMOKE, g, p, q, 0.0, 0.0, 0.0);
				}
				break;
			case 1504:
				PointedDripstoneBlock.spawnDripParticle(this.level, blockPos, this.level.getBlockState(blockPos));
				break;
			case 1505:
				BoneMealItem.addGrowthParticles(this.level, blockPos, j);
				this.level.playLocalSound(blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 2000:
				this.shootParticles(j, blockPos, randomSource, ParticleTypes.SMOKE);
				break;
			case 2001:
				BlockState blockState = Block.stateById(j);
				if (!blockState.isAir()) {
					SoundType soundType = blockState.getSoundType();
					this.level
						.playLocalSound(blockPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
				}

				this.level.addDestroyBlockEffect(blockPos, blockState);
				break;
			case 2002:
			case 2007:
				Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);

				for (int l = 0; l < 8; l++) {
					this.addParticle(
						new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)),
						vec3.x,
						vec3.y,
						vec3.z,
						randomSource.nextGaussian() * 0.15,
						randomSource.nextDouble() * 0.2,
						randomSource.nextGaussian() * 0.15
					);
				}

				float h = (float)(j >> 16 & 0xFF) / 255.0F;
				float m = (float)(j >> 8 & 0xFF) / 255.0F;
				float n = (float)(j >> 0 & 0xFF) / 255.0F;
				ParticleOptions particleOptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

				for (int ox = 0; ox < 100; ox++) {
					double g = randomSource.nextDouble() * 4.0;
					double p = randomSource.nextDouble() * Math.PI * 2.0;
					double q = Math.cos(p) * g;
					double r = 0.01 + randomSource.nextDouble() * 0.5;
					double s = Math.sin(p) * g;
					Particle particle = this.addParticleInternal(
						particleOptions, particleOptions.getType().getOverrideLimiter(), vec3.x + q * 0.1, vec3.y + 0.3, vec3.z + s * 0.1, q, r, s
					);
					if (particle != null) {
						float t = 0.75F + randomSource.nextFloat() * 0.25F;
						particle.setColor(h * t, m * t, n * t);
						particle.setPower((float)g);
					}
				}

				this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 2003:
				double d = (double)blockPos.getX() + 0.5;
				double e = (double)blockPos.getY();
				double f = (double)blockPos.getZ() + 0.5;

				for (int k = 0; k < 8; k++) {
					this.addParticle(
						new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)),
						d,
						e,
						f,
						randomSource.nextGaussian() * 0.15,
						randomSource.nextDouble() * 0.2,
						randomSource.nextGaussian() * 0.15
					);
				}

				for (double g = 0.0; g < Math.PI * 2; g += Math.PI / 20) {
					this.addParticle(ParticleTypes.PORTAL, d + Math.cos(g) * 5.0, e - 0.4, f + Math.sin(g) * 5.0, Math.cos(g) * -5.0, 0.0, Math.sin(g) * -5.0);
					this.addParticle(ParticleTypes.PORTAL, d + Math.cos(g) * 5.0, e - 0.4, f + Math.sin(g) * 5.0, Math.cos(g) * -7.0, 0.0, Math.sin(g) * -7.0);
				}
				break;
			case 2004:
				for (int ux = 0; ux < 20; ux++) {
					double v = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
					double w = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
					double x = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
					this.level.addParticle(ParticleTypes.SMOKE, v, w, x, 0.0, 0.0, 0.0);
					this.level.addParticle(ParticleTypes.FLAME, v, w, x, 0.0, 0.0, 0.0);
				}
				break;
			case 2006:
				for (int o = 0; o < 200; o++) {
					float ad = randomSource.nextFloat() * 4.0F;
					float ai = randomSource.nextFloat() * (float) (Math.PI * 2);
					double p = (double)(Mth.cos(ai) * ad);
					double q = 0.01 + randomSource.nextDouble() * 0.5;
					double r = (double)(Mth.sin(ai) * ad);
					Particle particle2 = this.addParticleInternal(
						ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + p * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + r * 0.1, p, q, r
					);
					if (particle2 != null) {
						particle2.setPower(ad);
					}
				}

				if (j == 1) {
					this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				}
				break;
			case 2008:
				this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
				break;
			case 2009:
				for (int ox = 0; ox < 8; ox++) {
					this.level
						.addParticle(
							ParticleTypes.CLOUD,
							(double)blockPos.getX() + randomSource.nextDouble(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + randomSource.nextDouble(),
							0.0,
							0.0,
							0.0
						);
				}
				break;
			case 2010:
				this.shootParticles(j, blockPos, randomSource, ParticleTypes.WHITE_SMOKE);
				break;
			case 2011:
				ParticleUtils.spawnParticleInBlock(this.level, blockPos, j, ParticleTypes.HAPPY_VILLAGER);
				break;
			case 2012:
				ParticleUtils.spawnParticleInBlock(this.level, blockPos, j, ParticleTypes.HAPPY_VILLAGER);
				break;
			case 3000:
				this.level
					.addParticle(
						ParticleTypes.EXPLOSION_EMITTER, true, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0
					);
				this.level
					.playLocalSound(
						blockPos,
						SoundEvents.END_GATEWAY_SPAWN,
						SoundSource.BLOCKS,
						10.0F,
						(1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
						false
					);
				break;
			case 3001:
				this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
				break;
			case 3002:
				if (j >= 0 && j < Direction.Axis.VALUES.length) {
					ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[j], this.level, blockPos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
				} else {
					ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
				}
				break;
			case 3003:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
				this.level.playLocalSound(blockPos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 3004:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
				break;
			case 3005:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
				break;
			case 3006:
				int u = j >> 6;
				if (u > 0) {
					if (randomSource.nextFloat() < 0.3F + (float)u * 0.1F) {
						float n = 0.15F + 0.02F * (float)u * (float)u * randomSource.nextFloat();
						float y = 0.4F + 0.3F * (float)u * randomSource.nextFloat();
						this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, n, y, false);
					}

					byte b = (byte)(j & 63);
					IntProvider intProvider = UniformInt.of(0, u);
					float z = 0.005F;
					Supplier<Vec3> supplier = () -> new Vec3(
							Mth.nextDouble(randomSource, -0.005F, 0.005F), Mth.nextDouble(randomSource, -0.005F, 0.005F), Mth.nextDouble(randomSource, -0.005F, 0.005F)
						);
					if (b == 0) {
						for (Direction direction : Direction.values()) {
							float aa = direction == Direction.DOWN ? (float) Math.PI : 0.0F;
							double r = direction.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
							ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(aa), intProvider, direction, supplier, r);
						}
					} else {
						for (Direction direction2 : MultifaceBlock.unpack(b)) {
							float ab = direction2 == Direction.UP ? (float) Math.PI : 0.0F;
							double q = 0.35;
							ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(ab), intProvider, direction2, supplier, 0.35);
						}
					}
				} else {
					this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
					boolean bl = this.level.getBlockState(blockPos).isCollisionShapeFullBlock(this.level, blockPos);
					int ac = bl ? 40 : 20;
					float z = bl ? 0.45F : 0.25F;
					float ad = 0.07F;

					for (int ae = 0; ae < ac; ae++) {
						float af = 2.0F * randomSource.nextFloat() - 1.0F;
						float ab = 2.0F * randomSource.nextFloat() - 1.0F;
						float ag = 2.0F * randomSource.nextFloat() - 1.0F;
						this.level
							.addParticle(
								ParticleTypes.SCULK_CHARGE_POP,
								(double)blockPos.getX() + 0.5 + (double)(af * z),
								(double)blockPos.getY() + 0.5 + (double)(ab * z),
								(double)blockPos.getZ() + 0.5 + (double)(ag * z),
								(double)(af * 0.07F),
								(double)(ab * 0.07F),
								(double)(ag * 0.07F)
							);
					}
				}
				break;
			case 3007:
				for (int ah = 0; ah < 10; ah++) {
					this.level
						.addParticle(
							new ShriekParticleOption(ah * 5),
							false,
							(double)blockPos.getX() + 0.5,
							(double)blockPos.getY() + SculkShriekerBlock.TOP_Y,
							(double)blockPos.getZ() + 0.5,
							0.0,
							0.0,
							0.0
						);
				}

				BlockState blockState3 = this.level.getBlockState(blockPos);
				boolean bl2 = blockState3.hasProperty(BlockStateProperties.WATERLOGGED) && (Boolean)blockState3.getValue(BlockStateProperties.WATERLOGGED);
				if (!bl2) {
					this.level
						.playLocalSound(
							(double)blockPos.getX() + 0.5,
							(double)blockPos.getY() + SculkShriekerBlock.TOP_Y,
							(double)blockPos.getZ() + 0.5,
							SoundEvents.SCULK_SHRIEKER_SHRIEK,
							SoundSource.BLOCKS,
							2.0F,
							0.6F + this.level.random.nextFloat() * 0.4F,
							false
						);
				}
				break;
			case 3008:
				BlockState blockState2 = Block.stateById(j);
				if (blockState2.getBlock() instanceof BrushableBlock brushableBlock) {
					this.level.playLocalSound(blockPos, brushableBlock.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
				}

				this.level.addDestroyBlockEffect(blockPos, blockState2);
				break;
			case 3009:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.EGG_CRACK, UniformInt.of(3, 6));
				break;
			case 3010:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.GUST_DUST, UniformInt.of(3, 6));
				break;
			case 3011:
				TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource);
				break;
			case 3012:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource);
				break;
			case 3013:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, j);
				break;
			case 3014:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addEjectItemParticles(this.level, blockPos, randomSource);
		}
	}

	public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
		if (j >= 0 && j < 10) {
			BlockDestructionProgress blockDestructionProgress = this.destroyingBlocks.get(i);
			if (blockDestructionProgress != null) {
				this.removeProgress(blockDestructionProgress);
			}

			if (blockDestructionProgress == null
				|| blockDestructionProgress.getPos().getX() != blockPos.getX()
				|| blockDestructionProgress.getPos().getY() != blockPos.getY()
				|| blockDestructionProgress.getPos().getZ() != blockPos.getZ()) {
				blockDestructionProgress = new BlockDestructionProgress(i, blockPos);
				this.destroyingBlocks.put(i, blockDestructionProgress);
			}

			blockDestructionProgress.setProgress(j);
			blockDestructionProgress.updateTick(this.ticks);
			this.destructionProgress
				.computeIfAbsent(
					blockDestructionProgress.getPos().asLong(),
					(Long2ObjectFunction<? extends SortedSet<BlockDestructionProgress>>)(l -> Sets.<BlockDestructionProgress>newTreeSet())
				)
				.add(blockDestructionProgress);
		} else {
			BlockDestructionProgress blockDestructionProgressx = this.destroyingBlocks.remove(i);
			if (blockDestructionProgressx != null) {
				this.removeProgress(blockDestructionProgressx);
			}
		}
	}

	public boolean hasRenderedAllSections() {
		return this.sectionRenderDispatcher.isQueueEmpty();
	}

	public void onChunkLoaded(ChunkPos chunkPos) {
		this.sectionOcclusionGraph.onChunkLoaded(chunkPos);
	}

	public void needsUpdate() {
		this.sectionOcclusionGraph.invalidate();
		this.generateClouds = true;
	}

	public void updateGlobalBlockEntities(Collection<BlockEntity> collection, Collection<BlockEntity> collection2) {
		synchronized (this.globalBlockEntities) {
			this.globalBlockEntities.removeAll(collection);
			this.globalBlockEntities.addAll(collection2);
		}
	}

	public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		return getLightColor(blockAndTintGetter, blockAndTintGetter.getBlockState(blockPos), blockPos);
	}

	public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos) {
		if (blockState.emissiveRendering(blockAndTintGetter, blockPos)) {
			return 15728880;
		} else {
			int i = blockAndTintGetter.getBrightness(LightLayer.SKY, blockPos);
			int j = blockAndTintGetter.getBrightness(LightLayer.BLOCK, blockPos);
			int k = blockState.getLightEmission();
			if (j < k) {
				j = k;
			}

			return i << 20 | j << 4;
		}
	}

	public boolean isSectionCompiled(BlockPos blockPos) {
		SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSectionAt(blockPos);
		return renderSection != null && renderSection.compiled.get() != SectionRenderDispatcher.CompiledSection.UNCOMPILED;
	}

	@Nullable
	public RenderTarget entityTarget() {
		return this.entityTarget;
	}

	@Nullable
	public RenderTarget getTranslucentTarget() {
		return this.translucentTarget;
	}

	@Nullable
	public RenderTarget getItemEntityTarget() {
		return this.itemEntityTarget;
	}

	@Nullable
	public RenderTarget getParticlesTarget() {
		return this.particlesTarget;
	}

	@Nullable
	public RenderTarget getWeatherTarget() {
		return this.weatherTarget;
	}

	@Nullable
	public RenderTarget getCloudsTarget() {
		return this.cloudsTarget;
	}

	private void shootParticles(int i, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
		Direction direction = Direction.from3DDataValue(i);
		int j = direction.getStepX();
		int k = direction.getStepY();
		int l = direction.getStepZ();
		double d = (double)blockPos.getX() + (double)j * 0.6 + 0.5;
		double e = (double)blockPos.getY() + (double)k * 0.6 + 0.5;
		double f = (double)blockPos.getZ() + (double)l * 0.6 + 0.5;

		for (int m = 0; m < 10; m++) {
			double g = randomSource.nextDouble() * 0.2 + 0.01;
			double h = d + (double)j * 0.01 + (randomSource.nextDouble() - 0.5) * (double)l * 0.5;
			double n = e + (double)k * 0.01 + (randomSource.nextDouble() - 0.5) * (double)k * 0.5;
			double o = f + (double)l * 0.01 + (randomSource.nextDouble() - 0.5) * (double)j * 0.5;
			double p = (double)j * g + randomSource.nextGaussian() * 0.01;
			double q = (double)k * g + randomSource.nextGaussian() * 0.01;
			double r = (double)l * g + randomSource.nextGaussian() * 0.01;
			this.addParticle(simpleParticleType, h, n, o, p, q, r);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TransparencyShaderException extends RuntimeException {
		public TransparencyShaderException(String string, Throwable throwable) {
			super(string, throwable);
		}
	}
}
