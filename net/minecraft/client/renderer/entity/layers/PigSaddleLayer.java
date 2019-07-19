/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

@Environment(value=EnvType.CLIENT)
public class PigSaddleLayer
extends RenderLayer<Pig, PigModel<Pig>> {
    private static final ResourceLocation SADDLE_LOCATION = new ResourceLocation("textures/entity/pig/pig_saddle.png");
    private final PigModel<Pig> model = new PigModel(0.5f);

    public PigSaddleLayer(RenderLayerParent<Pig, PigModel<Pig>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(Pig pig, float f, float g, float h, float i, float j, float k, float l) {
        if (!pig.hasSaddle()) {
            return;
        }
        this.bindTexture(SADDLE_LOCATION);
        ((PigModel)this.getParentModel()).copyPropertiesTo(this.model);
        this.model.render(pig, f, g, i, j, k, l);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

