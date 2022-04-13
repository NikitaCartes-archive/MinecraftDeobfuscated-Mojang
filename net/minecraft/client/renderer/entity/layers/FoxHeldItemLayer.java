/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
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
    private final ItemInHandRenderer itemInHandRenderer;

    public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
        super(renderLayerParent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Fox fox, float f, float g, float h, float j, float k, float l) {
        float m;
        boolean bl = fox.isSleeping();
        boolean bl2 = fox.isBaby();
        poseStack.pushPose();
        if (bl2) {
            m = 0.75f;
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.translate(0.0, 0.5, 0.209375f);
        }
        poseStack.translate(((FoxModel)this.getParentModel()).head.x / 16.0f, ((FoxModel)this.getParentModel()).head.y / 16.0f, ((FoxModel)this.getParentModel()).head.z / 16.0f);
        m = fox.getHeadRollAngle(h);
        poseStack.mulPose(Vector3f.ZP.rotation(m));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(l));
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
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        if (bl) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
        }
        ItemStack itemStack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
        this.itemInHandRenderer.renderItem(fox, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
        poseStack.popPose();
    }
}

