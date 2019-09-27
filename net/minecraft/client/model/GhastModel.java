/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class GhastModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart[] tentacles = new ModelPart[9];
    private final ImmutableList<ModelPart> parts;

    public GhastModel() {
        ImmutableList.Builder builder = ImmutableList.builder();
        ModelPart modelPart = new ModelPart(this, 0, 0);
        modelPart.addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f);
        modelPart.y = 17.6f;
        builder.add(modelPart);
        Random random = new Random(1660L);
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i] = new ModelPart(this, 0, 0);
            float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5f + 0.25f) / 2.0f * 2.0f - 1.0f) * 5.0f;
            float g = ((float)(i / 3) / 2.0f * 2.0f - 1.0f) * 5.0f;
            int j = random.nextInt(7) + 8;
            this.tentacles[i].addBox(-1.0f, 0.0f, -1.0f, 2.0f, j, 2.0f);
            this.tentacles[i].x = f;
            this.tentacles[i].z = g;
            this.tentacles[i].y = 24.6f;
            builder.add(this.tentacles[i]);
        }
        this.parts = builder.build();
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        for (int l = 0; l < this.tentacles.length; ++l) {
            this.tentacles[l].xRot = 0.2f * Mth.sin(h * 0.3f + (float)l) + 0.4f;
        }
    }

    @Override
    public Iterable<ModelPart> parts() {
        return this.parts;
    }
}

