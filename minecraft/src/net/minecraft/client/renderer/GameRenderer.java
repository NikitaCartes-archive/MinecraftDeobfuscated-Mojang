package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class GameRenderer implements ResourceManagerReloadListener, AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final ResourceManager resourceManager;
	private final Random random = new Random();
	private float renderDistance;
	public final ItemInHandRenderer itemInHandRenderer;
	private final MapRenderer mapRenderer;
	private final RenderBuffers renderBuffers;
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
	private final OverlayTexture overlayTexture = new OverlayTexture();
	private boolean panoramicMode;
	private float zoom = 1.0F;
	private float zoomX;
	private float zoomY;
	@Nullable
	private ItemStack itemActivationItem;
	private int itemActivationTicks;
	private float itemActivationOffX;
	private float itemActivationOffY;
	@Nullable
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
	private final Camera mainCamera = new Camera();

	public GameRenderer(Minecraft minecraft, ResourceManager resourceManager, RenderBuffers renderBuffers) {
		this.minecraft = minecraft;
		this.resourceManager = resourceManager;
		this.itemInHandRenderer = minecraft.getItemInHandRenderer();
		this.mapRenderer = new MapRenderer(minecraft.getTextureManager());
		this.lightTexture = new LightTexture(this, minecraft);
		this.renderBuffers = renderBuffers;
		this.postEffect = null;
	}

	public void close() {
		this.lightTexture.close();
		this.mapRenderer.close();
		this.overlayTexture.close();
		this.shutdownEffect();
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
			this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
			this.effectActive = true;
		} catch (IOException var3) {
			LOGGER.warn("Failed to load shader: {}", resourceLocation, var3);
			this.effectIndex = EFFECT_NONE;
			this.effectActive = false;
		} catch (JsonSyntaxException var4) {
			LOGGER.warn("Failed to parse shader: {}", resourceLocation, var4);
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
		this.tick++;
		this.itemInHandRenderer.tick();
		this.minecraft.levelRenderer.tickRain(this.mainCamera);
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

	@Nullable
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

			if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isDeadOrDying()) {
				float g = Math.min((float)((LivingEntity)camera.getEntity()).deathTime + f, 20.0F);
				d /= (double)((1.0F - 500.0F / (g + 500.0F)) * 2.0F + 1.0F);
			}

			FluidState fluidState = camera.getFluidInCamera();
			if (!fluidState.isEmpty()) {
				d = d * 60.0 / 70.0;
			}

			return d;
		}
	}

	private void bobHurt(PoseStack poseStack, float f) {
		if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)this.minecraft.getCameraEntity();
			float g = (float)livingEntity.hurtTime - f;
			if (livingEntity.isDeadOrDying()) {
				float h = Math.min((float)livingEntity.deathTime + f, 20.0F);
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(40.0F - 8000.0F / (h + 200.0F)));
			}

			if (g < 0.0F) {
				return;
			}

			g /= (float)livingEntity.hurtDuration;
			g = Mth.sin(g * g * g * g * (float) Math.PI);
			float h = livingEntity.hurtDir;
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-h));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(-g * 14.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
		}
	}

	private void bobView(PoseStack poseStack, float f) {
		if (this.minecraft.getCameraEntity() instanceof Player) {
			Player player = (Player)this.minecraft.getCameraEntity();
			float g = player.walkDist - player.walkDistO;
			float h = -(player.walkDist + g * f);
			float i = Mth.lerp(f, player.oBob, player.bob);
			poseStack.translate((double)(Mth.sin(h * (float) Math.PI) * i * 0.5F), (double)(-Math.abs(Mth.cos(h * (float) Math.PI) * i)), 0.0);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(h * (float) Math.PI) * i * 3.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(Math.abs(Mth.cos(h * (float) Math.PI - 0.2F) * i) * 5.0F));
		}
	}

	private void renderItemInHand(PoseStack poseStack, Camera camera, float f) {
		if (!this.panoramicMode) {
			this.resetProjectionMatrix(this.getProjectionMatrix(camera, f, false));
			PoseStack.Pose pose = poseStack.last();
			pose.pose().setIdentity();
			pose.normal().setIdentity();
			poseStack.pushPose();
			this.bobHurt(poseStack, f);
			if (this.minecraft.options.bobView) {
				this.bobView(poseStack, f);
			}

			boolean bl = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
			if (this.minecraft.options.thirdPersonView == 0 && !bl && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
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

			poseStack.popPose();
			if (this.minecraft.options.thirdPersonView == 0 && !bl) {
				ScreenEffectRenderer.renderScreenEffect(this.minecraft, poseStack);
				this.bobHurt(poseStack, f);
			}

			if (this.minecraft.options.bobView) {
				this.bobView(poseStack, f);
			}
		}
	}

	public void resetProjectionMatrix(Matrix4f matrix4f) {
		RenderSystem.matrixMode(5889);
		RenderSystem.loadIdentity();
		RenderSystem.multMatrix(matrix4f);
		RenderSystem.matrixMode(5888);
	}

	public Matrix4f getProjectionMatrix(Camera camera, float f, boolean bl) {
		PoseStack poseStack = new PoseStack();
		poseStack.last().pose().setIdentity();
		if (this.zoom != 1.0F) {
			poseStack.translate((double)this.zoomX, (double)(-this.zoomY), 0.0);
			poseStack.scale(this.zoom, this.zoom, 1.0F);
		}

		poseStack.last()
			.pose()
			.multiply(
				Matrix4f.perspective(
					this.getFov(camera, f, bl),
					(float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(),
					0.05F,
					this.renderDistance * 4.0F
				)
			);
		return poseStack.last().pose();
	}

	public static float getNightVisionScale(LivingEntity livingEntity, float f) {
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
			int i = (int)(
				this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth()
			);
			int j = (int)(
				this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight()
			);
			RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
			if (bl && this.minecraft.level != null) {
				this.minecraft.getProfiler().push("level");
				this.renderLevel(f, l, new PoseStack());
				if (this.minecraft.hasSingleplayerServer() && this.lastScreenshotAttempt < Util.getMillis() - 1000L) {
					this.lastScreenshotAttempt = Util.getMillis();
					if (!this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
						this.takeAutoScreenshot();
					}
				}

				this.minecraft.levelRenderer.doEntityOutline();
				if (this.postEffect != null && this.effectActive) {
					RenderSystem.disableBlend();
					RenderSystem.disableDepthTest();
					RenderSystem.disableAlphaTest();
					RenderSystem.enableTexture();
					RenderSystem.matrixMode(5890);
					RenderSystem.pushMatrix();
					RenderSystem.loadIdentity();
					this.postEffect.process(f);
					RenderSystem.popMatrix();
				}

				this.minecraft.getMainRenderTarget().bindWrite(true);
			}

			Window window = this.minecraft.getWindow();
			RenderSystem.clear(256, Minecraft.ON_OSX);
			RenderSystem.matrixMode(5889);
			RenderSystem.loadIdentity();
			RenderSystem.ortho(0.0, (double)window.getWidth() / window.getGuiScale(), (double)window.getHeight() / window.getGuiScale(), 0.0, 1000.0, 3000.0);
			RenderSystem.matrixMode(5888);
			RenderSystem.loadIdentity();
			RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
			Lighting.setupFor3DItems();
			PoseStack poseStack = new PoseStack();
			if (bl && this.minecraft.level != null) {
				this.minecraft.getProfiler().popPush("gui");
				if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
					RenderSystem.defaultAlphaFunc();
					this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), f);
					this.minecraft.gui.render(poseStack, f);
					RenderSystem.clear(256, Minecraft.ON_OSX);
				}

				this.minecraft.getProfiler().pop();
			}

			if (this.minecraft.overlay != null) {
				try {
					this.minecraft.overlay.render(poseStack, i, j, this.minecraft.getDeltaFrameTime());
				} catch (Throwable var13) {
					CrashReport crashReport = CrashReport.forThrowable(var13, "Rendering overlay");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Overlay render details");
					crashReportCategory.setDetail("Overlay name", (CrashReportDetail<String>)(() -> this.minecraft.overlay.getClass().getCanonicalName()));
					throw new ReportedException(crashReport);
				}
			} else if (this.minecraft.screen != null) {
				try {
					this.minecraft.screen.render(poseStack, i, j, this.minecraft.getDeltaFrameTime());
				} catch (Throwable var12) {
					CrashReport crashReport = CrashReport.forThrowable(var12, "Rendering screen");
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
			}
		}
	}

	private void takeAutoScreenshot() {
		if (this.minecraft.levelRenderer.countRenderedChunks() > 10
			&& this.minecraft.levelRenderer.hasRenderedAllChunks()
			&& !this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
			NativeImage nativeImage = Screenshot.takeScreenshot(
				this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getMainRenderTarget()
			);
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

	public void renderLevel(float f, long l, PoseStack poseStack) {
		this.lightTexture.updateLightTexture(f);
		if (this.minecraft.getCameraEntity() == null) {
			this.minecraft.setCameraEntity(this.minecraft.player);
		}

		this.pick(f);
		this.minecraft.getProfiler().push("center");
		boolean bl = this.shouldRenderBlockOutline();
		this.minecraft.getProfiler().popPush("camera");
		Camera camera = this.mainCamera;
		this.renderDistance = (float)(this.minecraft.options.renderDistance * 16);
		PoseStack poseStack2 = new PoseStack();
		poseStack2.last().pose().multiply(this.getProjectionMatrix(camera, f, true));
		this.bobHurt(poseStack2, f);
		if (this.minecraft.options.bobView) {
			this.bobView(poseStack2, f);
		}

		float g = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
		if (g > 0.0F) {
			int i = 20;
			if (this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
				i = 7;
			}

			float h = 5.0F / (g * g + 5.0F) - g * 0.04F;
			h *= h;
			Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
			poseStack2.mulPose(vector3f.rotationDegrees(((float)this.tick + f) * (float)i));
			poseStack2.scale(1.0F / h, 1.0F, 1.0F);
			float j = -((float)this.tick + f) * (float)i;
			poseStack2.mulPose(vector3f.rotationDegrees(j));
		}

		Matrix4f matrix4f = poseStack2.last().pose();
		this.resetProjectionMatrix(matrix4f);
		camera.setup(
			this.minecraft.level,
			(Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()),
			this.minecraft.options.thirdPersonView > 0,
			this.minecraft.options.thirdPersonView == 2,
			f
		);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
		this.minecraft.levelRenderer.renderLevel(poseStack, f, l, bl, camera, this, this.lightTexture, matrix4f);
		this.minecraft.getProfiler().popPush("hand");
		if (this.renderHand) {
			RenderSystem.clear(256, Minecraft.ON_OSX);
			this.renderItemInHand(poseStack, camera, f);
		}

		this.minecraft.getProfiler().pop();
	}

	public void resetData() {
		this.itemActivationItem = null;
		this.mapRenderer.resetData();
		this.mainCamera.reset();
	}

	public MapRenderer getMapRenderer() {
		return this.mapRenderer;
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
			RenderSystem.enableAlphaTest();
			RenderSystem.pushMatrix();
			RenderSystem.pushLightingAttributes();
			RenderSystem.enableDepthTest();
			RenderSystem.disableCull();
			PoseStack poseStack = new PoseStack();
			poseStack.pushPose();
			poseStack.translate((double)((float)(i / 2) + o * Mth.abs(Mth.sin(n * 2.0F))), (double)((float)(j / 2) + p * Mth.abs(Mth.sin(n * 2.0F))), -50.0);
			float q = 50.0F + 175.0F * Mth.sin(n);
			poseStack.scale(q, -q, q);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(n))));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(6.0F * Mth.cos(g * 8.0F)));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(6.0F * Mth.cos(g * 8.0F)));
			MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
			this.minecraft
				.getItemRenderer()
				.renderStatic(this.itemActivationItem, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, poseStack, bufferSource);
			poseStack.popPose();
			bufferSource.endBatch();
			RenderSystem.popAttributes();
			RenderSystem.popMatrix();
			RenderSystem.enableCull();
			RenderSystem.disableDepthTest();
		}
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
