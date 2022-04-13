/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface EntityRendererProvider<T extends Entity> {
    public EntityRenderer<T> create(Context var1);

    @Environment(value=EnvType.CLIENT)
    public static class Context {
        private final EntityRenderDispatcher entityRenderDispatcher;
        private final ItemRenderer itemRenderer;
        private final BlockRenderDispatcher blockRenderDispatcher;
        private final ItemInHandRenderer itemInHandRenderer;
        private final ResourceManager resourceManager;
        private final EntityModelSet modelSet;
        private final Font font;

        public Context(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, BlockRenderDispatcher blockRenderDispatcher, ItemInHandRenderer itemInHandRenderer, ResourceManager resourceManager, EntityModelSet entityModelSet, Font font) {
            this.entityRenderDispatcher = entityRenderDispatcher;
            this.itemRenderer = itemRenderer;
            this.blockRenderDispatcher = blockRenderDispatcher;
            this.itemInHandRenderer = itemInHandRenderer;
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

        public BlockRenderDispatcher getBlockRenderDispatcher() {
            return this.blockRenderDispatcher;
        }

        public ItemInHandRenderer getItemInHandRenderer() {
            return this.itemInHandRenderer;
        }

        public ResourceManager getResourceManager() {
            return this.resourceManager;
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

