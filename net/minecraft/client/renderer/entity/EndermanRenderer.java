/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

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
    public void render(EnderMan enderMan, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        BlockState blockState = enderMan.getCarriedBlock();
        EndermanModel endermanModel = (EndermanModel)this.getModel();
        endermanModel.carrying = blockState != null;
        endermanModel.creepy = enderMan.isCreepy();
        super.render(enderMan, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public Vec3 getRenderOffset(EnderMan enderMan, double d, double e, double f, float g) {
        if (enderMan.isCreepy()) {
            double h = 0.02;
            return new Vec3(this.random.nextGaussian() * 0.02, 0.0, this.random.nextGaussian() * 0.02);
        }
        return super.getRenderOffset(enderMan, d, e, f, g);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderMan enderMan) {
        return ENDERMAN_LOCATION;
    }
}

