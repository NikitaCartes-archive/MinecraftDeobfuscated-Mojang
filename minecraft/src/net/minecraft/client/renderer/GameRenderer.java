package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class GameRenderer implements AutoCloseable {
	private static final ResourceLocation BLUR_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("blur");
	public static final int MAX_BLUR_RADIUS = 10;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DEPTH_BUFFER_DEBUG = false;
	public static final float PROJECTION_Z_NEAR = 0.05F;
	private static final float GUI_Z_NEAR = 1000.0F;
	private final Minecraft minecraft;
	private final ResourceManager resourceManager;
	private final RandomSource random = RandomSource.create();
	private float renderDistance;
	public final ItemInHandRenderer itemInHandRenderer;
	private final RenderBuffers renderBuffers;
	private int confusionAnimationTick;
	private float fovModifier;
	private float oldFovModifier;
	private float darkenWorldAmount;
	private float darkenWorldAmountO;
	private boolean renderHand = true;
	private boolean renderBlockOutline = true;
	private long lastScreenshotAttempt;
	private boolean hasWorldScreenshot;
	private long lastActiveTime = Util.getMillis();
	private final LightTexture lightTexture;
	private final OverlayTexture overlayTexture = new OverlayTexture();
	private boolean panoramicMode;
	private float zoom = 1.0F;
	private float zoomX;
	private float zoomY;
	public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
	@Nullable
	private ItemStack itemActivationItem;
	private int itemActivationTicks;
	private float itemActivationOffX;
	private float itemActivationOffY;
	private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
	@Nullable
	private ResourceLocation postEffectId;
	private boolean effectActive;
	private final Camera mainCamera = new Camera();

	public GameRenderer(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, ResourceManager resourceManager, RenderBuffers renderBuffers) {
		this.minecraft = minecraft;
		this.resourceManager = resourceManager;
		this.itemInHandRenderer = itemInHandRenderer;
		this.lightTexture = new LightTexture(this, minecraft);
		this.renderBuffers = renderBuffers;
	}

	public void close() {
		this.lightTexture.close();
		this.overlayTexture.close();
		this.resourcePool.close();
	}

	public void setRenderHand(boolean bl) {
		this.renderHand = bl;
	}

	public void setRenderBlockOutline(boolean bl) {
		this.renderBlockOutline = bl;
	}

	public void setPanoramicMode(boolean bl) {
		this.panoramicMode = bl;
	}

	public boolean isPanoramicMode() {
		return this.panoramicMode;
	}

	public void clearPostEffect() {
		this.postEffectId = null;
	}

	public void togglePostEffect() {
		this.effectActive = !this.effectActive;
	}

	public void checkEntityPostEffect(@Nullable Entity entity) {
		this.postEffectId = null;
		if (entity instanceof Creeper) {
			this.setPostEffect(ResourceLocation.withDefaultNamespace("creeper"));
		} else if (entity instanceof Spider) {
			this.setPostEffect(ResourceLocation.withDefaultNamespace("spider"));
		} else if (entity instanceof EnderMan) {
			this.setPostEffect(ResourceLocation.withDefaultNamespace("invert"));
		}
	}

	private void setPostEffect(ResourceLocation resourceLocation) {
		this.postEffectId = resourceLocation;
		this.effectActive = true;
	}

	public void processBlurEffect() {
		float f = (float)this.minecraft.options.getMenuBackgroundBlurriness();
		if (!(f < 1.0F)) {
			PostChain postChain = this.minecraft.getShaderManager().getPostChain(BLUR_POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
			if (postChain != null) {
				postChain.setUniform("Radius", f);
				postChain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
			}
		}
	}

	public void preloadUiShader(ResourceProvider resourceProvider) {
		try {
			this.minecraft
				.getShaderManager()
				.preloadForStartup(resourceProvider, CoreShaders.RENDERTYPE_GUI, CoreShaders.RENDERTYPE_GUI_OVERLAY, CoreShaders.POSITION_TEX_COLOR);
		} catch (ShaderManager.CompilationException | IOException var3) {
			throw new RuntimeException("Could not preload shaders for loading UI", var3);
		}
	}

	public void tick() {
		this.tickFov();
		this.lightTexture.tick();
		if (this.minecraft.getCameraEntity() == null) {
			this.minecraft.setCameraEntity(this.minecraft.player);
		}

		this.mainCamera.tick();
		this.itemInHandRenderer.tick();
		this.confusionAnimationTick++;
		if (this.minecraft.level.tickRateManager().runsNormally()) {
			this.minecraft.levelRenderer.tickParticles(this.mainCamera);
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
	}

	@Nullable
	public ResourceLocation currentPostEffect() {
		return this.postEffectId;
	}

	public void resize(int i, int j) {
		this.resourcePool.clear();
		this.minecraft.levelRenderer.resize(i, j);
	}

	public void pick(float f) {
		Entity entity = this.minecraft.getCameraEntity();
		if (entity != null) {
			if (this.minecraft.level != null && this.minecraft.player != null) {
				this.minecraft.getProfiler().push("pick");
				double d = this.minecraft.player.blockInteractionRange();
				double e = this.minecraft.player.entityInteractionRange();
				HitResult hitResult = this.pick(entity, d, e, f);
				this.minecraft.hitResult = hitResult;
				this.minecraft.crosshairPickEntity = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
				this.minecraft.getProfiler().pop();
			}
		}
	}

	private HitResult pick(Entity entity, double d, double e, float f) {
		double g = Math.max(d, e);
		double h = Mth.square(g);
		Vec3 vec3 = entity.getEyePosition(f);
		HitResult hitResult = entity.pick(g, f, false);
		double i = hitResult.getLocation().distanceToSqr(vec3);
		if (hitResult.getType() != HitResult.Type.MISS) {
			h = i;
			g = Math.sqrt(i);
		}

		Vec3 vec32 = entity.getViewVector(f);
		Vec3 vec33 = vec3.add(vec32.x * g, vec32.y * g, vec32.z * g);
		float j = 1.0F;
		AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(g)).inflate(1.0, 1.0, 1.0);
		EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, EntitySelector.CAN_BE_PICKED, h);
		return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(vec3) < i
			? filterHitResult(entityHitResult, vec3, e)
			: filterHitResult(hitResult, vec3, d);
	}

	private static HitResult filterHitResult(HitResult hitResult, Vec3 vec3, double d) {
		Vec3 vec32 = hitResult.getLocation();
		if (!vec32.closerThan(vec3, d)) {
			Vec3 vec33 = hitResult.getLocation();
			Direction direction = Direction.getApproximateNearest(vec33.x - vec3.x, vec33.y - vec3.y, vec33.z - vec3.z);
			return BlockHitResult.miss(vec33, direction, BlockPos.containing(vec33));
		} else {
			return hitResult;
		}
	}

	private void tickFov() {
		float g;
		if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer abstractClientPlayer) {
			Options options = this.minecraft.options;
			boolean bl = options.getCameraType().isFirstPerson();
			float f = options.fovEffectScale().get().floatValue();
			g = abstractClientPlayer.getFieldOfViewModifier(bl, f);
		} else {
			g = 1.0F;
		}

		this.oldFovModifier = this.fovModifier;
		this.fovModifier = this.fovModifier + (g - this.fovModifier) * 0.5F;
		this.fovModifier = Mth.clamp(this.fovModifier, 0.1F, 1.5F);
	}

	private float getFov(Camera camera, float f, boolean bl) {
		if (this.panoramicMode) {
			return 90.0F;
		} else {
			float g = 70.0F;
			if (bl) {
				g = (float)this.minecraft.options.fov().get().intValue();
				g *= Mth.lerp(f, this.oldFovModifier, this.fovModifier);
			}

			if (camera.getEntity() instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
				float h = Math.min((float)livingEntity.deathTime + f, 20.0F);
				g /= (1.0F - 500.0F / (h + 500.0F)) * 2.0F + 1.0F;
			}

			FogType fogType = camera.getFluidInCamera();
			if (fogType == FogType.LAVA || fogType == FogType.WATER) {
				float h = this.minecraft.options.fovEffectScale().get().floatValue();
				g *= Mth.lerp(h, 1.0F, 0.85714287F);
			}

			return g;
		}
	}

	private void bobHurt(PoseStack poseStack, float f) {
		if (this.minecraft.getCameraEntity() instanceof LivingEntity livingEntity) {
			float g = (float)livingEntity.hurtTime - f;
			if (livingEntity.isDeadOrDying()) {
				float h = Math.min((float)livingEntity.deathTime + f, 20.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (h + 200.0F)));
			}

			if (g < 0.0F) {
				return;
			}

			g /= (float)livingEntity.hurtDuration;
			g = Mth.sin(g * g * g * g * (float) Math.PI);
			float h = livingEntity.getHurtDir();
			poseStack.mulPose(Axis.YP.rotationDegrees(-h));
			float i = (float)((double)(-g) * 14.0 * this.minecraft.options.damageTiltStrength().get());
			poseStack.mulPose(Axis.ZP.rotationDegrees(i));
			poseStack.mulPose(Axis.YP.rotationDegrees(h));
		}
	}

	private void bobView(PoseStack poseStack, float f) {
		if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer abstractClientPlayer) {
			float var7 = abstractClientPlayer.walkDist - abstractClientPlayer.walkDistO;
			float h = -(abstractClientPlayer.walkDist + var7 * f);
			float i = Mth.lerp(f, abstractClientPlayer.oBob, abstractClientPlayer.bob);
			poseStack.translate(Mth.sin(h * (float) Math.PI) * i * 0.5F, -Math.abs(Mth.cos(h * (float) Math.PI) * i), 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(h * (float) Math.PI) * i * 3.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(h * (float) Math.PI - 0.2F) * i) * 5.0F));
		}
	}

	public void renderZoomed(float f, float g, float h) {
		this.zoom = f;
		this.zoomX = g;
		this.zoomY = h;
		this.setRenderBlockOutline(false);
		this.setRenderHand(false);
		this.renderLevel(DeltaTracker.ZERO);
		this.zoom = 1.0F;
	}

	private void renderItemInHand(Camera camera, float f, Matrix4f matrix4f) {
		if (!this.panoramicMode) {
			Matrix4f matrix4f2 = this.getProjectionMatrix(this.getFov(camera, f, false));
			RenderSystem.setProjectionMatrix(matrix4f2, VertexSorting.DISTANCE_TO_ORIGIN);
			PoseStack poseStack = new PoseStack();
			poseStack.pushPose();
			poseStack.mulPose(matrix4f.invert(new Matrix4f()));
			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			matrix4fStack.pushMatrix().mul(matrix4f);
			this.bobHurt(poseStack, f);
			if (this.minecraft.options.bobView().get()) {
				this.bobView(poseStack, f);
			}

			boolean bl = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
			if (this.minecraft.options.getCameraType().isFirstPerson()
				&& !bl
				&& !this.minecraft.options.hideGui
				&& this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
				this.lightTexture.turnOnLightLayer();
				this.itemInHandRenderer
					.renderHandsWithItems(
						f,
						poseStack,
						this.renderBuffers.bufferSource(),
						this.minecraft.player,
						this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, f)
					);
				this.lightTexture.turnOffLightLayer();
			}

			matrix4fStack.popMatrix();
			poseStack.popPose();
			if (this.minecraft.options.getCameraType().isFirstPerson() && !bl) {
				ScreenEffectRenderer.renderScreenEffect(this.minecraft, poseStack);
			}
		}
	}

	public Matrix4f getProjectionMatrix(float f) {
		Matrix4f matrix4f = new Matrix4f();
		if (this.zoom != 1.0F) {
			matrix4f.translate(this.zoomX, -this.zoomY, 0.0F);
			matrix4f.scale(this.zoom, this.zoom, 1.0F);
		}

		return matrix4f.perspective(
			f * (float) (Math.PI / 180.0), (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(), 0.05F, this.getDepthFar()
		);
	}

	public float getDepthFar() {
		return this.renderDistance * 4.0F;
	}

	public static float getNightVisionScale(LivingEntity livingEntity, float f) {
		MobEffectInstance mobEffectInstance = livingEntity.getEffect(MobEffects.NIGHT_VISION);
		return !mobEffectInstance.endsWithin(200) ? 1.0F : 0.7F + Mth.sin(((float)mobEffectInstance.getDuration() - f) * (float) Math.PI * 0.2F) * 0.3F;
	}

	public void render(DeltaTracker deltaTracker, boolean bl) {
		if (!this.minecraft.isWindowActive()
			&& this.minecraft.options.pauseOnLostFocus
			&& (!this.minecraft.options.touchscreen().get() || !this.minecraft.mouseHandler.isRightPressed())) {
			if (Util.getMillis() - this.lastActiveTime > 500L) {
				this.minecraft.pauseGame(false);
			}
		} else {
			this.lastActiveTime = Util.getMillis();
		}

		if (!this.minecraft.noRender) {
			boolean bl2 = this.minecraft.isGameLoadFinished();
			int i = (int)(
				this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth()
			);
			int j = (int)(
				this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight()
			);
			RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
			if (bl2 && bl && this.minecraft.level != null) {
				this.minecraft.getProfiler().push("level");
				this.renderLevel(deltaTracker);
				this.tryTakeScreenshotIfNeeded();
				this.minecraft.levelRenderer.doEntityOutline();
				if (this.postEffectId != null && this.effectActive) {
					RenderSystem.disableBlend();
					RenderSystem.disableDepthTest();
					RenderSystem.resetTextureMatrix();
					PostChain postChain = this.minecraft.getShaderManager().getPostChain(this.postEffectId, LevelTargetBundle.MAIN_TARGETS);
					if (postChain != null) {
						postChain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
					}
				}

				this.minecraft.getMainRenderTarget().bindWrite(true);
			}

			Window window = this.minecraft.getWindow();
			RenderSystem.clear(256);
			Matrix4f matrix4f = new Matrix4f()
				.setOrtho(
					0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, 21000.0F
				);
			RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
			Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
			matrix4fStack.pushMatrix();
			matrix4fStack.translation(0.0F, 0.0F, -11000.0F);
			Lighting.setupFor3DItems();
			GuiGraphics guiGraphics = new GuiGraphics(this.minecraft, this.renderBuffers.bufferSource());
			if (bl2 && bl && this.minecraft.level != null) {
				this.minecraft.getProfiler().popPush("gui");
				if (!this.minecraft.options.hideGui) {
					this.renderItemActivationAnimation(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false));
				}

				this.minecraft.gui.render(guiGraphics, deltaTracker);
				guiGraphics.flush();
				RenderSystem.clear(256);
				this.minecraft.getProfiler().pop();
			}

			if (this.minecraft.getOverlay() != null) {
				try {
					this.minecraft.getOverlay().render(guiGraphics, i, j, deltaTracker.getGameTimeDeltaTicks());
				} catch (Throwable var15) {
					CrashReport crashReport = CrashReport.forThrowable(var15, "Rendering overlay");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Overlay render details");
					crashReportCategory.setDetail("Overlay name", (CrashReportDetail<String>)(() -> this.minecraft.getOverlay().getClass().getCanonicalName()));
					throw new ReportedException(crashReport);
				}
			} else if (bl2 && this.minecraft.screen != null) {
				try {
					this.minecraft.screen.renderWithTooltip(guiGraphics, i, j, deltaTracker.getGameTimeDeltaTicks());
				} catch (Throwable var14) {
					CrashReport crashReport = CrashReport.forThrowable(var14, "Rendering screen");
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
								this.minecraft.getWindow().getGuiScaledWidth(),
								this.minecraft.getWindow().getGuiScaledHeight(),
								this.minecraft.getWindow().getWidth(),
								this.minecraft.getWindow().getHeight(),
								this.minecraft.getWindow().getGuiScale()
							))
					);
					throw new ReportedException(crashReport);
				}

				try {
					if (this.minecraft.screen != null) {
						this.minecraft.screen.handleDelayedNarration();
					}
				} catch (Throwable var13) {
					CrashReport crashReport = CrashReport.forThrowable(var13, "Narrating screen");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Screen details");
					crashReportCategory.setDetail("Screen name", (CrashReportDetail<String>)(() -> this.minecraft.screen.getClass().getCanonicalName()));
					throw new ReportedException(crashReport);
				}
			}

			if (bl2 && bl && this.minecraft.level != null) {
				this.minecraft.gui.renderSavingIndicator(guiGraphics, deltaTracker);
			}

			if (bl2) {
				this.minecraft.getProfiler().push("toasts");
				this.minecraft.getToastManager().render(guiGraphics);
				this.minecraft.getProfiler().pop();
			}

			guiGraphics.flush();
			matrix4fStack.popMatrix();
			this.resourcePool.endFrame();
		}
	}

	private void tryTakeScreenshotIfNeeded() {
		if (!this.hasWorldScreenshot && this.minecraft.isLocalServer()) {
			long l = Util.getMillis();
			if (l - this.lastScreenshotAttempt >= 1000L) {
				this.lastScreenshotAttempt = l;
				IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
				if (integratedServer != null && !integratedServer.isStopped()) {
					integratedServer.getWorldScreenshotFile().ifPresent(path -> {
						if (Files.isRegularFile(path, new LinkOption[0])) {
							this.hasWorldScreenshot = true;
						} else {
							this.takeAutoScreenshot(path);
						}
					});
				}
			}
		}
	}

	private void takeAutoScreenshot(Path path) {
		if (this.minecraft.levelRenderer.countRenderedSections() > 10 && this.minecraft.levelRenderer.hasRenderedAllSections()) {
			NativeImage nativeImage = Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget());
			Util.ioPool().execute(() -> {
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
					nativeImage2.writeToFile(path);
				} catch (IOException var16) {
					LOGGER.warn("Couldn't save auto screenshot", (Throwable)var16);
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
			if (bl && !((Player)entity).getAbilities().mayBuild) {
				ItemStack itemStack = ((LivingEntity)entity).getMainHandItem();
				HitResult hitResult = this.minecraft.hitResult;
				if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
					BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
					BlockState blockState = this.minecraft.level.getBlockState(blockPos);
					if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
						bl = blockState.getMenuProvider(this.minecraft.level, blockPos) != null;
					} else {
						BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, blockPos, false);
						Registry<Block> registry = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BLOCK);
						bl = !itemStack.isEmpty() && (itemStack.canBreakBlockInAdventureMode(blockInWorld) || itemStack.canPlaceOnBlockInAdventureMode(blockInWorld));
					}
				}
			}

			return bl;
		}
	}

	public void renderLevel(DeltaTracker deltaTracker) {
		float f = deltaTracker.getGameTimeDeltaPartialTick(true);
		this.lightTexture.updateLightTexture(f);
		if (this.minecraft.getCameraEntity() == null) {
			this.minecraft.setCameraEntity(this.minecraft.player);
		}

		this.pick(f);
		this.minecraft.getProfiler().push("center");
		boolean bl = this.shouldRenderBlockOutline();
		this.minecraft.getProfiler().popPush("camera");
		Camera camera = this.mainCamera;
		Entity entity = (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
		float g = this.minecraft.level.tickRateManager().isEntityFrozen(entity) ? 1.0F : f;
		camera.setup(this.minecraft.level, entity, !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), g);
		this.renderDistance = (float)(this.minecraft.options.getEffectiveRenderDistance() * 16);
		float h = this.getFov(camera, f, true);
		Matrix4f matrix4f = this.getProjectionMatrix(h);
		PoseStack poseStack = new PoseStack();
		this.bobHurt(poseStack, camera.getPartialTickTime());
		if (this.minecraft.options.bobView().get()) {
			this.bobView(poseStack, camera.getPartialTickTime());
		}

		matrix4f.mul(poseStack.last().pose());
		float i = this.minecraft.options.screenEffectScale().get().floatValue();
		float j = Mth.lerp(f, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity) * i * i;
		if (j > 0.0F) {
			int k = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
			float l = 5.0F / (j * j + 5.0F) - j * 0.04F;
			l *= l;
			Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
			float m = ((float)this.confusionAnimationTick + f) * (float)k * (float) (Math.PI / 180.0);
			matrix4f.rotate(m, vector3f);
			matrix4f.scale(1.0F / l, 1.0F, 1.0F);
			matrix4f.rotate(-m, vector3f);
		}

		float n = Math.max(h, (float)this.minecraft.options.fov().get().intValue());
		Matrix4f matrix4f2 = this.getProjectionMatrix(n);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.DISTANCE_TO_ORIGIN);
		Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
		Matrix4f matrix4f3 = new Matrix4f().rotation(quaternionf);
		this.minecraft.levelRenderer.prepareCullFrustum(camera.getPosition(), matrix4f3, matrix4f2);
		this.minecraft.getMainRenderTarget().bindWrite(true);
		this.minecraft.levelRenderer.renderLevel(this.resourcePool, deltaTracker, bl, camera, this, this.lightTexture, matrix4f3, matrix4f);
		this.minecraft.getProfiler().popPush("hand");
		if (this.renderHand) {
			RenderSystem.clear(256);
			this.renderItemInHand(camera, f, matrix4f3);
		}

		this.minecraft.getProfiler().pop();
	}

	public void resetData() {
		this.itemActivationItem = null;
		this.minecraft.getMapTextureManager().resetData();
		this.mainCamera.reset();
		this.hasWorldScreenshot = false;
	}

	public void displayItemActivation(ItemStack itemStack) {
		this.itemActivationItem = itemStack;
		this.itemActivationTicks = 40;
		this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
		this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
	}

	private void renderItemActivationAnimation(GuiGraphics guiGraphics, float f) {
		if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
			int i = 40 - this.itemActivationTicks;
			float g = ((float)i + f) / 40.0F;
			float h = g * g;
			float j = g * h;
			float k = 10.25F * j * h - 24.95F * h * h + 25.5F * j - 13.8F * h + 4.0F * g;
			float l = k * (float) Math.PI;
			float m = this.itemActivationOffX * (float)(guiGraphics.guiWidth() / 4);
			float n = this.itemActivationOffY * (float)(guiGraphics.guiHeight() / 4);
			PoseStack poseStack = guiGraphics.pose();
			poseStack.pushPose();
			poseStack.translate(
				(float)(guiGraphics.guiWidth() / 2) + m * Mth.abs(Mth.sin(l * 2.0F)), (float)(guiGraphics.guiHeight() / 2) + n * Mth.abs(Mth.sin(l * 2.0F)), -50.0F
			);
			float o = 50.0F + 175.0F * Mth.sin(l);
			poseStack.scale(o, -o, o);
			poseStack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(l))));
			poseStack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(g * 8.0F)));
			poseStack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(g * 8.0F)));
			guiGraphics.drawSpecial(
				multiBufferSource -> this.minecraft
						.getItemRenderer()
						.renderStatic(
							this.itemActivationItem, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, this.minecraft.level, 0
						)
			);
			poseStack.popPose();
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

	public LightTexture lightTexture() {
		return this.lightTexture;
	}

	public OverlayTexture overlayTexture() {
		return this.overlayTexture;
	}
}
