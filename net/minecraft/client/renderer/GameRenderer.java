/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.FrustumCuller;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GameRenderer
implements AutoCloseable,
ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
    private final Minecraft minecraft;
    private final ResourceManager resourceManager;
    private final Random random = new Random();
    private float renderDistance;
    public final ItemInHandRenderer itemInHandRenderer;
    private final MapRenderer mapRenderer;
    private int tick;
    private float fov;
    private float oldFov;
    private float darkenWorldAmount;
    private float darkenWorldAmountO;
    private boolean renderHand = true;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private long lastActiveTime = Util.getMillis();
    private final LightTexture lightTexture;
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private final FogRenderer fog;
    private boolean panoramicMode;
    private double zoom = 1.0;
    private double zoom_x;
    private double zoom_y;
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;
    private PostChain postEffect;
    private static final ResourceLocation[] EFFECTS = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
    public static final int EFFECT_NONE = EFFECTS.length;
    private int effectIndex = EFFECT_NONE;
    private boolean effectActive;
    private int frameId;
    private final Camera mainCamera = new Camera();

    public GameRenderer(Minecraft minecraft, ResourceManager resourceManager) {
        this.minecraft = minecraft;
        this.resourceManager = resourceManager;
        this.itemInHandRenderer = minecraft.getItemInHandRenderer();
        this.mapRenderer = new MapRenderer(minecraft.getTextureManager());
        this.lightTexture = new LightTexture(this);
        this.fog = new FogRenderer(this);
        this.postEffect = null;
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float g = i - 16;
                float h = Mth.sqrt(f * f + g * g);
                this.rainSizeX[i << 5 | j] = -g / h;
                this.rainSizeZ[i << 5 | j] = f / h;
            }
        }
    }

    @Override
    public void close() {
        this.lightTexture.close();
        this.mapRenderer.close();
        this.shutdownEffect();
    }

    public boolean postEffectActive() {
        return this.postEffect != null;
    }

    public void shutdownEffect() {
        if (this.postEffect != null) {
            this.postEffect.close();
        }
        this.postEffect = null;
        this.effectIndex = EFFECT_NONE;
    }

    public void togglePostEffect() {
        this.effectActive = !this.effectActive;
    }

    public void checkEntityPostEffect(@Nullable Entity entity) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }
        this.postEffect = null;
        if (entity instanceof Creeper) {
            this.loadEffect(new ResourceLocation("shaders/post/creeper.json"));
        } else if (entity instanceof Spider) {
            this.loadEffect(new ResourceLocation("shaders/post/spider.json"));
        } else if (entity instanceof EnderMan) {
            this.loadEffect(new ResourceLocation("shaders/post/invert.json"));
        }
    }

    private void loadEffect(ResourceLocation resourceLocation) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }
        try {
            this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), resourceLocation);
            this.postEffect.resize(this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
            this.effectActive = true;
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load shader: {}", (Object)resourceLocation, (Object)iOException);
            this.effectIndex = EFFECT_NONE;
            this.effectActive = false;
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("Failed to load shader: {}", (Object)resourceLocation, (Object)jsonSyntaxException);
            this.effectIndex = EFFECT_NONE;
            this.effectActive = false;
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }
        this.postEffect = null;
        if (this.effectIndex == EFFECT_NONE) {
            this.checkEntityPostEffect(this.minecraft.getCameraEntity());
        } else {
            this.loadEffect(EFFECTS[this.effectIndex]);
        }
    }

    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }
        this.mainCamera.tick();
        ++this.tick;
        this.itemInHandRenderer.tick();
        this.tickRain();
        this.darkenWorldAmountO = this.darkenWorldAmount;
        if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
            this.darkenWorldAmount += 0.05f;
            if (this.darkenWorldAmount > 1.0f) {
                this.darkenWorldAmount = 1.0f;
            }
        } else if (this.darkenWorldAmount > 0.0f) {
            this.darkenWorldAmount -= 0.0125f;
        }
        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    public PostChain currentEffect() {
        return this.postEffect;
    }

    public void resize(int i, int j) {
        if (this.postEffect != null) {
            this.postEffect.resize(i, j);
        }
        this.minecraft.levelRenderer.resize(i, j);
    }

    public void pick(float f) {
        Entity entity2 = this.minecraft.getCameraEntity();
        if (entity2 == null) {
            return;
        }
        if (this.minecraft.level == null) {
            return;
        }
        this.minecraft.getProfiler().push("pick");
        this.minecraft.crosshairPickEntity = null;
        double d = this.minecraft.gameMode.getPickRange();
        this.minecraft.hitResult = entity2.pick(d, f, false);
        Vec3 vec3 = entity2.getEyePosition(f);
        boolean bl = false;
        int i = 3;
        double e = d;
        if (this.minecraft.gameMode.hasFarPickRange()) {
            d = e = 6.0;
        } else {
            if (e > 3.0) {
                bl = true;
            }
            d = e;
        }
        e *= e;
        if (this.minecraft.hitResult != null) {
            e = this.minecraft.hitResult.getLocation().distanceToSqr(vec3);
        }
        Vec3 vec32 = entity2.getViewVector(1.0f);
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        float g = 1.0f;
        AABB aABB = entity2.getBoundingBox().expandTowards(vec32.scale(d)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity2, vec3, vec33, aABB, entity -> !entity.isSpectator() && entity.isPickable(), e);
        if (entityHitResult != null) {
            Entity entity22 = entityHitResult.getEntity();
            Vec3 vec34 = entityHitResult.getLocation();
            double h = vec3.distanceToSqr(vec34);
            if (bl && h > 9.0) {
                this.minecraft.hitResult = BlockHitResult.miss(vec34, Direction.getNearest(vec32.x, vec32.y, vec32.z), new BlockPos(vec34));
            } else if (h < e || this.minecraft.hitResult == null) {
                this.minecraft.hitResult = entityHitResult;
                if (entity22 instanceof LivingEntity || entity22 instanceof ItemFrame) {
                    this.minecraft.crosshairPickEntity = entity22;
                }
            }
        }
        this.minecraft.getProfiler().pop();
    }

    private void tickFov() {
        float f = 1.0f;
        if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)this.minecraft.getCameraEntity();
            f = abstractClientPlayer.getFieldOfViewModifier();
        }
        this.oldFov = this.fov;
        this.fov += (f - this.fov) * 0.5f;
        if (this.fov > 1.5f) {
            this.fov = 1.5f;
        }
        if (this.fov < 0.1f) {
            this.fov = 0.1f;
        }
    }

    private double getFov(Camera camera, float f, boolean bl) {
        FluidState fluidState;
        if (this.panoramicMode) {
            return 90.0;
        }
        double d = 70.0;
        if (bl) {
            d = this.minecraft.options.fov;
            d *= (double)Mth.lerp(f, this.oldFov, this.fov);
        }
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).getHealth() <= 0.0f) {
            float g = Math.min((float)((LivingEntity)camera.getEntity()).deathTime + f, 20.0f);
            d /= (double)((1.0f - 500.0f / (g + 500.0f)) * 2.0f + 1.0f);
        }
        if (!(fluidState = camera.getFluidInCamera()).isEmpty()) {
            d = d * 60.0 / 70.0;
        }
        return d;
    }

    private void bobHurt(float f) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
            float h;
            LivingEntity livingEntity = (LivingEntity)this.minecraft.getCameraEntity();
            float g = (float)livingEntity.hurtTime - f;
            if (livingEntity.getHealth() <= 0.0f) {
                h = Math.min((float)livingEntity.deathTime + f, 20.0f);
                RenderSystem.rotatef(40.0f - 8000.0f / (h + 200.0f), 0.0f, 0.0f, 1.0f);
            }
            if (g < 0.0f) {
                return;
            }
            g /= (float)livingEntity.hurtDuration;
            g = Mth.sin(g * g * g * g * (float)Math.PI);
            h = livingEntity.hurtDir;
            RenderSystem.rotatef(-h, 0.0f, 1.0f, 0.0f);
            RenderSystem.rotatef(-g * 14.0f, 0.0f, 0.0f, 1.0f);
            RenderSystem.rotatef(h, 0.0f, 1.0f, 0.0f);
        }
    }

    private void bobView(float f) {
        if (!(this.minecraft.getCameraEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)this.minecraft.getCameraEntity();
        float g = player.walkDist - player.walkDistO;
        float h = -(player.walkDist + g * f);
        float i = Mth.lerp(f, player.oBob, player.bob);
        RenderSystem.translatef(Mth.sin(h * (float)Math.PI) * i * 0.5f, -Math.abs(Mth.cos(h * (float)Math.PI) * i), 0.0f);
        RenderSystem.rotatef(Mth.sin(h * (float)Math.PI) * i * 3.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.rotatef(Math.abs(Mth.cos(h * (float)Math.PI - 0.2f) * i) * 5.0f, 1.0f, 0.0f, 0.0f);
    }

    private void setupCamera(float f) {
        float g;
        this.renderDistance = this.minecraft.options.renderDistance * 16;
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        if (this.zoom != 1.0) {
            RenderSystem.translatef((float)this.zoom_x, (float)(-this.zoom_y), 0.0f);
            RenderSystem.scaled(this.zoom, this.zoom, 1.0);
        }
        RenderSystem.multMatrix(Matrix4f.perspective(this.getFov(this.mainCamera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05f, this.renderDistance * Mth.SQRT_OF_TWO));
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        this.bobHurt(f);
        if (this.minecraft.options.bobView) {
            this.bobView(f);
        }
        if ((g = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime)) > 0.0f) {
            int i = 20;
            if (this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
                i = 7;
            }
            float h = 5.0f / (g * g + 5.0f) - g * 0.04f;
            h *= h;
            RenderSystem.rotatef(((float)this.tick + f) * (float)i, 0.0f, 1.0f, 1.0f);
            RenderSystem.scalef(1.0f / h, 1.0f, 1.0f);
            RenderSystem.rotatef(-((float)this.tick + f) * (float)i, 0.0f, 1.0f, 1.0f);
        }
    }

    private void renderItemInHand(Camera camera, float f) {
        boolean bl;
        if (this.panoramicMode) {
            return;
        }
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective(this.getFov(camera, f, false), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05f, this.renderDistance * 2.0f));
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.pushMatrix();
        this.bobHurt(f);
        if (this.minecraft.options.bobView) {
            this.bobView(f);
        }
        boolean bl2 = bl = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
        if (this.minecraft.options.thirdPersonView == 0 && !bl && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.turnOnLightLayer();
            this.itemInHandRenderer.render(f);
            this.turnOffLightLayer();
        }
        RenderSystem.popMatrix();
        if (this.minecraft.options.thirdPersonView == 0 && !bl) {
            this.itemInHandRenderer.renderScreenEffect(f);
            this.bobHurt(f);
        }
        if (this.minecraft.options.bobView) {
            this.bobView(f);
        }
    }

    public void turnOffLightLayer() {
        this.lightTexture.turnOffLightLayer();
    }

    public void turnOnLightLayer() {
        this.lightTexture.turnOnLightLayer();
    }

    public float getNightVisionScale(LivingEntity livingEntity, float f) {
        int i = livingEntity.getEffect(MobEffects.NIGHT_VISION).getDuration();
        if (i > 200) {
            return 1.0f;
        }
        return 0.7f + Mth.sin(((float)i - f) * (float)Math.PI * 0.2f) * 0.3f;
    }

    public void render(float f, long l, boolean bl) {
        if (this.minecraft.isWindowActive() || !this.minecraft.options.pauseOnLostFocus || this.minecraft.options.touchscreen && this.minecraft.mouseHandler.isRightPressed()) {
            this.lastActiveTime = Util.getMillis();
        } else if (Util.getMillis() - this.lastActiveTime > 500L) {
            this.minecraft.pauseGame(false);
        }
        if (this.minecraft.noRender) {
            return;
        }
        int i = (int)(this.minecraft.mouseHandler.xpos() * (double)this.minecraft.window.getGuiScaledWidth() / (double)this.minecraft.window.getScreenWidth());
        int j = (int)(this.minecraft.mouseHandler.ypos() * (double)this.minecraft.window.getGuiScaledHeight() / (double)this.minecraft.window.getScreenHeight());
        int k = this.minecraft.options.framerateLimit;
        if (bl && this.minecraft.level != null) {
            this.minecraft.getProfiler().push("level");
            int m = Math.min(Minecraft.getAverageFps(), k);
            m = Math.max(m, 60);
            long n = Util.getNanos() - l;
            long o = Math.max((long)(1000000000 / m / 4) - n, 0L);
            this.renderLevel(f, Util.getNanos() + o);
            if (this.minecraft.hasSingleplayerServer() && this.lastScreenshotAttempt < Util.getMillis() - 1000L) {
                this.lastScreenshotAttempt = Util.getMillis();
                if (!this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
                    this.takeAutoScreenshot();
                }
            }
            this.minecraft.levelRenderer.doEntityOutline();
            if (this.postEffect != null && this.effectActive) {
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                this.postEffect.process(f);
                RenderSystem.popMatrix();
            }
            this.minecraft.getMainRenderTarget().bindWrite(true);
            this.minecraft.getProfiler().popPush("gui");
            if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
                RenderSystem.alphaFunc(516, 0.1f);
                this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
                this.renderItemActivationAnimation(this.minecraft.window.getGuiScaledWidth(), this.minecraft.window.getGuiScaledHeight(), f);
                this.minecraft.gui.render(f);
            }
            this.minecraft.getProfiler().pop();
        } else {
            RenderSystem.viewport(0, 0, this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
        }
        if (this.minecraft.overlay != null) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            try {
                this.minecraft.overlay.render(i, j, this.minecraft.getDeltaFrameTime());
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering overlay");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Overlay render details");
                crashReportCategory.setDetail("Overlay name", () -> this.minecraft.overlay.getClass().getCanonicalName());
                throw new ReportedException(crashReport);
            }
        }
        if (this.minecraft.screen != null) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            try {
                this.minecraft.screen.render(i, j, this.minecraft.getDeltaFrameTime());
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering screen");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Screen render details");
                crashReportCategory.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                crashReportCategory.setDetail("Mouse location", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos()));
                crashReportCategory.setDetail("Screen size", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.minecraft.window.getGuiScaledWidth(), this.minecraft.window.getGuiScaledHeight(), this.minecraft.window.getWidth(), this.minecraft.window.getHeight(), this.minecraft.window.getGuiScale()));
                throw new ReportedException(crashReport);
            }
        }
    }

    private void takeAutoScreenshot() {
        if (this.minecraft.levelRenderer.countRenderedChunks() > 10 && this.minecraft.levelRenderer.hasRenderedAllChunks() && !this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
            NativeImage nativeImage = Screenshot.takeScreenshot(this.minecraft.window.getWidth(), this.minecraft.window.getHeight(), this.minecraft.getMainRenderTarget());
            SimpleResource.IO_EXECUTOR.execute(() -> {
                int i = nativeImage.getWidth();
                int j = nativeImage.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }
                try (NativeImage nativeImage2 = new NativeImage(64, 64, false);){
                    nativeImage.resizeSubRectTo(k, l, i, j, nativeImage2);
                    nativeImage2.writeToFile(this.minecraft.getSingleplayerServer().getWorldScreenshotFile());
                } catch (IOException iOException) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable)iOException);
                } finally {
                    nativeImage.close();
                }
            });
        }
    }

    private boolean shouldRenderBlockOutline() {
        boolean bl;
        if (!this.renderBlockOutline) {
            return false;
        }
        Entity entity = this.minecraft.getCameraEntity();
        boolean bl2 = bl = entity instanceof Player && !this.minecraft.options.hideGui;
        if (bl && !((Player)entity).abilities.mayBuild) {
            ItemStack itemStack = ((LivingEntity)entity).getMainHandItem();
            HitResult hitResult = this.minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockState = this.minecraft.level.getBlockState(blockPos);
                if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                    bl = blockState.getMenuProvider(this.minecraft.level, blockPos) != null;
                } else {
                    BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, blockPos, false);
                    bl = !itemStack.isEmpty() && (itemStack.hasAdventureModeBreakTagForBlock(this.minecraft.level.getTagManager(), blockInWorld) || itemStack.hasAdventureModePlaceTagForBlock(this.minecraft.level.getTagManager(), blockInWorld));
                }
            }
        }
        return bl;
    }

    public void renderLevel(float f, long l) {
        this.lightTexture.updateLightTexture(f);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }
        this.pick(f);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(516, 0.5f);
        this.minecraft.getProfiler().push("center");
        this.render(f, l);
        this.minecraft.getProfiler().pop();
    }

    private void render(float f, long l) {
        LevelRenderer levelRenderer = this.minecraft.levelRenderer;
        ParticleEngine particleEngine = this.minecraft.particleEngine;
        boolean bl = this.shouldRenderBlockOutline();
        RenderSystem.enableCull();
        this.minecraft.getProfiler().popPush("camera");
        this.setupCamera(f);
        Camera camera = this.mainCamera;
        camera.setup(this.minecraft.level, this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity(), this.minecraft.options.thirdPersonView > 0, this.minecraft.options.thirdPersonView == 2, f);
        FrustumData frustumData = Frustum.getFrustum();
        levelRenderer.prepare(camera);
        this.minecraft.getProfiler().popPush("clear");
        RenderSystem.viewport(0, 0, this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
        this.fog.setupClearColor(camera, f);
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        this.minecraft.getProfiler().popPush("culling");
        FrustumCuller culler = new FrustumCuller(frustumData);
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double g = camera.getPosition().z;
        culler.prepare(d, e, g);
        if (this.minecraft.options.renderDistance >= 4) {
            this.fog.setupFog(camera, -1);
            this.minecraft.getProfiler().popPush("sky");
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(Matrix4f.perspective(this.getFov(camera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05f, this.renderDistance * 2.0f));
            RenderSystem.matrixMode(5888);
            levelRenderer.renderSky(f);
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(Matrix4f.perspective(this.getFov(camera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05f, this.renderDistance * Mth.SQRT_OF_TWO));
            RenderSystem.matrixMode(5888);
        }
        this.fog.setupFog(camera, 0);
        RenderSystem.shadeModel(7425);
        if (camera.getPosition().y < 128.0) {
            this.prepareAndRenderClouds(camera, levelRenderer, f, d, e, g);
        }
        this.minecraft.getProfiler().popPush("prepareterrain");
        this.fog.setupFog(camera, 0);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        Lighting.turnOff();
        this.minecraft.getProfiler().popPush("terrain_setup");
        this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        levelRenderer.setupRender(camera, culler, this.frameId++, this.minecraft.player.isSpectator());
        this.minecraft.getProfiler().popPush("updatechunks");
        this.minecraft.levelRenderer.compileChunksUntil(l);
        this.minecraft.getProfiler().popPush("terrain");
        RenderSystem.matrixMode(5888);
        RenderSystem.pushMatrix();
        RenderSystem.disableAlphaTest();
        levelRenderer.render(BlockLayer.SOLID, camera);
        RenderSystem.enableAlphaTest();
        levelRenderer.render(BlockLayer.CUTOUT_MIPPED, camera);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        levelRenderer.render(BlockLayer.CUTOUT, camera);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
        RenderSystem.shadeModel(7424);
        RenderSystem.alphaFunc(516, 0.1f);
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        Lighting.turnOn();
        this.minecraft.getProfiler().popPush("entities");
        levelRenderer.renderEntities(camera, culler, f);
        Lighting.turnOff();
        this.turnOffLightLayer();
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
        if (bl && this.minecraft.hitResult != null) {
            RenderSystem.disableAlphaTest();
            this.minecraft.getProfiler().popPush("outline");
            levelRenderer.renderHitOutline(camera, this.minecraft.hitResult, 0);
            RenderSystem.enableAlphaTest();
        }
        this.minecraft.debugRenderer.render(l);
        this.minecraft.getProfiler().popPush("destroyProgress");
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        levelRenderer.renderDestroyAnimation(Tesselator.getInstance(), Tesselator.getInstance().getBuilder(), camera);
        this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
        RenderSystem.disableBlend();
        this.turnOnLightLayer();
        this.fog.setupFog(camera, 0);
        this.minecraft.getProfiler().popPush("particles");
        particleEngine.render(camera, f);
        this.turnOffLightLayer();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();
        this.minecraft.getProfiler().popPush("weather");
        this.renderSnowAndRain(f);
        RenderSystem.depthMask(true);
        levelRenderer.renderWorldBounds(camera, f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.alphaFunc(516, 0.1f);
        this.fog.setupFog(camera, 0);
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.shadeModel(7425);
        this.minecraft.getProfiler().popPush("translucent");
        levelRenderer.render(BlockLayer.TRANSLUCENT, camera);
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableFog();
        if (camera.getPosition().y >= 128.0) {
            this.minecraft.getProfiler().popPush("aboveClouds");
            this.prepareAndRenderClouds(camera, levelRenderer, f, d, e, g);
        }
        this.minecraft.getProfiler().popPush("hand");
        if (this.renderHand) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            this.renderItemInHand(camera, f);
        }
    }

    private void prepareAndRenderClouds(Camera camera, LevelRenderer levelRenderer, float f, double d, double e, double g) {
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            this.minecraft.getProfiler().popPush("clouds");
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(Matrix4f.perspective(this.getFov(camera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05f, this.renderDistance * 4.0f));
            RenderSystem.matrixMode(5888);
            RenderSystem.pushMatrix();
            this.fog.setupFog(camera, 0);
            levelRenderer.renderClouds(f, d, e, g);
            RenderSystem.disableFog();
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            RenderSystem.multMatrix(Matrix4f.perspective(this.getFov(camera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05f, this.renderDistance * Mth.SQRT_OF_TWO));
            RenderSystem.matrixMode(5888);
        }
    }

    private void tickRain() {
        float f = this.minecraft.level.getRainLevel(1.0f);
        if (!this.minecraft.options.fancyGraphics) {
            f /= 2.0f;
        }
        if (f == 0.0f) {
            return;
        }
        this.random.setSeed((long)this.tick * 312987231L);
        MultiPlayerLevel levelReader = this.minecraft.level;
        BlockPos blockPos = new BlockPos(this.mainCamera.getPosition());
        int i = 10;
        double d = 0.0;
        double e = 0.0;
        double g = 0.0;
        int j = 0;
        int k = (int)(100.0f * f * f);
        if (this.minecraft.options.particles == ParticleStatus.DECREASED) {
            k >>= 1;
        } else if (this.minecraft.options.particles == ParticleStatus.MINIMAL) {
            k = 0;
        }
        for (int l = 0; l < k; ++l) {
            double q;
            double p;
            double o;
            BlockPos blockPos2 = levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(this.random.nextInt(10) - this.random.nextInt(10), 0, this.random.nextInt(10) - this.random.nextInt(10)));
            Biome biome = levelReader.getBiome(blockPos2);
            BlockPos blockPos3 = blockPos2.below();
            if (blockPos2.getY() > blockPos.getY() + 10 || blockPos2.getY() < blockPos.getY() - 10 || biome.getPrecipitation() != Biome.Precipitation.RAIN || !(biome.getTemperature(blockPos2) >= 0.15f)) continue;
            double h = this.random.nextDouble();
            double m = this.random.nextDouble();
            BlockState blockState = levelReader.getBlockState(blockPos3);
            FluidState fluidState = levelReader.getFluidState(blockPos2);
            VoxelShape voxelShape = blockState.getCollisionShape(levelReader, blockPos3);
            double n = voxelShape.max(Direction.Axis.Y, h, m);
            if (n >= (o = (double)fluidState.getHeight(levelReader, blockPos2))) {
                p = n;
                q = voxelShape.min(Direction.Axis.Y, h, m);
            } else {
                p = 0.0;
                q = 0.0;
            }
            if (!(p > -1.7976931348623157E308)) continue;
            if (fluidState.is(FluidTags.LAVA) || blockState.getBlock() == Blocks.MAGMA_BLOCK || blockState.getBlock() == Blocks.CAMPFIRE && blockState.getValue(CampfireBlock.LIT).booleanValue()) {
                this.minecraft.level.addParticle(ParticleTypes.SMOKE, (double)blockPos2.getX() + h, (double)((float)blockPos2.getY() + 0.1f) - q, (double)blockPos2.getZ() + m, 0.0, 0.0, 0.0);
                continue;
            }
            if (this.random.nextInt(++j) == 0) {
                d = (double)blockPos3.getX() + h;
                e = (double)((float)blockPos3.getY() + 0.1f) + p - 1.0;
                g = (double)blockPos3.getZ() + m;
            }
            this.minecraft.level.addParticle(ParticleTypes.RAIN, (double)blockPos3.getX() + h, (double)((float)blockPos3.getY() + 0.1f) + p, (double)blockPos3.getZ() + m, 0.0, 0.0, 0.0);
        }
        if (j > 0 && this.random.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (e > (double)(blockPos.getY() + 1) && levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor(blockPos.getY())) {
                this.minecraft.level.playLocalSound(d, e, g, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1f, 0.5f, false);
            } else {
                this.minecraft.level.playLocalSound(d, e, g, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2f, 1.0f, false);
            }
        }
    }

    protected void renderSnowAndRain(float f) {
        float g = this.minecraft.level.getRainLevel(f);
        if (g <= 0.0f) {
            return;
        }
        this.turnOnLightLayer();
        MultiPlayerLevel level = this.minecraft.level;
        int i = Mth.floor(this.mainCamera.getPosition().x);
        int j = Mth.floor(this.mainCamera.getPosition().y);
        int k = Mth.floor(this.mainCamera.getPosition().z);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.normal3f(0.0f, 1.0f, 0.0f);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.alphaFunc(516, 0.1f);
        double d = this.mainCamera.getPosition().x;
        double e = this.mainCamera.getPosition().y;
        double h = this.mainCamera.getPosition().z;
        int l = Mth.floor(e);
        int m = 5;
        if (this.minecraft.options.fancyGraphics) {
            m = 10;
        }
        int n = -1;
        float o = (float)this.tick + f;
        bufferBuilder.offset(-d, -e, -h);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int p = k - m; p <= k + m; ++p) {
            for (int q = i - m; q <= i + m; ++q) {
                double ab;
                double aa;
                double z;
                int x;
                int r = (p - k + 16) * 32 + q - i + 16;
                double s = (double)this.rainSizeX[r] * 0.5;
                double t = (double)this.rainSizeZ[r] * 0.5;
                mutableBlockPos.set(q, 0, p);
                Biome biome = level.getBiome(mutableBlockPos);
                if (biome.getPrecipitation() == Biome.Precipitation.NONE) continue;
                int u = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos).getY();
                int v = j - m;
                int w = j + m;
                if (v < u) {
                    v = u;
                }
                if (w < u) {
                    w = u;
                }
                if ((x = u) < l) {
                    x = l;
                }
                if (v == w) continue;
                this.random.setSeed(q * q * 3121 + q * 45238971 ^ p * p * 418711 + p * 13761);
                mutableBlockPos.set(q, v, p);
                float y = biome.getTemperature(mutableBlockPos);
                if (y >= 0.15f) {
                    if (n != 0) {
                        if (n >= 0) {
                            tesselator.end();
                        }
                        n = 0;
                        this.minecraft.getTextureManager().bind(RAIN_LOCATION);
                        bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
                    }
                    z = -((double)(this.tick + q * q * 3121 + q * 45238971 + p * p * 418711 + p * 13761 & 0x1F) + (double)f) / 32.0 * (3.0 + this.random.nextDouble());
                    aa = (double)((float)q + 0.5f) - this.mainCamera.getPosition().x;
                    ab = (double)((float)p + 0.5f) - this.mainCamera.getPosition().z;
                    float ac = Mth.sqrt(aa * aa + ab * ab) / (float)m;
                    float ad = ((1.0f - ac * ac) * 0.5f + 0.5f) * g;
                    mutableBlockPos.set(q, x, p);
                    int ae = level.getLightColor(mutableBlockPos);
                    int af = ae >> 16 & 0xFFFF;
                    int ag = ae & 0xFFFF;
                    bufferBuilder.vertex((double)q - s + 0.5, w, (double)p - t + 0.5).uv(0.0, (double)v * 0.25 + z).color(1.0f, 1.0f, 1.0f, ad).uv2(af, ag).endVertex();
                    bufferBuilder.vertex((double)q + s + 0.5, w, (double)p + t + 0.5).uv(1.0, (double)v * 0.25 + z).color(1.0f, 1.0f, 1.0f, ad).uv2(af, ag).endVertex();
                    bufferBuilder.vertex((double)q + s + 0.5, v, (double)p + t + 0.5).uv(1.0, (double)w * 0.25 + z).color(1.0f, 1.0f, 1.0f, ad).uv2(af, ag).endVertex();
                    bufferBuilder.vertex((double)q - s + 0.5, v, (double)p - t + 0.5).uv(0.0, (double)w * 0.25 + z).color(1.0f, 1.0f, 1.0f, ad).uv2(af, ag).endVertex();
                    continue;
                }
                if (n != 1) {
                    if (n >= 0) {
                        tesselator.end();
                    }
                    n = 1;
                    this.minecraft.getTextureManager().bind(SNOW_LOCATION);
                    bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
                }
                z = -((float)(this.tick & 0x1FF) + f) / 512.0f;
                aa = this.random.nextDouble() + (double)o * 0.01 * (double)((float)this.random.nextGaussian());
                ab = this.random.nextDouble() + (double)(o * (float)this.random.nextGaussian()) * 0.001;
                double ah = (double)((float)q + 0.5f) - this.mainCamera.getPosition().x;
                double ai = (double)((float)p + 0.5f) - this.mainCamera.getPosition().z;
                float aj = Mth.sqrt(ah * ah + ai * ai) / (float)m;
                float ak = ((1.0f - aj * aj) * 0.3f + 0.5f) * g;
                mutableBlockPos.set(q, x, p);
                int al = (level.getLightColor(mutableBlockPos) * 3 + 0xF000F0) / 4;
                int am = al >> 16 & 0xFFFF;
                int an = al & 0xFFFF;
                bufferBuilder.vertex((double)q - s + 0.5, w, (double)p - t + 0.5).uv(0.0 + aa, (double)v * 0.25 + z + ab).color(1.0f, 1.0f, 1.0f, ak).uv2(am, an).endVertex();
                bufferBuilder.vertex((double)q + s + 0.5, w, (double)p + t + 0.5).uv(1.0 + aa, (double)v * 0.25 + z + ab).color(1.0f, 1.0f, 1.0f, ak).uv2(am, an).endVertex();
                bufferBuilder.vertex((double)q + s + 0.5, v, (double)p + t + 0.5).uv(1.0 + aa, (double)w * 0.25 + z + ab).color(1.0f, 1.0f, 1.0f, ak).uv2(am, an).endVertex();
                bufferBuilder.vertex((double)q - s + 0.5, v, (double)p - t + 0.5).uv(0.0 + aa, (double)w * 0.25 + z + ab).color(1.0f, 1.0f, 1.0f, ak).uv2(am, an).endVertex();
            }
        }
        if (n >= 0) {
            tesselator.end();
        }
        bufferBuilder.offset(0.0, 0.0, 0.0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.alphaFunc(516, 0.1f);
        this.turnOffLightLayer();
    }

    public void resetFogColor(boolean bl) {
        this.fog.resetFogColor(bl);
    }

    public void resetData() {
        this.itemActivationItem = null;
        this.mapRenderer.resetData();
        this.mainCamera.reset();
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    public static void renderNameTagInWorld(Font font, String string, float f, float g, float h, int i, float j, float k, boolean bl) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(f, g, h);
        RenderSystem.normal3f(0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(-j, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(k, 1.0f, 0.0f, 0.0f);
        RenderSystem.scalef(-0.025f, -0.025f, 0.025f);
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        if (!bl) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int l = font.width(string) / 2;
        RenderSystem.disableTexture();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        float m = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        bufferBuilder.vertex(-l - 1, -1 + i, 0.0).color(0.0f, 0.0f, 0.0f, m).endVertex();
        bufferBuilder.vertex(-l - 1, 8 + i, 0.0).color(0.0f, 0.0f, 0.0f, m).endVertex();
        bufferBuilder.vertex(l + 1, 8 + i, 0.0).color(0.0f, 0.0f, 0.0f, m).endVertex();
        bufferBuilder.vertex(l + 1, -1 + i, 0.0).color(0.0f, 0.0f, 0.0f, m).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
        if (!bl) {
            font.draw(string, -font.width(string) / 2, i, 0x20FFFFFF);
            RenderSystem.enableDepthTest();
        }
        RenderSystem.depthMask(true);
        font.draw(string, -font.width(string) / 2, i, bl ? 0x20FFFFFF : -1);
        RenderSystem.enableLighting();
        RenderSystem.disableBlend();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.popMatrix();
    }

    public void displayItemActivation(ItemStack itemStack) {
        this.itemActivationItem = itemStack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = this.random.nextFloat() * 2.0f - 1.0f;
        this.itemActivationOffY = this.random.nextFloat() * 2.0f - 1.0f;
    }

    private void renderItemActivationAnimation(int i, int j, float f) {
        if (this.itemActivationItem == null || this.itemActivationTicks <= 0) {
            return;
        }
        int k = 40 - this.itemActivationTicks;
        float g = ((float)k + f) / 40.0f;
        float h = g * g;
        float l = g * h;
        float m = 10.25f * l * h - 24.95f * h * h + 25.5f * l - 13.8f * h + 4.0f * g;
        float n = m * (float)Math.PI;
        float o = this.itemActivationOffX * (float)(i / 4);
        float p = this.itemActivationOffY * (float)(j / 4);
        RenderSystem.enableAlphaTest();
        RenderSystem.pushMatrix();
        RenderSystem.pushLightingAttributes();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        Lighting.turnOn();
        RenderSystem.translatef((float)(i / 2) + o * Mth.abs(Mth.sin(n * 2.0f)), (float)(j / 2) + p * Mth.abs(Mth.sin(n * 2.0f)), -50.0f);
        float q = 50.0f + 175.0f * Mth.sin(n);
        RenderSystem.scalef(q, -q, q);
        RenderSystem.rotatef(900.0f * Mth.abs(Mth.sin(n)), 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(6.0f * Mth.cos(g * 8.0f), 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(6.0f * Mth.cos(g * 8.0f), 0.0f, 0.0f, 1.0f);
        this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemTransforms.TransformType.FIXED);
        RenderSystem.popAttributes();
        RenderSystem.popMatrix();
        Lighting.turnOff();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public float getDarkenWorldAmount(float f) {
        return Mth.lerp(f, this.darkenWorldAmountO, this.darkenWorldAmount);
    }

    public float getRenderDistance() {
        return this.renderDistance;
    }

    public Camera getMainCamera() {
        return this.mainCamera;
    }
}

