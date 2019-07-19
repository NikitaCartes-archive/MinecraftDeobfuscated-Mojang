/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public class DrownedOuterLayer<T extends Zombie>
extends RenderLayer<T, DrownedModel<T>> {
    private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
    private final DrownedModel<T> model = new DrownedModel(0.25f, 0.0f, 64, 64);

    public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T zombie, float f, float g, float h, float i, float j, float k, float l) {
        if (((Entity)zombie).isInvisible()) {
            return;
        }
        ((DrownedModel)this.getParentModel()).copyPropertiesTo(this.model);
        this.model.prepareMobModel(zombie, f, g, h);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(DROWNED_OUTER_LAYER_LOCATION);
        this.model.render(zombie, f, g, i, j, k, l);
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}

