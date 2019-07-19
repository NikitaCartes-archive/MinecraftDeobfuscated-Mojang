/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.AbstractArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
extends AbstractArmorLayer<T, M, A> {
    public HumanoidArmorLayer(RenderLayerParent<T, M> renderLayerParent, A humanoidModel, A humanoidModel2) {
        super(renderLayerParent, humanoidModel, humanoidModel2);
    }

    @Override
    protected void setPartVisibility(A humanoidModel, EquipmentSlot equipmentSlot) {
        this.hideAllArmor(humanoidModel);
        switch (equipmentSlot) {
            case HEAD: {
                ((HumanoidModel)humanoidModel).head.visible = true;
                ((HumanoidModel)humanoidModel).hat.visible = true;
                break;
            }
            case CHEST: {
                ((HumanoidModel)humanoidModel).body.visible = true;
                ((HumanoidModel)humanoidModel).rightArm.visible = true;
                ((HumanoidModel)humanoidModel).leftArm.visible = true;
                break;
            }
            case LEGS: {
                ((HumanoidModel)humanoidModel).body.visible = true;
                ((HumanoidModel)humanoidModel).rightLeg.visible = true;
                ((HumanoidModel)humanoidModel).leftLeg.visible = true;
                break;
            }
            case FEET: {
                ((HumanoidModel)humanoidModel).rightLeg.visible = true;
                ((HumanoidModel)humanoidModel).leftLeg.visible = true;
            }
        }
    }

    @Override
    protected void hideAllArmor(A humanoidModel) {
        ((HumanoidModel)humanoidModel).setAllVisible(false);
    }
}

