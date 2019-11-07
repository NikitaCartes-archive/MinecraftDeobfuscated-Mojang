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
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SquidModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ImmutableList<ModelPart> parts;

    public SquidModel() {
        int i = -16;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-6.0f, -8.0f, -6.0f, 12.0f, 16.0f, 12.0f);
        this.body.y += 8.0f;
        for (int j = 0; j < this.tentacles.length; ++j) {
            this.tentacles[j] = new ModelPart(this, 48, 0);
            double d = (double)j * Math.PI * 2.0 / (double)this.tentacles.length;
            float f = (float)Math.cos(d) * 5.0f;
            float g = (float)Math.sin(d) * 5.0f;
            this.tentacles[j].addBox(-1.0f, 0.0f, -1.0f, 2.0f, 18.0f, 2.0f);
            this.tentacles[j].x = f;
            this.tentacles[j].z = g;
            this.tentacles[j].y = 15.0f;
            d = (double)j * Math.PI * -2.0 / (double)this.tentacles.length + 1.5707963267948966;
            this.tentacles[j].yRot = (float)d;
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(this.body);
        builder.addAll(Arrays.asList(this.tentacles));
        this.parts = builder.build();
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        for (ModelPart modelPart : this.tentacles) {
            modelPart.xRot = h;
        }
    }

    @Override
    public Iterable<ModelPart> parts() {
        return this.parts;
    }
}

