/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ArmedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public ItemInHandLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        ItemStack itemStack2;
        boolean bl = ((LivingEntity)livingEntity).getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemStack = bl ? ((LivingEntity)livingEntity).getOffhandItem() : ((LivingEntity)livingEntity).getMainHandItem();
        ItemStack itemStack3 = itemStack2 = bl ? ((LivingEntity)livingEntity).getMainHandItem() : ((LivingEntity)livingEntity).getOffhandItem();
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return;
        }
        RenderSystem.pushMatrix();
        if (((EntityModel)this.getParentModel()).young) {
            float m = 0.5f;
            RenderSystem.translatef(0.0f, 0.75f, 0.0f);
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
        }
        this.renderArmWithItem((LivingEntity)livingEntity, itemStack2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT);
        this.renderArmWithItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT);
        RenderSystem.popMatrix();
    }

    private void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm humanoidArm) {
        if (itemStack.isEmpty()) {
            return;
        }
        RenderSystem.pushMatrix();
        this.translateToHand(humanoidArm);
        if (livingEntity.isCrouching()) {
            RenderSystem.translatef(0.0f, 0.2f, 0.0f);
        }
        RenderSystem.rotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        RenderSystem.translatef((float)(bl ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, bl);
        RenderSystem.popMatrix();
    }

    protected void translateToHand(HumanoidArm humanoidArm) {
        ((ArmedModel)this.getParentModel()).translateToHand(0.0625f, humanoidArm);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

