/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;

@Environment(value=EnvType.CLIENT)
public class OverlayTexture
implements AutoCloseable {
    private static final int SIZE = 16;
    public static final int NO_WHITE_U = 0;
    public static final int RED_OVERLAY_V = 3;
    public static final int WHITE_OVERLAY_V = 10;
    public static final int NO_OVERLAY = OverlayTexture.pack(0, 10);
    private final DynamicTexture texture = new DynamicTexture(16, 16, false);

    public OverlayTexture() {
        NativeImage nativeImage = this.texture.getPixels();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (i < 8) {
                    nativeImage.setPixelRGBA(j, i, -1308622593);
                    continue;
                }
                int k = (int)((1.0f - (float)j / 15.0f * 0.75f) * 255.0f);
                nativeImage.setPixelRGBA(j, i, k << 24 | 0xFFFFFF);
            }
        }
        RenderSystem.activeTexture(33985);
        this.texture.bind();
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
        RenderSystem.activeTexture(33984);
    }

    @Override
    public void close() {
        this.texture.close();
    }

    public void setupOverlayColor() {
        RenderSystem.setupOverlayColor(this.texture::getId, 16);
    }

    public static int u(float f) {
        return (int)(f * 15.0f);
    }

    public static int v(boolean bl) {
        return bl ? 3 : 10;
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

