package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface EntityRendererProvider<T extends Entity> {
	EntityRenderer<T, ?> create(EntityRendererProvider.Context context);

	@Environment(EnvType.CLIENT)
	public static class Context {
		private final EntityRenderDispatcher entityRenderDispatcher;
		private final ItemRenderer itemRenderer;
		private final MapRenderer mapRenderer;
		private final BlockRenderDispatcher blockRenderDispatcher;
		private final ResourceManager resourceManager;
		private final EntityModelSet modelSet;
		private final Font font;

		public Context(
			EntityRenderDispatcher entityRenderDispatcher,
			ItemRenderer itemRenderer,
			MapRenderer mapRenderer,
			BlockRenderDispatcher blockRenderDispatcher,
			ResourceManager resourceManager,
			EntityModelSet entityModelSet,
			Font font
		) {
			this.entityRenderDispatcher = entityRenderDispatcher;
			this.itemRenderer = itemRenderer;
			this.mapRenderer = mapRenderer;
			this.blockRenderDispatcher = blockRenderDispatcher;
			this.resourceManager = resourceManager;
			this.modelSet = entityModelSet;
			this.font = font;
		}

		public EntityRenderDispatcher getEntityRenderDispatcher() {
			return this.entityRenderDispatcher;
		}

		public ItemRenderer getItemRenderer() {
			return this.itemRenderer;
		}

		public MapRenderer getMapRenderer() {
			return this.mapRenderer;
		}

		public BlockRenderDispatcher getBlockRenderDispatcher() {
			return this.blockRenderDispatcher;
		}

		public ResourceManager getResourceManager() {
			return this.resourceManager;
		}

		public EntityModelSet getModelSet() {
			return this.modelSet;
		}

		public ModelManager getModelManager() {
			return this.blockRenderDispatcher.getBlockModelShaper().getModelManager();
		}

		public ModelPart bakeLayer(ModelLayerLocation modelLayerLocation) {
			return this.modelSet.bakeLayer(modelLayerLocation);
		}

		public Font getFont() {
			return this.font;
		}
	}
}
