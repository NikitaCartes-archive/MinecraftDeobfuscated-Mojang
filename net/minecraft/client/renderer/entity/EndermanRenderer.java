/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class EndermanRenderer
extends MobRenderer<EnderMan, EndermanModel<EnderMan>> {
    private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
    private final Random random = new Random();

    public EndermanRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new EndermanModel(0.0f), 0.5f);
        this.addLayer(new EnderEyesLayer<EnderMan>(this));
        this.addLayer(new CarriedBlockLayer(this));
    }

    @Override
    public void render(EnderMan enderMan, double d, double e, double f, float g, float h) {
        BlockState blockState = enderMan.getCarriedBlock();
        EndermanModel endermanModel = (EndermanModel)this.getModel();
        endermanModel.carrying = blockState != null;
        endermanModel.creepy = enderMan.isCreepy();
        if (enderMan.isCreepy()) {
            double i = 0.02;
            d += this.random.nextGaussian() * 0.02;
            f += this.random.nextGaussian() * 0.02;
        }
        super.render(enderMan, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(EnderMan enderMan) {
        return ENDERMAN_LOCATION;
    }
}

