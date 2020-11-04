/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class CatRenderer
extends MobRenderer<Cat, CatModel<Cat>> {
    public CatRenderer(EntityRendererProvider.Context context) {
        super(context, new CatModel(context.getLayer(ModelLayers.CAT)), 0.4f);
        this.addLayer(new CatCollarLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Cat cat) {
        return cat.getResourceLocation();
    }

    @Override
    protected void scale(Cat cat, PoseStack poseStack, float f) {
        super.scale(cat, poseStack, f);
        poseStack.scale(0.8f, 0.8f, 0.8f);
    }

    @Override
    protected void setupRotations(Cat cat, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(cat, poseStack, f, g, h);
        float i = cat.getLieDownAmount(h);
        if (i > 0.0f) {
            poseStack.translate(0.4f * i, 0.15f * i, 0.1f * i);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(i, 0.0f, 90.0f)));
            BlockPos blockPos = cat.blockPosition();
            List<Player> list = cat.level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(2.0, 2.0, 2.0));
            for (Player player : list) {
                if (!player.isSleeping()) continue;
                poseStack.translate(0.15f * i, 0.0, 0.0);
                break;
            }
        }
    }
}

