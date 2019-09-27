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
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class BlazeModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart[] upperBodyParts;
    private final ModelPart head = new ModelPart(this, 0, 0);
    private final ImmutableList<ModelPart> parts;

    public BlazeModel() {
        this.head.addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        this.upperBodyParts = new ModelPart[12];
        for (int i = 0; i < this.upperBodyParts.length; ++i) {
            this.upperBodyParts[i] = new ModelPart(this, 0, 16);
            this.upperBodyParts[i].addBox(0.0f, 0.0f, 0.0f, 2.0f, 8.0f, 2.0f);
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(this.head);
        builder.addAll(Arrays.asList(this.upperBodyParts));
        this.parts = builder.build();
    }

    @Override
    public Iterable<ModelPart> parts() {
        return this.parts;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        int m;
        float l = h * (float)Math.PI * -0.1f;
        for (m = 0; m < 4; ++m) {
            this.upperBodyParts[m].y = -2.0f + Mth.cos(((float)(m * 2) + h) * 0.25f);
            this.upperBodyParts[m].x = Mth.cos(l) * 9.0f;
            this.upperBodyParts[m].z = Mth.sin(l) * 9.0f;
            l += 1.5707964f;
        }
        l = 0.7853982f + h * (float)Math.PI * 0.03f;
        for (m = 4; m < 8; ++m) {
            this.upperBodyParts[m].y = 2.0f + Mth.cos(((float)(m * 2) + h) * 0.25f);
            this.upperBodyParts[m].x = Mth.cos(l) * 7.0f;
            this.upperBodyParts[m].z = Mth.sin(l) * 7.0f;
            l += 1.5707964f;
        }
        l = 0.47123894f + h * (float)Math.PI * -0.05f;
        for (m = 8; m < 12; ++m) {
            this.upperBodyParts[m].y = 11.0f + Mth.cos(((float)m * 1.5f + h) * 0.5f);
            this.upperBodyParts[m].x = Mth.cos(l) * 5.0f;
            this.upperBodyParts[m].z = Mth.sin(l) * 5.0f;
            l += 1.5707964f;
        }
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
    }
}

