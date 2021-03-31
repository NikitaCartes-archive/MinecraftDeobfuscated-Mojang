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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
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
import net.minecraft.client.Option;
import net.minecraft.client.ParticleStatus;
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
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
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
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LevelRenderer
implements ResourceManagerReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int CHUNK_SIZE = 16;
    public static final int MAX_CHUNKS_WIDTH = 66;
    public static final int MAX_CHUNKS_AREA = 4356;
    private static final float SKY_DISC_RADIUS = 512.0f;
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
    private final TextureManager textureManager;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private ClientLevel level;
    private Set<ChunkRenderDispatcher.RenderChunk> chunksToCompile = Sets.newLinkedHashSet();
    private final ObjectArrayList<RenderChunkInfo> renderChunks = new ObjectArrayList();
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private ViewArea viewArea;
    private RenderInfoMap renderInfoMap;
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
    private CloudStatus prevCloudsType;
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
    private boolean needsUpdate = true;
    private int frameId;
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    public LevelRenderer(Minecraft minecraft, RenderBuffers renderBuffers) {
        this.minecraft = minecraft;
        this.entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
        this.blockEntityRenderDispatcher = minecraft.getBlockEntityRenderDispatcher();
        this.renderBuffers = renderBuffers;
        this.textureManager = minecraft.getTextureManager();
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
                float ad;
                float z;
                int w;
                int q = (o - k + 16) * 32 + p - i + 16;
                double r = (double)this.rainSizeX[q] * 0.5;
                double s = (double)this.rainSizeZ[q] * 0.5;
                mutableBlockPos.set(p, 0, o);
                Biome biome = level.getBiome(mutableBlockPos);
                if (biome.getPrecipitation() == Biome.Precipitation.NONE) continue;
                int t = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos).getY();
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
                Random random = new Random(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761);
                mutableBlockPos.set(p, u, o);
                float x = biome.getTemperature(mutableBlockPos);
                if (x >= 0.15f) {
                    if (m != 0) {
                        if (m >= 0) {
                            tesselator.end();
                        }
                        m = 0;
                        RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    }
                    int y = this.ticks + p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 0x1F;
                    z = -((float)y + f) / 32.0f * (3.0f + random.nextFloat());
                    double aa = (double)((float)p + 0.5f) - d;
                    double ab = (double)((float)o + 0.5f) - g;
                    float ac = Mth.sqrt(aa * aa + ab * ab) / (float)l;
                    ad = ((1.0f - ac * ac) * 0.5f + 0.5f) * h;
                    mutableBlockPos.set(p, w, o);
                    int ae = LevelRenderer.getLightColor(level, mutableBlockPos);
                    bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5).uv(0.0f, (float)u * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).uv2(ae).endVertex();
                    bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5).uv(1.0f, (float)u * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).uv2(ae).endVertex();
                    bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5).uv(1.0f, (float)v * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).uv2(ae).endVertex();
                    bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5).uv(0.0f, (float)v * 0.25f + z).color(1.0f, 1.0f, 1.0f, ad).uv2(ae).endVertex();
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
                float af = -((float)(this.ticks & 0x1FF) + f) / 512.0f;
                z = (float)(random.nextDouble() + (double)n * 0.01 * (double)((float)random.nextGaussian()));
                float ag = (float)(random.nextDouble() + (double)(n * (float)random.nextGaussian()) * 0.001);
                double ah = (double)((float)p + 0.5f) - d;
                double ai = (double)((float)o + 0.5f) - g;
                ad = Mth.sqrt(ah * ah + ai * ai) / (float)l;
                float aj = ((1.0f - ad * ad) * 0.3f + 0.5f) * h;
                mutableBlockPos.set(p, w, o);
                int ak = LevelRenderer.getLightColor(level, mutableBlockPos);
                int al = ak >> 16 & 0xFFFF;
                int am = (ak & 0xFFFF) * 3;
                int an = (al * 3 + 240) / 4;
                int ao = (am * 3 + 240) / 4;
                bufferBuilder.vertex((double)p - d - r + 0.5, (double)v - e, (double)o - g - s + 0.5).uv(0.0f + z, (float)u * 0.25f + af + ag).color(1.0f, 1.0f, 1.0f, aj).uv2(ao, an).endVertex();
                bufferBuilder.vertex((double)p - d + r + 0.5, (double)v - e, (double)o - g + s + 0.5).uv(1.0f + z, (float)u * 0.25f + af + ag).color(1.0f, 1.0f, 1.0f, aj).uv2(ao, an).endVertex();
                bufferBuilder.vertex((double)p - d + r + 0.5, (double)u - e, (double)o - g + s + 0.5).uv(1.0f + z, (float)v * 0.25f + af + ag).color(1.0f, 1.0f, 1.0f, aj).uv2(ao, an).endVertex();
                bufferBuilder.vertex((double)p - d - r + 0.5, (double)u - e, (double)o - g - s + 0.5).uv(0.0f + z, (float)v * 0.25f + af + ag).color(1.0f, 1.0f, 1.0f, aj).uv2(ao, an).endVertex();
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
        Random random = new Random((long)this.ticks * 312987231L);
        ClientLevel levelReader = this.minecraft.level;
        BlockPos blockPos = new BlockPos(camera.getPosition());
        Vec3i blockPos2 = null;
        int i = (int)(100.0f * f * f) / (this.minecraft.options.particles == ParticleStatus.DECREASED ? 2 : 1);
        for (int j = 0; j < i; ++j) {
            int k = random.nextInt(21) - 10;
            int l = random.nextInt(21) - 10;
            BlockPos blockPos3 = levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(k, 0, l)).below();
            Biome biome = levelReader.getBiome(blockPos3);
            if (blockPos3.getY() <= levelReader.getMinBuildHeight() || blockPos3.getY() > blockPos.getY() + 10 || blockPos3.getY() < blockPos.getY() - 10 || biome.getPrecipitation() != Biome.Precipitation.RAIN || !(biome.getTemperature(blockPos3) >= 0.15f)) continue;
            blockPos2 = blockPos3;
            if (this.minecraft.options.particles == ParticleStatus.MINIMAL) break;
            double d = random.nextDouble();
            double e = random.nextDouble();
            BlockState blockState = levelReader.getBlockState((BlockPos)blockPos2);
            FluidState fluidState = levelReader.getFluidState((BlockPos)blockPos2);
            VoxelShape voxelShape = blockState.getCollisionShape(levelReader, (BlockPos)blockPos2);
            double g = voxelShape.max(Direction.Axis.Y, d, e);
            double h = fluidState.getHeight(levelReader, (BlockPos)blockPos2);
            double m = Math.max(g, h);
            SimpleParticleType particleOptions = fluidState.is(FluidTags.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState) ? ParticleTypes.SMOKE : ParticleTypes.RAIN;
            this.minecraft.level.addParticle(particleOptions, (double)blockPos2.getX() + d, (double)blockPos2.getY() + m, (double)blockPos2.getZ() + e, 0.0, 0.0, 0.0);
        }
        if (blockPos2 != null && random.nextInt(3) < this.rainSoundTime++) {
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
                TextComponent component;
                try {
                    component = new TextComponent(this.minecraft.getResourceManager().getResource(resourceLocation).getSourceName());
                } catch (IOException iOException) {
                    component = null;
                }
                this.minecraft.options.graphicsMode = GraphicsStatus.FANCY;
                this.minecraft.clearResourcePacksOnError(transparencyShaderException, component);
            }
            CrashReport crashReport = this.minecraft.fillReport(new CrashReport(string2, transparencyShaderException));
            this.minecraft.options.graphicsMode = GraphicsStatus.FANCY;
            this.minecraft.options.save();
            LOGGER.fatal(string2, (Throwable)transparencyShaderException);
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
        this.darkBuffer.upload(bufferBuilder);
    }

    private void createLightSky() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }
        this.skyBuffer = new VertexBuffer();
        LevelRenderer.buildSkyDisc(bufferBuilder, 16.0f);
        this.skyBuffer.upload(bufferBuilder);
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
        this.starBuffer.upload(bufferBuilder);
    }

    private void drawStars(BufferBuilder bufferBuilder) {
        Random random = new Random(10842L);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 1500; ++i) {
            double d = random.nextFloat() * 2.0f - 1.0f;
            double e = random.nextFloat() * 2.0f - 1.0f;
            double f = random.nextFloat() * 2.0f - 1.0f;
            double g = 0.15f + random.nextFloat() * 0.1f;
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
            double s = random.nextDouble() * Math.PI * 2.0;
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
            this.renderChunks.ensureCapacity(4356 * clientLevel.getSectionsCount());
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
        Entity entity;
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
        this.needsUpdate = true;
        this.generateClouds = true;
        ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
        this.lastViewDistance = this.minecraft.options.renderDistance;
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
        }
        this.resetChunksToCompile();
        Set<BlockEntity> set = this.globalBlockEntities;
        synchronized (set) {
            this.globalBlockEntities.clear();
        }
        this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.renderDistance, this);
        this.renderInfoMap = new RenderInfoMap(this.viewArea.chunks.length);
        if (this.level != null && (entity = this.minecraft.getCameraEntity()) != null) {
            this.viewArea.repositionCamera(entity.getX(), entity.getZ());
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
        for (RenderChunkInfo renderChunkInfo : this.renderChunks) {
            if (renderChunkInfo.chunk.getCompiledChunk().hasNoRenderableLayers()) continue;
            ++i;
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
        double d = this.minecraft.player.getX();
        double e = this.minecraft.player.getY();
        double f = this.minecraft.player.getZ();
        double g = d - this.lastCameraX;
        double h = e - this.lastCameraY;
        double j = f - this.lastCameraZ;
        int k = SectionPos.posToSectionCoord(d);
        int l = SectionPos.posToSectionCoord(e);
        int m = SectionPos.posToSectionCoord(f);
        if (this.lastCameraChunkX != k || this.lastCameraChunkY != l || this.lastCameraChunkZ != m || g * g + h * h + j * j > 16.0) {
            this.lastCameraX = d;
            this.lastCameraY = e;
            this.lastCameraZ = f;
            this.lastCameraChunkX = k;
            this.lastCameraChunkY = l;
            this.lastCameraChunkZ = m;
            this.viewArea.repositionCamera(d, f);
        }
        this.chunkRenderDispatcher.setCamera(vec3);
        this.level.getProfiler().popPush("cull");
        this.minecraft.getProfiler().popPush("culling");
        BlockPos blockPos = camera.getBlockPosition();
        ChunkRenderDispatcher.RenderChunk renderChunk = this.viewArea.getRenderChunkAt(blockPos);
        int n = 16;
        BlockPos blockPos2 = new BlockPos(Mth.floor(vec3.x / 16.0) * 16, Mth.floor(vec3.y / 16.0) * 16, Mth.floor(vec3.z / 16.0) * 16);
        float o = camera.getXRot();
        float p = camera.getYRot();
        this.needsUpdate = this.needsUpdate || !this.chunksToCompile.isEmpty() || vec3.x != this.prevCamX || vec3.y != this.prevCamY || vec3.z != this.prevCamZ || (double)o != this.prevCamRotX || (double)p != this.prevCamRotY;
        this.prevCamX = vec3.x;
        this.prevCamY = vec3.y;
        this.prevCamZ = vec3.z;
        this.prevCamRotX = o;
        this.prevCamRotY = p;
        this.minecraft.getProfiler().popPush("update");
        if (!bl && this.needsUpdate) {
            this.needsUpdate = false;
            this.updateRenderChunks(frustum, i, bl2, vec3, blockPos, renderChunk, 16, blockPos2);
        }
        this.minecraft.getProfiler().popPush("rebuildNear");
        Set<ChunkRenderDispatcher.RenderChunk> set = this.chunksToCompile;
        this.chunksToCompile = Sets.newLinkedHashSet();
        for (RenderChunkInfo renderChunkInfo : this.renderChunks) {
            boolean bl3;
            ChunkRenderDispatcher.RenderChunk renderChunk2 = renderChunkInfo.chunk;
            if (!renderChunk2.isDirty() && !set.contains(renderChunk2)) continue;
            this.needsUpdate = true;
            BlockPos blockPos3 = renderChunk2.getOrigin().offset(8, 8, 8);
            boolean bl4 = bl3 = blockPos3.distSqr(blockPos) < 768.0;
            if (renderChunk2.isDirtyFromPlayer() || bl3) {
                this.minecraft.getProfiler().push("build near");
                this.chunkRenderDispatcher.rebuildChunkSync(renderChunk2);
                renderChunk2.setNotDirty();
                this.minecraft.getProfiler().pop();
                continue;
            }
            this.chunksToCompile.add(renderChunk2);
        }
        this.chunksToCompile.addAll(set);
        this.minecraft.getProfiler().pop();
    }

    private void updateRenderChunks(Frustum frustum, int i, boolean bl, Vec3 vec3, BlockPos blockPos, ChunkRenderDispatcher.RenderChunk renderChunk, int j, BlockPos blockPos2) {
        int k;
        this.renderChunks.clear();
        ArrayDeque queue = Queues.newArrayDeque();
        Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0, 1.0, 2.5) * (double)this.minecraft.options.entityDistanceScaling);
        boolean bl2 = this.minecraft.smartCull;
        if (renderChunk == null) {
            k = blockPos.getY() > this.level.getMinBuildHeight() ? this.level.getMaxBuildHeight() - 8 : this.level.getMinBuildHeight() + 8;
            int l = Mth.floor(vec3.x / (double)j) * j;
            int m = Mth.floor(vec3.z / (double)j) * j;
            ArrayList<RenderChunkInfo> list = Lists.newArrayList();
            for (int n = -this.lastViewDistance; n <= this.lastViewDistance; ++n) {
                for (int o = -this.lastViewDistance; o <= this.lastViewDistance; ++o) {
                    ChunkRenderDispatcher.RenderChunk renderChunk2 = this.viewArea.getRenderChunkAt(new BlockPos(l + SectionPos.sectionToBlockCoord(n, 8), k, m + SectionPos.sectionToBlockCoord(o, 8)));
                    if (renderChunk2 == null || !frustum.isVisible(renderChunk2.bb)) continue;
                    renderChunk2.setFrame(i);
                    list.add(new RenderChunkInfo(renderChunk2, null, 0));
                }
            }
            list.sort(Comparator.comparingDouble(renderChunkInfo -> blockPos.distSqr(((RenderChunkInfo)renderChunkInfo).chunk.getOrigin().offset(8, 8, 8))));
            queue.addAll(list);
        } else {
            if (bl && this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos)) {
                bl2 = false;
            }
            renderChunk.setFrame(i);
            queue.add(new RenderChunkInfo(renderChunk, null, 0));
        }
        this.minecraft.getProfiler().push("iteration");
        k = this.minecraft.options.renderDistance;
        this.renderInfoMap.clear();
        while (!queue.isEmpty()) {
            RenderChunkInfo renderChunkInfo2 = (RenderChunkInfo)queue.poll();
            ChunkRenderDispatcher.RenderChunk renderChunk3 = renderChunkInfo2.chunk;
            this.renderChunks.add(renderChunkInfo2);
            for (Direction direction : DIRECTIONS) {
                RenderChunkInfo renderChunkInfo22;
                ChunkRenderDispatcher.RenderChunk renderChunk4 = this.getRelativeFrom(blockPos2, renderChunk3, direction);
                if (bl2 && renderChunkInfo2.hasDirection(direction.getOpposite())) continue;
                if (bl2 && renderChunkInfo2.hasSourceDirections()) {
                    ChunkRenderDispatcher.CompiledChunk compiledChunk = renderChunk3.getCompiledChunk();
                    boolean bl3 = false;
                    for (int p = 0; p < DIRECTIONS.length; ++p) {
                        if (!renderChunkInfo2.hasSourceDirection(p) || !compiledChunk.facesCanSeeEachother(DIRECTIONS[p].getOpposite(), direction)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (renderChunk4 == null || !renderChunk4.hasAllNeighbors()) continue;
                if (!renderChunk4.setFrame(i)) {
                    renderChunkInfo22 = this.renderInfoMap.get(renderChunk4);
                    if (renderChunkInfo22 == null) continue;
                    renderChunkInfo22.addSourceDirection(direction);
                    continue;
                }
                if (!frustum.isVisible(renderChunk4.bb)) continue;
                renderChunkInfo22 = new RenderChunkInfo(renderChunk4, direction, renderChunkInfo2.step + 1);
                renderChunkInfo22.setDirections(renderChunkInfo2.directions, direction);
                queue.add(renderChunkInfo22);
                this.renderInfoMap.put(renderChunk4, renderChunkInfo22);
            }
        }
        this.minecraft.getProfiler().pop();
    }

    @Nullable
    private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos blockPos, ChunkRenderDispatcher.RenderChunk renderChunk, Direction direction) {
        BlockPos blockPos2 = renderChunk.getRelativeOrigin(direction);
        if (Mth.abs(blockPos.getX() - blockPos2.getX()) > this.lastViewDistance * 16) {
            return null;
        }
        if (blockPos2.getY() < this.level.getMinBuildHeight() || blockPos2.getY() >= this.level.getMaxBuildHeight()) {
            return null;
        }
        if (Mth.abs(blockPos.getZ() - blockPos2.getZ()) > this.lastViewDistance * 16) {
            return null;
        }
        return this.viewArea.getRenderChunkAt(blockPos2);
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
        int v;
        boolean bl3;
        Frustum frustum;
        boolean bl2;
        RenderSystem.setShaderGameTime(this.level.getGameTime(), f);
        this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
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
        boolean bl4 = bl2 = this.capturedFrustum != null;
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
        FogRenderer.setupColor(camera, f, this.minecraft.level, this.minecraft.options.renderDistance, gameRenderer.getDarkenWorldAmount(f));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float h = gameRenderer.getRenderDistance();
        boolean bl5 = bl3 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d), Mth.floor(e)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        if (this.minecraft.options.renderDistance >= 4) {
            FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, h, bl3);
            profilerFiller.popPush("sky");
            RenderSystem.setShader(GameRenderer::getPositionShader);
            this.renderSky(poseStack, matrix4f, f);
        }
        profilerFiller.popPush("fog");
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(h - 16.0f, 32.0f), bl3);
        profilerFiller.popPush("terrain_setup");
        this.setupRender(camera, frustum, bl2, this.frameId++, this.minecraft.player.isSpectator());
        profilerFiller.popPush("updatechunks");
        int i = 30;
        int j = this.minecraft.options.framerateLimit;
        long m = 33333333L;
        long n = (double)j == Option.FRAMERATE_LIMIT.getMaxValue() ? 0L : (long)(1000000000 / j);
        long o = Util.getNanos() - l;
        long p = this.frameTimes.registerValueAndGetMean(o);
        long q = p * 3L / 2L;
        long r = Mth.clamp(q, n, 33333333L);
        this.compileChunksUntil(l + r);
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
        boolean bl42 = false;
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        for (Entity entity : this.level.entitiesForRendering()) {
            MultiBufferSource multiBufferSource;
            if (!this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, g) && !entity.hasIndirectPassenger(this.minecraft.player) || entity == camera.getEntity() && !camera.isDetached() && (!(camera.getEntity() instanceof LivingEntity) || !((LivingEntity)camera.getEntity()).isSleeping()) || entity instanceof LocalPlayer && camera.getEntity() != entity) continue;
            ++this.renderedEntities;
            if (entity.tickCount == 0) {
                entity.xOld = entity.getX();
                entity.yOld = entity.getY();
                entity.zOld = entity.getZ();
            }
            if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
                bl42 = true;
                OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
                multiBufferSource = outlineBufferSource;
                int k = entity.getTeamColor();
                int s = 255;
                int t = k >> 16 & 0xFF;
                int u = k >> 8 & 0xFF;
                v = k & 0xFF;
                outlineBufferSource.setColor(t, u, v, 255);
            } else {
                multiBufferSource = bufferSource;
            }
            this.renderEntity(entity, d, e, g, f, poseStack, multiBufferSource);
        }
        this.checkPoseStack(poseStack);
        bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
        profilerFiller.popPush("blockentities");
        for (RenderChunkInfo renderChunkInfo : this.renderChunks) {
            List<BlockEntity> list = renderChunkInfo.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (list.isEmpty()) continue;
            for (BlockEntity blockEntity : list) {
                BlockPos blockPos = blockEntity.getBlockPos();
                MultiBufferSource multiBufferSource2 = bufferSource;
                poseStack.pushPose();
                poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - g);
                SortedSet sortedSet = (SortedSet)this.destructionProgress.get(blockPos.asLong());
                if (sortedSet != null && !sortedSet.isEmpty() && (v = ((BlockDestructionProgress)sortedSet.last()).getProgress()) >= 0) {
                    PoseStack.Pose pose = poseStack.last();
                    SheetedDecalTextureGenerator vertexConsumer = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(v)), pose.pose(), pose.normal());
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
                BlockPos blockPos2 = blockEntity2.getBlockPos();
                poseStack.pushPose();
                poseStack.translate((double)blockPos2.getX() - d, (double)blockPos2.getY() - e, (double)blockPos2.getZ() - g);
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
        if (bl42) {
            this.entityEffect.process(f);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        profilerFiller.popPush("destroyProgress");
        for (Long2ObjectMap.Entry entry : this.destructionProgress.long2ObjectEntrySet()) {
            SortedSet sortedSet2;
            double y;
            double x;
            BlockPos blockPos3 = BlockPos.of(entry.getLongKey());
            double w = (double)blockPos3.getX() - d;
            if (w * w + (x = (double)blockPos3.getY() - e) * x + (y = (double)blockPos3.getZ() - g) * y > 1024.0 || (sortedSet2 = (SortedSet)entry.getValue()) == null || sortedSet2.isEmpty()) continue;
            int z = ((BlockDestructionProgress)sortedSet2.last()).getProgress();
            poseStack.pushPose();
            poseStack.translate((double)blockPos3.getX() - d, (double)blockPos3.getY() - e, (double)blockPos3.getZ() - g);
            PoseStack.Pose pose2 = poseStack.last();
            SheetedDecalTextureGenerator vertexConsumer2 = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(z)), pose2.pose(), pose2.normal());
            this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos3), blockPos3, this.level, poseStack, vertexConsumer2);
            poseStack.popPose();
        }
        this.checkPoseStack(poseStack);
        HitResult hitResult = this.minecraft.hitResult;
        if (bl && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            profilerFiller.popPush("outline");
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = this.level.getBlockState(blockPos);
            if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos)) {
                VertexConsumer vertexConsumer3 = bufferSource.getBuffer(RenderType.lines());
                this.renderHitOutline(poseStack, vertexConsumer3, camera.getEntity(), d, e, g, blockPos, blockState);
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
        float k = Mth.lerp(g, entity.yRotO, entity.yRot);
        this.entityRenderDispatcher.render(entity, h - d, i - e, j - f, k, g, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, g));
    }

    private void renderChunkLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f matrix4f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
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
                for (RenderChunkInfo renderChunkInfo : this.renderChunks) {
                    if (j >= 15 || !renderChunkInfo.chunk.resortTransparency(renderType, this.chunkRenderDispatcher)) continue;
                    ++j;
                }
            }
            this.minecraft.getProfiler().pop();
        }
        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() -> "render_" + renderType);
        boolean bl = renderType != RenderType.translucent();
        ListIterator objectListIterator = this.renderChunks.listIterator(bl ? 0 : this.renderChunks.size());
        VertexFormat vertexFormat = renderType.format();
        ShaderInstance shaderInstance = RenderSystem.getShader();
        BufferUploader.reset();
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
        if (shaderInstance.TEXTURE_MATRIX != null) {
            shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (shaderInstance.GAME_TIME != null) {
            shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        RenderSystem.setupShaderLights(shaderInstance);
        shaderInstance.apply();
        Uniform uniform = shaderInstance.CHUNK_OFFSET;
        boolean bl2 = false;
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
            vertexBuffer.drawChunkLayer();
            bl2 = true;
        }
        if (uniform != null) {
            uniform.set(Vector3f.ZERO);
        }
        shaderInstance.clear();
        if (bl2) {
            vertexFormat.clearBufferState();
        }
        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
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
            for (RenderChunkInfo renderChunkInfo : this.renderChunks) {
                int i;
                ChunkRenderDispatcher.RenderChunk renderChunk = renderChunkInfo.chunk;
                BlockPos blockPos = renderChunk.getOrigin();
                PoseStack poseStack = RenderSystem.getModelViewStack();
                poseStack.pushPose();
                poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
                RenderSystem.applyModelViewMatrix();
                if (this.minecraft.chunkPath) {
                    bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0f);
                    i = renderChunkInfo.step == 0 ? 0 : Mth.hsvToRgb((float)renderChunkInfo.step / 50.0f, 0.9f, 0.9f);
                    int j = i >> 16 & 0xFF;
                    int k = i >> 8 & 0xFF;
                    int l = i & 0xFF;
                    for (int m = 0; m < DIRECTIONS.length; ++m) {
                        if (!renderChunkInfo.hasSourceDirection(m)) continue;
                        Direction direction = DIRECTIONS[m];
                        bufferBuilder.vertex(8.0, 8.0, 8.0).color(j, k, l, 255).endVertex();
                        bufferBuilder.vertex(8 - 16 * direction.getStepX(), 8 - 16 * direction.getStepY(), 8 - 16 * direction.getStepZ()).color(j, k, l, 255).endVertex();
                    }
                    tesselator.end();
                    RenderSystem.lineWidth(1.0f);
                }
                if (this.minecraft.chunkVisibility && !renderChunk.getCompiledChunk().hasNoRenderableLayers()) {
                    bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0f);
                    i = 0;
                    for (Direction direction2 : DIRECTIONS) {
                        for (Direction direction3 : DIRECTIONS) {
                            boolean bl = renderChunk.getCompiledChunk().facesCanSeeEachother(direction2, direction3);
                            if (bl) continue;
                            ++i;
                            bufferBuilder.vertex(8 + 8 * direction2.getStepX(), 8 + 8 * direction2.getStepY(), 8 + 8 * direction2.getStepZ()).color(1, 0, 0, 1).endVertex();
                            bufferBuilder.vertex(8 + 8 * direction3.getStepX(), 8 + 8 * direction3.getStepY(), 8 + 8 * direction3.getStepZ()).color(1, 0, 0, 1).endVertex();
                        }
                    }
                    tesselator.end();
                    RenderSystem.lineWidth(1.0f);
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
            RenderSystem.lineWidth(10.0f);
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
            RenderSystem.setShader(GameRenderer::getPositionShader);
            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
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
        vertexConsumer.vertex(this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z()).endVertex();
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

    public void renderSky(PoseStack poseStack, Matrix4f matrix4f, float f) {
        float r;
        float q;
        float p;
        int n;
        float l;
        float j;
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
        this.skyBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
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
            BufferUploader.end(bufferBuilder);
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
        BufferUploader.end(bufferBuilder);
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
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableTexture();
        float v = this.level.getStarBrightness(f) * j;
        if (v > 0.0f) {
            RenderSystem.setShaderColor(v, v, v, v);
            this.starBuffer.drawWithShader(poseStack.last().pose(), matrix4f, GameRenderer.getPositionShader());
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
            this.darkBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
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
            this.cloudBuffer.upload(bufferBuilder);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
        FogRenderer.levelFogColor();
        poseStack.pushPose();
        poseStack.scale(12.0f, 1.0f, 12.0f);
        poseStack.translate(-p, q, -r);
        if (this.cloudBuffer != null) {
            int v;
            for (int w = v = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1; w < 2; ++w) {
                if (w == 0) {
                    RenderSystem.colorMask(false, false, false, false);
                } else {
                    RenderSystem.colorMask(true, true, true, true);
                }
                ShaderInstance shaderInstance = RenderSystem.getShader();
                this.cloudBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shaderInstance);
            }
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

    private void compileChunksUntil(long l) {
        this.needsUpdate |= this.chunkRenderDispatcher.uploadAllPendingUploads();
        long m = Util.getNanos();
        int i = 0;
        if (!this.chunksToCompile.isEmpty()) {
            Iterator<ChunkRenderDispatcher.RenderChunk> iterator = this.chunksToCompile.iterator();
            while (iterator.hasNext()) {
                long o;
                long p;
                ChunkRenderDispatcher.RenderChunk renderChunk = iterator.next();
                if (renderChunk.isDirtyFromPlayer()) {
                    this.chunkRenderDispatcher.rebuildChunkSync(renderChunk);
                } else {
                    renderChunk.rebuildChunkAsync(this.chunkRenderDispatcher);
                }
                renderChunk.setNotDirty();
                iterator.remove();
                long n = Util.getNanos();
                long q = l - n;
                if (q >= (p = (o = n - m) / (long)(++i))) continue;
                break;
            }
        }
    }

    private void renderWorldBorder(Camera camera) {
        float v;
        double u;
        double t;
        float s;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        WorldBorder worldBorder = this.level.getWorldBorder();
        double d = this.minecraft.options.renderDistance * 16;
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
        BufferUploader.end(bufferBuilder);
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

    public void levelEvent(Player player, int i, BlockPos blockPos, int j) {
        Random random = this.level.random;
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
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, random.nextFloat() * 0.4f + 0.8f, 0.25f));
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
                    double g = random.nextDouble() * 0.2 + 0.01;
                    double h = d + (double)k * 0.01 + (random.nextDouble() - 0.5) * (double)m * 0.5;
                    double o = e + (double)l * 0.01 + (random.nextDouble() - 0.5) * (double)l * 0.5;
                    double p = f + (double)m * 0.01 + (random.nextDouble() - 0.5) * (double)k * 0.5;
                    double q = (double)k * g + random.nextGaussian() * 0.01;
                    double r = (double)l * g + random.nextGaussian() * 0.01;
                    double s = (double)m * g + random.nextGaussian() * 0.01;
                    this.addParticle(ParticleTypes.SMOKE, h, o, p, q, r, s);
                }
                break;
            }
            case 2003: {
                double t = (double)blockPos.getX() + 0.5;
                double u = blockPos.getY();
                double d = (double)blockPos.getZ() + 0.5;
                for (int v = 0; v < 8; ++v) {
                    this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), t, u, d, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
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
                    this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), vec3.x, vec3.y, vec3.z, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
                }
                float w = (float)(j >> 16 & 0xFF) / 255.0f;
                float x = (float)(j >> 8 & 0xFF) / 255.0f;
                float y = (float)(j >> 0 & 0xFF) / 255.0f;
                SimpleParticleType particleOptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;
                for (int z = 0; z < 100; ++z) {
                    double e = random.nextDouble() * 4.0;
                    double f = random.nextDouble() * Math.PI * 2.0;
                    double aa = Math.cos(f) * e;
                    double ab = 0.01 + random.nextDouble() * 0.5;
                    double ac = Math.sin(f) * e;
                    Particle particle = this.addParticleInternal(particleOptions, particleOptions.getType().getOverrideLimiter(), vec3.x + aa * 0.1, vec3.y + 0.3, vec3.z + ac * 0.1, aa, ab, ac);
                    if (particle == null) continue;
                    float ad = 0.75f + random.nextFloat() * 0.25f;
                    particle.setColor(w * ad, x * ad, y * ad);
                    particle.setPower((float)e);
                }
                this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
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
                    double u = (double)blockPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    double d = (double)blockPos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    double e = (double)blockPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
                    this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
                    this.level.addParticle(ParticleTypes.FLAME, u, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2005: {
                BoneMealItem.addGrowthParticles(this.level, blockPos, j);
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
                this.level.playLocalSound(blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f, false);
                for (int k = 0; k < 8; ++k) {
                    this.level.addParticle(ParticleTypes.LARGE_SMOKE, (double)blockPos.getX() + random.nextDouble(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1502: {
                this.level.playLocalSound(blockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f, false);
                for (int k = 0; k < 5; ++k) {
                    double u = (double)blockPos.getX() + random.nextDouble() * 0.6 + 0.2;
                    double d = (double)blockPos.getY() + random.nextDouble() * 0.6 + 0.2;
                    double e = (double)blockPos.getZ() + random.nextDouble() * 0.6 + 0.2;
                    this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1503: {
                this.level.playLocalSound(blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                for (int k = 0; k < 16; ++k) {
                    double u = (double)blockPos.getX() + (5.0 + random.nextDouble() * 6.0) / 16.0;
                    double d = (double)blockPos.getY() + 0.8125;
                    double e = (double)blockPos.getZ() + (5.0 + random.nextDouble() * 6.0) / 16.0;
                    this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2006: {
                for (int k = 0; k < 200; ++k) {
                    float x = random.nextFloat() * 4.0f;
                    float y = random.nextFloat() * ((float)Math.PI * 2);
                    double d = Mth.cos(y) * x;
                    double e = 0.01 + random.nextDouble() * 0.5;
                    double f = Mth.sin(y) * x;
                    Particle particle2 = this.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + d * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + f * 0.1, d, e, f);
                    if (particle2 == null) continue;
                    particle2.setPower(x);
                }
                if (j != 1) break;
                this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2009: {
                for (int k = 0; k < 8; ++k) {
                    this.level.addParticle(ParticleTypes.CLOUD, (double)blockPos.getX() + random.nextDouble(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1012: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1036: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1013: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1014: {
                this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1011: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1006: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1007: {
                this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1037: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1008: {
                this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1005: {
                this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1009: {
                if (j == 0) {
                    this.level.playLocalSound(blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f, false);
                    break;
                }
                if (j != 1) break;
                this.level.playLocalSound(blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7f, 1.6f + (random.nextFloat() - random.nextFloat()) * 0.4f, false);
                break;
            }
            case 1029: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1030: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.1f + 0.9f, false);
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
                this.level.playLocalSound(blockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1017: {
                this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1016: {
                this.level.playLocalSound(blockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1019: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1022: {
                this.level.playLocalSound(blockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1021: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1020: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1018: {
                this.level.playLocalSound(blockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1024: {
                this.level.playLocalSound(blockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1026: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1027: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1040: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1041: {
                this.level.playLocalSound(blockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1025: {
                this.level.playLocalSound(blockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
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
                this.level.playLocalSound(blockPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.NEUTRAL, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f, false);
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
        return this.chunksToCompile.isEmpty() && this.chunkRenderDispatcher.isQueueEmpty();
    }

    public void needsUpdate() {
        this.needsUpdate = true;
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
    static class RenderInfoMap {
        private final RenderChunkInfo[] infos;
        private final RenderChunkInfo[] blank;

        private RenderInfoMap(int i) {
            this.infos = new RenderChunkInfo[i];
            this.blank = new RenderChunkInfo[i];
        }

        private void clear() {
            System.arraycopy(this.blank, 0, this.infos, 0, this.infos.length);
        }

        public void put(ChunkRenderDispatcher.RenderChunk renderChunk, RenderChunkInfo renderChunkInfo) {
            this.infos[renderChunk.index] = renderChunkInfo;
        }

        public RenderChunkInfo get(ChunkRenderDispatcher.RenderChunk renderChunk) {
            return this.infos[renderChunk.index];
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderChunkInfo {
        private final ChunkRenderDispatcher.RenderChunk chunk;
        private byte sourceDirections;
        private byte directions;
        private final int step;

        private RenderChunkInfo(ChunkRenderDispatcher.RenderChunk renderChunk, @Nullable Direction direction, int i) {
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
    }
}

