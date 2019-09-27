/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;

@Environment(value=EnvType.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager>
extends IllagerRenderer<T> {
    private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new IllagerModel(0.0f, 0.0f, 64, 64), 0.5f);
        this.addLayer(new ItemInHandLayer<T, IllagerModel<T>>(this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T spellcasterIllager, float f, float g, float h, float j, float k, float l, float m) {
                if (((SpellcasterIllager)spellcasterIllager).isCastingSpell()) {
                    super.render(poseStack, multiBufferSource, i, spellcasterIllager, f, g, h, j, k, l, m);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(T spellcasterIllager) {
        return EVOKER_ILLAGER;
    }
}

