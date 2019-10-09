/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class LeashKnotModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart knot;

    public LeashKnotModel() {
        super(RenderType::entityCutoutNoCull);
        this.texWidth = 32;
        this.texHeight = 32;
        this.knot = new ModelPart(this, 0, 0);
        this.knot.addBox(-3.0f, -6.0f, -3.0f, 6.0f, 8.0f, 6.0f, 0.0f);
        this.knot.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.knot);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.knot.yRot = i * ((float)Math.PI / 180);
        this.knot.xRot = j * ((float)Math.PI / 180);
    }
}

