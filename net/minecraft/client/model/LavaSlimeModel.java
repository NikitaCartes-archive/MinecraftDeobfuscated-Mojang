/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(value=EnvType.CLIENT)
public class LavaSlimeModel<T extends Slime>
extends ListModel<T> {
    private final ModelPart[] bodyCubes = new ModelPart[8];
    private final ModelPart insideCube;
    private final ImmutableList<ModelPart> parts;

    public LavaSlimeModel() {
        for (int i = 0; i < this.bodyCubes.length; ++i) {
            int j = 0;
            int k = i;
            if (i == 2) {
                j = 24;
                k = 10;
            } else if (i == 3) {
                j = 24;
                k = 19;
            }
            this.bodyCubes[i] = new ModelPart(this, j, k);
            this.bodyCubes[i].addBox(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f);
        }
        this.insideCube = new ModelPart(this, 0, 16);
        this.insideCube.addBox(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f);
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(this.insideCube);
        builder.addAll(Arrays.asList(this.bodyCubes));
        this.parts = builder.build();
    }

    @Override
    public void setupAnim(T slime, float f, float g, float h, float i, float j) {
    }

    @Override
    public void prepareMobModel(T slime, float f, float g, float h) {
        float i = Mth.lerp(h, ((Slime)slime).oSquish, ((Slime)slime).squish);
        if (i < 0.0f) {
            i = 0.0f;
        }
        for (int j = 0; j < this.bodyCubes.length; ++j) {
            this.bodyCubes[j].y = (float)(-(4 - j)) * i * 1.7f;
        }
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    @Override
    public /* synthetic */ Iterable parts() {
        return this.parts();
    }
}

