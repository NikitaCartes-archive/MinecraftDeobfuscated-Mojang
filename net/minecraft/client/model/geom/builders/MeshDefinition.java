/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(value=EnvType.CLIENT)
public class MeshDefinition {
    private PartDefinition root = new PartDefinition(ImmutableList.of(), PartPose.ZERO);

    public PartDefinition getRoot() {
        return this.root;
    }
}

