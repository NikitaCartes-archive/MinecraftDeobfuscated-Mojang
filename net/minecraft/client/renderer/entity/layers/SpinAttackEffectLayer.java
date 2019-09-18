/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class SpinAttackEffectLayer<T extends LivingEntity>
extends RenderLayer<T, PlayerModel<T>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
    private final SpinAttackModel model = new SpinAttackModel();

    public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        if (!((LivingEntity)livingEntity).isAutoSpinAttack()) {
            return;
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(TEXTURE);
        for (int m = 0; m < 3; ++m) {
            RenderSystem.pushMatrix();
            RenderSystem.rotatef(i * (float)(-(45 + m * 5)), 0.0f, 1.0f, 0.0f);
            float n = 0.75f * (float)m;
            RenderSystem.scalef(n, n, n);
            RenderSystem.translatef(0.0f, -0.2f + 0.6f * (float)m, 0.0f);
            this.model.render(f, g, i, j, k, l);
            RenderSystem.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    static class SpinAttackModel
    extends Model {
        private final ModelPart box;

        public SpinAttackModel() {
            this.texWidth = 64;
            this.texHeight = 64;
            this.box = new ModelPart(this, 0, 0);
            this.box.addBox(-8.0f, -16.0f, -8.0f, 16.0f, 32.0f, 16.0f);
        }

        public void render(float f, float g, float h, float i, float j, float k) {
            this.box.render(k);
        }
    }
}

