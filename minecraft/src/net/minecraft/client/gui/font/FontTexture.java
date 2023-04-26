package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class FontTexture extends AbstractTexture implements Dumpable {
	private static final int SIZE = 256;
	private final GlyphRenderTypes renderTypes;
	private final boolean colored;
	private final FontTexture.Node root;

	public FontTexture(GlyphRenderTypes glyphRenderTypes, boolean bl) {
		this.colored = bl;
		this.root = new FontTexture.Node(0, 0, 256, 256);
		TextureUtil.prepareImage(bl ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.RED, this.getId(), 256, 256);
		this.renderTypes = glyphRenderTypes;
	}

	@Override
	public void load(ResourceManager resourceManager) {
	}

	@Override
	public void close() {
		this.releaseId();
	}

	@Nullable
	public BakedGlyph add(SheetGlyphInfo sheetGlyphInfo) {
		if (sheetGlyphInfo.isColored() != this.colored) {
			return null;
		} else {
			FontTexture.Node node = this.root.insert(sheetGlyphInfo);
			if (node != null) {
				this.bind();
				sheetGlyphInfo.upload(node.x, node.y);
				float f = 256.0F;
				float g = 256.0F;
				float h = 0.01F;
				return new BakedGlyph(
					this.renderTypes,
					((float)node.x + 0.01F) / 256.0F,
					((float)node.x - 0.01F + (float)sheetGlyphInfo.getPixelWidth()) / 256.0F,
					((float)node.y + 0.01F) / 256.0F,
					((float)node.y - 0.01F + (float)sheetGlyphInfo.getPixelHeight()) / 256.0F,
					sheetGlyphInfo.getLeft(),
					sheetGlyphInfo.getRight(),
					sheetGlyphInfo.getUp(),
					sheetGlyphInfo.getDown()
				);
			} else {
				return null;
			}
		}
	}

	@Override
	public void dumpContents(ResourceLocation resourceLocation, Path path) {
		String string = resourceLocation.toDebugFileName();
		TextureUtil.writeAsPNG(path, string, this.getId(), 0, 256, 256, i -> (i & 0xFF000000) == 0 ? -16777216 : i);
	}

	@Environment(EnvType.CLIENT)
	static class Node {
		final int x;
		final int y;
		private final int width;
		private final int height;
		@Nullable
		private FontTexture.Node left;
		@Nullable
		private FontTexture.Node right;
		private boolean occupied;

		Node(int i, int j, int k, int l) {
			this.x = i;
			this.y = j;
			this.width = k;
			this.height = l;
		}

		@Nullable
		FontTexture.Node insert(SheetGlyphInfo sheetGlyphInfo) {
			if (this.left != null && this.right != null) {
				FontTexture.Node node = this.left.insert(sheetGlyphInfo);
				if (node == null) {
					node = this.right.insert(sheetGlyphInfo);
				}

				return node;
			} else if (this.occupied) {
				return null;
			} else {
				int i = sheetGlyphInfo.getPixelWidth();
				int j = sheetGlyphInfo.getPixelHeight();
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

					return this.left.insert(sheetGlyphInfo);
				}
			}
		}
	}
}
