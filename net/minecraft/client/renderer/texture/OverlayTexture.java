/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OverlayTexture
implements AutoCloseable {
    public static final int NO_OVERLAY = OverlayTexture.pack(0, 10);
    private final DynamicTexture texture = new DynamicTexture(24, 24, false);

    public OverlayTexture() {
        NativeImage nativeImage = this.texture.getPixels();
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                if (i < 8) {
                    nativeImage.setPixelRGBA(j, i, -1308622593);
                    continue;
                }
                if (i < 16) {
                    int k = (int)((1.0f - (float)j / 15.0f * 0.75f) * 255.0f);
                    nativeImage.setPixelRGBA(j, i, k << 24 | 0xFFFFFF);
                    continue;
                }
                nativeImage.setPixelRGBA(j, i, -1291911168);
            }
        }
        RenderSystem.activeTexture(33985);
        this.texture.bind();
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        float f = 0.04347826f;
        RenderSystem.scalef(0.04347826f, 0.04347826f, 0.04347826f);
        RenderSystem.matrixMode(5888);
        this.texture.bind();
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
        RenderSystem.activeTexture(33984);
    }

    @Override
    public void close() {
        this.texture.close();
    }

    public void setupOverlayColor() {
        RenderSystem.setupOverlayColor(this.texture::getId, 24);
    }

    public static int u(float f) {
        return (int)(f * 23.0f);
    }

    public static int v(boolean bl) {
        return OverlayTexture.v(bl, null);
    }

    public static int v(boolean bl, @Nullable DamageSource damageSource) {
        if (bl) {
            return damageSource == DamageSource.FREEZE ? 19 : 3;
        }
        return 10;
    }

    public static int pack(int i, int j) {
        return i | j << 16;
    }

    public static int pack(float f, boolean bl) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(bl));
    }

    public void teardownOverlayColor() {
        RenderSystem.teardownOverlayColor();
    }
}

