/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
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
    public void render(Fox fox, float f, float g, float h, float i, float j, float k, float l) {
        float m;
        ItemStack itemStack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
        if (itemStack.isEmpty()) {
            return;
        }
        boolean bl = fox.isSleeping();
        boolean bl2 = fox.isBaby();
        RenderSystem.pushMatrix();
        if (bl2) {
            m = 0.75f;
            RenderSystem.scalef(0.75f, 0.75f, 0.75f);
            RenderSystem.translatef(0.0f, 8.0f * l, 3.35f * l);
        }
        RenderSystem.translatef(((FoxModel)this.getParentModel()).head.x / 16.0f, ((FoxModel)this.getParentModel()).head.y / 16.0f, ((FoxModel)this.getParentModel()).head.z / 16.0f);
        m = fox.getHeadRollAngle(h) * 57.295776f;
        RenderSystem.rotatef(m, 0.0f, 0.0f, 1.0f);
        RenderSystem.rotatef(j, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(k, 1.0f, 0.0f, 0.0f);
        if (fox.isBaby()) {
            if (bl) {
                RenderSystem.translatef(0.4f, 0.26f, 0.15f);
            } else {
                RenderSystem.translatef(0.06f, 0.26f, -0.5f);
            }
        } else if (bl) {
            RenderSystem.translatef(0.46f, 0.26f, 0.22f);
        } else {
            RenderSystem.translatef(0.06f, 0.27f, -0.5f);
        }
        RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
        if (bl) {
            RenderSystem.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
        }
        Minecraft.getInstance().getItemRenderer().renderWithMobState(itemStack, fox, ItemTransforms.TransformType.GROUND, false);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

