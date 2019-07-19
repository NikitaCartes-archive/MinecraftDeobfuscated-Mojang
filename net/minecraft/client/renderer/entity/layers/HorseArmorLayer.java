/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class HorseArmorLayer
extends RenderLayer<Horse, HorseModel<Horse>> {
    private final HorseModel<Horse> model = new HorseModel(0.1f);

    public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(Horse horse, float f, float g, float h, float i, float j, float k, float l) {
        ItemStack itemStack = horse.getArmor();
        if (itemStack.getItem() instanceof HorseArmorItem) {
            HorseArmorItem horseArmorItem = (HorseArmorItem)itemStack.getItem();
            ((HorseModel)this.getParentModel()).copyPropertiesTo(this.model);
            this.model.prepareMobModel(horse, f, g, h);
            this.bindTexture(horseArmorItem.getTexture());
            if (horseArmorItem instanceof DyeableHorseArmorItem) {
                int m = ((DyeableHorseArmorItem)horseArmorItem).getColor(itemStack);
                float n = (float)(m >> 16 & 0xFF) / 255.0f;
                float o = (float)(m >> 8 & 0xFF) / 255.0f;
                float p = (float)(m & 0xFF) / 255.0f;
                GlStateManager.color4f(n, o, p, 1.0f);
                this.model.render(horse, f, g, i, j, k, l);
                return;
            }
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.model.render(horse, f, g, i, j, k, l);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

