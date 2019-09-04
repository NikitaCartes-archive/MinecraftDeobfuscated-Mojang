package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.culling.FrustumCuller;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
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
public class LevelRenderer implements AutoCloseable, ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
	public static final Direction[] DIRECTIONS = Direction.values();
	private final Minecraft minecraft;
	private final TextureManager textureManager;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private MultiPlayerLevel level;
	private Set<RenderChunk> chunksToCompile = Sets.<RenderChunk>newLinkedHashSet();
	private List<LevelRenderer.RenderChunkInfo> renderChunks = Lists.<LevelRenderer.RenderChunkInfo>newArrayListWithCapacity(69696);
	private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
	private ViewArea viewArea;
	private int starList = -1;
	private int skyList = -1;
	private int darkList = -1;
	private final VertexFormat skyFormat;
	private VertexBuffer starBuffer;
	private VertexBuffer skyBuffer;
	private VertexBuffer darkBuffer;
	private final int CLOUD_VERTEX_SIZE = 28;
	private boolean generateClouds = true;
	private int cloudList = -1;
	private VertexBuffer cloudBuffer;
	private int ticks;
	private final Map<Integer, BlockDestructionProgress> destroyingBlocks = Maps.<Integer, BlockDestructionProgress>newHashMap();
	private final Map<BlockPos, SoundInstance> playingRecords = Maps.<BlockPos, SoundInstance>newHashMap();
	private final TextureAtlasSprite[] breakingTextures = new TextureAtlasSprite[10];
	private RenderTarget entityTarget;
	private PostChain entityEffect;
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
	private final ChunkRenderList renderList;
	private int lastViewDistance = -1;
	private int noEntityRenderFrames = 2;
	private int renderedEntities;
	private int culledEntities;
	private boolean captureFrustum;
	private FrustumData capturedFrustum;
	private final Vector4f[] frustumPoints = new Vector4f[8];
	private final Vector3d frustumPos = new Vector3d(0.0, 0.0, 0.0);
	private final RenderChunkFactory renderChunkFactory;
	private double xTransparentOld;
	private double yTransparentOld;
	private double zTransparentOld;
	private boolean needsUpdate = true;
	private boolean hadRenderedEntityOutlines;

	public LevelRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
		this.textureManager = minecraft.getTextureManager();
		this.renderList = new ChunkRenderList();
		this.renderChunkFactory = RenderChunk::new;
		this.skyFormat = new VertexFormat();
		this.skyFormat.addElement(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3));
		this.createStars();
		this.createLightSky();
		this.createDarkSky();
	}

	public void close() {
		if (this.entityEffect != null) {
			this.entityEffect.close();
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.textureManager.bind(FORCEFIELD_LOCATION);
		RenderSystem.texParameter(3553, 10242, 10497);
		RenderSystem.texParameter(3553, 10243, 10497);
		RenderSystem.bindTexture(0);
		this.setupBreakingTextureSprites();
		this.initOutline();
	}

	private void setupBreakingTextureSprites() {
		TextureAtlas textureAtlas = this.minecraft.getTextureAtlas();
		this.breakingTextures[0] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_0);
		this.breakingTextures[1] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_1);
		this.breakingTextures[2] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_2);
		this.breakingTextures[3] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_3);
		this.breakingTextures[4] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_4);
		this.breakingTextures[5] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_5);
		this.breakingTextures[6] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_6);
		this.breakingTextures[7] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_7);
		this.breakingTextures[8] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_8);
		this.breakingTextures[9] = textureAtlas.getSprite(ModelBakery.DESTROY_STAGE_9);
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
			this.entityEffect.resize(this.minecraft.window.getWidth(), this.minecraft.window.getHeight());
			this.entityTarget = this.entityEffect.getTempTarget("final");
		} catch (IOException var3) {
			LOGGER.warn("Failed to load shader: {}", resourceLocation, var3);
			this.entityEffect = null;
			this.entityTarget = null;
		} catch (JsonSyntaxException var4) {
			LOGGER.warn("Failed to load shader: {}", resourceLocation, var4);
			this.entityEffect = null;
			this.entityTarget = null;
		}
	}

	public void doEntityOutline() {
		if (this.shouldShowEntityOutlines()) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			this.entityTarget.blitToScreen(this.minecraft.window.getWidth(), this.minecraft.window.getHeight(), false);
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
			this.darkBuffer.delete();
		}

		if (this.darkList >= 0) {
			MemoryTracker.releaseList(this.darkList);
			this.darkList = -1;
		}

		this.darkBuffer = new VertexBuffer(this.skyFormat);
		this.drawSkyHemisphere(bufferBuilder, -16.0F, true);
		bufferBuilder.end();
		bufferBuilder.clear();
		this.darkBuffer.upload(bufferBuilder.getBuffer());
	}

	private void createLightSky() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.skyBuffer != null) {
			this.skyBuffer.delete();
		}

		if (this.skyList >= 0) {
			MemoryTracker.releaseList(this.skyList);
			this.skyList = -1;
		}

		this.skyBuffer = new VertexBuffer(this.skyFormat);
		this.drawSkyHemisphere(bufferBuilder, 16.0F, false);
		bufferBuilder.end();
		bufferBuilder.clear();
		this.skyBuffer.upload(bufferBuilder.getBuffer());
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
			this.starBuffer.delete();
		}

		if (this.starList >= 0) {
			MemoryTracker.releaseList(this.starList);
			this.starList = -1;
		}

		this.starBuffer = new VertexBuffer(this.skyFormat);
		this.drawStars(bufferBuilder);
		bufferBuilder.end();
		bufferBuilder.clear();
		this.starBuffer.upload(bufferBuilder.getBuffer());
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

	public void setLevel(@Nullable MultiPlayerLevel multiPlayerLevel) {
		this.lastCameraX = Double.MIN_VALUE;
		this.lastCameraY = Double.MIN_VALUE;
		this.lastCameraZ = Double.MIN_VALUE;
		this.lastCameraChunkX = Integer.MIN_VALUE;
		this.lastCameraChunkY = Integer.MIN_VALUE;
		this.lastCameraChunkZ = Integer.MIN_VALUE;
		this.entityRenderDispatcher.setLevel(multiPlayerLevel);
		this.level = multiPlayerLevel;
		if (multiPlayerLevel != null) {
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
			if (this.chunkRenderDispatcher == null) {
				this.chunkRenderDispatcher = new ChunkRenderDispatcher(this.minecraft.is64Bit());
			}

			this.needsUpdate = true;
			this.generateClouds = true;
			LeavesBlock.setFancy(this.minecraft.options.fancyGraphics);
			this.lastViewDistance = this.minecraft.options.renderDistance;
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
			}

			this.resetChunksToCompile();
			synchronized (this.globalBlockEntities) {
				this.globalBlockEntities.clear();
			}

			this.viewArea = new ViewArea(this.level, this.minecraft.options.renderDistance, this, this.renderChunkFactory);
			if (this.level != null) {
				Entity entity = this.minecraft.getCameraEntity();
				if (entity != null) {
					this.viewArea.repositionCamera(entity.x, entity.z);
				}
			}

			this.noEntityRenderFrames = 2;
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
	}

	public void prepare(Camera camera) {
		BlockEntityRenderDispatcher.instance.prepare(this.level, this.minecraft.getTextureManager(), this.minecraft.font, camera, this.minecraft.hitResult);
		this.entityRenderDispatcher.prepare(this.level, this.minecraft.font, camera, this.minecraft.crosshairPickEntity, this.minecraft.options);
	}

	public void renderEntities(Camera camera, Culler culler, float f) {
		if (this.noEntityRenderFrames > 0) {
			this.noEntityRenderFrames--;
		} else {
			double d = camera.getPosition().x;
			double e = camera.getPosition().y;
			double g = camera.getPosition().z;
			this.level.getProfiler().push("prepare");
			this.renderedEntities = 0;
			this.culledEntities = 0;
			double h = camera.getPosition().x;
			double i = camera.getPosition().y;
			double j = camera.getPosition().z;
			BlockEntityRenderDispatcher.xOff = h;
			BlockEntityRenderDispatcher.yOff = i;
			BlockEntityRenderDispatcher.zOff = j;
			this.entityRenderDispatcher.setPosition(h, i, j);
			this.minecraft.gameRenderer.turnOnLightLayer();
			this.level.getProfiler().popPush("entities");
			List<Entity> list = Lists.<Entity>newArrayList();
			List<Entity> list2 = Lists.<Entity>newArrayList();

			for (Entity entity : this.level.entitiesForRendering()) {
				if ((this.entityRenderDispatcher.shouldRender(entity, culler, d, e, g) || entity.hasIndirectPassenger(this.minecraft.player))
					&& (entity != camera.getEntity() || camera.isDetached() || camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping())) {
					this.renderedEntities++;
					this.entityRenderDispatcher.render(entity, f, false);
					if (entity.isGlowing() || entity instanceof Player && this.minecraft.player.isSpectator() && this.minecraft.options.keySpectatorOutlines.isDown()) {
						list.add(entity);
					}

					if (this.entityRenderDispatcher.hasSecondPass(entity)) {
						list2.add(entity);
					}
				}
			}

			if (!list2.isEmpty()) {
				for (Entity entityx : list2) {
					this.entityRenderDispatcher.renderSecondPass(entityx, f);
				}
			}

			if (this.shouldShowEntityOutlines() && (!list.isEmpty() || this.hadRenderedEntityOutlines)) {
				this.level.getProfiler().popPush("entityOutlines");
				this.entityTarget.clear(Minecraft.ON_OSX);
				this.hadRenderedEntityOutlines = !list.isEmpty();
				if (!list.isEmpty()) {
					RenderSystem.depthFunc(519);
					RenderSystem.disableFog();
					this.entityTarget.bindWrite(false);
					Lighting.turnOff();
					this.entityRenderDispatcher.setSolidRendering(true);

					for (int k = 0; k < list.size(); k++) {
						this.entityRenderDispatcher.render((Entity)list.get(k), f, false);
					}

					this.entityRenderDispatcher.setSolidRendering(false);
					Lighting.turnOn();
					RenderSystem.depthMask(false);
					this.entityEffect.process(f);
					RenderSystem.enableLighting();
					RenderSystem.depthMask(true);
					RenderSystem.enableFog();
					RenderSystem.enableBlend();
					RenderSystem.enableColorMaterial();
					RenderSystem.depthFunc(515);
					RenderSystem.enableDepthTest();
					RenderSystem.enableAlphaTest();
				}

				this.minecraft.getMainRenderTarget().bindWrite(false);
			}

			this.level.getProfiler().popPush("blockentities");
			Lighting.turnOn();

			for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
				List<BlockEntity> list3 = renderChunkInfo.chunk.getCompiledChunk().getRenderableBlockEntities();
				if (!list3.isEmpty()) {
					for (BlockEntity blockEntity : list3) {
						BlockEntityRenderDispatcher.instance.render(blockEntity, f, -1);
					}
				}
			}

			synchronized (this.globalBlockEntities) {
				for (BlockEntity blockEntity2 : this.globalBlockEntities) {
					BlockEntityRenderDispatcher.instance.render(blockEntity2, f, -1);
				}
			}

			this.setupDestroyState();

			for (BlockDestructionProgress blockDestructionProgress : this.destroyingBlocks.values()) {
				BlockPos blockPos = blockDestructionProgress.getPos();
				BlockState blockState = this.level.getBlockState(blockPos);
				if (blockState.getBlock().isEntityBlock()) {
					BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
					if (blockEntity instanceof ChestBlockEntity && blockState.getValue(ChestBlock.TYPE) == ChestType.LEFT) {
						blockPos = blockPos.relative(((Direction)blockState.getValue(ChestBlock.FACING)).getClockWise());
						blockEntity = this.level.getBlockEntity(blockPos);
					}

					if (blockEntity != null && blockState.hasCustomBreakingProgress()) {
						BlockEntityRenderDispatcher.instance.render(blockEntity, f, blockDestructionProgress.getProgress());
					}
				}
			}

			this.restoreDestroyState();
			this.minecraft.gameRenderer.turnOffLightLayer();
			this.minecraft.getProfiler().pop();
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
			CompiledChunk compiledChunk = renderChunkInfo.chunk.compiled;
			if (compiledChunk != CompiledChunk.UNCOMPILED && !compiledChunk.hasNoRenderableLayers()) {
				i++;
			}
		}

		return i;
	}

	public String getEntityStatistics() {
		return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities;
	}

	public void setupRender(Camera camera, Culler culler, int i, boolean bl) {
		if (this.minecraft.options.renderDistance != this.lastViewDistance) {
			this.allChanged();
		}

		this.level.getProfiler().push("camera");
		double d = this.minecraft.player.x - this.lastCameraX;
		double e = this.minecraft.player.y - this.lastCameraY;
		double f = this.minecraft.player.z - this.lastCameraZ;
		if (this.lastCameraChunkX != this.minecraft.player.xChunk
			|| this.lastCameraChunkY != this.minecraft.player.yChunk
			|| this.lastCameraChunkZ != this.minecraft.player.zChunk
			|| d * d + e * e + f * f > 16.0) {
			this.lastCameraX = this.minecraft.player.x;
			this.lastCameraY = this.minecraft.player.y;
			this.lastCameraZ = this.minecraft.player.z;
			this.lastCameraChunkX = this.minecraft.player.xChunk;
			this.lastCameraChunkY = this.minecraft.player.yChunk;
			this.lastCameraChunkZ = this.minecraft.player.zChunk;
			this.viewArea.repositionCamera(this.minecraft.player.x, this.minecraft.player.z);
		}

		this.level.getProfiler().popPush("renderlistcamera");
		this.renderList.setCameraLocation(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		this.chunkRenderDispatcher.setCamera(camera.getPosition());
		this.level.getProfiler().popPush("cull");
		if (this.capturedFrustum != null) {
			FrustumCuller frustumCuller = new FrustumCuller(this.capturedFrustum);
			frustumCuller.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
			culler = frustumCuller;
		}

		this.minecraft.getProfiler().popPush("culling");
		BlockPos blockPos = camera.getBlockPosition();
		RenderChunk renderChunk = this.viewArea.getRenderChunkAt(blockPos);
		BlockPos blockPos2 = new BlockPos(
			Mth.floor(camera.getPosition().x / 16.0) * 16, Mth.floor(camera.getPosition().y / 16.0) * 16, Mth.floor(camera.getPosition().z / 16.0) * 16
		);
		float g = camera.getXRot();
		float h = camera.getYRot();
		this.needsUpdate = this.needsUpdate
			|| !this.chunksToCompile.isEmpty()
			|| camera.getPosition().x != this.prevCamX
			|| camera.getPosition().y != this.prevCamY
			|| camera.getPosition().z != this.prevCamZ
			|| (double)g != this.prevCamRotX
			|| (double)h != this.prevCamRotY;
		this.prevCamX = camera.getPosition().x;
		this.prevCamY = camera.getPosition().y;
		this.prevCamZ = camera.getPosition().z;
		this.prevCamRotX = (double)g;
		this.prevCamRotY = (double)h;
		boolean bl2 = this.capturedFrustum != null;
		this.minecraft.getProfiler().popPush("update");
		if (!bl2 && this.needsUpdate) {
			this.needsUpdate = false;
			this.renderChunks = Lists.<LevelRenderer.RenderChunkInfo>newArrayList();
			Queue<LevelRenderer.RenderChunkInfo> queue = Queues.<LevelRenderer.RenderChunkInfo>newArrayDeque();
			Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0, 1.0, 2.5));
			boolean bl3 = this.minecraft.smartCull;
			if (renderChunk != null) {
				boolean bl4 = false;
				LevelRenderer.RenderChunkInfo renderChunkInfo = new LevelRenderer.RenderChunkInfo(renderChunk, null, 0);
				Set<Direction> set = this.getVisibleDirections(blockPos);
				if (set.size() == 1) {
					Vec3 vec3 = camera.getLookVector();
					Direction direction = Direction.getNearest(vec3.x, vec3.y, vec3.z).getOpposite();
					set.remove(direction);
				}

				if (set.isEmpty()) {
					bl4 = true;
				}

				if (bl4 && !bl) {
					this.renderChunks.add(renderChunkInfo);
				} else {
					if (bl && this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos)) {
						bl3 = false;
					}

					renderChunk.setFrame(i);
					queue.add(renderChunkInfo);
				}
			} else {
				int j = blockPos.getY() > 0 ? 248 : 8;

				for (int k = -this.lastViewDistance; k <= this.lastViewDistance; k++) {
					for (int l = -this.lastViewDistance; l <= this.lastViewDistance; l++) {
						RenderChunk renderChunk2 = this.viewArea.getRenderChunkAt(new BlockPos((k << 4) + 8, j, (l << 4) + 8));
						if (renderChunk2 != null && culler.isVisible(renderChunk2.bb)) {
							renderChunk2.setFrame(i);
							queue.add(new LevelRenderer.RenderChunkInfo(renderChunk2, null, 0));
						}
					}
				}
			}

			this.minecraft.getProfiler().push("iteration");

			while (!queue.isEmpty()) {
				LevelRenderer.RenderChunkInfo renderChunkInfo2 = (LevelRenderer.RenderChunkInfo)queue.poll();
				RenderChunk renderChunk3 = renderChunkInfo2.chunk;
				Direction direction2 = renderChunkInfo2.sourceDirection;
				this.renderChunks.add(renderChunkInfo2);

				for (Direction direction3 : DIRECTIONS) {
					RenderChunk renderChunk4 = this.getRelativeFrom(blockPos2, renderChunk3, direction3);
					if ((!bl3 || !renderChunkInfo2.hasDirection(direction3.getOpposite()))
						&& (!bl3 || direction2 == null || renderChunk3.getCompiledChunk().facesCanSeeEachother(direction2.getOpposite(), direction3))
						&& renderChunk4 != null
						&& renderChunk4.hasAllNeighbors()
						&& renderChunk4.setFrame(i)
						&& culler.isVisible(renderChunk4.bb)) {
						LevelRenderer.RenderChunkInfo renderChunkInfo3 = new LevelRenderer.RenderChunkInfo(renderChunk4, direction3, renderChunkInfo2.step + 1);
						renderChunkInfo3.setDirections(renderChunkInfo2.directions, direction3);
						queue.add(renderChunkInfo3);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}

		this.minecraft.getProfiler().popPush("captureFrustum");
		if (this.captureFrustum) {
			this.captureFrustum(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
			this.captureFrustum = false;
		}

		this.minecraft.getProfiler().popPush("rebuildNear");
		Set<RenderChunk> set2 = this.chunksToCompile;
		this.chunksToCompile = Sets.<RenderChunk>newLinkedHashSet();

		for (LevelRenderer.RenderChunkInfo renderChunkInfo2 : this.renderChunks) {
			RenderChunk renderChunk3 = renderChunkInfo2.chunk;
			if (renderChunk3.isDirty() || set2.contains(renderChunk3)) {
				this.needsUpdate = true;
				BlockPos blockPos3 = renderChunk3.getOrigin().offset(8, 8, 8);
				boolean bl5 = blockPos3.distSqr(blockPos) < 768.0;
				if (!renderChunk3.isDirtyFromPlayer() && !bl5) {
					this.chunksToCompile.add(renderChunk3);
				} else {
					this.minecraft.getProfiler().push("build near");
					this.chunkRenderDispatcher.rebuildChunkSync(renderChunk3);
					renderChunk3.setNotDirty();
					this.minecraft.getProfiler().pop();
				}
			}
		}

		this.chunksToCompile.addAll(set2);
		this.minecraft.getProfiler().pop();
	}

	private Set<Direction> getVisibleDirections(BlockPos blockPos) {
		VisGraph visGraph = new VisGraph();
		BlockPos blockPos2 = new BlockPos(blockPos.getX() >> 4 << 4, blockPos.getY() >> 4 << 4, blockPos.getZ() >> 4 << 4);
		LevelChunk levelChunk = this.level.getChunkAt(blockPos2);

		for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos2, blockPos2.offset(15, 15, 15))) {
			if (levelChunk.getBlockState(blockPos3).isSolidRender(this.level, blockPos3)) {
				visGraph.setOpaque(blockPos3);
			}
		}

		return visGraph.floodFill(blockPos);
	}

	@Nullable
	private RenderChunk getRelativeFrom(BlockPos blockPos, RenderChunk renderChunk, Direction direction) {
		BlockPos blockPos2 = renderChunk.getRelativeOrigin(direction);
		if (Mth.abs(blockPos.getX() - blockPos2.getX()) > this.lastViewDistance * 16) {
			return null;
		} else if (blockPos2.getY() < 0 || blockPos2.getY() >= 256) {
			return null;
		} else {
			return Mth.abs(blockPos.getZ() - blockPos2.getZ()) > this.lastViewDistance * 16 ? null : this.viewArea.getRenderChunkAt(blockPos2);
		}
	}

	private void captureFrustum(double d, double e, double f) {
	}

	public int render(BlockLayer blockLayer, Camera camera) {
		Lighting.turnOff();
		if (blockLayer == BlockLayer.TRANSLUCENT) {
			this.minecraft.getProfiler().push("translucent_sort");
			double d = camera.getPosition().x - this.xTransparentOld;
			double e = camera.getPosition().y - this.yTransparentOld;
			double f = camera.getPosition().z - this.zTransparentOld;
			if (d * d + e * e + f * f > 1.0) {
				this.xTransparentOld = camera.getPosition().x;
				this.yTransparentOld = camera.getPosition().y;
				this.zTransparentOld = camera.getPosition().z;
				int i = 0;

				for (LevelRenderer.RenderChunkInfo renderChunkInfo : this.renderChunks) {
					if (renderChunkInfo.chunk.compiled.hasLayer(blockLayer) && i++ < 15) {
						this.chunkRenderDispatcher.resortChunkTransparencyAsync(renderChunkInfo.chunk);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}

		this.minecraft.getProfiler().push("filterempty");
		int j = 0;
		boolean bl = blockLayer == BlockLayer.TRANSLUCENT;
		int k = bl ? this.renderChunks.size() - 1 : 0;
		int l = bl ? -1 : this.renderChunks.size();
		int m = bl ? -1 : 1;

		for (int n = k; n != l; n += m) {
			RenderChunk renderChunk = ((LevelRenderer.RenderChunkInfo)this.renderChunks.get(n)).chunk;
			if (!renderChunk.getCompiledChunk().isEmpty(blockLayer)) {
				j++;
				this.renderList.add(renderChunk, blockLayer);
			}
		}

		this.minecraft.getProfiler().popPush((Supplier<String>)(() -> "render_" + blockLayer));
		this.renderSameAsLast(blockLayer);
		this.minecraft.getProfiler().pop();
		return j;
	}

	private void renderSameAsLast(BlockLayer blockLayer) {
		this.minecraft.gameRenderer.turnOnLightLayer();
		RenderSystem.enableClientState(32884);
		RenderSystem.glClientActiveTexture(33984);
		RenderSystem.enableClientState(32888);
		RenderSystem.glClientActiveTexture(33985);
		RenderSystem.enableClientState(32888);
		RenderSystem.glClientActiveTexture(33984);
		RenderSystem.enableClientState(32886);
		this.renderList.render(blockLayer);

		for (VertexFormatElement vertexFormatElement : DefaultVertexFormat.BLOCK.getElements()) {
			VertexFormatElement.Usage usage = vertexFormatElement.getUsage();
			int i = vertexFormatElement.getIndex();
			switch (usage) {
				case POSITION:
					RenderSystem.disableClientState(32884);
					break;
				case UV:
					RenderSystem.glClientActiveTexture(33984 + i);
					RenderSystem.disableClientState(32888);
					RenderSystem.glClientActiveTexture(33984);
					break;
				case COLOR:
					RenderSystem.disableClientState(32886);
					RenderSystem.clearCurrentColor();
			}
		}

		this.minecraft.gameRenderer.turnOffLightLayer();
	}

	private void updateBlockDestruction(Iterator<BlockDestructionProgress> iterator) {
		while (iterator.hasNext()) {
			BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)iterator.next();
			int i = blockDestructionProgress.getUpdatedRenderTick();
			if (this.ticks - i > 400) {
				iterator.remove();
			}
		}
	}

	public void tick() {
		this.ticks++;
		if (this.ticks % 20 == 0) {
			this.updateBlockDestruction(this.destroyingBlocks.values().iterator());
		}
	}

	private void renderEndSky() {
		RenderSystem.disableFog();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		Lighting.turnOff();
		RenderSystem.depthMask(false);
		this.textureManager.bind(END_SKY_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();

		for (int i = 0; i < 6; i++) {
			RenderSystem.pushMatrix();
			if (i == 1) {
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
			}

			if (i == 2) {
				RenderSystem.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
			}

			if (i == 3) {
				RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
			}

			if (i == 4) {
				RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
			}

			if (i == 5) {
				RenderSystem.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
			}

			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(-100.0, -100.0, -100.0).uv(0.0, 0.0).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(-100.0, -100.0, 100.0).uv(0.0, 16.0).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(100.0, -100.0, 100.0).uv(16.0, 16.0).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(100.0, -100.0, -100.0).uv(16.0, 0.0).color(40, 40, 40, 255).endVertex();
			tesselator.end();
			RenderSystem.popMatrix();
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
	}

	public void renderSky(float f) {
		if (this.minecraft.level.dimension.getType() == DimensionType.THE_END) {
			this.renderEndSky();
		} else if (this.minecraft.level.dimension.isNaturalDimension()) {
			RenderSystem.disableTexture();
			Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getBlockPosition(), f);
			float g = (float)vec3.x;
			float h = (float)vec3.y;
			float i = (float)vec3.z;
			RenderSystem.color3f(g, h, i);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.enableFog();
			RenderSystem.color3f(g, h, i);
			this.skyBuffer.bind();
			RenderSystem.enableClientState(32884);
			RenderSystem.vertexPointer(3, 5126, 12, 0);
			this.skyBuffer.draw(7);
			VertexBuffer.unbind();
			RenderSystem.disableClientState(32884);
			RenderSystem.disableFog();
			RenderSystem.disableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			Lighting.turnOff();
			float[] fs = this.level.dimension.getSunriseColor(this.level.getTimeOfDay(f), f);
			if (fs != null) {
				RenderSystem.disableTexture();
				RenderSystem.shadeModel(7425);
				RenderSystem.pushMatrix();
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.rotatef(Mth.sin(this.level.getSunAngle(f)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
				float j = fs[0];
				float k = fs[1];
				float l = fs[2];
				bufferBuilder.begin(6, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder.vertex(0.0, 100.0, 0.0).color(j, k, l, fs[3]).endVertex();
				int m = 16;

				for (int n = 0; n <= 16; n++) {
					float o = (float)n * (float) (Math.PI * 2) / 16.0F;
					float p = Mth.sin(o);
					float q = Mth.cos(o);
					bufferBuilder.vertex((double)(p * 120.0F), (double)(q * 120.0F), (double)(-q * 40.0F * fs[3])).color(fs[0], fs[1], fs[2], 0.0F).endVertex();
				}

				tesselator.end();
				RenderSystem.popMatrix();
				RenderSystem.shadeModel(7424);
			}

			RenderSystem.enableTexture();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			RenderSystem.pushMatrix();
			float j = 1.0F - this.level.getRainLevel(f);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, j);
			RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			RenderSystem.rotatef(this.level.getTimeOfDay(f) * 360.0F, 1.0F, 0.0F, 0.0F);
			float k = 30.0F;
			this.textureManager.bind(SUN_LOCATION);
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex((double)(-k), 100.0, (double)(-k)).uv(0.0, 0.0).endVertex();
			bufferBuilder.vertex((double)k, 100.0, (double)(-k)).uv(1.0, 0.0).endVertex();
			bufferBuilder.vertex((double)k, 100.0, (double)k).uv(1.0, 1.0).endVertex();
			bufferBuilder.vertex((double)(-k), 100.0, (double)k).uv(0.0, 1.0).endVertex();
			tesselator.end();
			k = 20.0F;
			this.textureManager.bind(MOON_LOCATION);
			int r = this.level.getMoonPhase();
			int m = r % 4;
			int n = r / 4 % 2;
			float o = (float)(m + 0) / 4.0F;
			float p = (float)(n + 0) / 2.0F;
			float q = (float)(m + 1) / 4.0F;
			float s = (float)(n + 1) / 2.0F;
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex((double)(-k), -100.0, (double)k).uv((double)q, (double)s).endVertex();
			bufferBuilder.vertex((double)k, -100.0, (double)k).uv((double)o, (double)s).endVertex();
			bufferBuilder.vertex((double)k, -100.0, (double)(-k)).uv((double)o, (double)p).endVertex();
			bufferBuilder.vertex((double)(-k), -100.0, (double)(-k)).uv((double)q, (double)p).endVertex();
			tesselator.end();
			RenderSystem.disableTexture();
			float t = this.level.getStarBrightness(f) * j;
			if (t > 0.0F) {
				RenderSystem.color4f(t, t, t, t);
				this.starBuffer.bind();
				RenderSystem.enableClientState(32884);
				RenderSystem.vertexPointer(3, 5126, 12, 0);
				this.starBuffer.draw(7);
				VertexBuffer.unbind();
				RenderSystem.disableClientState(32884);
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.enableFog();
			RenderSystem.popMatrix();
			RenderSystem.disableTexture();
			RenderSystem.color3f(0.0F, 0.0F, 0.0F);
			double d = this.minecraft.player.getEyePosition(f).y - this.level.getHorizonHeight();
			if (d < 0.0) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef(0.0F, 12.0F, 0.0F);
				this.darkBuffer.bind();
				RenderSystem.enableClientState(32884);
				RenderSystem.vertexPointer(3, 5126, 12, 0);
				this.darkBuffer.draw(7);
				VertexBuffer.unbind();
				RenderSystem.disableClientState(32884);
				RenderSystem.popMatrix();
			}

			if (this.level.dimension.hasGround()) {
				RenderSystem.color3f(g * 0.2F + 0.04F, h * 0.2F + 0.04F, i * 0.6F + 0.1F);
			} else {
				RenderSystem.color3f(g, h, i);
			}

			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, -((float)(d - 16.0)), 0.0F);
			RenderSystem.callList(this.darkList);
			RenderSystem.popMatrix();
			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
		}
	}

	public void renderClouds(float f, double d, double e, double g) {
		if (this.minecraft.level.dimension.isNaturalDimension()) {
			float h = 12.0F;
			float i = 4.0F;
			double j = 2.0E-4;
			double k = (double)(((float)this.ticks + f) * 0.03F);
			double l = (d + k) / 12.0;
			double m = (double)(this.level.dimension.getCloudHeight() - (float)e + 0.33F);
			double n = g / 12.0 + 0.33F;
			l -= (double)(Mth.floor(l / 2048.0) * 2048);
			n -= (double)(Mth.floor(n / 2048.0) * 2048);
			float o = (float)(l - (double)Mth.floor(l));
			float p = (float)(m / 4.0 - (double)Mth.floor(m / 4.0)) * 4.0F;
			float q = (float)(n - (double)Mth.floor(n));
			Vec3 vec3 = this.level.getCloudColor(f);
			int r = (int)Math.floor(l);
			int s = (int)Math.floor(m / 4.0);
			int t = (int)Math.floor(n);
			if (r != this.prevCloudX
				|| s != this.prevCloudY
				|| t != this.prevCloudZ
				|| this.minecraft.options.getCloudsType() != this.prevCloudsType
				|| this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4) {
				this.prevCloudX = r;
				this.prevCloudY = s;
				this.prevCloudZ = t;
				this.prevCloudColor = vec3;
				this.prevCloudsType = this.minecraft.options.getCloudsType();
				this.generateClouds = true;
			}

			if (this.generateClouds) {
				this.generateClouds = false;
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				if (this.cloudBuffer != null) {
					this.cloudBuffer.delete();
				}

				if (this.cloudList >= 0) {
					MemoryTracker.releaseList(this.cloudList);
					this.cloudList = -1;
				}

				this.cloudBuffer = new VertexBuffer(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
				this.buildClouds(bufferBuilder, l, m, n, vec3);
				bufferBuilder.end();
				bufferBuilder.clear();
				this.cloudBuffer.upload(bufferBuilder.getBuffer());
			}

			RenderSystem.disableCull();
			this.textureManager.bind(CLOUDS_LOCATION);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			RenderSystem.pushMatrix();
			RenderSystem.scalef(12.0F, 1.0F, 12.0F);
			RenderSystem.translatef(-o, p, -q);
			if (this.cloudBuffer != null) {
				this.cloudBuffer.bind();
				RenderSystem.enableClientState(32884);
				RenderSystem.enableClientState(32888);
				RenderSystem.glClientActiveTexture(33984);
				RenderSystem.enableClientState(32886);
				RenderSystem.enableClientState(32885);
				RenderSystem.vertexPointer(3, 5126, 28, 0);
				RenderSystem.texCoordPointer(2, 5126, 28, 12);
				RenderSystem.colorPointer(4, 5121, 28, 20);
				RenderSystem.normalPointer(5120, 28, 24);
				int u = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

				for (int v = u; v < 2; v++) {
					if (v == 0) {
						RenderSystem.colorMask(false, false, false, false);
					} else {
						RenderSystem.colorMask(true, true, true, true);
					}

					this.cloudBuffer.draw(7);
				}

				VertexBuffer.unbind();
				RenderSystem.disableClientState(32884);
				RenderSystem.disableClientState(32888);
				RenderSystem.disableClientState(32886);
				RenderSystem.disableClientState(32885);
			} else if (this.cloudList >= 0) {
				int u = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

				for (int v = u; v < 2; v++) {
					if (v == 0) {
						RenderSystem.colorMask(false, false, false, false);
					} else {
						RenderSystem.colorMask(true, true, true, true);
					}

					RenderSystem.callList(this.cloudList);
				}
			}

			RenderSystem.popMatrix();
			RenderSystem.clearCurrentColor();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
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
							.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + 8.0F))
							.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + 0.0F))
							.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + 0.0F))
							.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
							.color(t, u, v, 0.8F)
							.normal(0.0F, -1.0F, 0.0F)
							.endVertex();
					}

					if (z <= 5.0F) {
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 8.0F))
							.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 8.0F))
							.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 0.0F))
							.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
						bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F - 9.765625E-4F), (double)(ad + 0.0F))
							.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
							.color(n, o, p, 0.8F)
							.normal(0.0F, 1.0F, 0.0F)
							.endVertex();
					}

					if (aa > -1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 0.0F), (double)(ad + 8.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 4.0F), (double)(ad + 8.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 4.0F), (double)(ad + 0.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 0.0F), (double)(z + 0.0F), (double)(ad + 0.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(-1.0F, 0.0F, 0.0F)
								.endVertex();
						}
					}

					if (aa <= 1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 0.0F), (double)(ad + 8.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 4.0F), (double)(ad + 8.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 8.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 4.0F), (double)(ad + 0.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + (float)ae + 1.0F - 9.765625E-4F), (double)(z + 0.0F), (double)(ad + 0.0F))
								.uv((double)((ac + (float)ae + 0.5F) * 0.00390625F + l), (double)((ad + 0.0F) * 0.00390625F + m))
								.color(q, r, s, 0.8F)
								.normal(1.0F, 0.0F, 0.0F)
								.endVertex();
						}
					}

					if (ab > -1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 0.0F))
								.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 0.0F))
								.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 0.0F))
								.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 0.0F))
								.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, -1.0F)
								.endVertex();
						}
					}

					if (ab <= 1) {
						for (int ae = 0; ae < 8; ae++) {
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 4.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 8.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((double)((ac + 8.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
								.color(w, x, y, 0.8F)
								.normal(0.0F, 0.0F, 1.0F)
								.endVertex();
							bufferBuilder.vertex((double)(ac + 0.0F), (double)(z + 0.0F), (double)(ad + (float)ae + 1.0F - 9.765625E-4F))
								.uv((double)((ac + 0.0F) * 0.00390625F + l), (double)((ad + (float)ae + 0.5F) * 0.00390625F + m))
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
						.uv((double)((float)(af + 0) * 0.00390625F + l), (double)((float)(ag + 32) * 0.00390625F + m))
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
					bufferBuilder.vertex((double)(af + 32), (double)z, (double)(ag + 32))
						.uv((double)((float)(af + 32) * 0.00390625F + l), (double)((float)(ag + 32) * 0.00390625F + m))
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
					bufferBuilder.vertex((double)(af + 32), (double)z, (double)(ag + 0))
						.uv((double)((float)(af + 32) * 0.00390625F + l), (double)((float)(ag + 0) * 0.00390625F + m))
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
					bufferBuilder.vertex((double)(af + 0), (double)z, (double)(ag + 0))
						.uv((double)((float)(af + 0) * 0.00390625F + l), (double)((float)(ag + 0) * 0.00390625F + m))
						.color(n, o, p, 0.8F)
						.normal(0.0F, -1.0F, 0.0F)
						.endVertex();
				}
			}
		}
	}

	public void compileChunksUntil(long l) {
		this.needsUpdate = this.needsUpdate | this.chunkRenderDispatcher.uploadAllPendingUploadsUntil(l);
		if (!this.chunksToCompile.isEmpty()) {
			Iterator<RenderChunk> iterator = this.chunksToCompile.iterator();

			while (iterator.hasNext()) {
				RenderChunk renderChunk = (RenderChunk)iterator.next();
				boolean bl;
				if (renderChunk.isDirtyFromPlayer()) {
					bl = this.chunkRenderDispatcher.rebuildChunkSync(renderChunk);
				} else {
					bl = this.chunkRenderDispatcher.rebuildChunkAsync(renderChunk);
				}

				if (!bl) {
					break;
				}

				renderChunk.setNotDirty();
				iterator.remove();
				long m = l - Util.getNanos();
				if (m < 0L) {
					break;
				}
			}
		}
	}

	public void renderWorldBounds(Camera camera, float f) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		WorldBorder worldBorder = this.level.getWorldBorder();
		double d = (double)(this.minecraft.options.renderDistance * 16);
		if (!(camera.getPosition().x < worldBorder.getMaxX() - d)
			|| !(camera.getPosition().x > worldBorder.getMinX() + d)
			|| !(camera.getPosition().z < worldBorder.getMaxZ() - d)
			|| !(camera.getPosition().z > worldBorder.getMinZ() + d)) {
			double e = 1.0 - worldBorder.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d;
			e = Math.pow(e, 4.0);
			double g = camera.getPosition().x;
			double h = camera.getPosition().y;
			double i = camera.getPosition().z;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			this.textureManager.bind(FORCEFIELD_LOCATION);
			RenderSystem.depthMask(false);
			RenderSystem.pushMatrix();
			int j = worldBorder.getStatus().getColor();
			float k = (float)(j >> 16 & 0xFF) / 255.0F;
			float l = (float)(j >> 8 & 0xFF) / 255.0F;
			float m = (float)(j & 0xFF) / 255.0F;
			RenderSystem.color4f(k, l, m, (float)e);
			RenderSystem.polygonOffset(-3.0F, -3.0F);
			RenderSystem.enablePolygonOffset();
			RenderSystem.alphaFunc(516, 0.1F);
			RenderSystem.enableAlphaTest();
			RenderSystem.disableCull();
			float n = (float)(Util.getMillis() % 3000L) / 3000.0F;
			float o = 0.0F;
			float p = 0.0F;
			float q = 128.0F;
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.offset(-g, -h, -i);
			double r = Math.max((double)Mth.floor(i - d), worldBorder.getMinZ());
			double s = Math.min((double)Mth.ceil(i + d), worldBorder.getMaxZ());
			if (g > worldBorder.getMaxX() - d) {
				float t = 0.0F;

				for (double u = r; u < s; t += 0.5F) {
					double v = Math.min(1.0, s - u);
					float w = (float)v * 0.5F;
					bufferBuilder.vertex(worldBorder.getMaxX(), 256.0, u).uv((double)(n + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(worldBorder.getMaxX(), 256.0, u + v).uv((double)(n + w + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(worldBorder.getMaxX(), 0.0, u + v).uv((double)(n + w + t), (double)(n + 128.0F)).endVertex();
					bufferBuilder.vertex(worldBorder.getMaxX(), 0.0, u).uv((double)(n + t), (double)(n + 128.0F)).endVertex();
					u++;
				}
			}

			if (g < worldBorder.getMinX() + d) {
				float t = 0.0F;

				for (double u = r; u < s; t += 0.5F) {
					double v = Math.min(1.0, s - u);
					float w = (float)v * 0.5F;
					bufferBuilder.vertex(worldBorder.getMinX(), 256.0, u).uv((double)(n + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(worldBorder.getMinX(), 256.0, u + v).uv((double)(n + w + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(worldBorder.getMinX(), 0.0, u + v).uv((double)(n + w + t), (double)(n + 128.0F)).endVertex();
					bufferBuilder.vertex(worldBorder.getMinX(), 0.0, u).uv((double)(n + t), (double)(n + 128.0F)).endVertex();
					u++;
				}
			}

			r = Math.max((double)Mth.floor(g - d), worldBorder.getMinX());
			s = Math.min((double)Mth.ceil(g + d), worldBorder.getMaxX());
			if (i > worldBorder.getMaxZ() - d) {
				float t = 0.0F;

				for (double u = r; u < s; t += 0.5F) {
					double v = Math.min(1.0, s - u);
					float w = (float)v * 0.5F;
					bufferBuilder.vertex(u, 256.0, worldBorder.getMaxZ()).uv((double)(n + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(u + v, 256.0, worldBorder.getMaxZ()).uv((double)(n + w + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(u + v, 0.0, worldBorder.getMaxZ()).uv((double)(n + w + t), (double)(n + 128.0F)).endVertex();
					bufferBuilder.vertex(u, 0.0, worldBorder.getMaxZ()).uv((double)(n + t), (double)(n + 128.0F)).endVertex();
					u++;
				}
			}

			if (i < worldBorder.getMinZ() + d) {
				float t = 0.0F;

				for (double u = r; u < s; t += 0.5F) {
					double v = Math.min(1.0, s - u);
					float w = (float)v * 0.5F;
					bufferBuilder.vertex(u, 256.0, worldBorder.getMinZ()).uv((double)(n + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(u + v, 256.0, worldBorder.getMinZ()).uv((double)(n + w + t), (double)(n + 0.0F)).endVertex();
					bufferBuilder.vertex(u + v, 0.0, worldBorder.getMinZ()).uv((double)(n + w + t), (double)(n + 128.0F)).endVertex();
					bufferBuilder.vertex(u, 0.0, worldBorder.getMinZ()).uv((double)(n + t), (double)(n + 128.0F)).endVertex();
					u++;
				}
			}

			tesselator.end();
			bufferBuilder.offset(0.0, 0.0, 0.0);
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

	private void setupDestroyState() {
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.5F);
		RenderSystem.polygonOffset(-1.0F, -10.0F);
		RenderSystem.enablePolygonOffset();
		RenderSystem.alphaFunc(516, 0.1F);
		RenderSystem.enableAlphaTest();
		RenderSystem.pushMatrix();
	}

	private void restoreDestroyState() {
		RenderSystem.disableAlphaTest();
		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();
		RenderSystem.enableAlphaTest();
		RenderSystem.depthMask(true);
		RenderSystem.popMatrix();
	}

	public void renderDestroyAnimation(Tesselator tesselator, BufferBuilder bufferBuilder, Camera camera) {
		double d = camera.getPosition().x;
		double e = camera.getPosition().y;
		double f = camera.getPosition().z;
		if (!this.destroyingBlocks.isEmpty()) {
			this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
			this.setupDestroyState();
			bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
			bufferBuilder.offset(-d, -e, -f);
			bufferBuilder.noColor();
			Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

			while (iterator.hasNext()) {
				BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)iterator.next();
				BlockPos blockPos = blockDestructionProgress.getPos();
				Block block = this.level.getBlockState(blockPos).getBlock();
				if (!(block instanceof ChestBlock) && !(block instanceof EnderChestBlock) && !(block instanceof SignBlock) && !(block instanceof AbstractSkullBlock)) {
					double g = (double)blockPos.getX() - d;
					double h = (double)blockPos.getY() - e;
					double i = (double)blockPos.getZ() - f;
					if (g * g + h * h + i * i > 1024.0) {
						iterator.remove();
					} else {
						BlockState blockState = this.level.getBlockState(blockPos);
						if (!blockState.isAir()) {
							int j = blockDestructionProgress.getProgress();
							TextureAtlasSprite textureAtlasSprite = this.breakingTextures[j];
							BlockRenderDispatcher blockRenderDispatcher = this.minecraft.getBlockRenderer();
							blockRenderDispatcher.renderBreakingTexture(blockState, blockPos, textureAtlasSprite, this.level);
						}
					}
				}
			}

			tesselator.end();
			bufferBuilder.offset(0.0, 0.0, 0.0);
			this.restoreDestroyState();
		}
	}

	public void renderHitOutline(Camera camera, HitResult hitResult, int i) {
		if (i == 0 && hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
			BlockState blockState = this.level.getBlockState(blockPos);
			if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos)) {
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(
					GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
				);
				RenderSystem.lineWidth(Math.max(2.5F, (float)this.minecraft.window.getWidth() / 1920.0F * 2.5F));
				RenderSystem.disableTexture();
				RenderSystem.depthMask(false);
				RenderSystem.matrixMode(5889);
				RenderSystem.pushMatrix();
				RenderSystem.scalef(1.0F, 1.0F, 0.999F);
				double d = camera.getPosition().x;
				double e = camera.getPosition().y;
				double f = camera.getPosition().z;
				renderShape(
					blockState.getShape(this.level, blockPos, CollisionContext.of(camera.getEntity())),
					(double)blockPos.getX() - d,
					(double)blockPos.getY() - e,
					(double)blockPos.getZ() - f,
					0.0F,
					0.0F,
					0.0F,
					0.4F
				);
				RenderSystem.popMatrix();
				RenderSystem.matrixMode(5888);
				RenderSystem.depthMask(true);
				RenderSystem.enableTexture();
				RenderSystem.disableBlend();
			}
		}
	}

	public static void renderVoxelShape(VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
		List<AABB> list = voxelShape.toAabbs();
		int k = Mth.ceil((double)list.size() / 3.0);

		for (int l = 0; l < list.size(); l++) {
			AABB aABB = (AABB)list.get(l);
			float m = ((float)l % (float)k + 1.0F) / (float)k;
			float n = (float)(l / k);
			float o = m * (float)(n == 0.0F ? 1 : 0);
			float p = m * (float)(n == 1.0F ? 1 : 0);
			float q = m * (float)(n == 2.0F ? 1 : 0);
			renderShape(Shapes.create(aABB.move(0.0, 0.0, 0.0)), d, e, f, o, p, q, 1.0F);
		}
	}

	public static void renderShape(VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
		voxelShape.forAllEdges((k, l, m, n, o, p) -> {
			bufferBuilder.vertex(k + d, l + e, m + f).color(g, h, i, j).endVertex();
			bufferBuilder.vertex(n + d, o + e, p + f).color(g, h, i, j).endVertex();
		});
		tesselator.end();
	}

	public static void renderLineBox(AABB aABB, float f, float g, float h, float i) {
		renderLineBox(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i);
	}

	public static void renderLineBox(double d, double e, double f, double g, double h, double i, float j, float k, float l, float m) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
		addChainedLineBoxVertices(bufferBuilder, d, e, f, g, h, i, j, k, l, m);
		tesselator.end();
	}

	public static void addChainedLineBoxVertices(
		BufferBuilder bufferBuilder, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		bufferBuilder.vertex(d, e, f).color(j, k, l, 0.0F).endVertex();
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(d, h, i).color(j, k, l, 0.0F).endVertex();
		bufferBuilder.vertex(d, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, i).color(j, k, l, 0.0F).endVertex();
		bufferBuilder.vertex(g, e, i).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, h, f).color(j, k, l, 0.0F).endVertex();
		bufferBuilder.vertex(g, e, f).color(j, k, l, m).endVertex();
		bufferBuilder.vertex(g, e, f).color(j, k, l, 0.0F).endVertex();
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
				this.minecraft.gui.setNowPlaying(recordItem.getDisplayName().getColoredString());
			}

			SoundInstance var5 = SimpleSoundInstance.forRecord(soundEvent, (float)blockPos.getX(), (float)blockPos.getY(), (float)blockPos.getZ());
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
			crashReportCategory.setDetail("ID", Registry.PARTICLE_TYPE.getKey((ParticleType<? extends ParticleOptions>)particleOptions.getType()));
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
				this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1006:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1007:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1008:
				this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
				this.level.playLocalSound(blockPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1012:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1013:
				this.level.playLocalSound(blockPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1014:
				this.level.playLocalSound(blockPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1030:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1031:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1032:
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PORTAL_TRAVEL, random.nextFloat() * 0.4F + 0.8F));
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
				this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1037:
				this.level.playLocalSound(blockPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
			case 1500:
				ComposterBlock.handleFill(this.level, blockPos, j > 0);
				break;
			case 1501:
				this.level
					.playLocalSound(
						blockPos,
						SoundEvents.LAVA_EXTINGUISH,
						SoundSource.BLOCKS,
						0.5F,
						2.6F + (this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.8F,
						false
					);

				for (int kx = 0; kx < 8; kx++) {
					this.level
						.addParticle(
							ParticleTypes.LARGE_SMOKE,
							(double)blockPos.getX() + Math.random(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + Math.random(),
							0.0,
							0.0,
							0.0
						);
				}
				break;
			case 1502:
				this.level
					.playLocalSound(
						blockPos,
						SoundEvents.REDSTONE_TORCH_BURNOUT,
						SoundSource.BLOCKS,
						0.5F,
						2.6F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.8F,
						false
					);

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
					double u = (double)((float)blockPos.getX() + (5.0F + random.nextFloat() * 6.0F) / 16.0F);
					double d = (double)((float)blockPos.getY() + 0.8125F);
					double e = (double)((float)blockPos.getZ() + (5.0F + random.nextFloat() * 6.0F) / 16.0F);
					double f = 0.0;
					double aa = 0.0;
					double ab = 0.0;
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

				for (int nx = 0; nx < 10; nx++) {
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
				double t = (double)blockPos.getX();
				double u = (double)blockPos.getY();
				double d = (double)blockPos.getZ();

				for (int v = 0; v < 8; v++) {
					this.addParticle(
						new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)),
						t,
						u,
						d,
						random.nextGaussian() * 0.15,
						random.nextDouble() * 0.2,
						random.nextGaussian() * 0.15
					);
				}

				float w = (float)(j >> 16 & 0xFF) / 255.0F;
				float x = (float)(j >> 8 & 0xFF) / 255.0F;
				float y = (float)(j >> 0 & 0xFF) / 255.0F;
				ParticleOptions particleOptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

				for (int n = 0; n < 100; n++) {
					double g = random.nextDouble() * 4.0;
					double h = random.nextDouble() * Math.PI * 2.0;
					double o = Math.cos(h) * g;
					double p = 0.01 + random.nextDouble() * 0.5;
					double q = Math.sin(h) * g;
					Particle particle = this.addParticleInternal(particleOptions, particleOptions.getType().getOverrideLimiter(), t + o * 0.1, u + 0.3, d + q * 0.1, o, p, q);
					if (particle != null) {
						float z = 0.75F + random.nextFloat() * 0.25F;
						particle.setColor(w * z, x * z, y * z);
						particle.setPower((float)g);
					}
				}

				this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
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
					double u = (double)blockPos.getX() + 0.5 + ((double)this.level.random.nextFloat() - 0.5) * 2.0;
					double d = (double)blockPos.getY() + 0.5 + ((double)this.level.random.nextFloat() - 0.5) * 2.0;
					double e = (double)blockPos.getZ() + 0.5 + ((double)this.level.random.nextFloat() - 0.5) * 2.0;
					this.level.addParticle(ParticleTypes.SMOKE, u, d, e, 0.0, 0.0, 0.0);
					this.level.addParticle(ParticleTypes.FLAME, u, d, e, 0.0, 0.0, 0.0);
				}
				break;
			case 2005:
				BoneMealItem.addGrowthParticles(this.level, blockPos, j);
				break;
			case 2006:
				for (int k = 0; k < 200; k++) {
					float ac = random.nextFloat() * 4.0F;
					float ad = random.nextFloat() * (float) (Math.PI * 2);
					double d = (double)(Mth.cos(ad) * ac);
					double e = 0.01 + random.nextDouble() * 0.5;
					double f = (double)(Mth.sin(ad) * ac);
					Particle particle2 = this.addParticleInternal(
						ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + d * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + f * 0.1, d, e, f
					);
					if (particle2 != null) {
						particle2.setPower(ac);
					}
				}

				this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 2008:
				this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
				break;
			case 2009:
				for (int k = 0; k < 8; k++) {
					this.level
						.addParticle(
							ParticleTypes.CLOUD, (double)blockPos.getX() + Math.random(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + Math.random(), 0.0, 0.0, 0.0
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
			BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)this.destroyingBlocks.get(i);
			if (blockDestructionProgress == null
				|| blockDestructionProgress.getPos().getX() != blockPos.getX()
				|| blockDestructionProgress.getPos().getY() != blockPos.getY()
				|| blockDestructionProgress.getPos().getZ() != blockPos.getZ()) {
				blockDestructionProgress = new BlockDestructionProgress(i, blockPos);
				this.destroyingBlocks.put(i, blockDestructionProgress);
			}

			blockDestructionProgress.setProgress(j);
			blockDestructionProgress.updateTick(this.ticks);
		} else {
			this.destroyingBlocks.remove(i);
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

	@Environment(EnvType.CLIENT)
	class RenderChunkInfo {
		private final RenderChunk chunk;
		private final Direction sourceDirection;
		private byte directions;
		private final int step;

		private RenderChunkInfo(RenderChunk renderChunk, @Nullable Direction direction, int i) {
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
}
