/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public interface BlockEntityRenderer<T extends BlockEntity> {
    public void render(T var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6);

    default public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    default public int getViewDistance() {
        return 64;
    }

    default public boolean shouldRender(T blockEntity, Vec3 vec3) {
        return Vec3.atCenterOf(((BlockEntity)blockEntity).getBlockPos()).closerThan(vec3, this.getViewDistance());
    }
}

