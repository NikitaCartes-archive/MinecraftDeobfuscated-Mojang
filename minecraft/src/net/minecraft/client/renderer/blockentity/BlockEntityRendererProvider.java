package net.minecraft.client.renderer.blockentity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface BlockEntityRendererProvider<T extends BlockEntity> {
	BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context context);

	@Environment(EnvType.CLIENT)
	public static class Context {
		private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
		private final BlockRenderDispatcher blockRenderDispatcher;
		private final EntityModelSet modelSet;
		private final Font font;

		public Context(BlockEntityRenderDispatcher blockEntityRenderDispatcher, BlockRenderDispatcher blockRenderDispatcher, EntityModelSet entityModelSet, Font font) {
			this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
			this.blockRenderDispatcher = blockRenderDispatcher;
			this.modelSet = entityModelSet;
			this.font = font;
		}

		public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
			return this.blockEntityRenderDispatcher;
		}

		public BlockRenderDispatcher getBlockRenderDispatcher() {
			return this.blockRenderDispatcher;
		}

		public EntityModelSet getModelSet() {
			return this.modelSet;
		}

		public ModelPart bakeLayer(ModelLayerLocation modelLayerLocation) {
			return this.modelSet.bakeLayer(modelLayerLocation);
		}

		public Font getFont() {
			return this.font;
		}
	}
}
