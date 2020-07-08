package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
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
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
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
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
	private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
	public static final Direction[] DIRECTIONS = Direction.values();
	private final Minecraft minecraft;
	private final TextureManager textureManager;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final RenderBuffers renderBuffers;
	private ClientLevel level;
	private Set<ChunkRenderDispatcher.RenderChunk> chunksToCompile = Sets.<ChunkRenderDispatcher.RenderChunk>newLinkedHashSet();
	private final ObjectList<LevelRenderer.RenderChunkInfo> renderChunks = new ObjectArrayList<>(69696);
	private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
	private ViewArea viewArea;
	private final VertexFormat skyFormat = DefaultVertexFormat.POSITION;
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
	private double lastCameraX = Double.MIN_VALUE;
	private double lastCameraY = Double.MIN_VALUE;
	private double lastCameraZ = Double.MIN_VALUE;
	private int lastCameraChunkX = Integer.MIN_VALUE;
	private int lastCameraChunkY = Integer.MIN_VALUE;
	private int lastCameraChunkZ = Integer.MIN_VALUE;
	private double prevCamX = Double.MIN_VALUE;
	private double prevCamY = Double.MIN_VALUE;
	private double prevCamZ = Double.MIN_VALUE;
	private double prevCamRotX = Double.MIN_VALUE;
	private double prevCamRotY = Double.MIN_VALUE;
	private int prevCloudX = Integer.MIN_VALUE;
	private int prevCloudY = Integer.MIN_VALUE;
	private int prevCloudZ = Integer.MIN_VALUE;
	private Vec3 prevCloudColor = Vec3.ZERO;
	private CloudStatus prevCloudsType;
	private ChunkRenderDispatcher chunkRenderDispatcher;
	private final VertexFormat format = DefaultVertexFormat.BLOCK;
	private int lastViewDistance = -1;
	private int renderedEntities;
	private int culledEntities;
	private boolean captureFrustum;
	@Nullable
	private Frustum capturedFrustum;
	private final Vector4f[] frustumPoints = new Vector4f[8];
	private final Vector3d frustumPos = new Vector3d(0.0, 0.0, 0.0);
	private double xTransparentOld;
	private double yTransparentOld;
	private double zTransparentOld;
	private boolean needsUpdate = true;
	private int frameId;
	private int rainSoundTime;
	private final float[] rainSizeX = new float[1024];
	private final float[] rainSizeZ = new float[1024];

	public LevelRenderer(Minecraft minecraft, RenderBuffers renderBuffers) {
		this.minecraft = minecraft;
		this.entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
		this.renderBuffers = renderBuffers;
		this.textureManager = minecraft.getTextureManager();

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
			RenderSystem.enableAlphaTest();
			RenderSystem.disableCull();
			RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableDepthTest();
			int l = 5;
			if (Minecraft.useFancyGraphics()) {
				l = 10;
			}

			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			int m = -1;
			float n = (float)this.ticks + f;
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int o = k - l; o <= k + l; o++) {
				for (int p = i - l; p <= i + l; p++) {
					int q = (o - k + 16) * 32 + p - i + 16;
					double r = (double)this.rainSizeX[q] * 0.5;
					double s = (double)this.rainSizeZ[q] * 0.5;
					mutableBlockPos.set(p, 0, o);
					Biome biome = level.getBiome(mutableBlockPos);
					if (biome.getPrecipitation() != Biome.Precipitation.NONE) {
						int t = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos).getY();
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
							Random random = new Random((long)(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761));
							mutableBlockPos.set(p, u, o);
							float x = biome.getTemperature(mutableBlockPos);
							if (x >= 0.15F) {
								if (m != 0) {
									if (m >= 0) {
										tesselator.end();
									}

									m = 0;
									this.minecraft.getTextureManager().bind(RAIN_LOCATION);
									bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
								}

								int y = this.ticks + p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 31;
								float z = -((float)y + f) / 32.0F * (3.0F + random.nextFloat());
								double aa = (double)((float)p + 0.5F) - d;
								double ab = (double)((float)o + 0.5F) - g;
								float ac = Mth.sqrt(aa * aa + ab * ab) / (float)l;
								float ad = ((1.0F - ac * ac) * 0.5F + 0.5F) * h;
								mutableBlockPos.set(p, w, o);
								int ae = getLightColor(level, mutableBlockPos);
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5)
									.uv(0.0F, (float)u * 0.25F + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(ae)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5)
									.uv(1.0F, (float)u * 0.25F + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(ae)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5)
									.uv(1.0F, (float)v * 0.25F + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(ae)
									.endVertex();
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5)
									.uv(0.0F, (float)v * 0.25F + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(ae)
									.endVertex();
							} else {
								if (m != 1) {
									if (m >= 0) {
										tesselator.end();
									}

									m = 1;
									this.minecraft.getTextureManager().bind(SNOW_LOCATION);
									bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
								}

								float af = -((float)(this.ticks & 511) + f) / 512.0F;
								float z = (float)(random.nextDouble() + (double)n * 0.01 * (double)((float)random.nextGaussian()));
								float ag = (float)(random.nextDouble() + (double)(n * (float)random.nextGaussian()) * 0.001);
								double ah = (double)((float)p + 0.5F) - d;
								double ai = (double)((float)o + 0.5F) - g;
								float ad = Mth.sqrt(ah * ah + ai * ai) / (float)l;
								float aj = ((1.0F - ad * ad) * 0.3F + 0.5F) * h;
								mutableBlockPos.set(p, w, o);
								int ak = getLightColor(level, mutableBlockPos);
								int al = ak >> 16 & 65535;
								int am = (ak & 65535) * 3;
								int an = (al * 3 + 240) / 4;
								int ao = (am * 3 + 240) / 4;
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5)
									.uv(0.0F + z, (float)u * 0.25F + af + ag)
									.color(1.0F, 1.0F, 1.0F, aj)
									.uv2(ao, an)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5)
									.uv(1.0F + z, (float)u * 0.25F + af + ag)
									.color(1.0F, 1.0F, 1.0F, aj)
									.uv2(ao, an)
									.endVertex();
								bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5)
									.uv(1.0F + z, (float)v * 0.25F + af + ag)
									.color(1.0F, 1.0F, 1.0F, aj)
									.uv2(ao, an)
									.endVertex();
								bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5)
									.uv(0.0F + z, (float)v * 0.25F + af + ag)
									.color(1.0F, 1.0F, 1.0F, aj)
									.uv2(ao, an)
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
			RenderSystem.defaultAlphaFunc();
			RenderSystem.disableAlphaTest();
			lightTexture.turnOffLightLayer();
		}
	}

	public void tickRain(Camera camera) {
		float f = this.minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
		if (!(f <= 0.0F)) {
			Random random = new Random((long)this.ticks * 312987231L);
			LevelReader levelReader = this.minecraft.level;
			BlockPos blockPos = new BlockPos(camera.getPosition());
			BlockPos blockPos2 = null;
			int i = (int)(100.0F * f * f) / (this.minecraft.options.particles == ParticleStatus.DECREASED ? 2 : 1);

			for (int j = 0; j < i; j++) {
				int k = random.nextInt(21) - 10;
				int l = random.nextInt(21) - 10;
				BlockPos blockPos3 = levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(k, 0, l)).below();
				Biome biome = levelReader.getBiome(blockPos3);
				if (blockPos3.getY() > 0
					&& blockPos3.getY() <= blockPos.getY() + 10
					&& blockPos3.getY() >= blockPos.getY() - 10
					&& biome.getPrecipitation() == Biome.Precipitation.RAIN
					&& biome.getTemperature(blockPos3) >= 0.15F) {
					blockPos2 = blockPos3;
					if (this.minecraft.options.particles == ParticleStatus.MINIMAL) {
						break;
					}

					double d = random.nextDouble();
					double e = random.nextDouble();
					BlockState blockState = levelReader.getBlockState(blockPos3);
					FluidState fluidState = levelReader.getFluidState(blockPos3);
					VoxelShape voxelShape = blockState.getCollisionShape(levelReader, blockPos3);
					double g = voxelShape.max(Direction.Axis.Y, d, e);
					double h = (double)fluidState.getHeight(levelReader, blockPos3);
					double m = Math.max(g, h);
					ParticleOptions particleOptions = !fluidState.is(FluidTags.LAVA) && !blockState.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockState)
						? ParticleTypes.RAIN
						: ParticleTypes.SMOKE;
					this.minecraft.level.addParticle(particleOptions, (double)blockPos3.getX() + d, (double)blockPos3.getY() + m, (double)blockPos3.getZ() + e, 0.0, 0.0, 0.0);
				}
			}

			if (blockPos2 != null && random.nextInt(3) < this.rainSoundTime++) {
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
		this.textureManager.bind(FORCEFIELD_LOCATION);
		RenderSystem.texParameter(3553, 10242, 10497);
		RenderSystem.texParameter(3553, 10243, 10497);
		RenderSystem.bindTexture(0);
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
			Options options = Minecraft.getInstance().options;
			options.graphicsMode = GraphicsStatus.FANCY;
			options.save();
			throw new LevelRenderer.TranparencyShaderException("Failed to " + string + " shader: " + resourceLocation, var8);
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
		}
	}

	protected boolean shouldShowEntityOutlines() {
		return this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
	}

	private void createDarkSky() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.darkBuffer != null) {
			this.darkBuffer.close();
		}

		this.darkBuffer = new VertexBuffer(this.skyFormat);
		this.drawSkyHemisphere(bufferBuilder, -16.0F, true);
		bufferBuilder.end();
		this.darkBuffer.upload(bufferBuilder);
	}

	private void createLightSky() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.skyBuffer != null) {
			this.skyBuffer.close();
		}

		this.skyBuffer = new VertexBuffer(this.skyFormat);
		this.drawSkyHemisphere(bufferBuilder, 16.0F, false);
		bufferBuilder.end();
		this.skyBuffer.upload(bufferBuilder);
	}

	private void drawSkyHemisphere(BufferBuilder bufferBuilder, float f, boolean bl) {
		int i = 64;
		int j = 6;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);

		for (int k = -384; k <= 384; k += 64) {
			for (int l = -384; l <= 384; l += 64) {
				float g = (float)k;
				float h = (float)(k + 64);
				if (bl) {
					h = (float)k;
					g = (float)(k + 64);
				}

				bufferBuilder.vertex((double)g, (double)f, (double)l).endVertex();
				bufferBuilder.vertex((double)h, (double)f, (double)l).endVertex();
				bufferBuilder.vertex((double)h, (double)f, (double)(l + 64)).endVertex();
				bufferBuilder.vertex((double)g, (double)f, (double)(l + 64)).endVertex();
			}
		}
	}

	private void createStars() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.starBuffer != null) {
			this.starBuffer.close();
		}

		this.starBuffer = new VertexBuffer(this.skyFormat);
		this.drawStars(bufferBuilder);
		bufferBuilder.end();
		this.starBuffer.upload(bufferBuilder);
	}

	private void drawStars(BufferBuilder bufferBuilder) {
		Random random = new Random(10842L);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);

		for (int i = 0; i < 1500; i++) {
			double d = (double)(random.nextFloat() * 2.0F - 1.0F);
			double e = (double)(random.nextFloat() * 2.0F - 1.0F);
			double f = (double)(random.nextFloat() * 2.0F - 1.0F);
			double g = (double)(0.15F + random.nextFloat() * 0.1F);
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
				double s = random.nextDouble() * Math.PI * 2.0;
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
	}

	public void setLevel(@Nullable ClientLevel clientLevel) {
		this.lastCameraX = Double.MIN_VALUE;
		this.lastCameraY = Double.MIN_VALUE;
		this.lastCameraZ = Double.MIN_VALUE;
		this.lastCameraChunkX = Integer.MIN_VALUE;
		this.lastCameraChunkY = Integer.MIN_VALUE;
		this.lastCameraChunkZ = Integer.MIN_VALUE;
		this.entityRenderDispatcher.setLevel(clientLevel);
		this.level = clientLevel;
		if (clientLevel != null) {
			this.allChanged();
		} else {
			this.chunksToCompile.clear();
			this.renderChunks.clear();
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
				this.viewArea = null;
			}

			if (this.chunkRenderDispatcher != null) {
				this.chunkRenderDispatcher.dispose();
			}

			this.chunkRenderDispatcher = null;
			this.globalBlockEntities.clear();
		}
	}

	public void allChanged() {
		if (this.level != null) {
			if (Minecraft.useShaderTransparency()) {
				this.initTransparency();
			} else {
				this.deinitTransparency();
			}

			this.level.clearTintCaches();
			if (this.chunkRenderDispatcher == null) {
				this.chunkRenderDispatcher = new ChunkRenderDispatcher(
					this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack()
				);
			} else {
				this.chunkRenderDispatcher.setLevel(this.level);
			}

			this.needsUpdate = true;
			this.generateClouds = true;
			ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
			this.lastViewDistance = this.minecraft.options.renderDistance;
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
			}

			this.resetChunksToCompile();
			synchronized (this.globalBlockEntities) {
				this.globalBlockEntities.clear();
			}

			this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.renderDistance, this);
			if (this.level != null) {
				Entity entity = this.minecraft.getCameraEntity();
				if (entity != null) {
					this.viewArea.repositionCamera(entity.getX(), entity.getZ());
				}
			}
		}
	}

	protected void resetChunksToCompile() {
		this.chunksToCompile.clear();
		this.chunkRenderDispatcher.blockUntilClear();
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

	public String getChunkStatistics() {
		int i = this.viewArea.chunks.length;
		int j = this.countRenderedChunks();
		return String.format(
			"C: %d/%d %sD: %d, %s",
			j,
			i,
			this.minecraft.smartCull ? "(s) " : "",
			this.lastViewDistance,
			this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats()
		);
	}

	protected int countRenderedChunks() {
		int i = 0;

		for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
			if (!renderChunkInfo.chunk.getCompiledChunk().hasNoRenderableLayers()) {
				i++;
			}
		}

		return i;
	}

	public String getEntityStatistics() {
		return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities;
	}

	private void setupRender(Camera camera, Frustum frustum, boolean bl, int i, boolean bl2) {
		Vec3 vec3 = camera.getPosition();
		if (this.minecraft.options.renderDistance != this.lastViewDistance) {
			this.allChanged();
		}

		this.level.getProfiler().push("camera");
		double d = this.minecraft.player.getX() - this.lastCameraX;
		double e = this.minecraft.player.getY() - this.lastCameraY;
		double f = this.minecraft.player.getZ() - this.lastCameraZ;
		if (this.lastCameraChunkX != this.minecraft.player.xChunk
			|| this.lastCameraChunkY != this.minecraft.player.yChunk
			|| this.lastCameraChunkZ != this.minecraft.player.zChunk
			|| d * d + e * e + f * f > 16.0) {
			this.lastCameraX = this.minecraft.player.getX();
			this.lastCameraY = this.minecraft.player.getY();
			this.lastCameraZ = this.minecraft.player.getZ();
			this.lastCameraChunkX = this.minecraft.player.xChunk;
			this.lastCameraChunkY = this.minecraft.player.yChunk;
			this.lastCameraChunkZ = this.minecraft.player.zChunk;
			this.viewArea.repositionCamera(this.minecraft.player.getX(), this.minecraft.player.getZ());
		}

		this.chunkRenderDispatcher.setCamera(vec3);
		this.level.getProfiler().popPush("cull");
		this.minecraft.getProfiler().popPush("culling");
		BlockPos blockPos = camera.getBlockPosition();
		ChunkRenderDispatcher.RenderChunk renderChunk = this.viewArea.getRenderChunkAt(blockPos);
		int j = 16;
		BlockPos blockPos2 = new BlockPos(Mth.floor(vec3.x / 16.0) * 16, Mth.floor(vec3.y / 16.0) * 16, Mth.floor(vec3.z / 16.0) * 16);
		float g = camera.getXRot();
		float h = camera.getYRot();
		this.needsUpdate = this.needsUpdate
			|| !this.chunksToCompile.isEmpty()
			|| vec3.x != this.prevCamX
			|| vec3.y != this.prevCamY
			|| vec3.z != this.prevCamZ
			|| (double)g != this.prevCamRotX
			|| (double)h != this.prevCamRotY;
		this.prevCamX = vec3.x;
		this.prevCamY = vec3.y;
		this.prevCamZ = vec3.z;
		this.prevCamRotX = (double)g;
		this.prevCamRotY = (double)h;
		this.minecraft.getProfiler().popPush("update");
		if (!bl && this.needsUpdate) {
			this.needsUpdate = false;
			this.renderChunks.clear();
			Queue<LevelRenderer.RenderChunkInfo> queue = Queues.<LevelRenderer.RenderChunkInfo>newArrayDeque();
			Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0, 1.0, 2.5) * (double)this.minecraft.options.entityDistanceScaling);
			boolean bl3 = this.minecraft.smartCull;
			if (renderChunk != null) {
				if (bl2 && this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos)) {
					bl3 = false;
				}

				renderChunk.setFrame(i);
				queue.add(new LevelRenderer.RenderChunkInfo(renderChunk, null, 0));
			} else {
				int k = blockPos.getY() > 0 ? 248 : 8;
				int l = Mth.floor(vec3.x / 16.0) * 16;
				int m = Mth.floor(vec3.z / 16.0) * 16;
				List<LevelRenderer.RenderChunkInfo> list = Lists.<LevelRenderer.RenderChunkInfo>newArrayList();

				for (int n = -this.lastViewDistance; n <= this.lastViewDistance; n++) {
					for (int o = -this.lastViewDistance; o <= this.lastViewDistance; o++) {
						ChunkRenderDispatcher.RenderChunk renderChunk2 = this.viewArea.getRenderChunkAt(new BlockPos(l + (n << 4) + 8, k, m + (o << 4) + 8));
						if (renderChunk2 != null && frustum.isVisible(renderChunk2.bb)) {
							renderChunk2.setFrame(i);
							list.add(new LevelRenderer.RenderChunkInfo(renderChunk2, null, 0));
						}
					}
				}

				list.sort(Comparator.comparingDouble(renderChunkInfox -> blockPos.distSqr(renderChunkInfox.chunk.getOrigin().offset(8, 8, 8))));
				queue.addAll(list);
			}

			this.minecraft.getProfiler().push("iteration");

			while (!queue.isEmpty()) {
				LevelRenderer.RenderChunkInfo renderChunkInfo = (LevelRenderer.RenderChunkInfo)queue.poll();
				ChunkRenderDispatcher.RenderChunk renderChunk3 = renderChunkInfo.chunk;
				Direction direction = renderChunkInfo.sourceDirection;
				this.renderChunks.add(renderChunkInfo);

				for (Direction direction2 : DIRECTIONS) {
					ChunkRenderDispatcher.RenderChunk renderChunk4 = this.getRelativeFrom(blockPos2, renderChunk3, direction2);
					if ((!bl3 || !renderChunkInfo.hasDirection(direction2.getOpposite()))
						&& (!bl3 || direction == null || renderChunk3.getCompiledChunk().facesCanSeeEachother(direction.getOpposite(), direction2))
						&& renderChunk4 != null
						&& renderChunk4.hasAllNeighbors()
						&& renderChunk4.setFrame(i)
						&& frustum.isVisible(renderChunk4.bb)) {
						LevelRenderer.RenderChunkInfo renderChunkInfo2 = new LevelRenderer.RenderChunkInfo(renderChunk4, direction2, renderChunkInfo.step + 1);
						renderChunkInfo2.setDirections(renderChunkInfo.directions, direction2);
						queue.add(renderChunkInfo2);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}

		this.minecraft.getProfiler().popPush("rebuildNear");
		Set<ChunkRenderDispatcher.RenderChunk> set = this.chunksToCompile;
		this.chunksToCompile = Sets.<ChunkRenderDispatcher.RenderChunk>newLinkedHashSet();

		for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
			ChunkRenderDispatcher.RenderChunk renderChunk3 = renderChunkInfo.chunk;
			if (renderChunk3.isDirty() || set.contains(renderChunk3)) {
				this.needsUpdate = true;
				BlockPos blockPos3 = renderChunk3.getOrigin().offset(8, 8, 8);
				boolean bl4 = blockPos3.distSqr(blockPos) < 768.0;
				if (!renderChunk3.isDirtyFromPlayer() && !bl4) {
					this.chunksToCompile.add(renderChunk3);
				} else {
					this.minecraft.getProfiler().push("build near");
					this.chunkRenderDispatcher.rebuildChunkSync(renderChunk3);
					renderChunk3.setNotDirty();
					this.minecraft.getProfiler().pop();
				}
			}
		}

		this.chunksToCompile.addAll(set);
		this.minecraft.getProfiler().pop();
	}

	@Nullable
	private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos blockPos, ChunkRenderDispatcher.RenderChunk renderChunk, Direction direction) {
		BlockPos blockPos2 = renderChunk.getRelativeOrigin(direction);
		if (Mth.abs(blockPos.getX() - blockPos2.getX()) > this.lastViewDistance * 16) {
			return null;
		} else if (blockPos2.getY() < 0 || blockPos2.getY() >= 256) {
			return null;
		} else {
			return Mth.abs(blockPos.getZ() - blockPos2.getZ()) > this.lastViewDistance * 16 ? null : this.viewArea.getRenderChunkAt(blockPos2);
		}
	}

	private void captureFrustum(Matrix4f matrix4f, Matrix4f matrix4f2, double d, double e, double f, Frustum frustum) {
		this.capturedFrustum = frustum;
		Matrix4f matrix4f3 = matrix4f2.copy();
		matrix4f3.multiply(matrix4f);
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
			this.frustumPoints[i].transform(matrix4f3);
			this.frustumPoints[i].perspectiveDivide();
		}
	}

	public void renderLevel(
		PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f
	) {
		BlockEntityRenderDispatcher.instance.prepare(this.level, this.minecraft.getTextureManager(), this.minecraft.font, camera, this.minecraft.hitResult);
		this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
		ProfilerFiller profilerFiller = this.level.getProfiler();
		profilerFiller.popPush("light_updates");
		this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double g = vec3.z();
		Matrix4f matrix4f2 = poseStack.last().pose();
		profilerFiller.popPush("culling");
		boolean bl2 = this.capturedFrustum != null;
		Frustum frustum;
		if (bl2) {
			frustum = this.capturedFrustum;
			frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
		} else {
			frustum = new Frustum(matrix4f2, matrix4f);
			frustum.prepare(d, e, g);
		}

		this.minecraft.getProfiler().popPush("captureFrustum");
		if (this.captureFrustum) {
			this.captureFrustum(matrix4f2, matrix4f, vec3.x, vec3.y, vec3.z, bl2 ? new Frustum(matrix4f2, matrix4f) : frustum);
			this.captureFrustum = false;
		}

		profilerFiller.popPush("clear");
		FogRenderer.setupColor(camera, f, this.minecraft.level, this.minecraft.options.renderDistance, gameRenderer.getDarkenWorldAmount(f));
		RenderSystem.clear(16640, Minecraft.ON_OSX);
		float h = gameRenderer.getRenderDistance();
		boolean bl3 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d), Mth.floor(e)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
		if (this.minecraft.options.renderDistance >= 4) {
			FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, h, bl3);
			profilerFiller.popPush("sky");
			this.renderSky(poseStack, f);
		}

		profilerFiller.popPush("fog");
		FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(h - 16.0F, 32.0F), bl3);
		profilerFiller.popPush("terrain_setup");
		this.setupRender(camera, frustum, bl2, this.frameId++, this.minecraft.player.isSpectator());
		profilerFiller.popPush("updatechunks");
		int i = 30;
		int j = this.minecraft.options.framerateLimit;
		long m = 33333333L;
		long n;
		if ((double)j == Option.FRAMERATE_LIMIT.getMaxValue()) {
			n = 0L;
		} else {
			n = (long)(1000000000 / j);
		}

		long o = Util.getNanos() - l;
		long p = this.frameTimes.registerValueAndGetMean(o);
		long q = p * 3L / 2L;
		long r = Mth.clamp(q, n, 33333333L);
		this.compileChunksUntil(l + r);
		profilerFiller.popPush("terrain");
		this.renderChunkLayer(RenderType.solid(), poseStack, d, e, g);
		this.renderChunkLayer(RenderType.cutoutMipped(), poseStack, d, e, g);
		this.renderChunkLayer(RenderType.cutout(), poseStack, d, e, g);
		if (this.level.effects().constantAmbientLight()) {
			Lighting.setupNetherLevel(poseStack.last().pose());
		} else {
			Lighting.setupLevel(poseStack.last().pose());
		}

		profilerFiller.popPush("entities");
		profilerFiller.push("prepare");
		this.renderedEntities = 0;
		this.culledEntities = 0;
		profilerFiller.popPush("entities");
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
			if ((this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, g) || entity.hasIndirectPassenger(this.minecraft.player))
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
					int k = entity.getTeamColor();
					int s = 255;
					int t = k >> 16 & 0xFF;
					int u = k >> 8 & 0xFF;
					int v = k & 0xFF;
					outlineBufferSource.setColor(t, u, v, 255);
				} else {
					multiBufferSource = bufferSource;
				}

				this.renderEntity(entity, d, e, g, f, poseStack, multiBufferSource);
			}
		}

		this.checkPoseStack(poseStack);
		bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
		bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
		bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
		bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
		profilerFiller.popPush("blockentities");

		for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
			List<BlockEntity> list = renderChunkInfo.chunk.getCompiledChunk().getRenderableBlockEntities();
			if (!list.isEmpty()) {
				for (BlockEntity blockEntity : list) {
					BlockPos blockPos = blockEntity.getBlockPos();
					MultiBufferSource multiBufferSource2 = bufferSource;
					poseStack.pushPose();
					poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - g);
					SortedSet<BlockDestructionProgress> sortedSet = this.destructionProgress.get(blockPos.asLong());
					if (sortedSet != null && !sortedSet.isEmpty()) {
						int v = ((BlockDestructionProgress)sortedSet.last()).getProgress();
						if (v >= 0) {
							PoseStack.Pose pose = poseStack.last();
							VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(
								this.renderBuffers.crumblingBufferSource().getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(v)), pose.pose(), pose.normal()
							);
							multiBufferSource2 = renderType -> {
								VertexConsumer vertexConsumer2x = bufferSource.getBuffer(renderType);
								return renderType.affectsCrumbling() ? VertexMultiConsumer.create(vertexConsumer, vertexConsumer2x) : vertexConsumer2x;
							};
						}
					}

					BlockEntityRenderDispatcher.instance.render(blockEntity, f, poseStack, multiBufferSource2);
					poseStack.popPose();
				}
			}
		}

		synchronized (this.globalBlockEntities) {
			for (BlockEntity blockEntity2 : this.globalBlockEntities) {
				BlockPos blockPos2 = blockEntity2.getBlockPos();
				poseStack.pushPose();
				poseStack.translate((double)blockPos2.getX() - d, (double)blockPos2.getY() - e, (double)blockPos2.getZ() - g);
				BlockEntityRenderDispatcher.instance.render(blockEntity2, f, poseStack, bufferSource);
				poseStack.popPose();
			}
		}

		this.checkPoseStack(poseStack);
		bufferSource.endBatch(RenderType.solid());
		bufferSource.endBatch(Sheets.solidBlockSheet());
		bufferSource.endBatch(Sheets.cutoutBlockSheet());
		bufferSource.endBatch(Sheets.bedSheet());
		bufferSource.endBatch(Sheets.shulkerBoxSheet());
		bufferSource.endBatch(Sheets.signSheet());
		bufferSource.endBatch(Sheets.chestSheet());
		this.renderBuffers.outlineBufferSource().endOutlineBatch();
		if (bl4) {
			this.entityEffect.process(f);
			this.minecraft.getMainRenderTarget().bindWrite(false);
		}

		profilerFiller.popPush("destroyProgress");

		for (Entry<SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.long2ObjectEntrySet()) {
			BlockPos blockPos3 = BlockPos.of(entry.getLongKey());
			double w = (double)blockPos3.getX() - d;
			double x = (double)blockPos3.getY() - e;
			double y = (double)blockPos3.getZ() - g;
			if (!(w * w + x * x + y * y > 1024.0)) {
				SortedSet<BlockDestructionProgress> sortedSet2 = (SortedSet<BlockDestructionProgress>)entry.getValue();
				if (sortedSet2 != null && !sortedSet2.isEmpty()) {
					int z = ((BlockDestructionProgress)sortedSet2.last()).getProgress();
					poseStack.pushPose();
					poseStack.translate((double)blockPos3.getX() - d, (double)blockPos3.getY() - e, (double)blockPos3.getZ() - g);
					PoseStack.Pose pose2 = poseStack.last();
					VertexConsumer vertexConsumer2 = new SheetedDecalTextureGenerator(
						this.renderBuffers.crumblingBufferSource().getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(z)), pose2.pose(), pose2.normal()
					);
					this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos3), blockPos3, this.level, poseStack, vertexConsumer2);
					poseStack.popPose();
				}
			}
		}

		this.checkPoseStack(poseStack);
		profilerFiller.pop();
		HitResult hitResult = this.minecraft.hitResult;
		if (bl && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
			profilerFiller.popPush("outline");
			BlockPos blockPos4 = ((BlockHitResult)hitResult).getBlockPos();
			BlockState blockState = this.level.getBlockState(blockPos4);
			if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos4)) {
				VertexConsumer vertexConsumer3 = bufferSource.getBuffer(RenderType.lines());
				this.renderHitOutline(poseStack, vertexConsumer3, camera.getEntity(), d, e, g, blockPos4, blockState);
			}
		}

		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(poseStack.last().pose());
		this.minecraft.debugRenderer.render(poseStack, bufferSource, d, e, g);
		RenderSystem.popMatrix();
		bufferSource.endBatch(Sheets.translucentCullBlockSheet());
		bufferSource.endBatch(Sheets.bannerSheet());
		bufferSource.endBatch(Sheets.shieldSheet());
		bufferSource.endBatch(RenderType.armorGlint());
		bufferSource.endBatch(RenderType.armorEntityGlint());
		bufferSource.endBatch(RenderType.glint());
		bufferSource.endBatch(RenderType.glintDirect());
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
			this.renderChunkLayer(RenderType.translucent(), poseStack, d, e, g);
			profilerFiller.popPush("string");
			this.renderChunkLayer(RenderType.tripwire(), poseStack, d, e, g);
			this.particlesTarget.clear(Minecraft.ON_OSX);
			this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
			RenderStateShard.PARTICLES_TARGET.setupRenderState();
			profilerFiller.popPush("particles");
			this.minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, f);
			RenderStateShard.PARTICLES_TARGET.clearRenderState();
		} else {
			profilerFiller.popPush("translucent");
			this.renderChunkLayer(RenderType.translucent(), poseStack, d, e, g);
			profilerFiller.popPush("string");
			this.renderChunkLayer(RenderType.tripwire(), poseStack, d, e, g);
			profilerFiller.popPush("particles");
			this.minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, f);
			bufferSource.endBatch(RenderType.lines());
			bufferSource.endBatch();
		}

		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(poseStack.last().pose());
		if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
			if (this.transparencyChain != null) {
				this.cloudsTarget.clear(Minecraft.ON_OSX);
				RenderStateShard.CLOUDS_TARGET.setupRenderState();
				profilerFiller.popPush("clouds");
				this.renderClouds(poseStack, f, d, e, g);
				RenderStateShard.CLOUDS_TARGET.clearRenderState();
			} else {
				profilerFiller.popPush("clouds");
				this.renderClouds(poseStack, f, d, e, g);
			}
		}

		if (this.transparencyChain != null) {
			RenderStateShard.WEATHER_TARGET.setupRenderState();
			profilerFiller.popPush("weather");
			this.renderSnowAndRain(lightTexture, f, d, e, g);
			this.renderWorldBounds(camera);
			RenderStateShard.WEATHER_TARGET.clearRenderState();
			this.transparencyChain.process(f);
			this.minecraft.getMainRenderTarget().bindWrite(false);
		} else {
			RenderSystem.depthMask(false);
			profilerFiller.popPush("weather");
			this.renderSnowAndRain(lightTexture, f, d, e, g);
			this.renderWorldBounds(camera);
			RenderSystem.depthMask(true);
		}

		this.renderDebug(camera);
		RenderSystem.shadeModel(7424);
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
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
		float k = Mth.lerp(g, entity.yRotO, entity.yRot);
		this.entityRenderDispatcher
			.render(entity, h - d, i - e, j - f, k, g, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, g));
	}

	private void renderChunkLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f) {
		renderType.setupRenderState();
		if (renderType == RenderType.translucent()) {
			this.minecraft.getProfiler().push("translucent_sort");
			double g = d - this.xTransparentOld;
			double h = e - this.yTransparentOld;
			double i = f - this.zTransparentOld;
			if (g * g + h * h + i * i > 1.0) {
				this.xTransparentOld = d;
				this.yTransparentOld = e;
				this.zTransparentOld = f;
				int j = 0;

				for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
					if (j < 15 && renderChunkInfo.chunk.resortTransparency(renderType, this.chunkRenderDispatcher)) {
						j++;
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}

		this.minecraft.getProfiler().push("filterempty");
		this.minecraft.getProfiler().popPush((Supplier<String>)(() -> "render_" + renderType));
		boolean bl = renderType != RenderType.translucent();
		ObjectListIterator<LevelRenderer.RenderChunkInfo> objectListIterator = this.renderChunks.listIterator(bl ? 0 : this.renderChunks.size());

		while (bl ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
			LevelRenderer.RenderChunkInfo renderChunkInfo2 = bl ? (LevelRenderer.RenderChunkInfo)objectListIterator.next() : objectListIterator.previous();
			ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo2.chunk;
			if (!renderChunk.getCompiledChunk().isEmpty(renderType)) {
				VertexBuffer vertexBuffer = renderChunk.getBuffer(renderType);
				poseStack.pushPose();
				BlockPos blockPos = renderChunk.getOrigin();
				poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
				vertexBuffer.bind();
				this.format.setupBufferState(0L);
				vertexBuffer.draw(poseStack.last().pose(), 7);
				poseStack.popPose();
			}
		}

		VertexBuffer.unbind();
		RenderSystem.clearCurrentColor();
		this.format.clearBufferState();
		this.minecraft.getProfiler().pop();
		renderType.clearRenderState();
	}

	private void renderDebug(Camera camera) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.minecraft.chunkPath || this.minecraft.chunkVisibility) {
			double d = camera.getPosition().x();
			double e = camera.getPosition().y();
			double f = camera.getPosition().z();
			RenderSystem.depthMask(true);
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableTexture();

			for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
				ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
				RenderSystem.pushMatrix();
				BlockPos blockPos = renderChunk.getOrigin();
				RenderSystem.translated((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
				if (this.minecraft.chunkPath) {
					bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
					RenderSystem.lineWidth(10.0F);
					int i = renderChunkInfo.step == 0 ? 0 : Mth.hsvToRgb((float)renderChunkInfo.step / 50.0F, 0.9F, 0.9F);
					int j = i >> 16 & 0xFF;
					int k = i >> 8 & 0xFF;
					int l = i & 0xFF;
					Direction direction = renderChunkInfo.sourceDirection;
					if (direction != null) {
						bufferBuilder.vertex(8.0, 8.0, 8.0).color(j, k, l, 255).endVertex();
						bufferBuilder.vertex((double)(8 - 16 * direction.getStepX()), (double)(8 - 16 * direction.getStepY()), (double)(8 - 16 * direction.getStepZ()))
							.color(j, k, l, 255)
							.endVertex();
					}

					tesselator.end();
					RenderSystem.lineWidth(1.0F);
				}

				if (this.minecraft.chunkVisibility && !renderChunk.getCompiledChunk().hasNoRenderableLayers()) {
					bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
					RenderSystem.lineWidth(10.0F);
					int i = 0;

					for (Direction direction : DIRECTIONS) {
						for (Direction direction2 : DIRECTIONS) {
							boolean bl = renderChunk.getCompiledChunk().facesCanSeeEachother(direction, direction2);
							if (!bl) {
								i++;
								bufferBuilder.vertex((double)(8 + 8 * direction.getStepX()), (double)(8 + 8 * direction.getStepY()), (double)(8 + 8 * direction.getStepZ()))
									.color(1, 0, 0, 1)
									.endVertex();
								bufferBuilder.vertex((double)(8 + 8 * direction2.getStepX()), (double)(8 + 8 * direction2.getStepY()), (double)(8 + 8 * direction2.getStepZ()))
									.color(1, 0, 0, 1)
									.endVertex();
							}
						}
					}

					tesselator.end();
					RenderSystem.lineWidth(1.0F);
					if (i > 0) {
						bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
						float g = 0.5F;
						float h = 0.2F;
						bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
						tesselator.end();
					}
				}

				RenderSystem.popMatrix();
			}

			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			RenderSystem.enableTexture();
		}

		if (this.capturedFrustum != null) {
			RenderSystem.disableCull();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.lineWidth(10.0F);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(
				(float)(this.frustumPos.x - camera.getPosition().x),
				(float)(this.frustumPos.y - camera.getPosition().y),
				(float)(this.frustumPos.z - camera.getPosition().z)
			);
			RenderSystem.depthMask(true);
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
			this.addFrustumQuad(bufferBuilder, 0, 1, 2, 3, 0, 1, 1);
			this.addFrustumQuad(bufferBuilder, 4, 5, 6, 7, 1, 0, 0);
			this.addFrustumQuad(bufferBuilder, 0, 1, 5, 4, 1, 1, 0);
			this.addFrustumQuad(bufferBuilder, 2, 3, 7, 6, 0, 0, 1);
			this.addFrustumQuad(bufferBuilder, 0, 4, 7, 3, 0, 1, 0);
			this.addFrustumQuad(bufferBuilder, 1, 5, 6, 2, 1, 0, 1);
			tesselator.end();
			RenderSystem.depthMask(false);
			bufferBuilder.begin(1, DefaultVertexFormat.POSITION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.addFrustumVertex(bufferBuilder, 0);
			this.addFrustumVertex(bufferBuilder, 1);
			this.addFrustumVertex(bufferBuilder, 1);
			this.addFrustumVertex(bufferBuilder, 2);
			this.addFrustumVertex(bufferBuilder, 2);
			this.addFrustumVertex(bufferBuilder, 3);
			this.addFrustumVertex(bufferBuilder, 3);
			this.addFrustumVertex(bufferBuilder, 0);
			this.addFrustumVertex(bufferBuilder, 4);
			this.addFrustumVertex(bufferBuilder, 5);
			this.addFrustumVertex(bufferBuilder, 5);
			this.addFrustumVertex(bufferBuilder, 6);
			this.addFrustumVertex(bufferBuilder, 6);
			this.addFrustumVertex(bufferBuilder, 7);
			this.addFrustumVertex(bufferBuilder, 7);
			this.addFrustumVertex(bufferBuilder, 4);
			this.addFrustumVertex(bufferBuilder, 0);
			this.addFrustumVertex(bufferBuilder, 4);
			this.addFrustumVertex(bufferBuilder, 1);
			this.addFrustumVertex(bufferBuilder, 5);
			this.addFrustumVertex(bufferBuilder, 2);
			this.addFrustumVertex(bufferBuilder, 6);
			this.addFrustumVertex(bufferBuilder, 3);
			this.addFrustumVertex(bufferBuilder, 7);
			tesselator.end();
			RenderSystem.popMatrix();
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			RenderSystem.enableTexture();
			RenderSystem.lineWidth(1.0F);
		}
	}

	private void addFrustumVertex(VertexConsumer vertexConsumer, int i) {
		vertexConsumer.vertex((double)this.frustumPoints[i].x(), (double)this.frustumPoints[i].y(), (double)this.frustumPoints[i].z()).endVertex();
	}

	private void addFrustumQuad(VertexConsumer vertexConsumer, int i, int j, int k, int l, int m, int n, int o) {
		float f = 0.25F;
		vertexConsumer.vertex((double)this.frustumPoints[i].x(), (double)this.frustumPoints[i].y(), (double)this.frustumPoints[i].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
		vertexConsumer.vertex((double)this.frustumPoints[j].x(), (double)this.frustumPoints[j].y(), (double)this.frustumPoints[j].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
		vertexConsumer.vertex((double)this.frustumPoints[k].x(), (double)this.frustumPoints[k].y(), (double)this.frustumPoints[k].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
		vertexConsumer.vertex((double)this.frustumPoints[l].x(), (double)this.frustumPoints[l].y(), (double)this.frustumPoints[l].z())
			.color((float)m, (float)n, (float)o, 0.25F)
			.endVertex();
	}

	public void tick() {
		this.ticks++;
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
		RenderSystem.disableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		this.textureManager.bind(END_SKY_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();

		for (int i = 0; i < 6; i++) {
			poseStack.pushPose();
			if (i == 1) {
				poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			}

			if (i == 2) {
				poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
			}

			if (i == 3) {
				poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
			}

			if (i == 4) {
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
			}

			if (i == 5) {
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
			}

			Matrix4f matrix4f = poseStack.last().pose();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			tesselator.end();
			poseStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
	}

	public void renderSky(PoseStack poseStack, float f) {
		if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
			this.renderEndSky(poseStack);
		} else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
			RenderSystem.disableTexture();
			Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getBlockPosition(), f);
			float g = (float)vec3.x;
			float h = (float)vec3.y;
			float i = (float)vec3.z;
			FogRenderer.levelFogColor();
			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.enableFog();
			RenderSystem.color3f(g, h, i);
			this.skyBuffer.bind();
			this.skyFormat.setupBufferState(0L);
			this.skyBuffer.draw(poseStack.last().pose(), 7);
			VertexBuffer.unbind();
			this.skyFormat.clearBufferState();
			RenderSystem.disableFog();
			RenderSystem.disableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			float[] fs = this.level.effects().getSunriseColor(this.level.getTimeOfDay(f), f);
			if (fs != null) {
				RenderSystem.disableTexture();
				RenderSystem.shadeModel(7425);
				poseStack.pushPose();
				poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
				float j = Mth.sin(this.level.getSunAngle(f)) < 0.0F ? 180.0F : 0.0F;
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(j));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
				float k = fs[0];
				float l = fs[1];
				float m = fs[2];
				Matrix4f matrix4f = poseStack.last().pose();
				bufferBuilder.begin(6, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(k, l, m, fs[3]).endVertex();
				int n = 16;

				for (int o = 0; o <= 16; o++) {
					float p = (float)o * (float) (Math.PI * 2) / 16.0F;
					float q = Mth.sin(p);
					float r = Mth.cos(p);
					bufferBuilder.vertex(matrix4f, q * 120.0F, r * 120.0F, -r * 40.0F * fs[3]).color(fs[0], fs[1], fs[2], 0.0F).endVertex();
				}

				bufferBuilder.end();
				BufferUploader.end(bufferBuilder);
				poseStack.popPose();
				RenderSystem.shadeModel(7424);
			}

			RenderSystem.enableTexture();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			poseStack.pushPose();
			float j = 1.0F - this.level.getRainLevel(f);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, j);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(this.level.getTimeOfDay(f) * 360.0F));
			Matrix4f matrix4f2 = poseStack.last().pose();
			float l = 30.0F;
			this.textureManager.bind(SUN_LOCATION);
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(matrix4f2, -l, 100.0F, -l).uv(0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(matrix4f2, l, 100.0F, -l).uv(1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(matrix4f2, l, 100.0F, l).uv(1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(matrix4f2, -l, 100.0F, l).uv(0.0F, 1.0F).endVertex();
			bufferBuilder.end();
			BufferUploader.end(bufferBuilder);
			l = 20.0F;
			this.textureManager.bind(MOON_LOCATION);
			int s = this.level.getMoonPhase();
			int t = s % 4;
			int n = s / 4 % 2;
			float u = (float)(t + 0) / 4.0F;
			float p = (float)(n + 0) / 2.0F;
			float q = (float)(t + 1) / 4.0F;
			float r = (float)(n + 1) / 2.0F;
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(matrix4f2, -l, -100.0F, l).uv(q, r).endVertex();
			bufferBuilder.vertex(matrix4f2, l, -100.0F, l).uv(u, r).endVertex();
			bufferBuilder.vertex(matrix4f2, l, -100.0F, -l).uv(u, p).endVertex();
			bufferBuilder.vertex(matrix4f2, -l, -100.0F, -l).uv(q, p).endVertex();
			bufferBuilder.end();
			BufferUploader.end(bufferBuilder);
			RenderSystem.disableTexture();
			float v = this.level.getStarBrightness(f) * j;
			if (v > 0.0F) {
				RenderSystem.color4f(v, v, v, v);
				this.starBuffer.bind();
				this.skyFormat.setupBufferState(0L);
				this.starBuffer.draw(poseStack.last().pose(), 7);
				VertexBuffer.unbind();
				this.skyFormat.clearBufferState();
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.enableFog();
			poseStack.popPose();
			RenderSystem.disableTexture();
			RenderSystem.color3f(0.0F, 0.0F, 0.0F);
			double d = this.minecraft.player.getEyePosition(f).y - this.level.getLevelData().getHorizonHeight();
			if (d < 0.0) {
				poseStack.pushPose();
				poseStack.translate(0.0, 12.0, 0.0);
				this.darkBuffer.bind();
				this.skyFormat.setupBufferState(0L);
				this.darkBuffer.draw(poseStack.last().pose(), 7);
				VertexBuffer.unbind();
				this.skyFormat.clearBufferState();
				poseStack.popPose();
			}

			if (this.level.effects().hasGround()) {
				RenderSystem.color3f(g * 0.2F + 0.04F, h * 0.2F + 0.04F, i * 0.6F + 0.1F);
			} else {
				RenderSystem.color3f(g, h, i);
			}

			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
			RenderSystem.disableFog();
		}
	}

	public void renderClouds(PoseStack poseStack, float f, double d, double e, double g) {
		float h = this.level.effects().getCloudHeight();
		if (!Float.isNaN(h)) {
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.enableDepthTest();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
			);
			RenderSystem.enableFog();
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

				this.cloudBuffer = new VertexBuffer(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
				this.buildClouds(bufferBuilder, m, n, o, vec3);
				bufferBuilder.end();
				this.cloudBuffer.upload(bufferBuilder);
			}

			this.textureManager.bind(CLOUDS_LOCATION);
			poseStack.pushPose();
			poseStack.scale(12.0F, 1.0F, 12.0F);
			poseStack.translate((double)(-p), (double)q, (double)(-r));
			if (this.cloudBuffer != null) {
				this.cloudBuffer.bind();
				DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);
				int v = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

				for (int w = v; w < 2; w++) {
					if (w == 0) {
						RenderSystem.colorMask(false, false, false, false);
					} else {
						RenderSystem.colorMask(true, true, true, true);
					}

					this.cloudBuffer.draw(poseStack.last().pose(), 7);
				}

				VertexBuffer.unbind();
				DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.clearBufferState();
			}

			poseStack.popPose();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableAlphaTest();
			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			RenderSystem.disableFog();
		}
	}

	private void buildClouds(BufferBuilder bufferBuilder, double d, double e, double f, Vec3 vec3) {
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
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
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
	}

	private void compileChunksUntil(long l) {
		this.needsUpdate = this.needsUpdate | this.chunkRenderDispatcher.uploadAllPendingUploads();
		long m = Util.getNanos();
		int i = 0;
		if (!this.chunksToCompile.isEmpty()) {
			Iterator<ChunkRenderDispatcher.RenderChunk> iterator = this.chunksToCompile.iterator();

			while (iterator.hasNext()) {
				ChunkRenderDispatcher.RenderChunk renderChunk = (ChunkRenderDispatcher.RenderChunk)iterator.next();
				if (renderChunk.isDirtyFromPlayer()) {
					this.chunkRenderDispatcher.rebuildChunkSync(renderChunk);
				} else {
					renderChunk.rebuildChunkAsync(this.chunkRenderDispatcher);
				}

				renderChunk.setNotDirty();
				iterator.remove();
				i++;
				long n = Util.getNanos();
				long o = n - m;
				long p = o / (long)i;
				long q = l - n;
				if (q < p) {
					break;
				}
			}
		}
	}

	private void renderWorldBounds(Camera camera) {
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		WorldBorder worldBorder = this.level.getWorldBorder();
		double d = (double)(this.minecraft.options.renderDistance * 16);
		if (!(camera.getPosition().x < worldBorder.getMaxX() - d)
			|| !(camera.getPosition().x > worldBorder.getMinX() + d)
			|| !(camera.getPosition().z < worldBorder.getMaxZ() - d)
			|| !(camera.getPosition().z > worldBorder.getMinZ() + d)) {
			double e = 1.0 - worldBorder.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d;
			e = Math.pow(e, 4.0);
			double f = camera.getPosition().x;
			double g = camera.getPosition().y;
			double h = camera.getPosition().z;
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			this.textureManager.bind(FORCEFIELD_LOCATION);
			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			RenderSystem.pushMatrix();
			int i = worldBorder.getStatus().getColor();
			float j = (float)(i >> 16 & 0xFF) / 255.0F;
			float k = (float)(i >> 8 & 0xFF) / 255.0F;
			float l = (float)(i & 0xFF) / 255.0F;
			RenderSystem.color4f(j, k, l, (float)e);
			RenderSystem.polygonOffset(-3.0F, -3.0F);
			RenderSystem.enablePolygonOffset();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableAlphaTest();
			RenderSystem.disableCull();
			float m = (float)(Util.getMillis() % 3000L) / 3000.0F;
			float n = 0.0F;
			float o = 0.0F;
			float p = 128.0F;
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			double q = Math.max((double)Mth.floor(h - d), worldBorder.getMinZ());
			double r = Math.min((double)Mth.ceil(h + d), worldBorder.getMaxZ());
			if (f > worldBorder.getMaxX() - d) {
				float s = 0.0F;

				for (double t = q; t < r; s += 0.5F) {
					double u = Math.min(1.0, r - t);
					float v = (float)u * 0.5F;
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMaxX(), 256, t, m + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMaxX(), 256, t + u, m + v + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMaxX(), 0, t + u, m + v + s, m + 128.0F);
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMaxX(), 0, t, m + s, m + 128.0F);
					t++;
				}
			}

			if (f < worldBorder.getMinX() + d) {
				float s = 0.0F;

				for (double t = q; t < r; s += 0.5F) {
					double u = Math.min(1.0, r - t);
					float v = (float)u * 0.5F;
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMinX(), 256, t, m + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMinX(), 256, t + u, m + v + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMinX(), 0, t + u, m + v + s, m + 128.0F);
					this.vertex(bufferBuilder, f, g, h, worldBorder.getMinX(), 0, t, m + s, m + 128.0F);
					t++;
				}
			}

			q = Math.max((double)Mth.floor(f - d), worldBorder.getMinX());
			r = Math.min((double)Mth.ceil(f + d), worldBorder.getMaxX());
			if (h > worldBorder.getMaxZ() - d) {
				float s = 0.0F;

				for (double t = q; t < r; s += 0.5F) {
					double u = Math.min(1.0, r - t);
					float v = (float)u * 0.5F;
					this.vertex(bufferBuilder, f, g, h, t, 256, worldBorder.getMaxZ(), m + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, t + u, 256, worldBorder.getMaxZ(), m + v + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, t + u, 0, worldBorder.getMaxZ(), m + v + s, m + 128.0F);
					this.vertex(bufferBuilder, f, g, h, t, 0, worldBorder.getMaxZ(), m + s, m + 128.0F);
					t++;
				}
			}

			if (h < worldBorder.getMinZ() + d) {
				float s = 0.0F;

				for (double t = q; t < r; s += 0.5F) {
					double u = Math.min(1.0, r - t);
					float v = (float)u * 0.5F;
					this.vertex(bufferBuilder, f, g, h, t, 256, worldBorder.getMinZ(), m + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, t + u, 256, worldBorder.getMinZ(), m + v + s, m + 0.0F);
					this.vertex(bufferBuilder, f, g, h, t + u, 0, worldBorder.getMinZ(), m + v + s, m + 128.0F);
					this.vertex(bufferBuilder, f, g, h, t, 0, worldBorder.getMinZ(), m + s, m + 128.0F);
					t++;
				}
			}

			bufferBuilder.end();
			BufferUploader.end(bufferBuilder);
			RenderSystem.enableCull();
			RenderSystem.disableAlphaTest();
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.enableAlphaTest();
			RenderSystem.disableBlend();
			RenderSystem.popMatrix();
			RenderSystem.depthMask(true);
		}
	}

	private void vertex(BufferBuilder bufferBuilder, double d, double e, double f, double g, int i, double h, float j, float k) {
		bufferBuilder.vertex(g - d, (double)i - e, h - f).uv(j, k).endVertex();
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

	public static void renderVoxelShape(
		PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j
	) {
		List<AABB> list = voxelShape.toAabbs();
		int k = Mth.ceil((double)list.size() / 3.0);

		for (int l = 0; l < list.size(); l++) {
			AABB aABB = (AABB)list.get(l);
			float m = ((float)l % (float)k + 1.0F) / (float)k;
			float n = (float)(l / k);
			float o = m * (float)(n == 0.0F ? 1 : 0);
			float p = m * (float)(n == 1.0F ? 1 : 0);
			float q = m * (float)(n == 2.0F ? 1 : 0);
			renderShape(poseStack, vertexConsumer, Shapes.create(aABB.move(0.0, 0.0, 0.0)), d, e, f, o, p, q, 1.0F);
		}
	}

	private static void renderShape(
		PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		voxelShape.forAllEdges((k, l, m, n, o, p) -> {
			vertexConsumer.vertex(matrix4f, (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).endVertex();
			vertexConsumer.vertex(matrix4f, (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).endVertex();
		});
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
		float q = (float)d;
		float r = (float)e;
		float s = (float)f;
		float t = (float)g;
		float u = (float)h;
		float v = (float)i;
		vertexConsumer.vertex(matrix4f, q, r, s).color(j, o, p, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, s).color(j, o, p, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, s).color(n, k, p, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, s).color(n, k, p, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, s).color(n, o, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, v).color(n, o, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, r, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).endVertex();
		vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).endVertex();
	}

	public static void addChainedFilledBoxVertices(
		BufferBuilder bufferBuilder, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, m).endVertex();
	}

	public void blockChanged(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
		this.setBlockDirty(blockPos, (i & 8) != 0);
	}

	private void setBlockDirty(BlockPos blockPos, boolean bl) {
		for (int i = blockPos.getZ() - 1; i <= blockPos.getZ() + 1; i++) {
			for (int j = blockPos.getX() - 1; j <= blockPos.getX() + 1; j++) {
				for (int k = blockPos.getY() - 1; k <= blockPos.getY() + 1; k++) {
					this.setSectionDirty(j >> 4, k >> 4, i >> 4, bl);
				}
			}
		}
	}

	public void setBlocksDirty(int i, int j, int k, int l, int m, int n) {
		for (int o = k - 1; o <= n + 1; o++) {
			for (int p = i - 1; p <= l + 1; p++) {
				for (int q = j - 1; q <= m + 1; q++) {
					this.setSectionDirty(p >> 4, q >> 4, o >> 4);
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

			SoundInstance var5 = SimpleSoundInstance.forRecord(soundEvent, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
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
			crashReportCategory.setDetail("ID", Registry.PARTICLE_TYPE.getKey(particleOptions.getType()));
			crashReportCategory.setDetail("Parameters", particleOptions.writeToString());
			crashReportCategory.setDetail("Position", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(d, e, f)));
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
		if (this.minecraft != null && camera.isInitialized() && this.minecraft.particleEngine != null) {
			ParticleStatus particleStatus = this.calculateParticleLevel(bl2);
			if (bl) {
				return this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
			} else if (camera.getPosition().distanceToSqr(d, e, f) > 1024.0) {
				return null;
			} else {
				return particleStatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
			}
		} else {
			return null;
		}
	}

	private ParticleStatus calculateParticleLevel(boolean bl) {
		ParticleStatus particleStatus = this.minecraft.options.particles;
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

	public void levelEvent(Player player, int i, BlockPos blockPos, int j) {
		Random random = this.level.random;
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
			case 1005:
				this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1006:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1007:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1008:
				this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1009:
				this.level.playLocalSound(blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
				break;
			case 1010:
				if (Item.byId(j) instanceof RecordItem) {
					this.playStreamingMusic(((RecordItem)Item.byId(j)).getSound(), blockPos);
				} else {
					this.playStreamingMusic(null, blockPos);
				}
				break;
			case 1011:
				this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1012:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1013:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1014:
				this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1015:
				this.level.playLocalSound(blockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1016:
				this.level.playLocalSound(blockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1017:
				this.level
					.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1018:
				this.level.playLocalSound(blockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1019:
				this.level
					.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1020:
				this.level
					.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1021:
				this.level
					.playLocalSound(blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1022:
				this.level
					.playLocalSound(blockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1024:
				this.level.playLocalSound(blockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1025:
				this.level.playLocalSound(blockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1026:
				this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1027:
				this.level
					.playLocalSound(blockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1029:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1030:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1031:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1032:
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, random.nextFloat() * 0.4F + 0.8F, 0.25F));
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
			case 1036:
				this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1037:
				this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1039:
				this.level.playLocalSound(blockPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1040:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1041:
				this.level
					.playLocalSound(blockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
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
			case 1500:
				ComposterBlock.handleFill(this.level, blockPos, j > 0);
				break;
			case 1501:
				this.level.playLocalSound(blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);

				for (int kx = 0; kx < 8; kx++) {
					this.level
						.addParticle(
							ParticleTypes.LARGE_SMOKE,
							(double)blockPos.getX() + random.nextDouble(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + random.nextDouble(),
							0.0,
							0.0,
							0.0
						);
				}
				break;
			case 1502:
				this.level
					.playLocalSound(blockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);

				for (int kx = 0; kx < 5; kx++) {
					double u = (double)blockPos.getX() + random.nextDouble() * 0.6 + 0.2;
					double d = (double)blockPos.getY() + random.nextDouble() * 0.6 + 0.2;
					double e = (double)blockPos.getZ() + random.nextDouble() * 0.6 + 0.2;
					this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
				}
				break;
			case 1503:
				this.level.playLocalSound(blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

				for (int kx = 0; kx < 16; kx++) {
					double u = (double)blockPos.getX() + (5.0 + random.nextDouble() * 6.0) / 16.0;
					double d = (double)blockPos.getY() + 0.8125;
					double e = (double)blockPos.getZ() + (5.0 + random.nextDouble() * 6.0) / 16.0;
					this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
				}
				break;
			case 2000:
				Direction direction = Direction.from3DDataValue(j);
				int kx = direction.getStepX();
				int l = direction.getStepY();
				int m = direction.getStepZ();
				double d = (double)blockPos.getX() + (double)kx * 0.6 + 0.5;
				double e = (double)blockPos.getY() + (double)l * 0.6 + 0.5;
				double f = (double)blockPos.getZ() + (double)m * 0.6 + 0.5;

				for (int n = 0; n < 10; n++) {
					double g = random.nextDouble() * 0.2 + 0.01;
					double h = d + (double)kx * 0.01 + (random.nextDouble() - 0.5) * (double)m * 0.5;
					double o = e + (double)l * 0.01 + (random.nextDouble() - 0.5) * (double)l * 0.5;
					double p = f + (double)m * 0.01 + (random.nextDouble() - 0.5) * (double)kx * 0.5;
					double q = (double)kx * g + random.nextGaussian() * 0.01;
					double r = (double)l * g + random.nextGaussian() * 0.01;
					double s = (double)m * g + random.nextGaussian() * 0.01;
					this.addParticle(ParticleTypes.SMOKE, h, o, p, q, r, s);
				}
				break;
			case 2001:
				BlockState blockState = Block.stateById(j);
				if (!blockState.isAir()) {
					SoundType soundType = blockState.getSoundType();
					this.level
						.playLocalSound(blockPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
				}

				this.minecraft.particleEngine.destroy(blockPos, blockState);
				break;
			case 2002:
			case 2007:
				Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);

				for (int kx = 0; kx < 8; kx++) {
					this.addParticle(
						new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)),
						vec3.x,
						vec3.y,
						vec3.z,
						random.nextGaussian() * 0.15,
						random.nextDouble() * 0.2,
						random.nextGaussian() * 0.15
					);
				}

				float w = (float)(j >> 16 & 0xFF) / 255.0F;
				float x = (float)(j >> 8 & 0xFF) / 255.0F;
				float y = (float)(j >> 0 & 0xFF) / 255.0F;
				ParticleOptions particleOptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

				for (int z = 0; z < 100; z++) {
					double e = random.nextDouble() * 4.0;
					double f = random.nextDouble() * Math.PI * 2.0;
					double aa = Math.cos(f) * e;
					double ab = 0.01 + random.nextDouble() * 0.5;
					double ac = Math.sin(f) * e;
					Particle particle = this.addParticleInternal(
						particleOptions, particleOptions.getType().getOverrideLimiter(), vec3.x + aa * 0.1, vec3.y + 0.3, vec3.z + ac * 0.1, aa, ab, ac
					);
					if (particle != null) {
						float ad = 0.75F + random.nextFloat() * 0.25F;
						particle.setColor(w * ad, x * ad, y * ad);
						particle.setPower((float)e);
					}
				}

				this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 2003:
				double t = (double)blockPos.getX() + 0.5;
				double u = (double)blockPos.getY();
				double d = (double)blockPos.getZ() + 0.5;

				for (int v = 0; v < 8; v++) {
					this.addParticle(
						new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)),
						t,
						u,
						d,
						random.nextGaussian() * 0.15,
						random.nextDouble() * 0.2,
						random.nextGaussian() * 0.15
					);
				}

				for (double e = 0.0; e < Math.PI * 2; e += Math.PI / 20) {
					this.addParticle(ParticleTypes.PORTAL, t + Math.cos(e) * 5.0, u - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -5.0, 0.0, Math.sin(e) * -5.0);
					this.addParticle(ParticleTypes.PORTAL, t + Math.cos(e) * 5.0, u - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -7.0, 0.0, Math.sin(e) * -7.0);
				}
				break;
			case 2004:
				for (int kx = 0; kx < 20; kx++) {
					double u = (double)blockPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
					double d = (double)blockPos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
					double e = (double)blockPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
					this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
					this.level.addParticle(ParticleTypes.FLAME, u, d, e, 0.0, 0.0, 0.0);
				}
				break;
			case 2005:
				BoneMealItem.addGrowthParticles(this.level, blockPos, j);
				break;
			case 2006:
				for (int k = 0; k < 200; k++) {
					float x = random.nextFloat() * 4.0F;
					float y = random.nextFloat() * (float) (Math.PI * 2);
					double d = (double)(Mth.cos(y) * x);
					double e = 0.01 + random.nextDouble() * 0.5;
					double f = (double)(Mth.sin(y) * x);
					Particle particle2 = this.addParticleInternal(
						ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + d * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + f * 0.1, d, e, f
					);
					if (particle2 != null) {
						particle2.setPower(x);
					}
				}

				if (j == 1) {
					this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
				}
				break;
			case 2008:
				this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
				break;
			case 2009:
				for (int kx = 0; kx < 8; kx++) {
					this.level
						.addParticle(
							ParticleTypes.CLOUD,
							(double)blockPos.getX() + random.nextDouble(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + random.nextDouble(),
							0.0,
							0.0,
							0.0
						);
				}
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
			this.destructionProgress.computeIfAbsent(blockDestructionProgress.getPos().asLong(), l -> Sets.newTreeSet()).add(blockDestructionProgress);
		} else {
			BlockDestructionProgress blockDestructionProgressx = this.destroyingBlocks.remove(i);
			if (blockDestructionProgressx != null) {
				this.removeProgress(blockDestructionProgressx);
			}
		}
	}

	public boolean hasRenderedAllChunks() {
		return this.chunksToCompile.isEmpty() && this.chunkRenderDispatcher.isQueueEmpty();
	}

	public void needsUpdate() {
		this.needsUpdate = true;
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

	@Environment(EnvType.CLIENT)
	class RenderChunkInfo {
		private final ChunkRenderDispatcher.RenderChunk chunk;
		private final Direction sourceDirection;
		private byte directions;
		private final int step;

		private RenderChunkInfo(ChunkRenderDispatcher.RenderChunk renderChunk, @Nullable Direction direction, int i) {
			this.chunk = renderChunk;
			this.sourceDirection = direction;
			this.step = i;
		}

		public void setDirections(byte b, Direction direction) {
			this.directions = (byte)(this.directions | b | 1 << direction.ordinal());
		}

		public boolean hasDirection(Direction direction) {
			return (this.directions & 1 << direction.ordinal()) > 0;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TranparencyShaderException extends RuntimeException {
		public TranparencyShaderException(String string, Throwable throwable) {
			super(string, throwable);
		}
	}
}
