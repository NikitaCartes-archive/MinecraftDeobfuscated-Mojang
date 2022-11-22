/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.npc;

import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;

public interface VillagerDataHolder
extends VariantHolder<VillagerType> {
    public VillagerData getVillagerData();

    public void setVillagerData(VillagerData var1);

    @Override
    default public VillagerType getVariant() {
        return this.getVillagerData().getType();
    }

    @Override
    default public void setVariant(VillagerType villagerType) {
        this.setVillagerData(this.getVillagerData().setType(villagerType));
    }

    @Override
    default public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }
}

