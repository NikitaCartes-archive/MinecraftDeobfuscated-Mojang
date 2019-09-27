/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class FoxHeldItemLayer
extends RenderLayer<Fox, FoxModel<Fox>> {
    public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Fox fox, float f, float g, float h, float j, float k, float l, float m) {
        float n;
        boolean bl = fox.isSleeping();
        boolean bl2 = fox.isBaby();
        poseStack.pushPose();
        if (bl2) {
            n = 0.75f;
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.translate(0.0, 8.0f * m, 3.35f * m);
        }
        poseStack.translate(((FoxModel)this.getParentModel()).head.x / 16.0f, ((FoxModel)this.getParentModel()).head.y / 16.0f, ((FoxModel)this.getParentModel()).head.z / 16.0f);
        n = fox.getHeadRollAngle(h);
        poseStack.mulPose(Vector3f.ZP.rotation(n, false));
        poseStack.mulPose(Vector3f.YP.rotation(k, true));
        poseStack.mulPose(Vector3f.XP.rotation(l, true));
        if (fox.isBaby()) {
            if (bl) {
                poseStack.translate(0.4f, 0.26f, 0.15f);
            } else {
                poseStack.translate(0.06f, 0.26f, -0.5);
            }
        } else if (bl) {
            poseStack.translate(0.46f, 0.26f, 0.22f);
        } else {
            poseStack.translate(0.06f, 0.27f, -0.5);
        }
        poseStack.mulPose(Vector3f.XP.rotation(90.0f, true));
        if (bl) {
            poseStack.mulPose(Vector3f.ZP.rotation(90.0f, true));
        }
        ItemStack itemStack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(fox, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource);
        poseStack.popPose();
    }
}

