package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
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
		GlStateManager.enableAlphaTest();
		return this.drawInternal(string, f, g, i, true);
	}

	public int draw(String string, float f, float g, int i) {
		GlStateManager.enableAlphaTest();
		return this.drawInternal(string, f, g, i, false);
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

	private int drawInternal(String string, float f, float g, int i, boolean bl) {
		if (string == null) {
			return 0;
		} else {
			if (this.bidirectional) {
				string = this.bidirectionalShaping(string);
			}

			if ((i & -67108864) == 0) {
				i |= -16777216;
			}

			if (bl) {
				this.renderText(string, f, g, i, true);
			}

			f = this.renderText(string, f, g, i, false);
			return (int)f + (bl ? 1 : 0);
		}
	}

	private float renderText(String string, float f, float g, int i, boolean bl) {
		float h = bl ? 0.25F : 1.0F;
		float j = (float)(i >> 16 & 0xFF) / 255.0F * h;
		float k = (float)(i >> 8 & 0xFF) / 255.0F * h;
		float l = (float)(i & 0xFF) / 255.0F * h;
		float m = j;
		float n = k;
		float o = l;
		float p = (float)(i >> 24 & 0xFF) / 255.0F;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		ResourceLocation resourceLocation = null;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		boolean bl2 = false;
		boolean bl3 = false;
		boolean bl4 = false;
		boolean bl5 = false;
		boolean bl6 = false;
		List<Font.Effect> list = Lists.<Font.Effect>newArrayList();

		for (int q = 0; q < string.length(); q++) {
			char c = string.charAt(q);
			if (c == 167 && q + 1 < string.length()) {
				ChatFormatting chatFormatting = ChatFormatting.getByCode(string.charAt(q + 1));
				if (chatFormatting != null) {
					if (chatFormatting.shouldReset()) {
						bl2 = false;
						bl3 = false;
						bl6 = false;
						bl5 = false;
						bl4 = false;
						m = j;
						n = k;
						o = l;
					}

					if (chatFormatting.getColor() != null) {
						int r = chatFormatting.getColor();
						m = (float)(r >> 16 & 0xFF) / 255.0F * h;
						n = (float)(r >> 8 & 0xFF) / 255.0F * h;
						o = (float)(r & 0xFF) / 255.0F * h;
					} else if (chatFormatting == ChatFormatting.OBFUSCATED) {
						bl2 = true;
					} else if (chatFormatting == ChatFormatting.BOLD) {
						bl3 = true;
					} else if (chatFormatting == ChatFormatting.STRIKETHROUGH) {
						bl6 = true;
					} else if (chatFormatting == ChatFormatting.UNDERLINE) {
						bl5 = true;
					} else if (chatFormatting == ChatFormatting.ITALIC) {
						bl4 = true;
					}
				}

				q++;
			} else {
				GlyphInfo glyphInfo = this.fonts.getGlyphInfo(c);
				BakedGlyph bakedGlyph = bl2 && c != ' ' ? this.fonts.getRandomGlyph(glyphInfo) : this.fonts.getGlyph(c);
				ResourceLocation resourceLocation2 = bakedGlyph.getTexture();
				if (resourceLocation2 != null) {
					if (resourceLocation != resourceLocation2) {
						tesselator.end();
						this.textureManager.bind(resourceLocation2);
						bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
						resourceLocation = resourceLocation2;
					}

					float s = bl3 ? glyphInfo.getBoldOffset() : 0.0F;
					float t = bl ? glyphInfo.getShadowOffset() : 0.0F;
					this.renderChar(bakedGlyph, bl3, bl4, s, f + t, g + t, bufferBuilder, m, n, o, p);
				}

				float s = glyphInfo.getAdvance(bl3);
				float t = bl ? 1.0F : 0.0F;
				if (bl6) {
					list.add(new Font.Effect(f + t - 1.0F, g + t + 4.5F, f + t + s, g + t + 4.5F - 1.0F, m, n, o, p));
				}

				if (bl5) {
					list.add(new Font.Effect(f + t - 1.0F, g + t + 9.0F, f + t + s, g + t + 9.0F - 1.0F, m, n, o, p));
				}

				f += s;
			}
		}

		tesselator.end();
		if (!list.isEmpty()) {
			GlStateManager.disableTexture();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);

			for (Font.Effect effect : list) {
				effect.render(bufferBuilder);
			}

			tesselator.end();
			GlStateManager.enableTexture();
		}

		return f;
	}

	private void renderChar(
		BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h, BufferBuilder bufferBuilder, float i, float j, float k, float l
	) {
		bakedGlyph.render(this.textureManager, bl2, g, h, bufferBuilder, i, j, k, l);
		if (bl) {
			bakedGlyph.render(this.textureManager, bl2, g + f, h, bufferBuilder, i, j, k, l);
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
		for (String string2 : this.split(string, k)) {
			float f = (float)i;
			if (this.bidirectional) {
				int m = this.width(this.bidirectionalShaping(string2));
				f += (float)(k - m);
			}

			this.drawInternal(string2, f, (float)j, l, false);
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

	@Environment(EnvType.CLIENT)
	static class Effect {
		protected final float x0;
		protected final float y0;
		protected final float x1;
		protected final float y1;
		protected final float r;
		protected final float g;
		protected final float b;
		protected final float a;

		private Effect(float f, float g, float h, float i, float j, float k, float l, float m) {
			this.x0 = f;
			this.y0 = g;
			this.x1 = h;
			this.y1 = i;
			this.r = j;
			this.g = k;
			this.b = l;
			this.a = m;
		}

		public void render(BufferBuilder bufferBuilder) {
			bufferBuilder.vertex((double)this.x0, (double)this.y0, 0.0).color(this.r, this.g, this.b, this.a).endVertex();
			bufferBuilder.vertex((double)this.x1, (double)this.y0, 0.0).color(this.r, this.g, this.b, this.a).endVertex();
			bufferBuilder.vertex((double)this.x1, (double)this.y1, 0.0).color(this.r, this.g, this.b, this.a).endVertex();
			bufferBuilder.vertex((double)this.x0, (double)this.y1, 0.0).color(this.r, this.g, this.b, this.a).endVertex();
		}
	}
}
