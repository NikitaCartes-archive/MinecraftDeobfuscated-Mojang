package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.culling.Culler;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

@Environment(EnvType.CLIENT)
public class GameRenderer implements AutoCloseable, ResourceManagerReloadListener {
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
	private static final ResourceLocation[] EFFECTS = new ResourceLocation[]{
		new ResourceLocation("shaders/post/notch.json"),
		new ResourceLocation("shaders/post/fxaa.json"),
		new ResourceLocation("shaders/post/art.json"),
		new ResourceLocation("shaders/post/bumpy.json"),
		new ResourceLocation("shaders/post/blobs2.json"),
		new ResourceLocation("shaders/post/pencil.json"),
		new ResourceLocation("shaders/post/color_convolve.json"),
		new ResourceLocation("shaders/post/deconverge.json"),
		new ResourceLocation("shaders/post/flip.json"),
		new ResourceLocation("shaders/post/invert.json"),
		new ResourceLocation("shaders/post/ntsc.json"),
		new ResourceLocation("shaders/post/outline.json"),
		new ResourceLocation("shaders/post/phosphor.json"),
		new ResourceLocation("shaders/post/scan_pincushion.json"),
		new ResourceLocation("shaders/post/sobel.json"),
		new ResourceLocation("shaders/post/bits.json"),
		new ResourceLocation("shaders/post/desaturate.json"),
		new ResourceLocation("shaders/post/green.json"),
		new ResourceLocation("shaders/post/blur.json"),
		new ResourceLocation("shaders/post/wobble.json"),
		new ResourceLocation("shaders/post/blobs.json"),
		new ResourceLocation("shaders/post/antialias.json"),
		new ResourceLocation("shaders/post/creeper.json"),
		new ResourceLocation("shaders/post/spider.json")
	};
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

		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				float f = (float)(j - 16);
				float g = (float)(i - 16);
				float h = Mth.sqrt(f * f + g * g);
				this.rainSizeX[i << 5 | j] = -g / h;
				this.rainSizeZ[i << 5 | j] = f / h;
			}
		}
	}

	public void close() {
		this.lightTexture.close();
		this.mapRenderer.close();
		this.shutdownEffect();
	}

	public boolean postEffectActive() {
		return GLX.usePostProcess && this.postEffect != null;
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
		if (GLX.usePostProcess) {
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
	}

	private void loadEffect(ResourceLocation resourceLocation) {
		if (this.postEffect != null) {
			this.postEffect.close();
		}

		try {
			this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), resourceLocation);
			this.postEffect.resize(this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
			this.effectActive = true;
		} catch (IOException var3) {
			LOGGER.warn("Failed to load shader: {}", resourceLocation, var3);
			this.effectIndex = EFFECT_NONE;
			this.effectActive = false;
		} catch (JsonSyntaxException var4) {
			LOGGER.warn("Failed to load shader: {}", resourceLocation, var4);
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
		if (GLX.usePostProcess && ProgramManager.getInstance() == null) {
			ProgramManager.createInstance();
		}

		this.tickFov();
		this.lightTexture.tick();
		if (this.minecraft.getCameraEntity() == null) {
			this.minecraft.setCameraEntity(this.minecraft.player);
		}

		this.mainCamera.tick();
		this.tick++;
		this.itemInHandRenderer.tick();
		this.tickRain();
		this.darkenWorldAmountO = this.darkenWorldAmount;
		if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
			this.darkenWorldAmount += 0.05F;
			if (this.darkenWorldAmount > 1.0F) {
				this.darkenWorldAmount = 1.0F;
			}
		} else if (this.darkenWorldAmount > 0.0F) {
			this.darkenWorldAmount -= 0.0125F;
		}

		if (this.itemActivationTicks > 0) {
			this.itemActivationTicks--;
			if (this.itemActivationTicks == 0) {
				this.itemActivationItem = null;
			}
		}
	}

	public PostChain currentEffect() {
		return this.postEffect;
	}

	public void resize(int i, int j) {
		if (GLX.usePostProcess) {
			if (this.postEffect != null) {
				this.postEffect.resize(i, j);
			}

			this.minecraft.levelRenderer.resize(i, j);
		}
	}

	public void pick(float f) {
		Entity entity = this.minecraft.getCameraEntity();
		if (entity != null) {
			if (this.minecraft.level != null) {
				this.minecraft.getProfiler().push("pick");
				this.minecraft.crosshairPickEntity = null;
				double d = (double)this.minecraft.gameMode.getPickRange();
				this.minecraft.hitResult = entity.pick(d, f, false);
				Vec3 vec3 = entity.getEyePosition(f);
				boolean bl = false;
				int i = 3;
				double e = d;
				if (this.minecraft.gameMode.hasFarPickRange()) {
					e = 6.0;
					d = e;
				} else {
					if (d > 3.0) {
						bl = true;
					}

					d = d;
				}

				e *= e;
				if (this.minecraft.hitResult != null) {
					e = this.minecraft.hitResult.getLocation().distanceToSqr(vec3);
				}

				Vec3 vec32 = entity.getViewVector(1.0F);
				Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
				float g = 1.0F;
				AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(d)).inflate(1.0, 1.0, 1.0);
				EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, entityx -> !entityx.isSpectator() && entityx.isPickable(), e);
				if (entityHitResult != null) {
					Entity entity2 = entityHitResult.getEntity();
					Vec3 vec34 = entityHitResult.getLocation();
					double h = vec3.distanceToSqr(vec34);
					if (bl && h > 9.0) {
						this.minecraft.hitResult = BlockHitResult.miss(vec34, Direction.getNearest(vec32.x, vec32.y, vec32.z), new BlockPos(vec34));
					} else if (h < e || this.minecraft.hitResult == null) {
						this.minecraft.hitResult = entityHitResult;
						if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrame) {
							this.minecraft.crosshairPickEntity = entity2;
						}
					}
				}

				this.minecraft.getProfiler().pop();
			}
		}
	}

	private void tickFov() {
		float f = 1.0F;
		if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer) {
			AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)this.minecraft.getCameraEntity();
			f = abstractClientPlayer.getFieldOfViewModifier();
		}

		this.oldFov = this.fov;
		this.fov = this.fov + (f - this.fov) * 0.5F;
		if (this.fov > 1.5F) {
			this.fov = 1.5F;
		}

		if (this.fov < 0.1F) {
			this.fov = 0.1F;
		}
	}

	private double getFov(Camera camera, float f, boolean bl) {
		if (this.panoramicMode) {
			return 90.0;
		} else {
			double d = 70.0;
			if (bl) {
				d = this.minecraft.options.fov;
				d *= (double)Mth.lerp(f, this.oldFov, this.fov);
			}

			if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).getHealth() <= 0.0F) {
				float g = (float)((LivingEntity)camera.getEntity()).deathTime + f;
				d /= (double)((1.0F - 500.0F / (g + 500.0F)) * 2.0F + 1.0F);
			}

			FluidState fluidState = camera.getFluidInCamera();
			if (!fluidState.isEmpty()) {
				d = d * 60.0 / 70.0;
			}

			return d;
		}
	}

	private void bobHurt(float f) {
		if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)this.minecraft.getCameraEntity();
			float g = (float)livingEntity.hurtTime - f;
			if (livingEntity.getHealth() <= 0.0F) {
				float h = (float)livingEntity.deathTime + f;
				GlStateManager.rotatef(40.0F - 8000.0F / (h + 200.0F), 0.0F, 0.0F, 1.0F);
			}

			if (g < 0.0F) {
				return;
			}

			g /= (float)livingEntity.hurtDuration;
			g = Mth.sin(g * g * g * g * (float) Math.PI);
			float h = livingEntity.hurtDir;
			GlStateManager.rotatef(-h, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(-g * 14.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotatef(h, 0.0F, 1.0F, 0.0F);
		}
	}

	private void bobView(float f) {
		if (this.minecraft.getCameraEntity() instanceof Player) {
			Player player = (Player)this.minecraft.getCameraEntity();
			float g = player.walkDist - player.walkDistO;
			float h = -(player.walkDist + g * f);
			float i = Mth.lerp(f, player.oBob, player.bob);
			GlStateManager.translatef(Mth.sin(h * (float) Math.PI) * i * 0.5F, -Math.abs(Mth.cos(h * (float) Math.PI) * i), 0.0F);
			GlStateManager.rotatef(Mth.sin(h * (float) Math.PI) * i * 3.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotatef(Math.abs(Mth.cos(h * (float) Math.PI - 0.2F) * i) * 5.0F, 1.0F, 0.0F, 0.0F);
		}
	}

	private void setupCamera(float f) {
		this.renderDistance = (float)(this.minecraft.options.renderDistance * 16);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		if (this.zoom != 1.0) {
			GlStateManager.translatef((float)this.zoom_x, (float)(-this.zoom_y), 0.0F);
			GlStateManager.scaled(this.zoom, this.zoom, 1.0);
		}

		GlStateManager.multMatrix(
			Matrix4f.perspective(
				this.getFov(this.mainCamera, f, true),
				(float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
				0.05F,
				this.renderDistance * Mth.SQRT_OF_TWO
			)
		);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		this.bobHurt(f);
		if (this.minecraft.options.bobView) {
			this.bobView(f);
		}

		float g = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
		if (g > 0.0F) {
			int i = 20;
			if (this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
				i = 7;
			}

			float h = 5.0F / (g * g + 5.0F) - g * 0.04F;
			h *= h;
			GlStateManager.rotatef(((float)this.tick + f) * (float)i, 0.0F, 1.0F, 1.0F);
			GlStateManager.scalef(1.0F / h, 1.0F, 1.0F);
			GlStateManager.rotatef(-((float)this.tick + f) * (float)i, 0.0F, 1.0F, 1.0F);
		}
	}

	private void renderItemInHand(Camera camera, float f) {
		if (!this.panoramicMode) {
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.multMatrix(
				Matrix4f.perspective(
					this.getFov(camera, f, false), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05F, this.renderDistance * 2.0F
				)
			);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.pushMatrix();
			this.bobHurt(f);
			if (this.minecraft.options.bobView) {
				this.bobView(f);
			}

			boolean bl = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
			if (this.minecraft.options.thirdPersonView == 0 && !bl && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
				this.turnOnLightLayer();
				this.itemInHandRenderer.render(f);
				this.turnOffLightLayer();
			}

			GlStateManager.popMatrix();
			if (this.minecraft.options.thirdPersonView == 0 && !bl) {
				this.itemInHandRenderer.renderScreenEffect(f);
				this.bobHurt(f);
			}

			if (this.minecraft.options.bobView) {
				this.bobView(f);
			}
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
		return i > 200 ? 1.0F : 0.7F + Mth.sin(((float)i - f) * (float) Math.PI * 0.2F) * 0.3F;
	}

	public void render(float f, long l, boolean bl) {
		if (!this.minecraft.isWindowActive()
			&& this.minecraft.options.pauseOnLostFocus
			&& (!this.minecraft.options.touchscreen || !this.minecraft.mouseHandler.isRightPressed())) {
			if (Util.getMillis() - this.lastActiveTime > 500L) {
				this.minecraft.pauseGame(false);
			}
		} else {
			this.lastActiveTime = Util.getMillis();
		}

		if (!this.minecraft.noRender) {
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

				if (GLX.usePostProcess) {
					this.minecraft.levelRenderer.doEntityOutline();
					if (this.postEffect != null && this.effectActive) {
						GlStateManager.matrixMode(5890);
						GlStateManager.pushMatrix();
						GlStateManager.loadIdentity();
						this.postEffect.process(f);
						GlStateManager.popMatrix();
					}

					this.minecraft.getMainRenderTarget().bindWrite(true);
				}

				this.minecraft.getProfiler().popPush("gui");
				if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
					GlStateManager.alphaFunc(516, 0.1F);
					this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
					this.renderItemActivationAnimation(this.minecraft.window.getGuiScaledWidth(), this.minecraft.window.getGuiScaledHeight(), f);
					this.minecraft.gui.render(f);
				}

				this.minecraft.getProfiler().pop();
			} else {
				GlStateManager.viewport(0, 0, this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
				GlStateManager.matrixMode(5889);
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(5888);
				GlStateManager.loadIdentity();
				this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
			}

			if (this.minecraft.overlay != null) {
				GlStateManager.clear(256, Minecraft.ON_OSX);

				try {
					this.minecraft.overlay.render(i, j, this.minecraft.getDeltaFrameTime());
				} catch (Throwable var14) {
					CrashReport crashReport = CrashReport.forThrowable(var14, "Rendering overlay");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Overlay render details");
					crashReportCategory.setDetail("Overlay name", (CrashReportDetail<String>)(() -> this.minecraft.overlay.getClass().getCanonicalName()));
					throw new ReportedException(crashReport);
				}
			} else if (this.minecraft.screen != null) {
				GlStateManager.clear(256, Minecraft.ON_OSX);

				try {
					this.minecraft.screen.render(i, j, this.minecraft.getDeltaFrameTime());
				} catch (Throwable var13) {
					CrashReport crashReport = CrashReport.forThrowable(var13, "Rendering screen");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Screen render details");
					crashReportCategory.setDetail("Screen name", (CrashReportDetail<String>)(() -> this.minecraft.screen.getClass().getCanonicalName()));
					crashReportCategory.setDetail(
						"Mouse location",
						(CrashReportDetail<String>)(() -> String.format(
								Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos()
							))
					);
					crashReportCategory.setDetail(
						"Screen size",
						(CrashReportDetail<String>)(() -> String.format(
								Locale.ROOT,
								"Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f",
								this.minecraft.window.getGuiScaledWidth(),
								this.minecraft.window.getGuiScaledHeight(),
								this.minecraft.window.getWidth(),
								this.minecraft.window.getHeight(),
								this.minecraft.window.getGuiScale()
							))
					);
					throw new ReportedException(crashReport);
				}
			}
		}
	}

	private void takeAutoScreenshot() {
		if (this.minecraft.levelRenderer.countRenderedChunks() > 10
			&& this.minecraft.levelRenderer.hasRenderedAllChunks()
			&& !this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
			NativeImage nativeImage = Screenshot.takeScreenshot(
				this.minecraft.window.getWidth(), this.minecraft.window.getHeight(), this.minecraft.getMainRenderTarget()
			);
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

				try (NativeImage nativeImage2 = new NativeImage(64, 64, false)) {
					nativeImage.resizeSubRectTo(k, l, i, j, nativeImage2);
					nativeImage2.writeToFile(this.minecraft.getSingleplayerServer().getWorldScreenshotFile());
				} catch (IOException var27) {
					LOGGER.warn("Couldn't save auto screenshot", (Throwable)var27);
				} finally {
					nativeImage.close();
				}
			});
		}
	}

	private boolean shouldRenderBlockOutline() {
		if (!this.renderBlockOutline) {
			return false;
		} else {
			Entity entity = this.minecraft.getCameraEntity();
			boolean bl = entity instanceof Player && !this.minecraft.options.hideGui;
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
						bl = !itemStack.isEmpty()
							&& (
								itemStack.hasAdventureModeBreakTagForBlock(this.minecraft.level.getTagManager(), blockInWorld)
									|| itemStack.hasAdventureModePlaceTagForBlock(this.minecraft.level.getTagManager(), blockInWorld)
							);
					}
				}
			}

			return bl;
		}
	}

	public void renderLevel(float f, long l) {
		this.lightTexture.updateLightTexture(f);
		if (this.minecraft.getCameraEntity() == null) {
			this.minecraft.setCameraEntity(this.minecraft.player);
		}

		this.pick(f);
		GlStateManager.enableDepthTest();
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.5F);
		this.minecraft.getProfiler().push("center");
		this.render(f, l);
		this.minecraft.getProfiler().pop();
	}

	private void render(float f, long l) {
		LevelRenderer levelRenderer = this.minecraft.levelRenderer;
		ParticleEngine particleEngine = this.minecraft.particleEngine;
		boolean bl = this.shouldRenderBlockOutline();
		GlStateManager.enableCull();
		this.minecraft.getProfiler().popPush("camera");
		this.setupCamera(f);
		Camera camera = this.mainCamera;
		camera.setup(
			this.minecraft.level,
			(Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()),
			this.minecraft.options.thirdPersonView > 0,
			this.minecraft.options.thirdPersonView == 2,
			f
		);
		FrustumData frustumData = Frustum.getFrustum();
		levelRenderer.prepare(camera);
		this.minecraft.getProfiler().popPush("clear");
		GlStateManager.viewport(0, 0, this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
		this.fog.setupClearColor(camera, f);
		GlStateManager.clear(16640, Minecraft.ON_OSX);
		this.minecraft.getProfiler().popPush("culling");
		Culler culler = new FrustumCuller(frustumData);
		double d = camera.getPosition().x;
		double e = camera.getPosition().y;
		double g = camera.getPosition().z;
		culler.prepare(d, e, g);
		if (this.minecraft.options.renderDistance >= 4) {
			this.fog.setupFog(camera, -1);
			this.minecraft.getProfiler().popPush("sky");
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.multMatrix(
				Matrix4f.perspective(
					this.getFov(camera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05F, this.renderDistance * 2.0F
				)
			);
			GlStateManager.matrixMode(5888);
			levelRenderer.renderSky(f);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.multMatrix(
				Matrix4f.perspective(
					this.getFov(camera, f, true),
					(float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
					0.05F,
					this.renderDistance * Mth.SQRT_OF_TWO
				)
			);
			GlStateManager.matrixMode(5888);
		}

		this.fog.setupFog(camera, 0);
		GlStateManager.shadeModel(7425);
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
		GlStateManager.matrixMode(5888);
		GlStateManager.pushMatrix();
		GlStateManager.disableAlphaTest();
		levelRenderer.render(BlockLayer.SOLID, camera);
		GlStateManager.enableAlphaTest();
		levelRenderer.render(BlockLayer.CUTOUT_MIPPED, camera);
		this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
		levelRenderer.render(BlockLayer.CUTOUT, camera);
		this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
		GlStateManager.shadeModel(7424);
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		Lighting.turnOn();
		this.minecraft.getProfiler().popPush("entities");
		levelRenderer.renderEntities(camera, culler, f);
		Lighting.turnOff();
		this.turnOffLightLayer();
		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();
		if (bl && this.minecraft.hitResult != null) {
			GlStateManager.disableAlphaTest();
			this.minecraft.getProfiler().popPush("outline");
			levelRenderer.renderHitOutline(camera, this.minecraft.hitResult, 0);
			GlStateManager.enableAlphaTest();
		}

		if (this.minecraft.debugRenderer.shouldRender()) {
			this.minecraft.debugRenderer.render(l);
		}

		this.minecraft.getProfiler().popPush("destroyProgress");
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
		levelRenderer.renderDestroyAnimation(Tesselator.getInstance(), Tesselator.getInstance().getBuilder(), camera);
		this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
		GlStateManager.disableBlend();
		this.turnOnLightLayer();
		this.fog.setupFog(camera, 0);
		this.minecraft.getProfiler().popPush("particles");
		particleEngine.render(camera, f);
		this.turnOffLightLayer();
		GlStateManager.depthMask(false);
		GlStateManager.enableCull();
		this.minecraft.getProfiler().popPush("weather");
		this.renderSnowAndRain(f);
		GlStateManager.depthMask(true);
		levelRenderer.renderWorldBounds(camera, f);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.alphaFunc(516, 0.1F);
		this.fog.setupFog(camera, 0);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
		GlStateManager.shadeModel(7425);
		this.minecraft.getProfiler().popPush("translucent");
		levelRenderer.render(BlockLayer.TRANSLUCENT, camera);
		GlStateManager.shadeModel(7424);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.disableFog();
		if (camera.getPosition().y >= 128.0) {
			this.minecraft.getProfiler().popPush("aboveClouds");
			this.prepareAndRenderClouds(camera, levelRenderer, f, d, e, g);
		}

		this.minecraft.getProfiler().popPush("hand");
		if (this.renderHand) {
			GlStateManager.clear(256, Minecraft.ON_OSX);
			this.renderItemInHand(camera, f);
		}
	}

	private void prepareAndRenderClouds(Camera camera, LevelRenderer levelRenderer, float f, double d, double e, double g) {
		if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
			this.minecraft.getProfiler().popPush("clouds");
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.multMatrix(
				Matrix4f.perspective(
					this.getFov(camera, f, true), (float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(), 0.05F, this.renderDistance * 4.0F
				)
			);
			GlStateManager.matrixMode(5888);
			GlStateManager.pushMatrix();
			this.fog.setupFog(camera, 0);
			levelRenderer.renderClouds(f, d, e, g);
			GlStateManager.disableFog();
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.multMatrix(
				Matrix4f.perspective(
					this.getFov(camera, f, true),
					(float)this.minecraft.window.getWidth() / (float)this.minecraft.window.getHeight(),
					0.05F,
					this.renderDistance * Mth.SQRT_OF_TWO
				)
			);
			GlStateManager.matrixMode(5888);
		}
	}

	private void tickRain() {
		float f = this.minecraft.level.getRainLevel(1.0F);
		if (!this.minecraft.options.fancyGraphics) {
			f /= 2.0F;
		}

		if (f != 0.0F) {
			this.random.setSeed((long)this.tick * 312987231L);
			LevelReader levelReader = this.minecraft.level;
			BlockPos blockPos = new BlockPos(this.mainCamera.getPosition());
			int i = 10;
			double d = 0.0;
			double e = 0.0;
			double g = 0.0;
			int j = 0;
			int k = (int)(100.0F * f * f);
			if (this.minecraft.options.particles == ParticleStatus.DECREASED) {
				k >>= 1;
			} else if (this.minecraft.options.particles == ParticleStatus.MINIMAL) {
				k = 0;
			}

			for (int l = 0; l < k; l++) {
				BlockPos blockPos2 = levelReader.getHeightmapPos(
					Heightmap.Types.MOTION_BLOCKING, blockPos.offset(this.random.nextInt(10) - this.random.nextInt(10), 0, this.random.nextInt(10) - this.random.nextInt(10))
				);
				Biome biome = levelReader.getBiome(blockPos2);
				BlockPos blockPos3 = blockPos2.below();
				if (blockPos2.getY() <= blockPos.getY() + 10
					&& blockPos2.getY() >= blockPos.getY() - 10
					&& biome.getPrecipitation() == Biome.Precipitation.RAIN
					&& biome.getTemperature(blockPos2) >= 0.15F) {
					double h = this.random.nextDouble();
					double m = this.random.nextDouble();
					BlockState blockState = levelReader.getBlockState(blockPos3);
					FluidState fluidState = levelReader.getFluidState(blockPos2);
					VoxelShape voxelShape = blockState.getCollisionShape(levelReader, blockPos3);
					double n = voxelShape.max(Direction.Axis.Y, h, m);
					double o = (double)fluidState.getHeight(levelReader, blockPos2);
					double p;
					double q;
					if (n >= o) {
						p = n;
						q = voxelShape.min(Direction.Axis.Y, h, m);
					} else {
						p = 0.0;
						q = 0.0;
					}

					if (p > -Double.MAX_VALUE) {
						if (!fluidState.is(FluidTags.LAVA)
							&& blockState.getBlock() != Blocks.MAGMA_BLOCK
							&& (blockState.getBlock() != Blocks.CAMPFIRE || !(Boolean)blockState.getValue(CampfireBlock.LIT))) {
							if (this.random.nextInt(++j) == 0) {
								d = (double)blockPos3.getX() + h;
								e = (double)((float)blockPos3.getY() + 0.1F) + p - 1.0;
								g = (double)blockPos3.getZ() + m;
							}

							this.minecraft
								.level
								.addParticle(
									ParticleTypes.RAIN, (double)blockPos3.getX() + h, (double)((float)blockPos3.getY() + 0.1F) + p, (double)blockPos3.getZ() + m, 0.0, 0.0, 0.0
								);
						} else {
							this.minecraft
								.level
								.addParticle(
									ParticleTypes.SMOKE, (double)blockPos2.getX() + h, (double)((float)blockPos2.getY() + 0.1F) - q, (double)blockPos2.getZ() + m, 0.0, 0.0, 0.0
								);
						}
					}
				}
			}

			if (j > 0 && this.random.nextInt(3) < this.rainSoundTime++) {
				this.rainSoundTime = 0;
				if (e > (double)(blockPos.getY() + 1) && levelReader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float)blockPos.getY())) {
					this.minecraft.level.playLocalSound(d, e, g, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
				} else {
					this.minecraft.level.playLocalSound(d, e, g, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
				}
			}
		}
	}

	protected void renderSnowAndRain(float f) {
		float g = this.minecraft.level.getRainLevel(f);
		if (!(g <= 0.0F)) {
			this.turnOnLightLayer();
			Level level = this.minecraft.level;
			int i = Mth.floor(this.mainCamera.getPosition().x);
			int j = Mth.floor(this.mainCamera.getPosition().y);
			int k = Mth.floor(this.mainCamera.getPosition().z);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			GlStateManager.disableCull();
			GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			GlStateManager.alphaFunc(516, 0.1F);
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
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int p = k - m; p <= k + m; p++) {
				for (int q = i - m; q <= i + m; q++) {
					int r = (p - k + 16) * 32 + q - i + 16;
					double s = (double)this.rainSizeX[r] * 0.5;
					double t = (double)this.rainSizeZ[r] * 0.5;
					mutableBlockPos.set(q, 0, p);
					Biome biome = level.getBiome(mutableBlockPos);
					if (biome.getPrecipitation() != Biome.Precipitation.NONE) {
						int u = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos).getY();
						int v = j - m;
						int w = j + m;
						if (v < u) {
							v = u;
						}

						if (w < u) {
							w = u;
						}

						int x = u;
						if (u < l) {
							x = l;
						}

						if (v != w) {
							this.random.setSeed((long)(q * q * 3121 + q * 45238971 ^ p * p * 418711 + p * 13761));
							mutableBlockPos.set(q, v, p);
							float y = biome.getTemperature(mutableBlockPos);
							if (y >= 0.15F) {
								if (n != 0) {
									if (n >= 0) {
										tesselator.end();
									}

									n = 0;
									this.minecraft.getTextureManager().bind(RAIN_LOCATION);
									bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
								}

								double z = -((double)(this.tick + q * q * 3121 + q * 45238971 + p * p * 418711 + p * 13761 & 31) + (double)f) / 32.0 * (3.0 + this.random.nextDouble());
								double aa = (double)((float)q + 0.5F) - this.mainCamera.getPosition().x;
								double ab = (double)((float)p + 0.5F) - this.mainCamera.getPosition().z;
								float ac = Mth.sqrt(aa * aa + ab * ab) / (float)m;
								float ad = ((1.0F - ac * ac) * 0.5F + 0.5F) * g;
								mutableBlockPos.set(q, x, p);
								int ae = level.getLightColor(mutableBlockPos, 0);
								int af = ae >> 16 & 65535;
								int ag = ae & 65535;
								bufferBuilder.vertex((double)q - s + 0.5, (double)w, (double)p - t + 0.5)
									.uv(0.0, (double)v * 0.25 + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(af, ag)
									.endVertex();
								bufferBuilder.vertex((double)q + s + 0.5, (double)w, (double)p + t + 0.5)
									.uv(1.0, (double)v * 0.25 + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(af, ag)
									.endVertex();
								bufferBuilder.vertex((double)q + s + 0.5, (double)v, (double)p + t + 0.5)
									.uv(1.0, (double)w * 0.25 + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(af, ag)
									.endVertex();
								bufferBuilder.vertex((double)q - s + 0.5, (double)v, (double)p - t + 0.5)
									.uv(0.0, (double)w * 0.25 + z)
									.color(1.0F, 1.0F, 1.0F, ad)
									.uv2(af, ag)
									.endVertex();
							} else {
								if (n != 1) {
									if (n >= 0) {
										tesselator.end();
									}

									n = 1;
									this.minecraft.getTextureManager().bind(SNOW_LOCATION);
									bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
								}

								double z = (double)(-((float)(this.tick & 511) + f) / 512.0F);
								double aa = this.random.nextDouble() + (double)o * 0.01 * (double)((float)this.random.nextGaussian());
								double ab = this.random.nextDouble() + (double)(o * (float)this.random.nextGaussian()) * 0.001;
								double ah = (double)((float)q + 0.5F) - this.mainCamera.getPosition().x;
								double ai = (double)((float)p + 0.5F) - this.mainCamera.getPosition().z;
								float aj = Mth.sqrt(ah * ah + ai * ai) / (float)m;
								float ak = ((1.0F - aj * aj) * 0.3F + 0.5F) * g;
								mutableBlockPos.set(q, x, p);
								int al = (level.getLightColor(mutableBlockPos, 0) * 3 + 15728880) / 4;
								int am = al >> 16 & 65535;
								int an = al & 65535;
								bufferBuilder.vertex((double)q - s + 0.5, (double)w, (double)p - t + 0.5)
									.uv(0.0 + aa, (double)v * 0.25 + z + ab)
									.color(1.0F, 1.0F, 1.0F, ak)
									.uv2(am, an)
									.endVertex();
								bufferBuilder.vertex((double)q + s + 0.5, (double)w, (double)p + t + 0.5)
									.uv(1.0 + aa, (double)v * 0.25 + z + ab)
									.color(1.0F, 1.0F, 1.0F, ak)
									.uv2(am, an)
									.endVertex();
								bufferBuilder.vertex((double)q + s + 0.5, (double)v, (double)p + t + 0.5)
									.uv(1.0 + aa, (double)w * 0.25 + z + ab)
									.color(1.0F, 1.0F, 1.0F, ak)
									.uv2(am, an)
									.endVertex();
								bufferBuilder.vertex((double)q - s + 0.5, (double)v, (double)p - t + 0.5)
									.uv(0.0 + aa, (double)w * 0.25 + z + ab)
									.color(1.0F, 1.0F, 1.0F, ak)
									.uv2(am, an)
									.endVertex();
							}
						}
					}
				}
			}

			if (n >= 0) {
				tesselator.end();
			}

			bufferBuilder.offset(0.0, 0.0, 0.0);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(516, 0.1F);
			this.turnOffLightLayer();
		}
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
		GlStateManager.pushMatrix();
		GlStateManager.translatef(f, g, h);
		GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(-j, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(k, 1.0F, 0.0F, 0.0F);
		GlStateManager.scalef(-0.025F, -0.025F, 0.025F);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		if (!bl) {
			GlStateManager.disableDepthTest();
		}

		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		int l = font.width(string) / 2;
		GlStateManager.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		float m = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
		bufferBuilder.vertex((double)(-l - 1), (double)(-1 + i), 0.0).color(0.0F, 0.0F, 0.0F, m).endVertex();
		bufferBuilder.vertex((double)(-l - 1), (double)(8 + i), 0.0).color(0.0F, 0.0F, 0.0F, m).endVertex();
		bufferBuilder.vertex((double)(l + 1), (double)(8 + i), 0.0).color(0.0F, 0.0F, 0.0F, m).endVertex();
		bufferBuilder.vertex((double)(l + 1), (double)(-1 + i), 0.0).color(0.0F, 0.0F, 0.0F, m).endVertex();
		tesselator.end();
		GlStateManager.enableTexture();
		if (!bl) {
			font.draw(string, (float)(-font.width(string) / 2), (float)i, 553648127);
			GlStateManager.enableDepthTest();
		}

		GlStateManager.depthMask(true);
		font.draw(string, (float)(-font.width(string) / 2), (float)i, bl ? 553648127 : -1);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	public void displayItemActivation(ItemStack itemStack) {
		this.itemActivationItem = itemStack;
		this.itemActivationTicks = 40;
		this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
		this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
	}

	private void renderItemActivationAnimation(int i, int j, float f) {
		if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
			int k = 40 - this.itemActivationTicks;
			float g = ((float)k + f) / 40.0F;
			float h = g * g;
			float l = g * h;
			float m = 10.25F * l * h - 24.95F * h * h + 25.5F * l - 13.8F * h + 4.0F * g;
			float n = m * (float) Math.PI;
			float o = this.itemActivationOffX * (float)(i / 4);
			float p = this.itemActivationOffY * (float)(j / 4);
			GlStateManager.enableAlphaTest();
			GlStateManager.pushMatrix();
			GlStateManager.pushLightingAttributes();
			GlStateManager.enableDepthTest();
			GlStateManager.disableCull();
			Lighting.turnOn();
			GlStateManager.translatef((float)(i / 2) + o * Mth.abs(Mth.sin(n * 2.0F)), (float)(j / 2) + p * Mth.abs(Mth.sin(n * 2.0F)), -50.0F);
			float q = 50.0F + 175.0F * Mth.sin(n);
			GlStateManager.scalef(q, -q, q);
			GlStateManager.rotatef(900.0F * Mth.abs(Mth.sin(n)), 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(6.0F * Mth.cos(g * 8.0F), 1.0F, 0.0F, 0.0F);
			GlStateManager.rotatef(6.0F * Mth.cos(g * 8.0F), 0.0F, 0.0F, 1.0F);
			this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemTransforms.TransformType.FIXED);
			GlStateManager.popAttributes();
			GlStateManager.popMatrix();
			Lighting.turnOff();
			GlStateManager.enableCull();
			GlStateManager.disableDepthTest();
		}
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
