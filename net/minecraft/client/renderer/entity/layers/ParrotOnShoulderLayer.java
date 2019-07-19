/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

@Environment(value=EnvType.CLIENT)
public class ParrotOnShoulderLayer<T extends Player>
extends RenderLayer<T, PlayerModel<T>> {
    private final ParrotModel model = new ParrotModel();

    public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T player, float f, float g, float h, float i, float j, float k, float l) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.render(player, f, g, h, j, k, l, true);
        this.render(player, f, g, h, j, k, l, false);
        GlStateManager.disableRescaleNormal();
    }

    private void render(T player, float f, float g, float h, float i, float j, float k, boolean bl) {
        CompoundTag compoundTag = bl ? ((Player)player).getShoulderEntityLeft() : ((Player)player).getShoulderEntityRight();
        EntityType.byString(compoundTag.getString("id")).filter(entityType -> entityType == EntityType.PARROT).ifPresent(entityType -> {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(bl ? 0.4f : -0.4f, player.isVisuallySneaking() ? -1.3f : -1.5f, 0.0f);
            this.bindTexture(ParrotRenderer.PARROT_LOCATIONS[compoundTag.getInt("Variant")]);
            this.model.renderOnShoulder(f, g, i, j, k, player.tickCount);
            GlStateManager.popMatrix();
        });
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

