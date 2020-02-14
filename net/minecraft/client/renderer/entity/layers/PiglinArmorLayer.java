/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ArmorItem;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PiglinArmorLayer<T extends Piglin, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
extends HumanoidArmorLayer<T, M, A> {
    private final A headModel;

    public PiglinArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2, A humanoidModel3) {
        super(renderLayerParent, humanoidModel, humanoidModel2);
        this.headModel = humanoidModel3;
    }

    @Override
    public A getArmorModel(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            return this.headModel;
        }
        return super.getArmorModel(equipmentSlot);
    }

    @Override
    protected ResourceLocation getArmorLocation(EquipmentSlot equipmentSlot, ArmorItem armorItem, boolean bl, @Nullable String string) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            String string2 = string == null ? "" : "_" + string;
            String string3 = "textures/models/armor/" + armorItem.getMaterial().getName() + "_piglin_helmet" + string2 + ".png";
            return ARMOR_LOCATION_CACHE.computeIfAbsent(string3, ResourceLocation::new);
        }
        return super.getArmorLocation(equipmentSlot, armorItem, bl, string);
    }
}

