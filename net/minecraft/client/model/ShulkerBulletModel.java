/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class ShulkerBulletModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart main;

    public ShulkerBulletModel() {
        this.texWidth = 64;
        this.texHeight = 32;
        this.main = new ModelPart(this);
        this.main.texOffs(0, 0).addBox(-4.0f, -4.0f, -1.0f, 8.0f, 8.0f, 2.0f, 0.0f);
        this.main.texOffs(0, 10).addBox(-1.0f, -4.0f, -4.0f, 2.0f, 8.0f, 8.0f, 0.0f);
        this.main.texOffs(20, 0).addBox(-4.0f, -1.0f, -4.0f, 8.0f, 2.0f, 8.0f, 0.0f);
        this.main.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.main);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        this.main.yRot = i * ((float)Math.PI / 180);
        this.main.xRot = j * ((float)Math.PI / 180);
    }
}

