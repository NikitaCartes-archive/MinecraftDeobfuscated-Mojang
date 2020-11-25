/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface BlockEntityRendererProvider<T extends BlockEntity> {
    public BlockEntityRenderer<T> create(Context var1);

    @Environment(value=EnvType.CLIENT)
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

