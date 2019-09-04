/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class PandaHoldsItemLayer
extends RenderLayer<Panda, PandaModel<Panda>> {
    public PandaHoldsItemLayer(RenderLayerParent<Panda, PandaModel<Panda>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(Panda panda, float f, float g, float h, float i, float j, float k, float l) {
        ItemStack itemStack = panda.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!panda.isSitting() || itemStack.isEmpty() || panda.isScared()) {
            return;
        }
        float m = -0.6f;
        float n = 1.4f;
        if (panda.isEating()) {
            m -= 0.2f * Mth.sin(i * 0.6f) + 0.2f;
            n -= 0.09f * Mth.sin(i * 0.6f);
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.1f, n, m);
        Minecraft.getInstance().getItemRenderer().renderWithMobState(itemStack, panda, ItemTransforms.TransformType.GROUND, false);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

