package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class BlockEntityRenderDispatcher implements ResourceManagerReloadListener {
	private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();
	private final Font font;
	private final EntityModelSet entityModelSet;
	public Level level;
	public Camera camera;
	public HitResult cameraHitResult;
	private final Supplier<BlockRenderDispatcher> blockRenderDispatcher;
	private final Supplier<ItemRenderer> itemRenderer;
	private final Supplier<EntityRenderDispatcher> entityRenderer;

	public BlockEntityRenderDispatcher(
		Font font,
		EntityModelSet entityModelSet,
		Supplier<BlockRenderDispatcher> supplier,
		Supplier<ItemRenderer> supplier2,
		Supplier<EntityRenderDispatcher> supplier3
	) {
		this.itemRenderer = supplier2;
		this.entityRenderer = supplier3;
		this.font = font;
		this.entityModelSet = entityModelSet;
		this.blockRenderDispatcher = supplier;
	}

	@Nullable
	public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity) {
		return (BlockEntityRenderer<E>)this.renderers.get(blockEntity.getType());
	}

	public void prepare(Level level, Camera camera, HitResult hitResult) {
		if (this.level != level) {
			this.setLevel(level);
		}

		this.camera = camera;
		this.cameraHitResult = hitResult;
	}

	public <E extends BlockEntity> void render(E blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		BlockEntityRenderer<E> blockEntityRenderer = this.getRenderer(blockEntity);
		if (blockEntityRenderer != null) {
			if (blockEntity.hasLevel() && blockEntity.getType().isValid(blockEntity.getBlockState())) {
				if (blockEntityRenderer.shouldRender(blockEntity, this.camera.getPosition())) {
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

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		BlockEntityRendererProvider.Context context = new BlockEntityRendererProvider.Context(
			this,
			(BlockRenderDispatcher)this.blockRenderDispatcher.get(),
			(ItemRenderer)this.itemRenderer.get(),
			(EntityRenderDispatcher)this.entityRenderer.get(),
			this.entityModelSet,
			this.font
		);
		this.renderers = BlockEntityRenderers.createEntityRenderers(context);
	}
}
