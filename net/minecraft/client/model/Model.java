/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class Model {
    public final List<ModelPart> cubes = Lists.newArrayList();
    public int texWidth = 64;
    public int texHeight = 32;

    public ModelPart getRandomModelPart(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }
}

