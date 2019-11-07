package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Font implements AutoCloseable {
	public final int lineHeight = 9;
	public final Random random = new Random();
	private final TextureManager textureManager;
	private final FontSet fonts;
	private boolean bidirectional;

	public Font(TextureManager textureManager, FontSet fontSet) {
		this.textureManager = textureManager;
		this.fonts = fontSet;
	}

	public void reload(List<GlyphProvider> list) {
		this.fonts.reload(list);
	}

	public void close() {
		this.fonts.close();
	}

	public int drawShadow(String string, float f, float g, int i) {
		RenderSystem.enableAlphaTest();
		return this.drawInternal(string, f, g, i, Transformation.identity().getMatrix(), true);
	}

	public int draw(String string, float f, float g, int i) {
		RenderSystem.enableAlphaTest();
		return this.drawInternal(string, f, g, i, Transformation.identity().getMatrix(), false);
	}

	public String bidirectionalShaping(String string) {
		try {
			Bidi bidi = new Bidi(new ArabicShaping(8).shape(string), 127);
			bidi.setReorderingMode(0);
			return bidi.writeReordered(2);
		} catch (ArabicShapingException var3) {
			return string;
		}
	}

	private int drawInternal(String string, float f, float g, int i, Matrix4f matrix4f, boolean bl) {
		if (string == null) {
			return 0;
		} else {
			MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			int j = this.drawInBatch(string, f, g, i, bl, matrix4f, bufferSource, false, 0, 15728880);
			bufferSource.endBatch();
			return j;
		}
	}

	public int drawInBatch(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
		return this.drawInternal(string, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k);
	}

	private int drawInternal(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
		if (this.bidirectional) {
			string = this.bidirectionalShaping(string);
		}

		if ((i & -67108864) == 0) {
			i |= -16777216;
		}

		if (bl) {
			this.renderText(string, f, g, i, true, matrix4f, multiBufferSource, bl2, j, k);
		}

		Matrix4f matrix4f2 = matrix4f.copy();
		matrix4f2.translate(new Vector3f(0.0F, 0.0F, 0.001F));
		f = this.renderText(string, f, g, i, false, matrix4f2, multiBufferSource, bl2, j, k);
		return (int)f + (bl ? 1 : 0);
	}

	private float renderText(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
		float h = bl ? 0.25F : 1.0F;
		float l = (float)(i >> 16 & 0xFF) / 255.0F * h;
		float m = (float)(i >> 8 & 0xFF) / 255.0F * h;
		float n = (float)(i & 0xFF) / 255.0F * h;
		float o = f;
		float p = l;
		float q = m;
		float r = n;
		float s = (float)(i >> 24 & 0xFF) / 255.0F;
		boolean bl3 = false;
		boolean bl4 = false;
		boolean bl5 = false;
		boolean bl6 = false;
		boolean bl7 = false;
		List<BakedGlyph.Effect> list = Lists.<BakedGlyph.Effect>newArrayList();

		for (int t = 0; t < string.length(); t++) {
			char c = string.charAt(t);
			if (c == 167 && t + 1 < string.length()) {
				ChatFormatting chatFormatting = ChatFormatting.getByCode(string.charAt(t + 1));
				if (chatFormatting != null) {
					if (chatFormatting.shouldReset()) {
						bl3 = false;
						bl4 = false;
						bl7 = false;
						bl6 = false;
						bl5 = false;
						p = l;
						q = m;
						r = n;
					}

					if (chatFormatting.getColor() != null) {
						int u = chatFormatting.getColor();
						p = (float)(u >> 16 & 0xFF) / 255.0F * h;
						q = (float)(u >> 8 & 0xFF) / 255.0F * h;
						r = (float)(u & 0xFF) / 255.0F * h;
					} else if (chatFormatting == ChatFormatting.OBFUSCATED) {
						bl3 = true;
					} else if (chatFormatting == ChatFormatting.BOLD) {
						bl4 = true;
					} else if (chatFormatting == ChatFormatting.STRIKETHROUGH) {
						bl7 = true;
					} else if (chatFormatting == ChatFormatting.UNDERLINE) {
						bl6 = true;
					} else if (chatFormatting == ChatFormatting.ITALIC) {
						bl5 = true;
					}
				}

				t++;
			} else {
				GlyphInfo glyphInfo = this.fonts.getGlyphInfo(c);
				BakedGlyph bakedGlyph = bl3 && c != ' ' ? this.fonts.getRandomGlyph(glyphInfo) : this.fonts.getGlyph(c);
				ResourceLocation resourceLocation = bakedGlyph.getTexture();
				if (resourceLocation != null) {
					float v = bl4 ? glyphInfo.getBoldOffset() : 0.0F;
					float w = bl ? glyphInfo.getShadowOffset() : 0.0F;
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bl2 ? RenderType.textSeeThrough(resourceLocation) : RenderType.text(resourceLocation));
					this.renderChar(bakedGlyph, bl4, bl5, v, o + w, g + w, matrix4f, vertexConsumer, p, q, r, s, k);
				}

				float v = glyphInfo.getAdvance(bl4);
				float w = bl ? 1.0F : 0.0F;
				if (bl7) {
					list.add(new BakedGlyph.Effect(o + w - 1.0F, g + w + 4.5F, o + w + v, g + w + 4.5F - 1.0F, -0.01F, p, q, r, s));
				}

				if (bl6) {
					list.add(new BakedGlyph.Effect(o + w - 1.0F, g + w + 9.0F, o + w + v, g + w + 9.0F - 1.0F, -0.01F, p, q, r, s));
				}

				o += v;
			}
		}

		if (j != 0) {
			float x = (float)(j >> 24 & 0xFF) / 255.0F;
			float y = (float)(j >> 16 & 0xFF) / 255.0F;
			float z = (float)(j >> 8 & 0xFF) / 255.0F;
			float aa = (float)(j & 0xFF) / 255.0F;
			list.add(new BakedGlyph.Effect(f - 1.0F, g + 9.0F, o + 1.0F, g - 1.0F, 0.01F, y, z, aa, x));
		}

		if (!list.isEmpty()) {
			BakedGlyph bakedGlyph2 = this.fonts.whiteGlyph();
			ResourceLocation resourceLocation2 = bakedGlyph2.getTexture();
			if (resourceLocation2 != null) {
				VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(bl2 ? RenderType.textSeeThrough(resourceLocation2) : RenderType.text(resourceLocation2));

				for (BakedGlyph.Effect effect : list) {
					bakedGlyph2.renderEffect(effect, matrix4f, vertexConsumer2, k);
				}
			}
		}

		return o;
	}

	private void renderChar(
		BakedGlyph bakedGlyph,
		boolean bl,
		boolean bl2,
		float f,
		float g,
		float h,
		Matrix4f matrix4f,
		VertexConsumer vertexConsumer,
		float i,
		float j,
		float k,
		float l,
		int m
	) {
		bakedGlyph.render(bl2, g, h, matrix4f, vertexConsumer, i, j, k, l, m);
		if (bl) {
			bakedGlyph.render(bl2, g + f, h, matrix4f, vertexConsumer, i, j, k, l, m);
		}
	}

	public int width(String string) {
		if (string == null) {
			return 0;
		} else {
			float f = 0.0F;
			boolean bl = false;

			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (c == 167 && i < string.length() - 1) {
					ChatFormatting chatFormatting = ChatFormatting.getByCode(string.charAt(++i));
					if (chatFormatting == ChatFormatting.BOLD) {
						bl = true;
					} else if (chatFormatting != null && chatFormatting.shouldReset()) {
						bl = false;
					}
				} else {
					f += this.fonts.getGlyphInfo(c).getAdvance(bl);
				}
			}

			return Mth.ceil(f);
		}
	}

	public float charWidth(char c) {
		return c == 167 ? 0.0F : this.fonts.getGlyphInfo(c).getAdvance(false);
	}

	public String substrByWidth(String string, int i) {
		return this.substrByWidth(string, i, false);
	}

	public String substrByWidth(String string, int i, boolean bl) {
		StringBuilder stringBuilder = new StringBuilder();
		float f = 0.0F;
		int j = bl ? string.length() - 1 : 0;
		int k = bl ? -1 : 1;
		boolean bl2 = false;
		boolean bl3 = false;

		for (int l = j; l >= 0 && l < string.length() && f < (float)i; l += k) {
			char c = string.charAt(l);
			if (bl2) {
				bl2 = false;
				ChatFormatting chatFormatting = ChatFormatting.getByCode(c);
				if (chatFormatting == ChatFormatting.BOLD) {
					bl3 = true;
				} else if (chatFormatting != null && chatFormatting.shouldReset()) {
					bl3 = false;
				}
			} else if (c == 167) {
				bl2 = true;
			} else {
				f += this.charWidth(c);
				if (bl3) {
					f++;
				}
			}

			if (f > (float)i) {
				break;
			}

			if (bl) {
				stringBuilder.insert(0, c);
			} else {
				stringBuilder.append(c);
			}
		}

		return stringBuilder.toString();
	}

	private String eraseTrailingNewLines(String string) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}

		return string;
	}

	public void drawWordWrap(String string, int i, int j, int k, int l) {
		string = this.eraseTrailingNewLines(string);
		this.drawWordWrapInternal(string, i, j, k, l);
	}

	private void drawWordWrapInternal(String string, int i, int j, int k, int l) {
		List<String> list = this.split(string, k);
		Matrix4f matrix4f = Transformation.identity().getMatrix();

		for (String string2 : list) {
			float f = (float)i;
			if (this.bidirectional) {
				int m = this.width(this.bidirectionalShaping(string2));
				f += (float)(k - m);
			}

			this.drawInternal(string2, f, (float)j, l, matrix4f, false);
			j += 9;
		}
	}

	public int wordWrapHeight(String string, int i) {
		return 9 * this.split(string, i).size();
	}

	public void setBidirectional(boolean bl) {
		this.bidirectional = bl;
	}

	public List<String> split(String string, int i) {
		return Arrays.asList(this.insertLineBreaks(string, i).split("\n"));
	}

	public String insertLineBreaks(String string, int i) {
		String string2 = "";

		while (!string.isEmpty()) {
			int j = this.indexAtWidth(string, i);
			if (string.length() <= j) {
				return string2 + string;
			}

			String string3 = string.substring(0, j);
			char c = string.charAt(j);
			boolean bl = c == ' ' || c == '\n';
			string = ChatFormatting.getLastColors(string3) + string.substring(j + (bl ? 1 : 0));
			string2 = string2 + string3 + "\n";
		}

		return string2;
	}

	public int indexAtWidth(String string, int i) {
		int j = Math.max(1, i);
		int k = string.length();
		float f = 0.0F;
		int l = 0;
		int m = -1;
		boolean bl = false;

		for (boolean bl2 = true; l < k; l++) {
			char c = string.charAt(l);
			switch (c) {
				case '\n':
					l--;
					break;
				case ' ':
					m = l;
				default:
					if (f != 0.0F) {
						bl2 = false;
					}

					f += this.charWidth(c);
					if (bl) {
						f++;
					}
					break;
				case '§':
					if (l < k - 1) {
						ChatFormatting chatFormatting = ChatFormatting.getByCode(string.charAt(++l));
						if (chatFormatting == ChatFormatting.BOLD) {
							bl = true;
						} else if (chatFormatting != null && chatFormatting.shouldReset()) {
							bl = false;
						}
					}
			}

			if (c == '\n') {
				m = ++l;
				break;
			}

			if (f > (float)j) {
				if (bl2) {
					l++;
				}
				break;
			}
		}

		return l != k && m != -1 && m < l ? m : l;
	}

	public int getWordPosition(String string, int i, int j, boolean bl) {
		int k = j;
		boolean bl2 = i < 0;
		int l = Math.abs(i);

		for (int m = 0; m < l; m++) {
			if (bl2) {
				while (bl && k > 0 && (string.charAt(k - 1) == ' ' || string.charAt(k - 1) == '\n')) {
					k--;
				}

				while (k > 0 && string.charAt(k - 1) != ' ' && string.charAt(k - 1) != '\n') {
					k--;
				}
			} else {
				int n = string.length();
				int o = string.indexOf(32, k);
				int p = string.indexOf(10, k);
				if (o == -1 && p == -1) {
					k = -1;
				} else if (o != -1 && p != -1) {
					k = Math.min(o, p);
				} else if (o != -1) {
					k = o;
				} else {
					k = p;
				}

				if (k == -1) {
					k = n;
				} else {
					while (bl && k < n && (string.charAt(k) == ' ' || string.charAt(k) == '\n')) {
						k++;
					}
				}
			}
		}

		return k;
	}

	public boolean isBidirectional() {
		return this.bidirectional;
	}
}
