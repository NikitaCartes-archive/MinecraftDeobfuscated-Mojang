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
public class LlamaSpitModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart main = new ModelPart(this);

    public LlamaSpitModel() {
        this(0.0f);
    }

    public LlamaSpitModel(float f) {
        int i = 2;
        this.main.texOffs(0, 0).addBox(-4.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.texOffs(0, 0).addBox(0.0f, -4.0f, 0.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.texOffs(0, 0).addBox(0.0f, 0.0f, -4.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.texOffs(0, 0).addBox(2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.texOffs(0, 0).addBox(0.0f, 2.0f, 0.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.texOffs(0, 0).addBox(0.0f, 0.0f, 2.0f, 2.0f, 2.0f, 2.0f, f);
        this.main.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.main);
    }
}

