package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class BlockEntityRenderDispatcher {
	private final Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = Maps.<BlockEntityType<?>, BlockEntityRenderer<?>>newHashMap();
	public static final BlockEntityRenderDispatcher instance = new BlockEntityRenderDispatcher();
	private final BufferBuilder singleRenderBuffer = new BufferBuilder(256);
	private Font font;
	public TextureManager textureManager;
	public Level level;
	public Camera camera;
	public HitResult cameraHitResult;

	private BlockEntityRenderDispatcher() {
		this.register(BlockEntityType.SIGN, new SignRenderer(this));
		this.register(BlockEntityType.MOB_SPAWNER, new SpawnerRenderer(this));
		this.register(BlockEntityType.PISTON, new PistonHeadRenderer(this));
		this.register(BlockEntityType.CHEST, new ChestRenderer<>(this));
		this.register(BlockEntityType.ENDER_CHEST, new ChestRenderer<>(this));
		this.register(BlockEntityType.TRAPPED_CHEST, new ChestRenderer<>(this));
		this.register(BlockEntityType.ENCHANTING_TABLE, new EnchantTableRenderer(this));
		this.register(BlockEntityType.LECTERN, new LecternRenderer(this));
		this.register(BlockEntityType.END_PORTAL, new TheEndPortalRenderer<>(this));
		this.register(BlockEntityType.END_GATEWAY, new TheEndGatewayRenderer(this));
		this.register(BlockEntityType.BEACON, new BeaconRenderer(this));
		this.register(BlockEntityType.SKULL, new SkullBlockRenderer(this));
		this.register(BlockEntityType.BANNER, new BannerRenderer(this));
		this.register(BlockEntityType.STRUCTURE_BLOCK, new StructureBlockRenderer(this));
		this.register(BlockEntityType.SHULKER_BOX, new ShulkerBoxRenderer(new ShulkerModel(), this));
		this.register(BlockEntityType.BED, new BedRenderer(this));
		this.register(BlockEntityType.CONDUIT, new ConduitRenderer(this));
		this.register(BlockEntityType.BELL, new BellRenderer(this));
		this.register(BlockEntityType.CAMPFIRE, new CampfireRenderer(this));
	}

	private <E extends BlockEntity> void register(BlockEntityType<E> blockEntityType, BlockEntityRenderer<E> blockEntityRenderer) {
		this.renderers.put(blockEntityType, blockEntityRenderer);
	}

	@Nullable
	public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity) {
		return (BlockEntityRenderer<E>)this.renderers.get(blockEntity.getType());
	}

	public void prepare(Level level, TextureManager textureManager, Font font, Camera camera, HitResult hitResult) {
		if (this.level != level) {
			this.setLevel(level);
		}

		this.textureManager = textureManager;
		this.camera = camera;
		this.font = font;
		this.cameraHitResult = hitResult;
	}

	public <E extends BlockEntity> void render(E blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		if (blockEntity.distanceToSqr(this.camera.getPosition().x, this.camera.getPosition().y, this.camera.getPosition().z) < blockEntity.getViewDistance()) {
			BlockEntityRenderer<E> blockEntityRenderer = this.getRenderer(blockEntity);
			if (blockEntityRenderer != null) {
				if (blockEntity.hasLevel() && blockEntity.getType().isValid(blockEntity.getBlockState().getBlock())) {
					tryRender(blockEntity, () -> setupAndRender(blockEntityRenderer, blockEntity, f, poseStack, multiBufferSource));
				}
			}
		}
	}

	private static <T extends BlockEntity> void setupAndRender(
		BlockEntityRenderer<T> blockEntityRenderer, T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource
	) {
		Level level = blockEntity.getLevel();
		int i;
		if (level != null) {
			i = LevelRenderer.getLightColor(level, blockEntity.getBlockPos());
		} else {
			i = 15728880;
		}

		blockEntityRenderer.render(blockEntity, f, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
	}

	@Deprecated
	public <E extends BlockEntity> void renderItem(E blockEntity, PoseStack poseStack) {
		MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(this.singleRenderBuffer);
		this.renderItem(blockEntity, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
		bufferSource.endBatch();
	}

	public <E extends BlockEntity> boolean renderItem(E blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockEntityRenderer<E> blockEntityRenderer = this.getRenderer(blockEntity);
		if (blockEntityRenderer == null) {
			return true;
		} else {
			tryRender(blockEntity, () -> blockEntityRenderer.render(blockEntity, 0.0F, poseStack, multiBufferSource, i, j));
			return false;
		}
	}

	private static void tryRender(BlockEntity blockEntity, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable var5) {
			CrashReport crashReport = CrashReport.forThrowable(var5, "Rendering Block Entity");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block Entity Details");
			blockEntity.fillCrashReportCategory(crashReportCategory);
			throw new ReportedException(crashReport);
		}
	}

	public void setLevel(@Nullable Level level) {
		this.level = level;
		if (level == null) {
			this.camera = null;
		}
	}

	public Font getFont() {
		return this.font;
	}
}
