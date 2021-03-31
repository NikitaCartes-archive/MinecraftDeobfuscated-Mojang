/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;

@Environment(value=EnvType.CLIENT)
public class TheEndGatewayRenderer
extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

    public TheEndGatewayRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TheEndGatewayBlockEntity theEndGatewayBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown()) {
            float g = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(f) : theEndGatewayBlockEntity.getCooldownPercent(f);
            double d = theEndGatewayBlockEntity.isSpawning() ? (double)theEndGatewayBlockEntity.getLevel().getMaxBuildHeight() : 50.0;
            g = Mth.sin(g * (float)Math.PI);
            int k = Mth.floor((double)g * d);
            float[] fs = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
            long l = theEndGatewayBlockEntity.getLevel().getGameTime();
            BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, g, l, -k, k * 2, fs, 0.15f, 0.175f);
        }
        super.render(theEndGatewayBlockEntity, f, poseStack, multiBufferSource, i, j);
    }

    @Override
    protected float getOffsetUp() {
        return 1.0f;
    }

    @Override
    protected float getOffsetDown() {
        return 0.0f;
    }

    @Override
    protected RenderType renderType() {
        return RenderType.endGateway();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}

