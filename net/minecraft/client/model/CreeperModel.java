/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class CreeperModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public CreeperModel() {
        this(0.0f);
    }

    public CreeperModel(float f) {
        int i = 6;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, f);
        this.head.setPos(0.0f, 6.0f, 0.0f);
        this.hair = new ModelPart(this, 32, 0);
        this.hair.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, f + 0.5f);
        this.hair.setPos(0.0f, 6.0f, 0.0f);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, f);
        this.body.setPos(0.0f, 6.0f, 0.0f);
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, f);
        this.leg0.setPos(-2.0f, 18.0f, 4.0f);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, f);
        this.leg1.setPos(2.0f, 18.0f, 4.0f);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, f);
        this.leg2.setPos(-2.0f, 18.0f, -4.0f);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, f);
        this.leg3.setPos(2.0f, 18.0f, -4.0f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.leg2, this.leg3);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg2.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg3.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
    }
}

