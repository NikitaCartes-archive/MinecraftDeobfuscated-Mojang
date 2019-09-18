package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.AbstractTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.Closeable;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class FontTexture extends AbstractTexture implements Closeable {
	private final ResourceLocation name;
	private final boolean colored;
	private final FontTexture.Node root;

	public FontTexture(ResourceLocation resourceLocation, boolean bl) {
		this.name = resourceLocation;
		this.colored = bl;
		this.root = new FontTexture.Node(0, 0, 256, 256);
		TextureUtil.prepareImage(bl ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.INTENSITY, this.getId(), 256, 256);
	}

	@Override
	public void load(ResourceManager resourceManager) {
	}

	public void close() {
		this.releaseId();
	}

	@Nullable
	public BakedGlyph add(RawGlyph rawGlyph) {
		if (rawGlyph.isColored() != this.colored) {
			return null;
		} else {
			FontTexture.Node node = this.root.insert(rawGlyph);
			if (node != null) {
				this.bind();
				rawGlyph.upload(node.x, node.y);
				float f = 256.0F;
				float g = 256.0F;
				float h = 0.01F;
				return new BakedGlyph(
					this.name,
					((float)node.x + 0.01F) / 256.0F,
					((float)node.x - 0.01F + (float)rawGlyph.getPixelWidth()) / 256.0F,
					((float)node.y + 0.01F) / 256.0F,
					((float)node.y - 0.01F + (float)rawGlyph.getPixelHeight()) / 256.0F,
					rawGlyph.getLeft(),
					rawGlyph.getRight(),
					rawGlyph.getUp(),
					rawGlyph.getDown()
				);
			} else {
				return null;
			}
		}
	}

	public ResourceLocation getName() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	static class Node {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private FontTexture.Node left;
		private FontTexture.Node right;
		private boolean occupied;

		private Node(int i, int j, int k, int l) {
			this.x = i;
			this.y = j;
			this.width = k;
			this.height = l;
		}

		@Nullable
		FontTexture.Node insert(RawGlyph rawGlyph) {
			if (this.left != null && this.right != null) {
				FontTexture.Node node = this.left.insert(rawGlyph);
				if (node == null) {
					node = this.right.insert(rawGlyph);
				}

				return node;
			} else if (this.occupied) {
				return null;
			} else {
				int i = rawGlyph.getPixelWidth();
				int j = rawGlyph.getPixelHeight();
				if (i > this.width || j > this.height) {
					return null;
				} else if (i == this.width && j == this.height) {
					this.occupied = true;
					return this;
				} else {
					int k = this.width - i;
					int l = this.height - j;
					if (k > l) {
						this.left = new FontTexture.Node(this.x, this.y, i, this.height);
						this.right = new FontTexture.Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
					} else {
						this.left = new FontTexture.Node(this.x, this.y, this.width, j);
						this.right = new FontTexture.Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
					}

					return this.left.insert(rawGlyph);
				}
			}
		}
	}
}
