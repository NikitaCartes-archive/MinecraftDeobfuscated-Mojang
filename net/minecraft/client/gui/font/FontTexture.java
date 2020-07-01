/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FontTexture
extends AbstractTexture {
    private final ResourceLocation name;
    private final RenderType normalType;
    private final RenderType seeThroughType;
    private final boolean colored;
    private final Node root;

    public FontTexture(ResourceLocation resourceLocation, boolean bl) {
        this.name = resourceLocation;
        this.colored = bl;
        this.root = new Node(0, 0, 256, 256);
        TextureUtil.prepareImage(bl ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.INTENSITY, this.getId(), 256, 256);
        this.normalType = RenderType.text(resourceLocation);
        this.seeThroughType = RenderType.textSeeThrough(resourceLocation);
    }

    @Override
    public void load(ResourceManager resourceManager) {
    }

    @Override
    public void close() {
        this.releaseId();
    }

    @Nullable
    public BakedGlyph add(RawGlyph rawGlyph) {
        if (rawGlyph.isColored() != this.colored) {
            return null;
        }
        Node node = this.root.insert(rawGlyph);
        if (node != null) {
            this.bind();
            rawGlyph.upload(node.x, node.y);
            float f = 256.0f;
            float g = 256.0f;
            float h = 0.01f;
            return new BakedGlyph(this.normalType, this.seeThroughType, ((float)node.x + 0.01f) / 256.0f, ((float)node.x - 0.01f + (float)rawGlyph.getPixelWidth()) / 256.0f, ((float)node.y + 0.01f) / 256.0f, ((float)node.y - 0.01f + (float)rawGlyph.getPixelHeight()) / 256.0f, rawGlyph.getLeft(), rawGlyph.getRight(), rawGlyph.getUp(), rawGlyph.getDown());
        }
        return null;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    @Environment(value=EnvType.CLIENT)
    static class Node {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private Node left;
        private Node right;
        private boolean occupied;

        private Node(int i, int j, int k, int l) {
            this.x = i;
            this.y = j;
            this.width = k;
            this.height = l;
        }

        @Nullable
        Node insert(RawGlyph rawGlyph) {
            if (this.left != null && this.right != null) {
                Node node = this.left.insert(rawGlyph);
                if (node == null) {
                    node = this.right.insert(rawGlyph);
                }
                return node;
            }
            if (this.occupied) {
                return null;
            }
            int i = rawGlyph.getPixelWidth();
            int j = rawGlyph.getPixelHeight();
            if (i > this.width || j > this.height) {
                return null;
            }
            if (i == this.width && j == this.height) {
                this.occupied = true;
                return this;
            }
            int k = this.width - i;
            int l = this.height - j;
            if (k > l) {
                this.left = new Node(this.x, this.y, i, this.height);
                this.right = new Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
            } else {
                this.left = new Node(this.x, this.y, this.width, j);
                this.right = new Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
            }
            return this.left.insert(rawGlyph);
        }
    }
}

