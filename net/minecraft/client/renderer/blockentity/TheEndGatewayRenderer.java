/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(value=EnvType.CLIENT)
public class TheEndGatewayRenderer
extends TheEndPortalRenderer {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

    @Override
    public void render(TheEndPortalBlockEntity theEndPortalBlockEntity, double d, double e, double f, float g, int i) {
        GlStateManager.disableFog();
        TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)theEndPortalBlockEntity;
        if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown()) {
            GlStateManager.alphaFunc(516, 0.1f);
            this.bindTexture(BEAM_LOCATION);
            float h = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(g) : theEndGatewayBlockEntity.getCooldownPercent(g);
            double j = theEndGatewayBlockEntity.isSpawning() ? 256.0 - e : 50.0;
            h = Mth.sin(h * (float)Math.PI);
            int k = Mth.floor((double)h * j);
            float[] fs = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
            BeaconRenderer.renderBeaconBeam(d, e, f, g, h, theEndGatewayBlockEntity.getLevel().getGameTime(), 0, k, fs, 0.15, 0.175);
            BeaconRenderer.renderBeaconBeam(d, e, f, g, h, theEndGatewayBlockEntity.getLevel().getGameTime(), 0, -k, fs, 0.15, 0.175);
        }
        super.render(theEndPortalBlockEntity, d, e, f, g, i);
        GlStateManager.enableFog();
    }

    @Override
    protected int getPasses(double d) {
        return super.getPasses(d) + 1;
    }

    @Override
    protected float getOffset() {
        return 1.0f;
    }
}

