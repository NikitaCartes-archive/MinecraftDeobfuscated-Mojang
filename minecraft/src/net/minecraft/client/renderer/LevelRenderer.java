package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation TRANSPARENCY_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("transparency");
	private static final ResourceLocation ENTITY_OUTLINE_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("entity_outline");
	public static final int SECTION_SIZE = 16;
	public static final int HALF_SECTION_SIZE = 8;
	private static final int TRANSPARENT_SORT_COUNT = 15;
	private final Minecraft minecraft;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	private final RenderBuffers renderBuffers;
	private final SkyRenderer skyRenderer = new SkyRenderer();
	private final CloudRenderer cloudRenderer = new CloudRenderer();
	private final WorldBorderRenderer worldBorderRenderer = new WorldBorderRenderer();
	private final WeatherEffectRenderer weatherEffectRenderer = new WeatherEffectRenderer();
	@Nullable
	private ClientLevel level;
	private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
	private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);
	private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
	@Nullable
	private ViewArea viewArea;
	private int ticks;
	private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
	private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
	@Nullable
	private RenderTarget entityOutlineTarget;
	private final LevelTargetBundle targets = new LevelTargetBundle();
	private int lastCameraSectionX = Integer.MIN_VALUE;
	private int lastCameraSectionY = Integer.MIN_VALUE;
	private int lastCameraSectionZ = Integer.MIN_VALUE;
	private double prevCamX = Double.MIN_VALUE;
	private double prevCamY = Double.MIN_VALUE;
	private double prevCamZ = Double.MIN_VALUE;
	private double prevCamRotX = Double.MIN_VALUE;
	private double prevCamRotY = Double.MIN_VALUE;
	@Nullable
	private SectionRenderDispatcher sectionRenderDispatcher;
	private int lastViewDistance = -1;
	private final List<Entity> visibleEntities = new ArrayList();
	private int visibleEntityCount;
	private Frustum cullingFrustum;
	private boolean captureFrustum;
	@Nullable
	private Frustum capturedFrustum;
	@Nullable
	private Vec3 lastTranslucentSortPos;

	public LevelRenderer(
		Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers
	) {
		this.minecraft = minecraft;
		this.entityRenderDispatcher = entityRenderDispatcher;
		this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
		this.renderBuffers = renderBuffers;
	}

	public void tickParticles(Camera camera) {
		this.weatherEffectRenderer.tickRainParticles(this.minecraft.level, camera, this.ticks, this.minecraft.options.particles().get());
	}

	public void close() {
		if (this.entityOutlineTarget != null) {
			this.entityOutlineTarget.destroyBuffers();
		}

		this.skyRenderer.close();
		this.cloudRenderer.close();
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.initOutline();
	}

	public void initOutline() {
		if (this.entityOutlineTarget != null) {
			this.entityOutlineTarget.destroyBuffers();
		}

		this.entityOutlineTarget = new TextureTarget(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), true);
		this.entityOutlineTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
	}

	@Nullable
	private PostChain getTransparencyChain() {
		if (!Minecraft.useShaderTransparency()) {
			return null;
		} else {
			PostChain postChain = this.minecraft.getShaderManager().getPostChain(TRANSPARENCY_POST_CHAIN_ID, LevelTargetBundle.SORTING_TARGETS);
			if (postChain == null) {
				this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
				this.minecraft.options.save();
			}

			return postChain;
		}
	}

	public void doEntityOutline() {
		if (this.shouldShowEntityOutlines()) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			this.entityOutlineTarget.blitAndBlendToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	}

	protected boolean shouldShowEntityOutlines() {
		return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityOutlineTarget != null && this.minecraft.player != null;
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

	public void allChanged() {
		if (this.level != null) {
			this.level.clearTintCaches();
			if (this.sectionRenderDispatcher == null) {
				this.sectionRenderDispatcher = new SectionRenderDispatcher(
					this.level, this, Util.backgroundExecutor(), this.renderBuffers, this.minecraft.getBlockRenderer(), this.minecraft.getBlockEntityRenderDispatcher()
				);
			} else {
				this.sectionRenderDispatcher.setLevel(this.level);
			}

			this.cloudRenderer.markForRebuild();
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
				this.viewArea.repositionCamera(SectionPos.of(entity));
			}
		}
	}

	public void resize(int i, int j) {
		this.needsUpdate();
		if (this.entityOutlineTarget != null) {
			this.entityOutlineTarget.resize(i, j);
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
			if (renderSection.getCompiled().hasRenderableLayers()) {
				i++;
			}
		}

		return i;
	}

	public String getEntityStatistics() {
		return "E: " + this.visibleEntityCount + "/" + this.level.getEntityCount() + ", SD: " + this.level.getServerSimulationDistance();
	}

	private void setupRender(Camera camera, Frustum frustum, boolean bl, boolean bl2) {
		Vec3 vec3 = camera.getPosition();
		if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
			this.allChanged();
		}

		ProfilerFiller profilerFiller = this.level.getProfiler();
		profilerFiller.push("camera");
		int i = SectionPos.posToSectionCoord(vec3.x());
		int j = SectionPos.posToSectionCoord(vec3.y());
		int k = SectionPos.posToSectionCoord(vec3.z());
		if (this.lastCameraSectionX != i || this.lastCameraSectionY != j || this.lastCameraSectionZ != k) {
			this.lastCameraSectionX = i;
			this.lastCameraSectionY = j;
			this.lastCameraSectionZ = k;
			this.viewArea.repositionCamera(SectionPos.of(vec3));
		}

		this.sectionRenderDispatcher.setCamera(vec3);
		profilerFiller.popPush("cull");
		double d = Math.floor(vec3.x / 8.0);
		double e = Math.floor(vec3.y / 8.0);
		double f = Math.floor(vec3.z / 8.0);
		if (d != this.prevCamX || e != this.prevCamY || f != this.prevCamZ) {
			this.sectionOcclusionGraph.invalidate();
		}

		this.prevCamX = d;
		this.prevCamY = e;
		this.prevCamZ = f;
		profilerFiller.popPush("update");
		if (!bl) {
			boolean bl3 = this.minecraft.smartCull;
			if (bl2 && this.level.getBlockState(camera.getBlockPosition()).isSolidRender()) {
				bl3 = false;
			}

			profilerFiller.push("section_occlusion_graph");
			this.sectionOcclusionGraph.update(bl3, camera, frustum, this.visibleSections, this.level.getChunkSource().getLoadedEmptySections());
			profilerFiller.pop();
			double g = Math.floor((double)(camera.getXRot() / 2.0F));
			double h = Math.floor((double)(camera.getYRot() / 2.0F));
			if (this.sectionOcclusionGraph.consumeFrustumUpdate() || g != this.prevCamRotX || h != this.prevCamRotY) {
				this.applyFrustum(offsetFrustum(frustum));
				this.prevCamRotX = g;
				this.prevCamRotY = h;
			}
		}

		profilerFiller.pop();
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
		this.sectionOcclusionGraph.schedulePropagationFrom(renderSection);
	}

	public void prepareCullFrustum(Vec3 vec3, Matrix4f matrix4f, Matrix4f matrix4f2) {
		this.cullingFrustum = new Frustum(matrix4f, matrix4f2);
		this.cullingFrustum.prepare(vec3.x(), vec3.y(), vec3.z());
	}

	public void renderLevel(
		GraphicsResourceAllocator graphicsResourceAllocator,
		DeltaTracker deltaTracker,
		boolean bl,
		Camera camera,
		GameRenderer gameRenderer,
		LightTexture lightTexture,
		Matrix4f matrix4f,
		Matrix4f matrix4f2
	) {
		float f = deltaTracker.getGameTimeDeltaPartialTick(false);
		RenderSystem.setShaderGameTime(this.level.getGameTime(), f);
		this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
		this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
		final ProfilerFiller profilerFiller = this.level.getProfiler();
		profilerFiller.popPush("light_update_queue");
		this.level.pollLightUpdates();
		profilerFiller.popPush("light_updates");
		this.level.getChunkSource().getLightEngine().runLightUpdates();
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double g = vec3.z();
		profilerFiller.popPush("culling");
		boolean bl2 = this.capturedFrustum != null;
		Frustum frustum = bl2 ? this.capturedFrustum : this.cullingFrustum;
		this.minecraft.getProfiler().popPush("captureFrustum");
		if (this.captureFrustum) {
			this.capturedFrustum = bl2 ? new Frustum(matrix4f, matrix4f2) : frustum;
			this.capturedFrustum.prepare(d, e, g);
			this.captureFrustum = false;
		}

		profilerFiller.popPush("fog");
		float h = gameRenderer.getRenderDistance();
		boolean bl3 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d), Mth.floor(e)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
		Vector4f vector4f = FogRenderer.computeFogColor(
			camera, f, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), gameRenderer.getDarkenWorldAmount(f)
		);
		FogParameters fogParameters = FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, vector4f, h, bl3, f);
		FogParameters fogParameters2 = FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, vector4f, h, bl3, f);
		profilerFiller.popPush("cullEntities");
		boolean bl4 = this.collectVisibleEntities(camera, frustum, this.visibleEntities);
		this.visibleEntityCount = this.visibleEntities.size();
		profilerFiller.popPush("terrain_setup");
		this.setupRender(camera, frustum, bl2, this.minecraft.player.isSpectator());
		profilerFiller.popPush("compile_sections");
		this.compileSections(camera);
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		matrix4fStack.mul(matrix4f);
		FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
		this.targets.main = frameGraphBuilder.importExternal("main", this.minecraft.getMainRenderTarget());
		int i = this.minecraft.getMainRenderTarget().width;
		int j = this.minecraft.getMainRenderTarget().height;
		RenderTargetDescriptor renderTargetDescriptor = new RenderTargetDescriptor(i, j, true);
		PostChain postChain = this.getTransparencyChain();
		if (postChain != null) {
			this.targets.translucent = frameGraphBuilder.createInternal("translucent", renderTargetDescriptor);
			this.targets.itemEntity = frameGraphBuilder.createInternal("item_entity", renderTargetDescriptor);
			this.targets.particles = frameGraphBuilder.createInternal("particles", renderTargetDescriptor);
			this.targets.weather = frameGraphBuilder.createInternal("weather", renderTargetDescriptor);
			this.targets.clouds = frameGraphBuilder.createInternal("clouds", renderTargetDescriptor);
		}

		if (this.entityOutlineTarget != null) {
			this.targets.entityOutline = frameGraphBuilder.importExternal("entity_outline", this.entityOutlineTarget);
		}

		FramePass framePass = frameGraphBuilder.addPass("clear");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		framePass.executes(() -> {
			RenderSystem.clearColor(vector4f.x, vector4f.y, vector4f.z, 0.0F);
			RenderSystem.clear(16640);
		});
		if (!bl3) {
			this.addSkyPass(frameGraphBuilder, camera, f, fogParameters2);
		}

		this.addMainPass(frameGraphBuilder, frustum, camera, matrix4f, matrix4f2, fogParameters, bl, bl4, deltaTracker, profilerFiller);
		PostChain postChain2 = this.minecraft.getShaderManager().getPostChain(ENTITY_OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS);
		if (bl4 && postChain2 != null) {
			postChain2.addToFrame(frameGraphBuilder, i, j, this.targets);
		}

		this.addParticlesPass(frameGraphBuilder, camera, lightTexture, f, fogParameters);
		CloudStatus cloudStatus = this.minecraft.options.getCloudsType();
		if (cloudStatus != CloudStatus.OFF) {
			float k = this.level.effects().getCloudHeight();
			if (!Float.isNaN(k)) {
				float l = (float)this.ticks + f;
				int m = this.level.getCloudColor(f);
				this.addCloudsPass(frameGraphBuilder, matrix4f, matrix4f2, cloudStatus, camera.getPosition(), l, m, k + 0.33F);
			}
		}

		this.addWeatherPass(frameGraphBuilder, lightTexture, camera.getPosition(), f, fogParameters);
		if (postChain != null) {
			postChain.addToFrame(frameGraphBuilder, i, j, this.targets);
		}

		this.addLateDebugPass(frameGraphBuilder, vec3, fogParameters);
		profilerFiller.popPush("framegraph");
		frameGraphBuilder.execute(graphicsResourceAllocator, new FrameGraphBuilder.Inspector() {
			@Override
			public void beforeExecutePass(String string) {
				profilerFiller.push(string);
			}

			@Override
			public void afterExecutePass(String string) {
				profilerFiller.pop();
			}
		});
		this.minecraft.getMainRenderTarget().bindWrite(false);
		this.visibleEntities.clear();
		this.targets.clear();
		matrix4fStack.popMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.setShaderFog(FogParameters.NO_FOG);
	}

	private void addMainPass(
		FrameGraphBuilder frameGraphBuilder,
		Frustum frustum,
		Camera camera,
		Matrix4f matrix4f,
		Matrix4f matrix4f2,
		FogParameters fogParameters,
		boolean bl,
		boolean bl2,
		DeltaTracker deltaTracker,
		ProfilerFiller profilerFiller
	) {
		FramePass framePass = frameGraphBuilder.addPass("main");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		if (this.targets.translucent != null) {
			this.targets.translucent = framePass.readsAndWrites(this.targets.translucent);
		}

		if (this.targets.itemEntity != null) {
			this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
		}

		if (this.targets.weather != null) {
			this.targets.weather = framePass.readsAndWrites(this.targets.weather);
		}

		if (bl2 && this.targets.entityOutline != null) {
			this.targets.entityOutline = framePass.readsAndWrites(this.targets.entityOutline);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
		ResourceHandle<RenderTarget> resourceHandle2 = this.targets.translucent;
		ResourceHandle<RenderTarget> resourceHandle3 = this.targets.itemEntity;
		ResourceHandle<RenderTarget> resourceHandle4 = this.targets.weather;
		ResourceHandle<RenderTarget> resourceHandle5 = this.targets.entityOutline;
		framePass.executes(() -> {
			RenderSystem.setShaderFog(fogParameters);
			float f = deltaTracker.getGameTimeDeltaPartialTick(false);
			Vec3 vec3 = camera.getPosition();
			double d = vec3.x();
			double e = vec3.y();
			double g = vec3.z();
			profilerFiller.push("terrain");
			this.renderSectionLayer(RenderType.solid(), d, e, g, matrix4f, matrix4f2);
			this.renderSectionLayer(RenderType.cutoutMipped(), d, e, g, matrix4f, matrix4f2);
			this.renderSectionLayer(RenderType.cutout(), d, e, g, matrix4f, matrix4f2);
			if (this.level.effects().constantAmbientLight()) {
				Lighting.setupNetherLevel();
			} else {
				Lighting.setupLevel();
			}

			if (resourceHandle3 != null) {
				resourceHandle3.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				resourceHandle3.get().clear();
				resourceHandle3.get().copyDepthFrom(this.minecraft.getMainRenderTarget());
				resourceHandle.get().bindWrite(false);
			}

			if (resourceHandle4 != null) {
				resourceHandle4.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				resourceHandle4.get().clear();
			}

			if (this.shouldShowEntityOutlines() && resourceHandle5 != null) {
				resourceHandle5.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				resourceHandle5.get().clear();
				resourceHandle.get().bindWrite(false);
			}

			PoseStack poseStack = new PoseStack();
			MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
			MultiBufferSource.BufferSource bufferSource2 = this.renderBuffers.crumblingBufferSource();
			profilerFiller.popPush("entities");
			this.renderEntities(poseStack, bufferSource, camera, deltaTracker, this.visibleEntities);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
			profilerFiller.popPush("blockentities");
			this.renderBlockEntities(poseStack, bufferSource, bufferSource2, camera, f);
			bufferSource.endLastBatch();
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
			if (bl) {
				this.renderBlockOutline(camera, bufferSource, poseStack, false);
			}

			profilerFiller.popPush("debug");
			this.minecraft.debugRenderer.render(poseStack, frustum, bufferSource, d, e, g);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
			bufferSource.endBatch(Sheets.translucentItemSheet());
			bufferSource.endBatch(Sheets.bannerSheet());
			bufferSource.endBatch(Sheets.shieldSheet());
			bufferSource.endBatch(RenderType.armorEntityGlint());
			bufferSource.endBatch(RenderType.glint());
			bufferSource.endBatch(RenderType.glintTranslucent());
			bufferSource.endBatch(RenderType.entityGlint());
			profilerFiller.popPush("destroyProgress");
			this.renderBlockDestroyAnimation(poseStack, camera, bufferSource2);
			bufferSource2.endBatch();
			this.checkPoseStack(poseStack);
			bufferSource.endBatch(RenderType.waterMask());
			bufferSource.endBatch();
			if (resourceHandle2 != null) {
				resourceHandle2.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				resourceHandle2.get().clear();
				resourceHandle2.get().copyDepthFrom(resourceHandle.get());
			}

			profilerFiller.popPush("translucent");
			this.renderSectionLayer(RenderType.translucent(), d, e, g, matrix4f, matrix4f2);
			profilerFiller.popPush("string");
			this.renderSectionLayer(RenderType.tripwire(), d, e, g, matrix4f, matrix4f2);
			if (bl) {
				this.renderBlockOutline(camera, bufferSource, poseStack, true);
			}

			bufferSource.endBatch();
			profilerFiller.pop();
		});
	}

	private void addParticlesPass(FrameGraphBuilder frameGraphBuilder, Camera camera, LightTexture lightTexture, float f, FogParameters fogParameters) {
		FramePass framePass = frameGraphBuilder.addPass("particles");
		if (this.targets.particles != null) {
			this.targets.particles = framePass.readsAndWrites(this.targets.particles);
			framePass.reads(this.targets.main);
		} else {
			this.targets.main = framePass.readsAndWrites(this.targets.main);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
		ResourceHandle<RenderTarget> resourceHandle2 = this.targets.particles;
		framePass.executes(() -> {
			RenderSystem.setShaderFog(fogParameters);
			if (resourceHandle2 != null) {
				resourceHandle2.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				resourceHandle2.get().clear();
				resourceHandle2.get().copyDepthFrom(resourceHandle.get());
			}

			RenderStateShard.PARTICLES_TARGET.setupRenderState();
			this.minecraft.particleEngine.render(lightTexture, camera, f);
			RenderStateShard.PARTICLES_TARGET.clearRenderState();
		});
	}

	private void addCloudsPass(
		FrameGraphBuilder frameGraphBuilder, Matrix4f matrix4f, Matrix4f matrix4f2, CloudStatus cloudStatus, Vec3 vec3, float f, int i, float g
	) {
		FramePass framePass = frameGraphBuilder.addPass("clouds");
		if (this.targets.clouds != null) {
			this.targets.clouds = framePass.readsAndWrites(this.targets.clouds);
		} else {
			this.targets.main = framePass.readsAndWrites(this.targets.main);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.clouds;
		framePass.executes(() -> {
			if (resourceHandle != null) {
				resourceHandle.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				resourceHandle.get().clear();
			}

			this.cloudRenderer.render(i, cloudStatus, g, matrix4f, matrix4f2, vec3, f);
		});
	}

	private void addWeatherPass(FrameGraphBuilder frameGraphBuilder, LightTexture lightTexture, Vec3 vec3, float f, FogParameters fogParameters) {
		int i = this.minecraft.options.getEffectiveRenderDistance() * 16;
		float g = this.minecraft.gameRenderer.getDepthFar();
		FramePass framePass = frameGraphBuilder.addPass("weather");
		if (this.targets.weather != null) {
			this.targets.weather = framePass.readsAndWrites(this.targets.weather);
		} else {
			this.targets.main = framePass.readsAndWrites(this.targets.main);
		}

		framePass.executes(() -> {
			RenderSystem.setShaderFog(fogParameters);
			RenderStateShard.WEATHER_TARGET.setupRenderState();
			this.weatherEffectRenderer.render(this.minecraft.level, lightTexture, this.ticks, f, vec3);
			this.worldBorderRenderer.render(this.level.getWorldBorder(), vec3, (double)i, (double)g);
			RenderStateShard.WEATHER_TARGET.clearRenderState();
		});
	}

	private void addLateDebugPass(FrameGraphBuilder frameGraphBuilder, Vec3 vec3, FogParameters fogParameters) {
		FramePass framePass = frameGraphBuilder.addPass("late_debug");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		if (this.targets.itemEntity != null) {
			this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
		framePass.executes(() -> {
			RenderSystem.setShaderFog(fogParameters);
			resourceHandle.get().bindWrite(false);
			PoseStack poseStack = new PoseStack();
			MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
			this.minecraft.debugRenderer.renderAfterTranslucents(poseStack, bufferSource, vec3.x, vec3.y, vec3.z);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
		});
	}

	private boolean collectVisibleEntities(Camera camera, Frustum frustum, List<Entity> list) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		boolean bl = false;
		boolean bl2 = this.shouldShowEntityOutlines();
		Entity.setViewScale(
			Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get()
		);

		for (Entity entity : this.level.entitiesForRendering()) {
			if (this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, f) || entity.hasIndirectPassenger(this.minecraft.player)) {
				BlockPos blockPos = entity.blockPosition();
				if ((this.level.isOutsideBuildHeight(blockPos.getY()) || this.isSectionCompiled(blockPos))
					&& (entity != camera.getEntity() || camera.isDetached() || camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping())
					&& (!(entity instanceof LocalPlayer) || camera.getEntity() == entity)) {
					list.add(entity);
					if (bl2 && this.minecraft.shouldEntityAppearGlowing(entity)) {
						bl = true;
					}
				}
			}
		}

		return bl;
	}

	private void renderEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> list) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
		boolean bl = this.shouldShowEntityOutlines();

		for (Entity entity : list) {
			if (entity.tickCount == 0) {
				entity.xOld = entity.getX();
				entity.yOld = entity.getY();
				entity.zOld = entity.getZ();
			}

			MultiBufferSource multiBufferSource;
			if (bl && this.minecraft.shouldEntityAppearGlowing(entity)) {
				OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
				multiBufferSource = outlineBufferSource;
				int i = entity.getTeamColor();
				outlineBufferSource.setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), 255);
			} else {
				multiBufferSource = bufferSource;
			}

			float g = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
			this.renderEntity(entity, d, e, f, g, poseStack, multiBufferSource);
		}
	}

	private void renderBlockEntities(
		PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource bufferSource2, Camera camera, float f
	) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double g = vec3.z();

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			List<BlockEntity> list = renderSection.getCompiled().getRenderableBlockEntities();
			if (!list.isEmpty()) {
				for (BlockEntity blockEntity : list) {
					BlockPos blockPos = blockEntity.getBlockPos();
					MultiBufferSource multiBufferSource = bufferSource;
					poseStack.pushPose();
					poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - g);
					SortedSet<BlockDestructionProgress> sortedSet = this.destructionProgress.get(blockPos.asLong());
					if (sortedSet != null && !sortedSet.isEmpty()) {
						int i = ((BlockDestructionProgress)sortedSet.last()).getProgress();
						if (i >= 0) {
							PoseStack.Pose pose = poseStack.last();
							VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(bufferSource2.getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(i)), pose, 1.0F);
							multiBufferSource = renderType -> {
								VertexConsumer vertexConsumer2 = bufferSource.getBuffer(renderType);
								return renderType.affectsCrumbling() ? VertexMultiConsumer.create(vertexConsumer, vertexConsumer2) : vertexConsumer2;
							};
						}
					}

					this.blockEntityRenderDispatcher.render(blockEntity, f, poseStack, multiBufferSource);
					poseStack.popPose();
				}
			}
		}

		synchronized (this.globalBlockEntities) {
			for (BlockEntity blockEntity2 : this.globalBlockEntities) {
				BlockPos blockPos2 = blockEntity2.getBlockPos();
				poseStack.pushPose();
				poseStack.translate((double)blockPos2.getX() - d, (double)blockPos2.getY() - e, (double)blockPos2.getZ() - g);
				this.blockEntityRenderDispatcher.render(blockEntity2, f, poseStack, bufferSource);
				poseStack.popPose();
			}
		}
	}

	private void renderBlockDestroyAnimation(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource bufferSource) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();

		for (Entry<SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.long2ObjectEntrySet()) {
			BlockPos blockPos = BlockPos.of(entry.getLongKey());
			if (!(blockPos.distToCenterSqr(d, e, f) > 1024.0)) {
				SortedSet<BlockDestructionProgress> sortedSet = (SortedSet<BlockDestructionProgress>)entry.getValue();
				if (sortedSet != null && !sortedSet.isEmpty()) {
					int i = ((BlockDestructionProgress)sortedSet.last()).getProgress();
					poseStack.pushPose();
					poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f);
					PoseStack.Pose pose = poseStack.last();
					VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(bufferSource.getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(i)), pose, 1.0F);
					this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos), blockPos, this.level, poseStack, vertexConsumer);
					poseStack.popPose();
				}
			}
		}
	}

	private void renderBlockOutline(Camera camera, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl) {
		if (this.minecraft.hitResult instanceof BlockHitResult blockHitResult) {
			if (blockHitResult.getType() != HitResult.Type.MISS) {
				BlockPos blockPos = blockHitResult.getBlockPos();
				BlockState blockState = this.level.getBlockState(blockPos);
				if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos)) {
					boolean bl2 = ItemBlockRenderTypes.getChunkRenderType(blockState).sortOnUpload();
					if (bl2 != bl) {
						return;
					}

					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
					Vec3 vec3 = camera.getPosition();
					this.renderHitOutline(poseStack, vertexConsumer, camera.getEntity(), vec3.x, vec3.y, vec3.z, blockPos, blockState);
					bufferSource.endLastBatch();
				}
			}
		}
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
		this.entityRenderDispatcher.render(entity, h - d, i - e, j - f, g, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, g));
	}

	private void scheduleTranslucentSectionResort(Vec3 vec3, RenderType renderType) {
		if (this.lastTranslucentSortPos == null || !(vec3.distanceToSqr(this.lastTranslucentSortPos) <= 1.0)) {
			this.minecraft.getProfiler().push("translucent_sort");
			int i = SectionPos.posToSectionCoord(vec3.x);
			int j = SectionPos.posToSectionCoord(vec3.y);
			int k = SectionPos.posToSectionCoord(vec3.z);
			boolean bl = this.lastTranslucentSortPos == null
				|| i != SectionPos.posToSectionCoord(this.lastTranslucentSortPos.x)
				|| k != SectionPos.posToSectionCoord(this.lastTranslucentSortPos.y)
				|| j != SectionPos.posToSectionCoord(this.lastTranslucentSortPos.z);
			this.lastTranslucentSortPos = vec3;
			int l = 0;

			for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
				if (l < 15 && (bl || renderSection.isAxisAlignedWith(i, j, k)) && renderSection.resortTransparency(renderType, this.sectionRenderDispatcher)) {
					l++;
				}
			}

			this.minecraft.getProfiler().pop();
		}
	}

	private void renderSectionLayer(RenderType renderType, double d, double e, double f, Matrix4f matrix4f, Matrix4f matrix4f2) {
		RenderSystem.assertOnRenderThread();
		this.minecraft.getProfiler().push((Supplier<String>)(() -> "render_" + renderType));
		boolean bl = renderType != RenderType.translucent();
		ObjectListIterator<SectionRenderDispatcher.RenderSection> objectListIterator = this.visibleSections.listIterator(bl ? 0 : this.visibleSections.size());
		renderType.setupRenderState();
		CompiledShaderProgram compiledShaderProgram = RenderSystem.getShader();
		if (compiledShaderProgram == null) {
			renderType.clearRenderState();
		} else {
			compiledShaderProgram.setDefaultUniforms(VertexFormat.Mode.QUADS, matrix4f, matrix4f2, this.minecraft.getWindow());
			compiledShaderProgram.apply();
			Uniform uniform = compiledShaderProgram.MODEL_OFFSET;

			while (bl ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
				SectionRenderDispatcher.RenderSection renderSection = bl ? (SectionRenderDispatcher.RenderSection)objectListIterator.next() : objectListIterator.previous();
				if (!renderSection.getCompiled().isEmpty(renderType)) {
					VertexBuffer vertexBuffer = renderSection.getBuffer(renderType);
					BlockPos blockPos = renderSection.getOrigin();
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

			compiledShaderProgram.clear();
			VertexBuffer.unbind();
			this.minecraft.getProfiler().pop();
			renderType.clearRenderState();
		}
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

	private void addSkyPass(FrameGraphBuilder frameGraphBuilder, Camera camera, float f, FogParameters fogParameters) {
		FogType fogType = camera.getFluidInCamera();
		if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
			DimensionSpecialEffects dimensionSpecialEffects = this.level.effects();
			DimensionSpecialEffects.SkyType skyType = dimensionSpecialEffects.skyType();
			if (skyType != DimensionSpecialEffects.SkyType.NONE) {
				FramePass framePass = frameGraphBuilder.addPass("sky");
				this.targets.main = framePass.readsAndWrites(this.targets.main);
				framePass.executes(() -> {
					RenderSystem.setShaderFog(fogParameters);
					RenderStateShard.MAIN_TARGET.setupRenderState();
					PoseStack poseStack = new PoseStack();
					if (skyType == DimensionSpecialEffects.SkyType.END) {
						this.skyRenderer.renderEndSky(poseStack);
					} else {
						Tesselator tesselator = Tesselator.getInstance();
						float g = this.level.getSunAngle(f);
						float h = this.level.getTimeOfDay(f);
						float i = 1.0F - this.level.getRainLevel(f);
						float j = this.level.getStarBrightness(f) * i;
						int k = dimensionSpecialEffects.getSunriseOrSunsetColor(h);
						int l = this.level.getMoonPhase();
						int m = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), f);
						float n = ARGB.from8BitChannel(ARGB.red(m));
						float o = ARGB.from8BitChannel(ARGB.green(m));
						float p = ARGB.from8BitChannel(ARGB.blue(m));
						this.skyRenderer.renderSkyDisc(n, o, p);
						if (dimensionSpecialEffects.isSunriseOrSunset(h)) {
							this.skyRenderer.renderSunriseAndSunset(poseStack, tesselator, g, k);
						}

						this.skyRenderer.renderSunMoonAndStars(poseStack, tesselator, h, l, i, j, fogParameters);
						if (this.shouldRenderDarkDisc(f)) {
							this.skyRenderer.renderDarkDisc(poseStack);
						}
					}
				});
			}
		}
	}

	private boolean shouldRenderDarkDisc(float f) {
		return this.minecraft.player.getEyePosition(f).y - this.level.getLevelData().getHorizonHeight(this.level) < 0.0;
	}

	private boolean doesMobEffectBlockSky(Camera camera) {
		return !(camera.getEntity() instanceof LivingEntity livingEntity)
			? false
			: livingEntity.hasEffect(MobEffects.BLINDNESS) || livingEntity.hasEffect(MobEffects.DARKNESS);
	}

	private void compileSections(Camera camera) {
		this.minecraft.getProfiler().push("populate_sections_to_compile");
		LevelLightEngine levelLightEngine = this.level.getLightEngine();
		RenderRegionCache renderRegionCache = new RenderRegionCache();
		BlockPos blockPos = camera.getBlockPosition();
		List<SectionRenderDispatcher.RenderSection> list = Lists.<SectionRenderDispatcher.RenderSection>newArrayList();

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			long l = renderSection.getSectionNode();
			if (renderSection.isDirty() && renderSection.hasAllNeighbors() && isLightOnInSectionAndNeighbors(levelLightEngine, l)) {
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
		this.scheduleTranslucentSectionResort(camera.getPosition(), RenderType.translucent());
	}

	private static boolean isLightOnInSectionAndNeighbors(LevelLightEngine levelLightEngine, long l) {
		int i = SectionPos.z(l);
		int j = SectionPos.x(l);

		for (int k = i - 1; k <= i + 1; k++) {
			for (int m = j - 1; m <= j + 1; m++) {
				if (!levelLightEngine.lightOnInColumn(SectionPos.getZeroNode(m, k))) {
					return false;
				}
			}
		}

		return true;
	}

	private void renderHitOutline(
		PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState
	) {
		ShapeRenderer.renderShape(
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
		this.setSectionRangeDirty(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
	}

	public void setSectionRangeDirty(int i, int j, int k, int l, int m, int n) {
		for (int o = k; o <= n; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = j; q <= m; q++) {
					this.setSectionDirty(p, q, o);
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

	public void onSectionBecomingNonEmpty(long l) {
		SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSection(l);
		if (renderSection != null) {
			this.sectionOcclusionGraph.schedulePropagationFrom(renderSection);
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
			crashReportCategory.setDetail(
				"Parameters",
				(CrashReportDetail<String>)(() -> ParticleTypes.CODEC
						.encodeStart(this.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), particleOptions)
						.toString())
			);
			crashReportCategory.setDetail("Position", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.level, d, e, f)));
			throw new ReportedException(crashReport);
		}
	}

	public <T extends ParticleOptions> void addParticle(T particleOptions, double d, double e, double f, double g, double h, double i) {
		this.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter(), d, e, f, g, h, i);
	}

	@Nullable
	Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
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
		this.cloudRenderer.markForRebuild();
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
	public RenderTarget entityOutlineTarget() {
		return this.targets.entityOutline != null ? this.targets.entityOutline.get() : null;
	}

	@Nullable
	public RenderTarget getTranslucentTarget() {
		return this.targets.translucent != null ? this.targets.translucent.get() : null;
	}

	@Nullable
	public RenderTarget getItemEntityTarget() {
		return this.targets.itemEntity != null ? this.targets.itemEntity.get() : null;
	}

	@Nullable
	public RenderTarget getParticlesTarget() {
		return this.targets.particles != null ? this.targets.particles.get() : null;
	}

	@Nullable
	public RenderTarget getWeatherTarget() {
		return this.targets.weather != null ? this.targets.weather.get() : null;
	}

	@Nullable
	public RenderTarget getCloudsTarget() {
		return this.targets.clouds != null ? this.targets.clouds.get() : null;
	}

	@VisibleForDebug
	public ObjectArrayList<SectionRenderDispatcher.RenderSection> getVisibleSections() {
		return this.visibleSections;
	}

	@VisibleForDebug
	public SectionOcclusionGraph getSectionOcclusionGraph() {
		return this.sectionOcclusionGraph;
	}

	@Nullable
	public Frustum getCapturedFrustum() {
		return this.capturedFrustum;
	}

	public CloudRenderer getCloudRenderer() {
		return this.cloudRenderer;
	}
}
