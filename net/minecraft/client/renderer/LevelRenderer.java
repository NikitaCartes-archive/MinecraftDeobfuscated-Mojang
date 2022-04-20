/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
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
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
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
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RunningTrimmedMean;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
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
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LevelRenderer
implements ResourceManagerReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int CHUNK_SIZE = 16;
    private static final int HALF_CHUNK_SIZE = 8;
    private static final float SKY_DISC_RADIUS = 512.0f;
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private static final int MIN_FOG_DISTANCE = 32;
    private static final int RAIN_RADIUS = 10;
    private static final int RAIN_DIAMETER = 21;
    private static final int TRANSPARENT_SORT_COUNT = 15;
    private static final int HALF_A_SECOND_IN_MILLIS = 500;
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
    private final BlockingQueue<ChunkRenderDispatcher.RenderChunk> recentlyCompiledChunks = new LinkedBlockingQueue<ChunkRenderDispatcher.RenderChunk>();
    private final AtomicReference<RenderChunkStorage> renderChunkStorage = new AtomicReference();
    private final ObjectArrayList<RenderChunkInfo> renderChunksInFrustum = new ObjectArrayList(10000);
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    @Nullable
    private Future<?> lastFullRenderChunkUpdate;
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
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<BlockDestructionProgress>();
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<SortedSet<BlockDestructionProgress>>();
    private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
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
    @Nullable
    private CloudStatus prevCloudsType;
    @Nullable
    private ChunkRenderDispatcher chunkRenderDispatcher;
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
    private boolean needsFullRenderChunkUpdate = true;
    private final AtomicLong nextFullUpdateMillis = new AtomicLong(0L);
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    public LevelRenderer(Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers) {
        this.minecraft = minecraft;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.renderBuffers = renderBuffers;
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float g = i - 16;
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
        if (h <= 0.0f) {
            return;
        }
        lightTexture.turnOnLightLayer();
        ClientLevel level = this.minecraft.level;
        int i = Mth.floor(d);
        int j = Mth.floor(e);
        int k = Mth.floor(g);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int l = 5;
        if (Minecraft.useFancyGraphics()) {
            l = 10;
        }
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        int m = -1;
        float n = (float)this.ticks + f;
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int o = k - l; o <= k + l; ++o) {
            for (int p = i - l; p <= i + l; ++p) {
                float ac;
                float y;
                int w;
                int q = (o - k + 16) * 32 + p - i + 16;
                double r = (double)this.rainSizeX[q] * 0.5;
                double s = (double)this.rainSizeZ[q] * 0.5;
                mutableBlockPos.set((double)p, e, (double)o);
                Biome biome = level.getBiome(mutableBlockPos).value();
                if (biome.getPrecipitation() == Biome.Precipitation.NONE) continue;
                int t = level.getHeight(Heightmap.Types.MOTION_BLOCKING, p, o);
                int u = j - l;
                int v = j + l;
                if (u < t) {
                    u = t;
                }
                if (v < t) {
                    v = t;
                }
                if ((w = t) < j) {
                    w = j;
                }
                if (u == v) continue;
                RandomSource randomSource = RandomSource.create(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761);
                mutableBlockPos.set(p, u, o);
                if (biome.warmEnoughToRain(mutableBlockPos)) {
                    if (m != 0) {
                        if (m >= 0) {
                            tesselator.end();
                        }
                        m = 0;
                        RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    }
                    int x = this.ticks + p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 0x1F;
                    y = -((float)x + f) / 32.0f * (3.0f + randomSource.nextFloat());
                    double z = (double)p + 0.5 - d;
                    double aa = (double)o + 0.5 - g;
                    float ab = (float)Math.sqrt(z * z + aa * aa) / (float)l;
                    ac = ((1.0f - ab * ab) * 0.5f + 0.5f) * h;
                    mutableBlockPos.set(p, w, o);
                    int ad = LevelRenderer.getLightColor(level, mutableBlockPos);
                    bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5).uv(0.0f, (float)u * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).uv2(ad).endVertex();
                    bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5).uv(1.0f, (float)u * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).uv2(ad).endVertex();
                    bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5).uv(1.0f, (float)v * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).uv2(ad).endVertex();
                    bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5).uv(0.0f, (float)v * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).uv2(ad).endVertex();
                    continue;
                }
                if (m != 1) {
                    if (m >= 0) {
                        tesselator.end();
                    }
                    m = 1;
                    RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                }
                float ae = -((float)(this.ticks & 0x1FF) + f) / 512.0f;
                y = (float)(randomSource.nextDouble() + (double)n * 0.01 * (double)((float)randomSource.nextGaussian()));
                float af = (float)(randomSource.nextDouble() + (double)(n * (float)randomSource.nextGaussian()) * 0.001);
                double ag = (double)p + 0.5 - d;
                double ah = (double)o + 0.5 - g;
                ac = (float)Math.sqrt(ag * ag + ah * ah) / (float)l;
                float ai = ((1.0f - ac * ac) * 0.3f + 0.5f) * h;
                mutableBlockPos.set(p, w, o);
                int aj = LevelRenderer.getLightColor(level, mutableBlockPos);
                int ak = aj >> 16 & 0xFFFF;
                int al = aj & 0xFFFF;
                int am = (ak * 3 + 240) / 4;
                int an = (al * 3 + 240) / 4;
                bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5).uv(0.0f + y, (float)u * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).uv2(an, am).endVertex();
                bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5).uv(1.0f + y, (float)u * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).uv2(an, am).endVertex();
                bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5).uv(1.0f + y, (float)v * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).uv2(an, am).endVertex();
                bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5).uv(0.0f + y, (float)v * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).uv2(an, am).endVertex();
            }
        }
        if (m >= 0) {
            tesselator.end();
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
    }

    public void tickRain(Camera camera) {
        float f = this.minecraft.level.getRainLevel(1.0f) / (Minecraft.useFancyGraphics() ? 1.0f : 2.0f);
        if (f <= 0.0f) {
            return;
        }
        RandomSource randomSource = RandomSource.create((long)this.ticks * 312987231L);
        ClientLevel levelReader = this.minecraft.level;
        BlockPos blockPos = new BlockPos(camera.getPosition());
        Vec3i blockPos2 = null;
        int i = (int)(100.0f * f * f) / (this.minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);
        for (int j = 0; j < i; ++j) {
            int k = randomSource.nextInt(21) - 10;
            int l = randomSource.nextInt(21) - 10;
            BlockPos blockPos3 = levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(k, 0, l));
            Biome biome = levelReader.getBiome(blockPos3).value();
            if (blockPos3.getY() <= levelReader.getMinBuildHeight() || blockPos3.getY() > blockPos.getY() + 10 || blockPos3.getY() < blockPos.getY() - 10 || biome.getPrecipitation() != Biome.Precipitation.RAIN || !biome.warmEnoughToRain(blockPos3)) continue;
            blockPos2 = blockPos3.below();
            if (this.minecraft.options.particles().get() == ParticleStatus.MINIMAL) break;
            double d = randomSource.nextDouble();
            double e = randomSource.nextDouble();
            BlockState blockState = levelReader.getBlockState((BlockPos)blockPos2);
            FluidState fluidState = levelReader.getFluidState((BlockPos)blockPos2);
            VoxelShape voxelShape = blockState.getCollisionShape(levelReader, (BlockPos)blockPos2);
            double g = voxelShape.max(Direction.Axis.Y, d, e);
            double h = fluidState.getHeight(levelReader, (BlockPos)blockPos2);
            double m = Math.max(g, h);
            SimpleParticleType particleOptions = fluidState.is(FluidTags.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState) ? ParticleTypes.SMOKE : ParticleTypes.RAIN;
            this.minecraft.level.addParticle(particleOptions, (double)blockPos2.getX() + d, (double)blockPos2.getY() + m, (double)blockPos2.getZ() + e, 0.0, 0.0, 0.0);
        }
        if (blockPos2 != null && randomSource.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (blockPos2.getY() > blockPos.getY() + 1 && levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor(blockPos.getY())) {
                this.minecraft.level.playLocalSound((BlockPos)blockPos2, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1f, 0.5f, false);
            } else {
                this.minecraft.level.playLocalSound((BlockPos)blockPos2, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2f, 1.0f, false);
            }
        }
    }

    @Override
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
            this.entityEffect = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourceLocation);
            this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.entityTarget = this.entityEffect.getTempTarget("final");
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load shader: {}", (Object)resourceLocation, (Object)iOException);
            this.entityEffect = null;
            this.entityTarget = null;
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("Failed to parse shader: {}", (Object)resourceLocation, (Object)jsonSyntaxException);
            this.entityEffect = null;
            this.entityTarget = null;
        }
    }

    private void initTransparency() {
        this.deinitTransparency();
        ResourceLocation resourceLocation = new ResourceLocation("shaders/post/transparency.json");
        try {
            PostChain postChain = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourceLocation);
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
        } catch (Exception exception) {
            String string = exception instanceof JsonSyntaxException ? "parse" : "load";
            String string2 = "Failed to " + string + " shader: " + resourceLocation;
            TransparencyShaderException transparencyShaderException = new TransparencyShaderException(string2, exception);
            if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1) {
                Component component = this.minecraft.getResourceManager().listPacks().findFirst().map(packResources -> Component.literal(packResources.getName())).orElse(null);
                this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
                this.minecraft.clearResourcePacksOnError(transparencyShaderException, component);
            }
            CrashReport crashReport = this.minecraft.fillReport(new CrashReport(string2, transparencyShaderException));
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
            this.minecraft.options.save();
            LOGGER.error(LogUtils.FATAL_MARKER, string2, transparencyShaderException);
            this.minecraft.emergencySave();
            Minecraft.crash(crashReport);
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
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            this.entityTarget.blitToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
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
        this.darkBuffer = new VertexBuffer();
        LevelRenderer.buildSkyDisc(bufferBuilder, -16.0f);
        this.darkBuffer.bind();
        this.darkBuffer.upload(bufferBuilder);
        VertexBuffer.unbind();
    }

    private void createLightSky() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }
        this.skyBuffer = new VertexBuffer();
        LevelRenderer.buildSkyDisc(bufferBuilder, 16.0f);
        this.skyBuffer.bind();
        this.skyBuffer.upload(bufferBuilder);
        VertexBuffer.unbind();
    }

    private static void buildSkyDisc(BufferBuilder bufferBuilder, float f) {
        float g = Math.signum(f) * 512.0f;
        float h = 512.0f;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(0.0, f, 0.0).endVertex();
        for (int i = -180; i <= 180; i += 45) {
            bufferBuilder.vertex(g * Mth.cos((float)i * ((float)Math.PI / 180)), f, 512.0f * Mth.sin((float)i * ((float)Math.PI / 180))).endVertex();
        }
        bufferBuilder.end();
    }

    private void createStars() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }
        this.starBuffer = new VertexBuffer();
        this.drawStars(bufferBuilder);
        bufferBuilder.end();
        this.starBuffer.bind();
        this.starBuffer.upload(bufferBuilder);
        VertexBuffer.unbind();
    }

    private void drawStars(BufferBuilder bufferBuilder) {
        RandomSource randomSource = RandomSource.create(10842L);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 1500; ++i) {
            double d = randomSource.nextFloat() * 2.0f - 1.0f;
            double e = randomSource.nextFloat() * 2.0f - 1.0f;
            double f = randomSource.nextFloat() * 2.0f - 1.0f;
            double g = 0.15f + randomSource.nextFloat() * 0.1f;
            double h = d * d + e * e + f * f;
            if (!(h < 1.0) || !(h > 0.01)) continue;
            h = 1.0 / Math.sqrt(h);
            double j = (d *= h) * 100.0;
            double k = (e *= h) * 100.0;
            double l = (f *= h) * 100.0;
            double m = Math.atan2(d, f);
            double n = Math.sin(m);
            double o = Math.cos(m);
            double p = Math.atan2(Math.sqrt(d * d + f * f), e);
            double q = Math.sin(p);
            double r = Math.cos(p);
            double s = randomSource.nextDouble() * Math.PI * 2.0;
            double t = Math.sin(s);
            double u = Math.cos(s);
            for (int v = 0; v < 4; ++v) {
                double ab;
                double w = 0.0;
                double x = (double)((v & 2) - 1) * g;
                double y = (double)((v + 1 & 2) - 1) * g;
                double z = 0.0;
                double aa = x * u - y * t;
                double ac = ab = y * u + x * t;
                double ad = aa * q + 0.0 * r;
                double ae = 0.0 * q - aa * r;
                double af = ae * n - ac * o;
                double ag = ad;
                double ah = ac * n + ae * o;
                bufferBuilder.vertex(j + af, k + ag, l + ah).endVertex();
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
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }
            if (this.chunkRenderDispatcher != null) {
                this.chunkRenderDispatcher.dispose();
            }
            this.chunkRenderDispatcher = null;
            this.globalBlockEntities.clear();
            this.renderChunkStorage.set(null);
            this.renderChunksInFrustum.clear();
        }
    }

    public void graphicsChanged() {
        if (Minecraft.useShaderTransparency()) {
            this.initTransparency();
        } else {
            this.deinitTransparency();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void allChanged() {
        if (this.level == null) {
            return;
        }
        this.graphicsChanged();
        this.level.clearTintCaches();
        if (this.chunkRenderDispatcher == null) {
            this.chunkRenderDispatcher = new ChunkRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack());
        } else {
            this.chunkRenderDispatcher.setLevel(this.level);
        }
        this.needsFullRenderChunkUpdate = true;
        this.generateClouds = true;
        this.recentlyCompiledChunks.clear();
        ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
        this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
        }
        this.chunkRenderDispatcher.blockUntilClear();
        Set<BlockEntity> set = this.globalBlockEntities;
        synchronized (set) {
            this.globalBlockEntities.clear();
        }
        this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
        if (this.lastFullRenderChunkUpdate != null) {
            try {
                this.lastFullRenderChunkUpdate.get();
                this.lastFullRenderChunkUpdate = null;
            } catch (Exception exception) {
                LOGGER.warn("Full update failed", exception);
            }
        }
        this.renderChunkStorage.set(new RenderChunkStorage(this.viewArea.chunks.length));
        this.renderChunksInFrustum.clear();
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.viewArea.repositionCamera(entity.getX(), entity.getZ());
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

    public String getChunkStatistics() {
        int i = this.viewArea.chunks.length;
        int j = this.countRenderedChunks();
        return String.format("C: %d/%d %sD: %d, %s", j, i, this.minecraft.smartCull ? "(s) " : "", this.lastViewDistance, this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats());
    }

    public ChunkRenderDispatcher getChunkRenderDispatcher() {
        return this.chunkRenderDispatcher;
    }

    public double getTotalChunks() {
        return this.viewArea.chunks.length;
    }

    public double getLastViewDistance() {
        return this.lastViewDistance;
    }

    public int countRenderedChunks() {
        int i = 0;
        for (RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
            if (renderChunkInfo.chunk.getCompiledChunk().hasNoRenderableLayers()) continue;
            ++i;
        }
        return i;
    }

    public String getEntityStatistics() {
        return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities + ", SD: " + this.level.getServerSimulationDistance();
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
        if (this.lastCameraChunkX != i || this.lastCameraChunkY != j || this.lastCameraChunkZ != k) {
            this.lastCameraX = d;
            this.lastCameraY = e;
            this.lastCameraZ = f;
            this.lastCameraChunkX = i;
            this.lastCameraChunkY = j;
            this.lastCameraChunkZ = k;
            this.viewArea.repositionCamera(d, f);
        }
        this.chunkRenderDispatcher.setCamera(vec3);
        this.level.getProfiler().popPush("cull");
        this.minecraft.getProfiler().popPush("culling");
        BlockPos blockPos = camera.getBlockPosition();
        double g = Math.floor(vec3.x / 8.0);
        double h = Math.floor(vec3.y / 8.0);
        double l2 = Math.floor(vec3.z / 8.0);
        this.needsFullRenderChunkUpdate = this.needsFullRenderChunkUpdate || g != this.prevCamX || h != this.prevCamY || l2 != this.prevCamZ;
        this.nextFullUpdateMillis.updateAndGet(l -> {
            if (l > 0L && System.currentTimeMillis() > l) {
                this.needsFullRenderChunkUpdate = true;
                return 0L;
            }
            return l;
        });
        this.prevCamX = g;
        this.prevCamY = h;
        this.prevCamZ = l2;
        this.minecraft.getProfiler().popPush("update");
        boolean bl3 = this.minecraft.smartCull;
        if (bl2 && this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos)) {
            bl3 = false;
        }
        if (!bl) {
            if (this.needsFullRenderChunkUpdate && (this.lastFullRenderChunkUpdate == null || this.lastFullRenderChunkUpdate.isDone())) {
                this.minecraft.getProfiler().push("full_update_schedule");
                this.needsFullRenderChunkUpdate = false;
                boolean bl4 = bl3;
                this.lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> {
                    ArrayDeque<RenderChunkInfo> queue = Queues.newArrayDeque();
                    this.initializeQueueForFullUpdate(camera, queue);
                    RenderChunkStorage renderChunkStorage = new RenderChunkStorage(this.viewArea.chunks.length);
                    this.updateRenderChunks(renderChunkStorage.renderChunks, renderChunkStorage.renderInfoMap, vec3, queue, bl4);
                    this.renderChunkStorage.set(renderChunkStorage);
                    this.needsFrustumUpdate.set(true);
                });
                this.minecraft.getProfiler().pop();
            }
            RenderChunkStorage renderChunkStorage = this.renderChunkStorage.get();
            if (!this.recentlyCompiledChunks.isEmpty()) {
                this.minecraft.getProfiler().push("partial_update");
                ArrayDeque<RenderChunkInfo> queue = Queues.newArrayDeque();
                while (!this.recentlyCompiledChunks.isEmpty()) {
                    ChunkRenderDispatcher.RenderChunk renderChunk = (ChunkRenderDispatcher.RenderChunk)this.recentlyCompiledChunks.poll();
                    RenderChunkInfo renderChunkInfo = renderChunkStorage.renderInfoMap.get(renderChunk);
                    if (renderChunkInfo == null || renderChunkInfo.chunk != renderChunk) continue;
                    queue.add(renderChunkInfo);
                }
                this.updateRenderChunks(renderChunkStorage.renderChunks, renderChunkStorage.renderInfoMap, vec3, queue, bl3);
                this.needsFrustumUpdate.set(true);
                this.minecraft.getProfiler().pop();
            }
            double m = Math.floor(camera.getXRot() / 2.0f);
            double n = Math.floor(camera.getYRot() / 2.0f);
            if (this.needsFrustumUpdate.compareAndSet(true, false) || m != this.prevCamRotX || n != this.prevCamRotY) {
                this.applyFrustum(new Frustum(frustum).offsetToFullyIncludeCameraCube(8));
                this.prevCamRotX = m;
                this.prevCamRotY = n;
            }
        }
        this.minecraft.getProfiler().pop();
    }

    private void applyFrustum(Frustum frustum) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        }
        this.minecraft.getProfiler().push("apply_frustum");
        this.renderChunksInFrustum.clear();
        for (RenderChunkInfo renderChunkInfo : this.renderChunkStorage.get().renderChunks) {
            if (!frustum.isVisible(renderChunkInfo.chunk.getBoundingBox())) continue;
            this.renderChunksInFrustum.add(renderChunkInfo);
        }
        this.minecraft.getProfiler().pop();
    }

    private void initializeQueueForFullUpdate(Camera camera, Queue<RenderChunkInfo> queue) {
        int i = 16;
        Vec3 vec3 = camera.getPosition();
        BlockPos blockPos = camera.getBlockPosition();
        ChunkRenderDispatcher.RenderChunk renderChunk = this.viewArea.getRenderChunkAt(blockPos);
        if (renderChunk == null) {
            boolean bl = blockPos.getY() > this.level.getMinBuildHeight();
            int j = bl ? this.level.getMaxBuildHeight() - 8 : this.level.getMinBuildHeight() + 8;
            int k = Mth.floor(vec3.x / 16.0) * 16;
            int l = Mth.floor(vec3.z / 16.0) * 16;
            ArrayList<RenderChunkInfo> list = Lists.newArrayList();
            for (int m = -this.lastViewDistance; m <= this.lastViewDistance; ++m) {
                for (int n = -this.lastViewDistance; n <= this.lastViewDistance; ++n) {
                    ChunkRenderDispatcher.RenderChunk renderChunk2 = this.viewArea.getRenderChunkAt(new BlockPos(k + SectionPos.sectionToBlockCoord(m, 8), j, l + SectionPos.sectionToBlockCoord(n, 8)));
                    if (renderChunk2 == null) continue;
                    list.add(new RenderChunkInfo(renderChunk2, null, 0));
                }
            }
            list.sort(Comparator.comparingDouble(renderChunkInfo -> blockPos.distSqr(renderChunkInfo.chunk.getOrigin().offset(8, 8, 8))));
            queue.addAll(list);
        } else {
            queue.add(new RenderChunkInfo(renderChunk, null, 0));
        }
    }

    public void addRecentlyCompiledChunk(ChunkRenderDispatcher.RenderChunk renderChunk) {
        this.recentlyCompiledChunks.add(renderChunk);
    }

    private void updateRenderChunks(LinkedHashSet<RenderChunkInfo> linkedHashSet, RenderInfoMap renderInfoMap, Vec3 vec3, Queue<RenderChunkInfo> queue, boolean bl) {
        int i = 16;
        BlockPos blockPos = new BlockPos(Mth.floor(vec3.x / 16.0) * 16, Mth.floor(vec3.y / 16.0) * 16, Mth.floor(vec3.z / 16.0) * 16);
        BlockPos blockPos2 = blockPos.offset(8, 8, 8);
        Entity.setViewScale(Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get());
        while (!queue.isEmpty()) {
            RenderChunkInfo renderChunkInfo = queue.poll();
            ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
            linkedHashSet.add(renderChunkInfo);
            boolean bl2 = Math.abs(renderChunk.getOrigin().getX() - blockPos.getX()) > 60 || Math.abs(renderChunk.getOrigin().getY() - blockPos.getY()) > 60 || Math.abs(renderChunk.getOrigin().getZ() - blockPos.getZ()) > 60;
            for (Direction direction : DIRECTIONS) {
                RenderChunkInfo renderChunkInfo2;
                ChunkRenderDispatcher.RenderChunk renderChunk2 = this.getRelativeFrom(blockPos, renderChunk, direction);
                if (renderChunk2 == null || bl && renderChunkInfo.hasDirection(direction.getOpposite())) continue;
                if (bl && renderChunkInfo.hasSourceDirections()) {
                    ChunkRenderDispatcher.CompiledChunk compiledChunk = renderChunk.getCompiledChunk();
                    boolean bl3 = false;
                    for (int j = 0; j < DIRECTIONS.length; ++j) {
                        if (!renderChunkInfo.hasSourceDirection(j) || !compiledChunk.facesCanSeeEachother(DIRECTIONS[j].getOpposite(), direction)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (bl && bl2) {
                    BlockPos blockPos3 = renderChunk2.getOrigin();
                    BlockPos blockPos4 = blockPos3.offset((direction.getAxis() == Direction.Axis.X ? blockPos2.getX() > blockPos3.getX() : blockPos2.getX() < blockPos3.getX()) ? 16 : 0, (direction.getAxis() == Direction.Axis.Y ? blockPos2.getY() > blockPos3.getY() : blockPos2.getY() < blockPos3.getY()) ? 16 : 0, (direction.getAxis() == Direction.Axis.Z ? blockPos2.getZ() > blockPos3.getZ() : blockPos2.getZ() < blockPos3.getZ()) ? 16 : 0);
                    Vec3 vec32 = new Vec3(blockPos4.getX(), blockPos4.getY(), blockPos4.getZ());
                    Vec3 vec33 = vec3.subtract(vec32).normalize().scale(CEILED_SECTION_DIAGONAL);
                    boolean bl4 = true;
                    while (vec3.subtract(vec32).lengthSqr() > 3600.0) {
                        vec32 = vec32.add(vec33);
                        if (vec32.y > (double)this.level.getMaxBuildHeight() || vec32.y < (double)this.level.getMinBuildHeight()) break;
                        ChunkRenderDispatcher.RenderChunk renderChunk3 = this.viewArea.getRenderChunkAt(new BlockPos(vec32.x, vec32.y, vec32.z));
                        if (renderChunk3 != null && renderInfoMap.get(renderChunk3) != null) continue;
                        bl4 = false;
                        break;
                    }
                    if (!bl4) continue;
                }
                if ((renderChunkInfo2 = renderInfoMap.get(renderChunk2)) != null) {
                    renderChunkInfo2.addSourceDirection(direction);
                    continue;
                }
                if (!renderChunk2.hasAllNeighbors()) {
                    if (this.closeToBorder(blockPos, renderChunk)) continue;
                    this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                    continue;
                }
                RenderChunkInfo renderChunkInfo3 = new RenderChunkInfo(renderChunk2, direction, renderChunkInfo.step + 1);
                renderChunkInfo3.setDirections(renderChunkInfo.directions, direction);
                queue.add(renderChunkInfo3);
                renderInfoMap.put(renderChunk2, renderChunkInfo3);
            }
        }
    }

    @Nullable
    private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos blockPos, ChunkRenderDispatcher.RenderChunk renderChunk, Direction direction) {
        BlockPos blockPos2 = renderChunk.getRelativeOrigin(direction);
        if (Mth.abs(blockPos.getX() - blockPos2.getX()) > this.lastViewDistance * 16) {
            return null;
        }
        if (Mth.abs(blockPos.getY() - blockPos2.getY()) > this.lastViewDistance * 16 || blockPos2.getY() < this.level.getMinBuildHeight() || blockPos2.getY() >= this.level.getMaxBuildHeight()) {
            return null;
        }
        if (Mth.abs(blockPos.getZ() - blockPos2.getZ()) > this.lastViewDistance * 16) {
            return null;
        }
        return this.viewArea.getRenderChunkAt(blockPos2);
    }

    private boolean closeToBorder(BlockPos blockPos, ChunkRenderDispatcher.RenderChunk renderChunk) {
        int l;
        int i = SectionPos.blockToSectionCoord(blockPos.getX());
        int j = SectionPos.blockToSectionCoord(blockPos.getZ());
        BlockPos blockPos2 = renderChunk.getOrigin();
        int k = SectionPos.blockToSectionCoord(blockPos2.getX());
        return !ChunkMap.isChunkInRange(k, l = SectionPos.blockToSectionCoord(blockPos2.getZ()), i, j, this.lastViewDistance - 2);
    }

    private void captureFrustum(Matrix4f matrix4f, Matrix4f matrix4f2, double d, double e, double f, Frustum frustum) {
        this.capturedFrustum = frustum;
        Matrix4f matrix4f3 = matrix4f2.copy();
        matrix4f3.multiply(matrix4f);
        matrix4f3.invert();
        this.frustumPos.x = d;
        this.frustumPos.y = e;
        this.frustumPos.z = f;
        this.frustumPoints[0] = new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f);
        this.frustumPoints[1] = new Vector4f(1.0f, -1.0f, -1.0f, 1.0f);
        this.frustumPoints[2] = new Vector4f(1.0f, 1.0f, -1.0f, 1.0f);
        this.frustumPoints[3] = new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f);
        this.frustumPoints[4] = new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f);
        this.frustumPoints[5] = new Vector4f(1.0f, -1.0f, 1.0f, 1.0f);
        this.frustumPoints[6] = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.frustumPoints[7] = new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f);
        for (int i = 0; i < 8; ++i) {
            this.frustumPoints[i].transform(matrix4f3);
            this.frustumPoints[i].perspectiveDivide();
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderLevel(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f) {
        int m;
        BlockPos blockPos;
        Frustum frustum;
        boolean bl3;
        RenderSystem.setShaderGameTime(this.level.getGameTime(), f);
        this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
        ProfilerFiller profilerFiller = this.level.getProfiler();
        profilerFiller.popPush("light_update_queue");
        this.level.pollLightUpdates();
        profilerFiller.popPush("light_updates");
        boolean bl2 = this.level.isLightUpdateQueueEmpty();
        this.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, bl2, true);
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double e = vec3.y();
        double g = vec3.z();
        Matrix4f matrix4f2 = poseStack.last().pose();
        profilerFiller.popPush("culling");
        boolean bl4 = bl3 = this.capturedFrustum != null;
        if (bl3) {
            frustum = this.capturedFrustum;
            frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
        } else {
            frustum = this.cullingFrustum;
        }
        this.minecraft.getProfiler().popPush("captureFrustum");
        if (this.captureFrustum) {
            this.captureFrustum(matrix4f2, matrix4f, vec3.x, vec3.y, vec3.z, bl3 ? new Frustum(matrix4f2, matrix4f) : frustum);
            this.captureFrustum = false;
        }
        profilerFiller.popPush("clear");
        FogRenderer.setupColor(camera, f, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), gameRenderer.getDarkenWorldAmount(f));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float h = gameRenderer.getRenderDistance();
        boolean bl42 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d), Mth.floor(e)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        profilerFiller.popPush("sky");
        RenderSystem.setShader(GameRenderer::getPositionShader);
        this.renderSky(poseStack, matrix4f, f, camera, bl42, () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, h, bl42, f));
        profilerFiller.popPush("fog");
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(h, 32.0f), bl42, f);
        profilerFiller.popPush("terrain_setup");
        this.setupRender(camera, frustum, bl3, this.minecraft.player.isSpectator());
        profilerFiller.popPush("compilechunks");
        this.compileChunks(camera);
        profilerFiller.popPush("terrain");
        this.renderChunkLayer(RenderType.solid(), poseStack, d, e, g, matrix4f);
        this.renderChunkLayer(RenderType.cutoutMipped(), poseStack, d, e, g, matrix4f);
        this.renderChunkLayer(RenderType.cutout(), poseStack, d, e, g, matrix4f);
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
        boolean bl5 = false;
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        for (Entity entity : this.level.entitiesForRendering()) {
            Object multiBufferSource;
            if (!this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, g) && !entity.hasIndirectPassenger(this.minecraft.player) || !this.level.isOutsideBuildHeight((blockPos = entity.blockPosition()).getY()) && !this.isChunkCompiled(blockPos) || entity == camera.getEntity() && !camera.isDetached() && (!(camera.getEntity() instanceof LivingEntity) || !((LivingEntity)camera.getEntity()).isSleeping()) || entity instanceof LocalPlayer && camera.getEntity() != entity) continue;
            ++this.renderedEntities;
            if (entity.tickCount == 0) {
                entity.xOld = entity.getX();
                entity.yOld = entity.getY();
                entity.zOld = entity.getZ();
            }
            if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
                bl5 = true;
                OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
                multiBufferSource = outlineBufferSource;
                int i = entity.getTeamColor();
                int j = 255;
                int k = i >> 16 & 0xFF;
                m = i >> 8 & 0xFF;
                int n = i & 0xFF;
                outlineBufferSource.setColor(k, m, n, 255);
            } else {
                multiBufferSource = bufferSource;
            }
            this.renderEntity(entity, d, e, g, f, poseStack, (MultiBufferSource)multiBufferSource);
        }
        bufferSource.endLastBatch();
        this.checkPoseStack(poseStack);
        bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
        profilerFiller.popPush("blockentities");
        for (RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
            List<BlockEntity> list = renderChunkInfo.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (list.isEmpty()) continue;
            for (BlockEntity blockEntity : list) {
                BlockPos blockPos2 = blockEntity.getBlockPos();
                MultiBufferSource multiBufferSource2 = bufferSource;
                poseStack.pushPose();
                poseStack.translate((double)blockPos2.getX() - d, (double)blockPos2.getY() - e, (double)blockPos2.getZ() - g);
                SortedSet sortedSet = (SortedSet)this.destructionProgress.get(blockPos2.asLong());
                if (sortedSet != null && !sortedSet.isEmpty() && (m = ((BlockDestructionProgress)sortedSet.last()).getProgress()) >= 0) {
                    PoseStack.Pose pose = poseStack.last();
                    SheetedDecalTextureGenerator vertexConsumer = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(m)), pose.pose(), pose.normal());
                    multiBufferSource2 = renderType -> {
                        VertexConsumer vertexConsumer2 = bufferSource.getBuffer(renderType);
                        if (renderType.affectsCrumbling()) {
                            return VertexMultiConsumer.create(vertexConsumer, vertexConsumer2);
                        }
                        return vertexConsumer2;
                    };
                }
                this.blockEntityRenderDispatcher.render(blockEntity, f, poseStack, multiBufferSource2);
                poseStack.popPose();
            }
        }
        Set<BlockEntity> set = this.globalBlockEntities;
        synchronized (set) {
            for (BlockEntity blockEntity2 : this.globalBlockEntities) {
                BlockPos blockPos3 = blockEntity2.getBlockPos();
                poseStack.pushPose();
                poseStack.translate((double)blockPos3.getX() - d, (double)blockPos3.getY() - e, (double)blockPos3.getZ() - g);
                this.blockEntityRenderDispatcher.render(blockEntity2, f, poseStack, bufferSource);
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
        bufferSource.endBatch(Sheets.chestSheet());
        this.renderBuffers.outlineBufferSource().endOutlineBatch();
        if (bl5) {
            this.entityEffect.process(f);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        profilerFiller.popPush("destroyProgress");
        for (Long2ObjectMap.Entry entry : this.destructionProgress.long2ObjectEntrySet()) {
            SortedSet sortedSet2;
            double q;
            double p;
            blockPos = BlockPos.of(entry.getLongKey());
            double o = (double)blockPos.getX() - d;
            if (o * o + (p = (double)blockPos.getY() - e) * p + (q = (double)blockPos.getZ() - g) * q > 1024.0 || (sortedSet2 = (SortedSet)entry.getValue()) == null || sortedSet2.isEmpty()) continue;
            int r = ((BlockDestructionProgress)sortedSet2.last()).getProgress();
            poseStack.pushPose();
            poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - g);
            PoseStack.Pose pose2 = poseStack.last();
            SheetedDecalTextureGenerator vertexConsumer2 = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(r)), pose2.pose(), pose2.normal());
            this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos), blockPos, this.level, poseStack, vertexConsumer2);
            poseStack.popPose();
        }
        this.checkPoseStack(poseStack);
        HitResult hitResult = this.minecraft.hitResult;
        if (bl && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            profilerFiller.popPush("outline");
            BlockPos blockPos2 = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = this.level.getBlockState(blockPos2);
            if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos2)) {
                VertexConsumer vertexConsumer3 = bufferSource.getBuffer(RenderType.lines());
                this.renderHitOutline(poseStack, vertexConsumer3, camera.getEntity(), d, e, g, blockPos2, blockState);
            }
        }
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.pushPose();
        poseStack2.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        this.minecraft.debugRenderer.render(poseStack, bufferSource, d, e, g);
        poseStack2.popPose();
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
            this.renderChunkLayer(RenderType.translucent(), poseStack, d, e, g, matrix4f);
            profilerFiller.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), poseStack, d, e, g, matrix4f);
            this.particlesTarget.clear(Minecraft.ON_OSX);
            this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
            profilerFiller.popPush("particles");
            this.minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, f);
            RenderStateShard.PARTICLES_TARGET.clearRenderState();
        } else {
            profilerFiller.popPush("translucent");
            if (this.translucentTarget != null) {
                this.translucentTarget.clear(Minecraft.ON_OSX);
            }
            this.renderChunkLayer(RenderType.translucent(), poseStack, d, e, g, matrix4f);
            bufferSource.endBatch(RenderType.lines());
            bufferSource.endBatch();
            profilerFiller.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), poseStack, d, e, g, matrix4f);
            profilerFiller.popPush("particles");
            this.minecraft.particleEngine.render(poseStack, bufferSource, lightTexture, camera, f);
        }
        poseStack2.pushPose();
        poseStack2.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            if (this.transparencyChain != null) {
                this.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderStateShard.CLOUDS_TARGET.setupRenderState();
                profilerFiller.popPush("clouds");
                this.renderClouds(poseStack, matrix4f, f, d, e, g);
                RenderStateShard.CLOUDS_TARGET.clearRenderState();
            } else {
                profilerFiller.popPush("clouds");
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                this.renderClouds(poseStack, matrix4f, f, d, e, g);
            }
        }
        if (this.transparencyChain != null) {
            RenderStateShard.WEATHER_TARGET.setupRenderState();
            profilerFiller.popPush("weather");
            this.renderSnowAndRain(lightTexture, f, d, e, g);
            this.renderWorldBorder(camera);
            RenderStateShard.WEATHER_TARGET.clearRenderState();
            this.transparencyChain.process(f);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        } else {
            RenderSystem.depthMask(false);
            profilerFiller.popPush("weather");
            this.renderSnowAndRain(lightTexture, f, d, e, g);
            this.renderWorldBorder(camera);
            RenderSystem.depthMask(true);
        }
        this.renderDebug(camera);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        poseStack2.popPose();
        RenderSystem.applyModelViewMatrix();
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
        this.entityRenderDispatcher.render(entity, h - d, i - e, j - f, k, g, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, g));
    }

    private void renderChunkLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f matrix4f) {
        RenderSystem.assertOnRenderThread();
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
                for (RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
                    if (j >= 15 || !renderChunkInfo.chunk.resortTransparency(renderType, this.chunkRenderDispatcher)) continue;
                    ++j;
                }
            }
            this.minecraft.getProfiler().pop();
        }
        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() -> "render_" + renderType);
        boolean bl = renderType != RenderType.translucent();
        ListIterator objectListIterator = this.renderChunksInFrustum.listIterator(bl ? 0 : this.renderChunksInFrustum.size());
        ShaderInstance shaderInstance = RenderSystem.getShader();
        for (int k = 0; k < 12; ++k) {
            int l = RenderSystem.getShaderTexture(k);
            shaderInstance.setSampler("Sampler" + k, l);
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
        while (bl ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
            RenderChunkInfo renderChunkInfo2 = bl ? (RenderChunkInfo)objectListIterator.next() : (RenderChunkInfo)objectListIterator.previous();
            ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo2.chunk;
            if (renderChunk.getCompiledChunk().isEmpty(renderType)) continue;
            VertexBuffer vertexBuffer = renderChunk.getBuffer(renderType);
            BlockPos blockPos = renderChunk.getOrigin();
            if (uniform != null) {
                uniform.set((float)((double)blockPos.getX() - d), (float)((double)blockPos.getY() - e), (float)((double)blockPos.getZ() - f));
                uniform.upload();
            }
            vertexBuffer.bind();
            vertexBuffer.draw();
        }
        if (uniform != null) {
            uniform.set(Vector3f.ZERO);
        }
        shaderInstance.clear();
        VertexBuffer.unbind();
        this.minecraft.getProfiler().pop();
        renderType.clearRenderState();
    }

    private void renderDebug(Camera camera) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (this.minecraft.chunkPath || this.minecraft.chunkVisibility) {
            double d = camera.getPosition().x();
            double e = camera.getPosition().y();
            double f = camera.getPosition().z();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            for (RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
                int i;
                ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
                BlockPos blockPos = renderChunk.getOrigin();
                PoseStack poseStack = RenderSystem.getModelViewStack();
                poseStack.pushPose();
                poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                if (this.minecraft.chunkPath) {
                    bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                    RenderSystem.lineWidth(5.0f);
                    i = renderChunkInfo.step == 0 ? 0 : Mth.hsvToRgb((float)renderChunkInfo.step / 50.0f, 0.9f, 0.9f);
                    int j = i >> 16 & 0xFF;
                    int k = i >> 8 & 0xFF;
                    int l = i & 0xFF;
                    for (int m = 0; m < DIRECTIONS.length; ++m) {
                        if (!renderChunkInfo.hasSourceDirection(m)) continue;
                        Direction direction = DIRECTIONS[m];
                        bufferBuilder.vertex(8.0, 8.0, 8.0).color(j, k, l, 255).normal(direction.getStepX(), direction.getStepY(), direction.getStepZ()).endVertex();
                        bufferBuilder.vertex(8 - 16 * direction.getStepX(), 8 - 16 * direction.getStepY(), 8 - 16 * direction.getStepZ()).color(j, k, l, 255).normal(direction.getStepX(), direction.getStepY(), direction.getStepZ()).endVertex();
                    }
                    tesselator.end();
                    RenderSystem.lineWidth(1.0f);
                }
                if (this.minecraft.chunkVisibility && !renderChunk.getCompiledChunk().hasNoRenderableLayers()) {
                    bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                    RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                    RenderSystem.lineWidth(5.0f);
                    i = 0;
                    for (Direction direction2 : DIRECTIONS) {
                        for (Direction direction3 : DIRECTIONS) {
                            boolean bl = renderChunk.getCompiledChunk().facesCanSeeEachother(direction2, direction3);
                            if (bl) continue;
                            ++i;
                            bufferBuilder.vertex(8 + 8 * direction2.getStepX(), 8 + 8 * direction2.getStepY(), 8 + 8 * direction2.getStepZ()).color(255, 0, 0, 255).normal(direction2.getStepX(), direction2.getStepY(), direction2.getStepZ()).endVertex();
                            bufferBuilder.vertex(8 + 8 * direction3.getStepX(), 8 + 8 * direction3.getStepY(), 8 + 8 * direction3.getStepZ()).color(255, 0, 0, 255).normal(direction3.getStepX(), direction3.getStepY(), direction3.getStepZ()).endVertex();
                        }
                    }
                    tesselator.end();
                    RenderSystem.lineWidth(1.0f);
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    if (i > 0) {
                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                        float g = 0.5f;
                        float h = 0.2f;
                        bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 0.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 15.5, 0.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 15.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(15.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        bufferBuilder.vertex(0.5, 0.5, 15.5).color(0.9f, 0.9f, 0.0f, 0.2f).endVertex();
                        tesselator.end();
                    }
                }
                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();
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
            RenderSystem.lineWidth(5.0f);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.pushPose();
            poseStack2.translate((float)(this.frustumPos.x - camera.getPosition().x), (float)(this.frustumPos.y - camera.getPosition().y), (float)(this.frustumPos.z - camera.getPosition().z));
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            this.addFrustumQuad(bufferBuilder, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(bufferBuilder, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(bufferBuilder, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(bufferBuilder, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(bufferBuilder, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(bufferBuilder, 1, 5, 6, 2, 1, 0, 1);
            tesselator.end();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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
            poseStack2.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.lineWidth(1.0f);
        }
    }

    private void addFrustumVertex(VertexConsumer vertexConsumer, int i) {
        vertexConsumer.vertex(this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z()).color(0, 0, 0, 255).normal(0.0f, 0.0f, -1.0f).endVertex();
    }

    private void addFrustumQuad(VertexConsumer vertexConsumer, int i, int j, int k, int l, int m, int n, int o) {
        float f = 0.25f;
        vertexConsumer.vertex(this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z()).color((float)m, (float)n, (float)o, 0.25f).endVertex();
        vertexConsumer.vertex(this.frustumPoints[j].x(), this.frustumPoints[j].y(), this.frustumPoints[j].z()).color((float)m, (float)n, (float)o, 0.25f).endVertex();
        vertexConsumer.vertex(this.frustumPoints[k].x(), this.frustumPoints[k].y(), this.frustumPoints[k].z()).color((float)m, (float)n, (float)o, 0.25f).endVertex();
        vertexConsumer.vertex(this.frustumPoints[l].x(), this.frustumPoints[l].y(), this.frustumPoints[l].z()).color((float)m, (float)n, (float)o, 0.25f).endVertex();
    }

    public void captureFrustum() {
        this.captureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick() {
        ++this.ticks;
        if (this.ticks % 20 != 0) {
            return;
        }
        Iterator iterator = this.destroyingBlocks.values().iterator();
        while (iterator.hasNext()) {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)iterator.next();
            int i = blockDestructionProgress.getUpdatedRenderTick();
            if (this.ticks - i <= 400) continue;
            iterator.remove();
            this.removeProgress(blockDestructionProgress);
        }
    }

    private void removeProgress(BlockDestructionProgress blockDestructionProgress) {
        long l = blockDestructionProgress.getPos().asLong();
        Set set = (Set)this.destructionProgress.get(l);
        set.remove(blockDestructionProgress);
        if (set.isEmpty()) {
            this.destructionProgress.remove(l);
        }
    }

    private void renderEndSky(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        for (int i = 0; i < 6; ++i) {
            poseStack.pushPose();
            if (i == 1) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
            }
            if (i == 2) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0f));
            }
            if (i == 3) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
            }
            if (i == 4) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
            }
            if (i == 5) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0f));
            }
            Matrix4f matrix4f = poseStack.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).uv(0.0f, 0.0f).color(40, 40, 40, 255).endVertex();
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).uv(0.0f, 16.0f).color(40, 40, 40, 255).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).uv(16.0f, 16.0f).color(40, 40, 40, 255).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).uv(16.0f, 0.0f).color(40, 40, 40, 255).endVertex();
            tesselator.end();
            poseStack.popPose();
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderSky(PoseStack poseStack, Matrix4f matrix4f, float f, Camera camera, boolean bl, Runnable runnable) {
        float r;
        float q;
        float p;
        int n;
        float l;
        float j;
        LivingEntity livingEntity;
        Entity entity;
        runnable.run();
        if (bl) {
            return;
        }
        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA || (entity = camera.getEntity()) instanceof LivingEntity && (livingEntity = (LivingEntity)entity).hasEffect(MobEffects.BLINDNESS)) {
            return;
        }
        if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
            this.renderEndSky(poseStack);
            return;
        }
        if (this.minecraft.level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) {
            return;
        }
        RenderSystem.disableTexture();
        Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), f);
        float g = (float)vec3.x;
        float h = (float)vec3.y;
        float i = (float)vec3.z;
        FogRenderer.levelFogColor();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(g, h, i, 1.0f);
        ShaderInstance shaderInstance = RenderSystem.getShader();
        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
        VertexBuffer.unbind();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float[] fs = this.level.effects().getSunriseColor(this.level.getTimeOfDay(f), f);
        if (fs != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
            j = Mth.sin(this.level.getSunAngle(f)) < 0.0f ? 180.0f : 0.0f;
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(j));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
            float k = fs[0];
            l = fs[1];
            float m = fs[2];
            Matrix4f matrix4f2 = poseStack.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f2, 0.0f, 100.0f, 0.0f).color(k, l, m, fs[3]).endVertex();
            n = 16;
            for (int o = 0; o <= 16; ++o) {
                p = (float)o * ((float)Math.PI * 2) / 16.0f;
                q = Mth.sin(p);
                r = Mth.cos(p);
                bufferBuilder.vertex(matrix4f2, q * 120.0f, r * 120.0f, -r * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f).endVertex();
            }
            bufferBuilder.end();
            BufferUploader.drawWithShader(bufferBuilder);
            poseStack.popPose();
        }
        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        poseStack.pushPose();
        j = 1.0f - this.level.getRainLevel(f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, j);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(this.level.getTimeOfDay(f) * 360.0f));
        Matrix4f matrix4f3 = poseStack.last().pose();
        l = 30.0f;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_LOCATION);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f3, -l, 100.0f, -l).uv(0.0f, 0.0f).endVertex();
        bufferBuilder.vertex(matrix4f3, l, 100.0f, -l).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(matrix4f3, l, 100.0f, l).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(matrix4f3, -l, 100.0f, l).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.end();
        BufferUploader.drawWithShader(bufferBuilder);
        l = 20.0f;
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        int s = this.level.getMoonPhase();
        int t = s % 4;
        n = s / 4 % 2;
        float u = (float)(t + 0) / 4.0f;
        p = (float)(n + 0) / 2.0f;
        q = (float)(t + 1) / 4.0f;
        r = (float)(n + 1) / 2.0f;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f3, -l, -100.0f, l).uv(q, r).endVertex();
        bufferBuilder.vertex(matrix4f3, l, -100.0f, l).uv(u, r).endVertex();
        bufferBuilder.vertex(matrix4f3, l, -100.0f, -l).uv(u, p).endVertex();
        bufferBuilder.vertex(matrix4f3, -l, -100.0f, -l).uv(q, p).endVertex();
        bufferBuilder.end();
        BufferUploader.drawWithShader(bufferBuilder);
        RenderSystem.disableTexture();
        float v = this.level.getStarBrightness(f) * j;
        if (v > 0.0f) {
            RenderSystem.setShaderColor(v, v, v, v);
            FogRenderer.setupNoFog();
            this.starBuffer.bind();
            this.starBuffer.drawWithShader(poseStack.last().pose(), matrix4f, GameRenderer.getPositionShader());
            VertexBuffer.unbind();
            runnable.run();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        double d = this.minecraft.player.getEyePosition((float)f).y - this.level.getLevelData().getHorizonHeight(this.level);
        if (d < 0.0) {
            poseStack.pushPose();
            poseStack.translate(0.0, 12.0, 0.0);
            this.darkBuffer.bind();
            this.darkBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
            VertexBuffer.unbind();
            poseStack.popPose();
        }
        if (this.level.effects().hasGround()) {
            RenderSystem.setShaderColor(g * 0.2f + 0.04f, h * 0.2f + 0.04f, i * 0.6f + 0.1f, 1.0f);
        } else {
            RenderSystem.setShaderColor(g, h, i, 1.0f);
        }
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
    }

    public void renderClouds(PoseStack poseStack, Matrix4f matrix4f, float f, double d, double e, double g) {
        float h = this.level.effects().getCloudHeight();
        if (Float.isNaN(h)) {
            return;
        }
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);
        float i = 12.0f;
        float j = 4.0f;
        double k = 2.0E-4;
        double l = ((float)this.ticks + f) * 0.03f;
        double m = (d + l) / 12.0;
        double n = h - (float)e + 0.33f;
        double o = g / 12.0 + (double)0.33f;
        m -= (double)(Mth.floor(m / 2048.0) * 2048);
        o -= (double)(Mth.floor(o / 2048.0) * 2048);
        float p = (float)(m - (double)Mth.floor(m));
        float q = (float)(n / 4.0 - (double)Mth.floor(n / 4.0)) * 4.0f;
        float r = (float)(o - (double)Mth.floor(o));
        Vec3 vec3 = this.level.getCloudColor(f);
        int s = (int)Math.floor(m);
        int t = (int)Math.floor(n / 4.0);
        int u = (int)Math.floor(o);
        if (s != this.prevCloudX || t != this.prevCloudY || u != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4) {
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
            this.cloudBuffer = new VertexBuffer();
            this.buildClouds(bufferBuilder, m, n, o, vec3);
            bufferBuilder.end();
            this.cloudBuffer.bind();
            this.cloudBuffer.upload(bufferBuilder);
            VertexBuffer.unbind();
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
        FogRenderer.levelFogColor();
        poseStack.pushPose();
        poseStack.scale(12.0f, 1.0f, 12.0f);
        poseStack.translate(-p, q, -r);
        if (this.cloudBuffer != null) {
            int v;
            this.cloudBuffer.bind();
            for (int w = v = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1; w < 2; ++w) {
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
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void buildClouds(BufferBuilder bufferBuilder, double d, double e, double f, Vec3 vec3) {
        float g = 4.0f;
        float h = 0.00390625f;
        int i = 8;
        int j = 4;
        float k = 9.765625E-4f;
        float l = (float)Mth.floor(d) * 0.00390625f;
        float m = (float)Mth.floor(f) * 0.00390625f;
        float n = (float)vec3.x;
        float o = (float)vec3.y;
        float p = (float)vec3.z;
        float q = n * 0.9f;
        float r = o * 0.9f;
        float s = p * 0.9f;
        float t = n * 0.7f;
        float u = o * 0.7f;
        float v = p * 0.7f;
        float w = n * 0.8f;
        float x = o * 0.8f;
        float y = p * 0.8f;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float z = (float)Math.floor(e / 4.0) * 4.0f;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            for (int aa = -3; aa <= 4; ++aa) {
                for (int ab = -3; ab <= 4; ++ab) {
                    int ae;
                    float ac = aa * 8;
                    float ad = ab * 8;
                    if (z > -5.0f) {
                        bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + 8.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + 8.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(t, u, v, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    }
                    if (z <= 5.0f) {
                        bufferBuilder.vertex(ac + 0.0f, z + 4.0f - 9.765625E-4f, ad + 8.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(ac + 8.0f, z + 4.0f - 9.765625E-4f, ad + 8.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(ac + 8.0f, z + 4.0f - 9.765625E-4f, ad + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                        bufferBuilder.vertex(ac + 0.0f, z + 4.0f - 9.765625E-4f, ad + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
                    }
                    if (aa > -1) {
                        for (ae = 0; ae < 8; ++ae) {
                            bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 0.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 4.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 4.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 0.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
                        }
                    }
                    if (aa <= 1) {
                        for (ae = 0; ae < 8; ++ae) {
                            bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 0.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 4.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 4.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                            bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 0.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(q, r, s, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
                        }
                    }
                    if (ab > -1) {
                        for (ae = 0; ae < 8; ++ae) {
                            bufferBuilder.vertex(ac + 0.0f, z + 4.0f, ad + (float)ae + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                            bufferBuilder.vertex(ac + 8.0f, z + 4.0f, ad + (float)ae + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                            bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + (float)ae + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                            bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + (float)ae + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
                        }
                    }
                    if (ab > 1) continue;
                    for (ae = 0; ae < 8; ++ae) {
                        bufferBuilder.vertex(ac + 0.0f, z + 4.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                        bufferBuilder.vertex(ac + 8.0f, z + 4.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                        bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                        bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(w, x, y, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
                    }
                }
            }
        } else {
            boolean aa = true;
            int ab = 32;
            for (int af = -32; af < 32; af += 32) {
                for (int ag = -32; ag < 32; ag += 32) {
                    bufferBuilder.vertex(af + 0, z, ag + 32).uv((float)(af + 0) * 0.00390625f + l, (float)(ag + 32) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    bufferBuilder.vertex(af + 32, z, ag + 32).uv((float)(af + 32) * 0.00390625f + l, (float)(ag + 32) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    bufferBuilder.vertex(af + 32, z, ag + 0).uv((float)(af + 32) * 0.00390625f + l, (float)(ag + 0) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                    bufferBuilder.vertex(af + 0, z, ag + 0).uv((float)(af + 0) * 0.00390625f + l, (float)(ag + 0) * 0.00390625f + m).color(n, o, p, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
                }
            }
        }
    }

    private void compileChunks(Camera camera) {
        this.minecraft.getProfiler().push("populate_chunks_to_compile");
        RenderRegionCache renderRegionCache = new RenderRegionCache();
        BlockPos blockPos = camera.getBlockPosition();
        ArrayList<ChunkRenderDispatcher.RenderChunk> list = Lists.newArrayList();
        for (RenderChunkInfo renderChunkInfo : this.renderChunksInFrustum) {
            ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
            ChunkPos chunkPos = new ChunkPos(renderChunk.getOrigin());
            if (!renderChunk.isDirty() || !this.level.getChunk(chunkPos.x, chunkPos.z).isClientLightReady()) continue;
            boolean bl = false;
            if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                BlockPos blockPos2 = renderChunk.getOrigin().offset(8, 8, 8);
                bl = blockPos2.distSqr(blockPos) < 768.0 || renderChunk.isDirtyFromPlayer();
            } else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                bl = renderChunk.isDirtyFromPlayer();
            }
            if (bl) {
                this.minecraft.getProfiler().push("build_near_sync");
                this.chunkRenderDispatcher.rebuildChunkSync(renderChunk, renderRegionCache);
                renderChunk.setNotDirty();
                this.minecraft.getProfiler().pop();
                continue;
            }
            list.add(renderChunk);
        }
        this.minecraft.getProfiler().popPush("upload");
        this.chunkRenderDispatcher.uploadAllPendingUploads();
        this.minecraft.getProfiler().popPush("schedule_async_compile");
        for (ChunkRenderDispatcher.RenderChunk renderChunk2 : list) {
            renderChunk2.rebuildChunkAsync(this.chunkRenderDispatcher, renderRegionCache);
            renderChunk2.setNotDirty();
        }
        this.minecraft.getProfiler().pop();
    }

    private void renderWorldBorder(Camera camera) {
        float v;
        double u;
        double t;
        float s;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        WorldBorder worldBorder = this.level.getWorldBorder();
        double d = this.minecraft.options.getEffectiveRenderDistance() * 16;
        if (camera.getPosition().x < worldBorder.getMaxX() - d && camera.getPosition().x > worldBorder.getMinX() + d && camera.getPosition().z < worldBorder.getMaxZ() - d && camera.getPosition().z > worldBorder.getMinZ() + d) {
            return;
        }
        double e = 1.0 - worldBorder.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d;
        e = Math.pow(e, 4.0);
        e = Mth.clamp(e, 0.0, 1.0);
        double f = camera.getPosition().x;
        double g = camera.getPosition().z;
        double h = this.minecraft.gameRenderer.getDepthFar();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        RenderSystem.applyModelViewMatrix();
        int i = worldBorder.getStatus().getColor();
        float j = (float)(i >> 16 & 0xFF) / 255.0f;
        float k = (float)(i >> 8 & 0xFF) / 255.0f;
        float l = (float)(i & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(j, k, l, (float)e);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.polygonOffset(-3.0f, -3.0f);
        RenderSystem.enablePolygonOffset();
        RenderSystem.disableCull();
        float m = (float)(Util.getMillis() % 3000L) / 3000.0f;
        float n = 0.0f;
        float o = 0.0f;
        float p = (float)(h - Mth.frac(camera.getPosition().y));
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        double q = Math.max((double)Mth.floor(g - d), worldBorder.getMinZ());
        double r = Math.min((double)Mth.ceil(g + d), worldBorder.getMaxZ());
        if (f > worldBorder.getMaxX() - d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(worldBorder.getMaxX() - f, -h, t - g).uv(m - s, m + p).endVertex();
                bufferBuilder.vertex(worldBorder.getMaxX() - f, -h, t + u - g).uv(m - (v + s), m + p).endVertex();
                bufferBuilder.vertex(worldBorder.getMaxX() - f, h, t + u - g).uv(m - (v + s), m + 0.0f).endVertex();
                bufferBuilder.vertex(worldBorder.getMaxX() - f, h, t - g).uv(m - s, m + 0.0f).endVertex();
                t += 1.0;
                s += 0.5f;
            }
        }
        if (f < worldBorder.getMinX() + d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(worldBorder.getMinX() - f, -h, t - g).uv(m + s, m + p).endVertex();
                bufferBuilder.vertex(worldBorder.getMinX() - f, -h, t + u - g).uv(m + v + s, m + p).endVertex();
                bufferBuilder.vertex(worldBorder.getMinX() - f, h, t + u - g).uv(m + v + s, m + 0.0f).endVertex();
                bufferBuilder.vertex(worldBorder.getMinX() - f, h, t - g).uv(m + s, m + 0.0f).endVertex();
                t += 1.0;
                s += 0.5f;
            }
        }
        q = Math.max((double)Mth.floor(f - d), worldBorder.getMinX());
        r = Math.min((double)Mth.ceil(f + d), worldBorder.getMaxX());
        if (g > worldBorder.getMaxZ() - d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(t - f, -h, worldBorder.getMaxZ() - g).uv(m + s, m + p).endVertex();
                bufferBuilder.vertex(t + u - f, -h, worldBorder.getMaxZ() - g).uv(m + v + s, m + p).endVertex();
                bufferBuilder.vertex(t + u - f, h, worldBorder.getMaxZ() - g).uv(m + v + s, m + 0.0f).endVertex();
                bufferBuilder.vertex(t - f, h, worldBorder.getMaxZ() - g).uv(m + s, m + 0.0f).endVertex();
                t += 1.0;
                s += 0.5f;
            }
        }
        if (g < worldBorder.getMinZ() + d) {
            s = 0.0f;
            t = q;
            while (t < r) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5f;
                bufferBuilder.vertex(t - f, -h, worldBorder.getMinZ() - g).uv(m - s, m + p).endVertex();
                bufferBuilder.vertex(t + u - f, -h, worldBorder.getMinZ() - g).uv(m - (v + s), m + p).endVertex();
                bufferBuilder.vertex(t + u - f, h, worldBorder.getMinZ() - g).uv(m - (v + s), m + 0.0f).endVertex();
                bufferBuilder.vertex(t - f, h, worldBorder.getMinZ() - g).uv(m - s, m + 0.0f).endVertex();
                t += 1.0;
                s += 0.5f;
            }
        }
        bufferBuilder.end();
        BufferUploader.drawWithShader(bufferBuilder);
        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
    }

    private void renderHitOutline(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
        LevelRenderer.renderShape(poseStack, vertexConsumer, blockState.getShape(this.level, blockPos, CollisionContext.of(entity)), (double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f, 0.0f, 0.0f, 0.0f, 0.4f);
    }

    public static void renderVoxelShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        List<AABB> list = voxelShape.toAabbs();
        int k = Mth.ceil((double)list.size() / 3.0);
        for (int l = 0; l < list.size(); ++l) {
            AABB aABB = list.get(l);
            float m = ((float)l % (float)k + 1.0f) / (float)k;
            float n = l / k;
            float o = m * (float)(n == 0.0f ? 1 : 0);
            float p = m * (float)(n == 1.0f ? 1 : 0);
            float q = m * (float)(n == 2.0f ? 1 : 0);
            LevelRenderer.renderShape(poseStack, vertexConsumer, Shapes.create(aABB.move(0.0, 0.0, 0.0)), d, e, f, o, p, q, 1.0f);
        }
    }

    private static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        PoseStack.Pose pose = poseStack.last();
        voxelShape.forAllEdges((k, l, m, n, o, p) -> {
            float q = (float)(n - k);
            float r = (float)(o - l);
            float s = (float)(p - m);
            float t = Mth.sqrt(q * q + r * r + s * s);
            vertexConsumer.vertex(pose.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(pose.normal(), q /= t, r /= t, s /= t).endVertex();
            vertexConsumer.vertex(pose.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(pose.normal(), q, r, s).endVertex();
        });
    }

    public static void renderLineBox(VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
        LevelRenderer.renderLineBox(new PoseStack(), vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aABB, float f, float g, float h, float i) {
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i, f, g, h);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m, float n, float o, float p) {
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        float q = (float)d;
        float r = (float)e;
        float s = (float)f;
        float t = (float)g;
        float u = (float)h;
        float v = (float)i;
        vertexConsumer.vertex(matrix4f, q, r, s).color(j, o, p, m).normal(matrix3f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, r, s).color(j, o, p, m).normal(matrix3f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, r, s).color(n, k, p, m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, u, s).color(n, k, p, m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, r, s).color(n, o, l, m).normal(matrix3f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, r, v).color(n, o, l, m).normal(matrix3f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, r, s).color(j, k, l, m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).normal(matrix3f, -1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, u, s).color(j, k, l, m).normal(matrix3f, -1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, u, s).color(j, k, l, m).normal(matrix3f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).normal(matrix3f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, r, v).color(j, k, l, m).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, r, v).color(j, k, l, m).normal(matrix3f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).normal(matrix3f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).normal(matrix3f, 0.0f, 0.0f, -1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, r, s).color(j, k, l, m).normal(matrix3f, 0.0f, 0.0f, -1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, q, u, v).color(j, k, l, m).normal(matrix3f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).normal(matrix3f, 1.0f, 0.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, r, v).color(j, k, l, m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, u, s).color(j, k, l, m).normal(matrix3f, 0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, t, u, v).color(j, k, l, m).normal(matrix3f, 0.0f, 0.0f, 1.0f).endVertex();
    }

    public static void addChainedFilledBoxVertices(BufferBuilder bufferBuilder, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
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
        for (int i = blockPos.getZ() - 1; i <= blockPos.getZ() + 1; ++i) {
            for (int j = blockPos.getX() - 1; j <= blockPos.getX() + 1; ++j) {
                for (int k = blockPos.getY() - 1; k <= blockPos.getY() + 1; ++k) {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), bl);
                }
            }
        }
    }

    public void setBlocksDirty(int i, int j, int k, int l, int m, int n) {
        for (int o = k - 1; o <= n + 1; ++o) {
            for (int p = i - 1; p <= l + 1; ++p) {
                for (int q = j - 1; q <= m + 1; ++q) {
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
        for (int l = k - 1; l <= k + 1; ++l) {
            for (int m = i - 1; m <= i + 1; ++m) {
                for (int n = j - 1; n <= j + 1; ++n) {
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
        SoundInstance soundInstance = this.playingRecords.get(blockPos);
        if (soundInstance != null) {
            this.minecraft.getSoundManager().stop(soundInstance);
            this.playingRecords.remove(blockPos);
        }
        if (soundEvent != null) {
            RecordItem recordItem = RecordItem.getBySound(soundEvent);
            if (recordItem != null) {
                this.minecraft.gui.setNowPlaying(recordItem.getDisplayName());
            }
            soundInstance = SimpleSoundInstance.forRecord(soundEvent, blockPos.getX(), blockPos.getY(), blockPos.getZ());
            this.playingRecords.put(blockPos, soundInstance);
            this.minecraft.getSoundManager().play(soundInstance);
        }
        this.notifyNearbyEntities(this.level, blockPos, soundEvent != null);
    }

    private void notifyNearbyEntities(Level level, BlockPos blockPos, boolean bl) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(3.0));
        for (LivingEntity livingEntity : list) {
            livingEntity.setRecordPlayingNearby(blockPos, bl);
        }
    }

    public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
        this.addParticle(particleOptions, bl, false, d, e, f, g, h, i);
    }

    public void addParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i) {
        try {
            this.addParticleInternal(particleOptions, bl, bl2, d, e, f, g, h, i);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception while adding particle");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being added");
            crashReportCategory.setDetail("ID", Registry.PARTICLE_TYPE.getKey(particleOptions.getType()));
            crashReportCategory.setDetail("Parameters", particleOptions.writeToString());
            crashReportCategory.setDetail("Position", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this.level, d, e, f));
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
        if (this.minecraft == null || !camera.isInitialized() || this.minecraft.particleEngine == null) {
            return null;
        }
        ParticleStatus particleStatus = this.calculateParticleLevel(bl2);
        if (bl) {
            return this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
        }
        if (camera.getPosition().distanceToSqr(d, e, f) > 1024.0) {
            return null;
        }
        if (particleStatus == ParticleStatus.MINIMAL) {
            return null;
        }
        return this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
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
            case 1038: {
                Camera camera = this.minecraft.gameRenderer.getMainCamera();
                if (!camera.isInitialized()) break;
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
                    this.level.playLocalSound(h, k, l, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                if (i == 1038) {
                    this.level.playLocalSound(h, k, l, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                this.level.playLocalSound(h, k, l, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0f, 1.0f, false);
            }
        }
    }

    public void levelEvent(int i, BlockPos blockPos, int j) {
        RandomSource randomSource = this.level.random;
        switch (i) {
            case 1035: {
                this.level.playLocalSound(blockPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1033: {
                this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1034: {
                this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1032: {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, randomSource.nextFloat() * 0.4f + 0.8f, 0.25f));
                break;
            }
            case 1001: {
                this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 1000: {
                this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1003: {
                this.level.playLocalSound(blockPos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0f, 1.2f, false);
                break;
            }
            case 1004: {
                this.level.playLocalSound(blockPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0f, 1.2f, false);
                break;
            }
            case 1002: {
                this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 2000: {
                Direction direction = Direction.from3DDataValue(j);
                int k = direction.getStepX();
                int l = direction.getStepY();
                int m = direction.getStepZ();
                double d = (double)blockPos.getX() + (double)k * 0.6 + 0.5;
                double e = (double)blockPos.getY() + (double)l * 0.6 + 0.5;
                double f = (double)blockPos.getZ() + (double)m * 0.6 + 0.5;
                for (int n = 0; n < 10; ++n) {
                    double g = randomSource.nextDouble() * 0.2 + 0.01;
                    double h = d + (double)k * 0.01 + (randomSource.nextDouble() - 0.5) * (double)m * 0.5;
                    double o = e + (double)l * 0.01 + (randomSource.nextDouble() - 0.5) * (double)l * 0.5;
                    double p = f + (double)m * 0.01 + (randomSource.nextDouble() - 0.5) * (double)k * 0.5;
                    double q = (double)k * g + randomSource.nextGaussian() * 0.01;
                    double r = (double)l * g + randomSource.nextGaussian() * 0.01;
                    double s = (double)m * g + randomSource.nextGaussian() * 0.01;
                    this.addParticle(ParticleTypes.SMOKE, h, o, p, q, r, s);
                }
                break;
            }
            case 2003: {
                double t = (double)blockPos.getX() + 0.5;
                double u = blockPos.getY();
                double d = (double)blockPos.getZ() + 0.5;
                for (int v = 0; v < 8; ++v) {
                    this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), t, u, d, randomSource.nextGaussian() * 0.15, randomSource.nextDouble() * 0.2, randomSource.nextGaussian() * 0.15);
                }
                for (double e = 0.0; e < Math.PI * 2; e += 0.15707963267948966) {
                    this.addParticle(ParticleTypes.PORTAL, t + Math.cos(e) * 5.0, u - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -5.0, 0.0, Math.sin(e) * -5.0);
                    this.addParticle(ParticleTypes.PORTAL, t + Math.cos(e) * 5.0, u - 0.4, d + Math.sin(e) * 5.0, Math.cos(e) * -7.0, 0.0, Math.sin(e) * -7.0);
                }
                break;
            }
            case 2002: 
            case 2007: {
                Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                for (int k = 0; k < 8; ++k) {
                    this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), vec3.x, vec3.y, vec3.z, randomSource.nextGaussian() * 0.15, randomSource.nextDouble() * 0.2, randomSource.nextGaussian() * 0.15);
                }
                float w = (float)(j >> 16 & 0xFF) / 255.0f;
                float x = (float)(j >> 8 & 0xFF) / 255.0f;
                float y = (float)(j >> 0 & 0xFF) / 255.0f;
                SimpleParticleType particleOptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;
                for (int z = 0; z < 100; ++z) {
                    double e = randomSource.nextDouble() * 4.0;
                    double f = randomSource.nextDouble() * Math.PI * 2.0;
                    double aa = Math.cos(f) * e;
                    double ab = 0.01 + randomSource.nextDouble() * 0.5;
                    double ac = Math.sin(f) * e;
                    Particle particle = this.addParticleInternal(particleOptions, particleOptions.getType().getOverrideLimiter(), vec3.x + aa * 0.1, vec3.y + 0.3, vec3.z + ac * 0.1, aa, ab, ac);
                    if (particle == null) continue;
                    float ad = 0.75f + randomSource.nextFloat() * 0.25f;
                    particle.setColor(w * ad, x * ad, y * ad);
                    particle.setPower((float)e);
                }
                this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2001: {
                BlockState blockState = Block.stateById(j);
                if (!blockState.isAir()) {
                    SoundType soundType = blockState.getSoundType();
                    this.level.playLocalSound(blockPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f, false);
                }
                this.level.addDestroyBlockEffect(blockPos, blockState);
                break;
            }
            case 2004: {
                for (int k = 0; k < 20; ++k) {
                    double u = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
                    double d = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
                    double e = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
                    this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
                    this.level.addParticle(ParticleTypes.FLAME, u, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2005: {
                BoneMealItem.addGrowthParticles(this.level, blockPos, j);
                break;
            }
            case 1505: {
                BoneMealItem.addGrowthParticles(this.level, blockPos, j);
                this.level.playLocalSound(blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 3002: {
                if (j >= 0 && j < Direction.Axis.VALUES.length) {
                    ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[j], this.level, blockPos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
                    break;
                }
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
                break;
            }
            case 3006: {
                int k = j >> 6;
                if (k > 0) {
                    if (randomSource.nextFloat() < 0.3f + (float)k * 0.1f) {
                        float x = 0.15f + 0.02f * (float)k * (float)k * randomSource.nextFloat();
                        float y = 0.4f + 0.3f * (float)k * randomSource.nextFloat();
                        this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, x, y, false);
                    }
                    byte b = (byte)(j & 0x3F);
                    UniformInt intProvider = UniformInt.of(0, k);
                    float ae = 0.005f;
                    Supplier<Vec3> supplier = () -> new Vec3(Mth.nextDouble(randomSource, -0.005f, 0.005f), Mth.nextDouble(randomSource, -0.005f, 0.005f), Mth.nextDouble(randomSource, -0.005f, 0.005f));
                    if (b == 0) {
                        for (Direction direction2 : Direction.values()) {
                            float af = direction2 == Direction.DOWN ? (float)Math.PI : 0.0f;
                            double g = direction2.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
                            ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(af), intProvider, direction2, supplier, g);
                        }
                    } else {
                        for (Direction direction3 : MultifaceBlock.unpack(b)) {
                            float ag = direction3 == Direction.UP ? (float)Math.PI : 0.0f;
                            double ah = 0.35;
                            ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(ag), intProvider, direction3, supplier, 0.35);
                        }
                    }
                } else {
                    this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                    boolean bl = this.level.getBlockState(blockPos).isCollisionShapeFullBlock(this.level, blockPos);
                    int m = bl ? 40 : 20;
                    float ae = bl ? 0.45f : 0.25f;
                    float ai = 0.07f;
                    for (int v = 0; v < m; ++v) {
                        float aj = 2.0f * randomSource.nextFloat() - 1.0f;
                        float ag = 2.0f * randomSource.nextFloat() - 1.0f;
                        float ak = 2.0f * randomSource.nextFloat() - 1.0f;
                        this.level.addParticle(ParticleTypes.SCULK_CHARGE_POP, (double)blockPos.getX() + 0.5 + (double)(aj * ae), (double)blockPos.getY() + 0.5 + (double)(ag * ae), (double)blockPos.getZ() + 0.5 + (double)(ak * ae), aj * 0.07f, ag * 0.07f, ak * 0.07f);
                    }
                }
                break;
            }
            case 3007: {
                for (int l = 0; l < 10; ++l) {
                    this.level.addParticle(new ShriekParticleOption(l * 5), false, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + SculkShriekerBlock.TOP_Y, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
                }
                this.level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + SculkShriekerBlock.TOP_Y, (double)blockPos.getZ() + 0.5, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 2.0f, 0.6f + this.level.random.nextFloat() * 0.4f, false);
                break;
            }
            case 3003: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                this.level.playLocalSound(blockPos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 3004: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
                break;
            }
            case 3005: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
                break;
            }
            case 2008: {
                this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            }
            case 1500: {
                ComposterBlock.handleFill(this.level, blockPos, j > 0);
                break;
            }
            case 1504: {
                PointedDripstoneBlock.spawnDripParticle(this.level, blockPos, this.level.getBlockState(blockPos));
                break;
            }
            case 1501: {
                this.level.playLocalSound(blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8f, false);
                for (int l = 0; l < 8; ++l) {
                    this.level.addParticle(ParticleTypes.LARGE_SMOKE, (double)blockPos.getX() + randomSource.nextDouble(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + randomSource.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1502: {
                this.level.playLocalSound(blockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5f, 2.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8f, false);
                for (int l = 0; l < 5; ++l) {
                    double al = (double)blockPos.getX() + randomSource.nextDouble() * 0.6 + 0.2;
                    double am = (double)blockPos.getY() + randomSource.nextDouble() * 0.6 + 0.2;
                    double an = (double)blockPos.getZ() + randomSource.nextDouble() * 0.6 + 0.2;
                    this.level.addParticle(ParticleTypes.SMOKE, al, am, an, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1503: {
                this.level.playLocalSound(blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                for (int l = 0; l < 16; ++l) {
                    double al = (double)blockPos.getX() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
                    double am = (double)blockPos.getY() + 0.8125;
                    double an = (double)blockPos.getZ() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
                    this.level.addParticle(ParticleTypes.SMOKE, al, am, an, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2006: {
                for (int l = 0; l < 200; ++l) {
                    float y = randomSource.nextFloat() * 4.0f;
                    float ae = randomSource.nextFloat() * ((float)Math.PI * 2);
                    double am = Mth.cos(ae) * y;
                    double an = 0.01 + randomSource.nextDouble() * 0.5;
                    double ah = Mth.sin(ae) * y;
                    Particle particle2 = this.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + am * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + ah * 0.1, am, an, ah);
                    if (particle2 == null) continue;
                    particle2.setPower(y);
                }
                if (j != 1) break;
                this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2009: {
                for (int l = 0; l < 8; ++l) {
                    this.level.addParticle(ParticleTypes.CLOUD, (double)blockPos.getX() + randomSource.nextDouble(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + randomSource.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1012: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1036: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1013: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1014: {
                this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1011: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1006: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1007: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1037: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1008: {
                this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1005: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1009: {
                if (j == 0) {
                    this.level.playLocalSound(blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8f, false);
                    break;
                }
                if (j != 1) break;
                this.level.playLocalSound(blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7f, 1.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.4f, false);
                break;
            }
            case 1029: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1030: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1044: {
                this.level.playLocalSound(blockPos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1031: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1039: {
                this.level.playLocalSound(blockPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1010: {
                if (Item.byId(j) instanceof RecordItem) {
                    this.playStreamingMusic(((RecordItem)Item.byId(j)).getSound(), blockPos);
                    break;
                }
                this.playStreamingMusic(null, blockPos);
                break;
            }
            case 1015: {
                this.level.playLocalSound(blockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1017: {
                this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1016: {
                this.level.playLocalSound(blockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1019: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1022: {
                this.level.playLocalSound(blockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1021: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1020: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1018: {
                this.level.playLocalSound(blockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1024: {
                this.level.playLocalSound(blockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1026: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1027: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1040: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1041: {
                this.level.playLocalSound(blockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1025: {
                this.level.playLocalSound(blockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1042: {
                this.level.playLocalSound(blockPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1043: {
                this.level.playLocalSound(blockPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 3000: {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
                this.level.playLocalSound(blockPos, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0f, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
                break;
            }
            case 3001: {
                this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0f, 0.8f + this.level.random.nextFloat() * 0.3f, false);
                break;
            }
            case 1045: {
                this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1046: {
                this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1047: {
                this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1048: {
                this.level.playLocalSound(blockPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
            }
        }
    }

    public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
        if (j < 0 || j >= 10) {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)this.destroyingBlocks.remove(i);
            if (blockDestructionProgress != null) {
                this.removeProgress(blockDestructionProgress);
            }
        } else {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)this.destroyingBlocks.get(i);
            if (blockDestructionProgress != null) {
                this.removeProgress(blockDestructionProgress);
            }
            if (blockDestructionProgress == null || blockDestructionProgress.getPos().getX() != blockPos.getX() || blockDestructionProgress.getPos().getY() != blockPos.getY() || blockDestructionProgress.getPos().getZ() != blockPos.getZ()) {
                blockDestructionProgress = new BlockDestructionProgress(i, blockPos);
                this.destroyingBlocks.put(i, blockDestructionProgress);
            }
            blockDestructionProgress.setProgress(j);
            blockDestructionProgress.updateTick(this.ticks);
            this.destructionProgress.computeIfAbsent(blockDestructionProgress.getPos().asLong(), l -> Sets.newTreeSet()).add(blockDestructionProgress);
        }
    }

    public boolean hasRenderedAllChunks() {
        return this.chunkRenderDispatcher.isQueueEmpty();
    }

    public void needsUpdate() {
        this.needsFullRenderChunkUpdate = true;
        this.generateClouds = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateGlobalBlockEntities(Collection<BlockEntity> collection, Collection<BlockEntity> collection2) {
        Set<BlockEntity> set = this.globalBlockEntities;
        synchronized (set) {
            this.globalBlockEntities.removeAll(collection);
            this.globalBlockEntities.addAll(collection2);
        }
    }

    public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        return LevelRenderer.getLightColor(blockAndTintGetter, blockAndTintGetter.getBlockState(blockPos), blockPos);
    }

    public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos) {
        int k;
        if (blockState.emissiveRendering(blockAndTintGetter, blockPos)) {
            return 0xF000F0;
        }
        int i = blockAndTintGetter.getBrightness(LightLayer.SKY, blockPos);
        int j = blockAndTintGetter.getBrightness(LightLayer.BLOCK, blockPos);
        if (j < (k = blockState.getLightEmission())) {
            j = k;
        }
        return i << 20 | j << 4;
    }

    public boolean isChunkCompiled(BlockPos blockPos) {
        ChunkRenderDispatcher.RenderChunk renderChunk = this.viewArea.getRenderChunkAt(blockPos);
        return renderChunk != null && renderChunk.compiled.get() != ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
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

    @Environment(value=EnvType.CLIENT)
    public static class TransparencyShaderException
    extends RuntimeException {
        public TransparencyShaderException(String string, Throwable throwable) {
            super(string, throwable);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderChunkStorage {
        public final RenderInfoMap renderInfoMap;
        public final LinkedHashSet<RenderChunkInfo> renderChunks;

        public RenderChunkStorage(int i) {
            this.renderInfoMap = new RenderInfoMap(i);
            this.renderChunks = new LinkedHashSet(i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderChunkInfo {
        final ChunkRenderDispatcher.RenderChunk chunk;
        private byte sourceDirections;
        byte directions;
        final int step;

        RenderChunkInfo(ChunkRenderDispatcher.RenderChunk renderChunk, @Nullable Direction direction, int i) {
            this.chunk = renderChunk;
            if (direction != null) {
                this.addSourceDirection(direction);
            }
            this.step = i;
        }

        public void setDirections(byte b, Direction direction) {
            this.directions = (byte)(this.directions | (b | 1 << direction.ordinal()));
        }

        public boolean hasDirection(Direction direction) {
            return (this.directions & 1 << direction.ordinal()) > 0;
        }

        public void addSourceDirection(Direction direction) {
            this.sourceDirections = (byte)(this.sourceDirections | (this.sourceDirections | 1 << direction.ordinal()));
        }

        public boolean hasSourceDirection(int i) {
            return (this.sourceDirections & 1 << i) > 0;
        }

        public boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        public int hashCode() {
            return this.chunk.getOrigin().hashCode();
        }

        public boolean equals(Object object) {
            if (!(object instanceof RenderChunkInfo)) {
                return false;
            }
            RenderChunkInfo renderChunkInfo = (RenderChunkInfo)object;
            return this.chunk.getOrigin().equals(renderChunkInfo.chunk.getOrigin());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderInfoMap {
        private final RenderChunkInfo[] infos;

        RenderInfoMap(int i) {
            this.infos = new RenderChunkInfo[i];
        }

        public void put(ChunkRenderDispatcher.RenderChunk renderChunk, RenderChunkInfo renderChunkInfo) {
            this.infos[renderChunk.index] = renderChunkInfo;
        }

        @Nullable
        public RenderChunkInfo get(ChunkRenderDispatcher.RenderChunk renderChunk) {
            int i = renderChunk.index;
            if (i < 0 || i >= this.infos.length) {
                return null;
            }
            return this.infos[i];
        }
    }
}

