package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class WinScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/minecraft.png");
	private static final ResourceLocation EDITION_LOCATION = new ResourceLocation("textures/gui/title/edition.png");
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
	private final boolean poem;
	private final Runnable onFinished;
	private float time;
	private List<FormattedCharSequence> lines;
	private IntSet centeredLines;
	private int totalScrollLength;
	private float scrollSpeed = 0.5F;

	public WinScreen(boolean bl, Runnable runnable) {
		super(NarratorChatListener.NO_TITLE);
		this.poem = bl;
		this.onFinished = runnable;
		if (!bl) {
			this.scrollSpeed = 0.75F;
		}
	}

	@Override
	public void tick() {
		this.minecraft.getMusicManager().tick();
		this.minecraft.getSoundManager().tick(false);
		float f = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
		if (this.time > f) {
			this.respawn();
		}
	}

	@Override
	public void onClose() {
		this.respawn();
	}

	private void respawn() {
		this.onFinished.run();
		this.minecraft.setScreen(null);
	}

	@Override
	protected void init() {
		if (this.lines == null) {
			this.lines = Lists.<FormattedCharSequence>newArrayList();
			this.centeredLines = new IntOpenHashSet();
			Resource resource = null;

			try {
				int i = 274;
				if (this.poem) {
					resource = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
					InputStream inputStream = resource.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
					Random random = new Random(8124371L);

					String string;
					while ((string = bufferedReader.readLine()) != null) {
						string = string.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());

						int j;
						while ((j = string.indexOf(OBFUSCATE_TOKEN)) != -1) {
							String string2 = string.substring(0, j);
							String string3 = string.substring(j + OBFUSCATE_TOKEN.length());
							string = string2 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + string3;
						}

						this.lines.addAll(this.minecraft.font.split(new TextComponent(string), 274));
						this.lines.add(FormattedCharSequence.EMPTY);
					}

					inputStream.close();

					for (int j = 0; j < 8; j++) {
						this.lines.add(FormattedCharSequence.EMPTY);
					}
				}

				InputStream inputStream = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

				String string4;
				while ((string4 = bufferedReader.readLine()) != null) {
					string4 = string4.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
					string4 = string4.replaceAll("\t", "    ");
					boolean bl;
					if (string4.startsWith("[C]")) {
						string4 = string4.substring(3);
						bl = true;
					} else {
						bl = false;
					}

					for (FormattedCharSequence formattedCharSequence : this.minecraft.font.split(new TextComponent(string4), 274)) {
						if (bl) {
							this.centeredLines.add(this.lines.size());
						}

						this.lines.add(formattedCharSequence);
					}

					this.lines.add(FormattedCharSequence.EMPTY);
				}

				inputStream.close();
				this.totalScrollLength = this.lines.size() * 12;
			} catch (Exception var13) {
				LOGGER.error("Couldn't load credits", (Throwable)var13);
			} finally {
				IOUtils.closeQuietly(resource);
			}
		}
	}

	private void renderBg(int i, int j, float f) {
		this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
		int k = this.width;
		float g = -this.time * 0.5F * this.scrollSpeed;
		float h = (float)this.height - this.time * 0.5F * this.scrollSpeed;
		float l = 0.015625F;
		float m = this.time * 0.02F;
		float n = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
		float o = (n - 20.0F - this.time) * 0.005F;
		if (o < m) {
			m = o;
		}

		if (m > 1.0F) {
			m = 1.0F;
		}

		m *= m;
		m = m * 96.0F / 255.0F;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)this.height, (double)this.getBlitOffset()).uv(0.0F, g * 0.015625F).color(m, m, m, 1.0F).endVertex();
		bufferBuilder.vertex((double)k, (double)this.height, (double)this.getBlitOffset()).uv((float)k * 0.015625F, g * 0.015625F).color(m, m, m, 1.0F).endVertex();
		bufferBuilder.vertex((double)k, 0.0, (double)this.getBlitOffset()).uv((float)k * 0.015625F, h * 0.015625F).color(m, m, m, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, h * 0.015625F).color(m, m, m, 1.0F).endVertex();
		tesselator.end();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBg(i, j, f);
		int k = 274;
		int l = this.width / 2 - 137;
		int m = this.height + 50;
		this.time += f;
		float g = -this.time * this.scrollSpeed;
		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.0F, g, 0.0F);
		this.minecraft.getTextureManager().bind(LOGO_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		this.blitOutlineBlack(l, m, (integer, integer2) -> {
			this.blit(poseStack, integer + 0, integer2, 0, 0, 155, 44);
			this.blit(poseStack, integer + 155, integer2, 0, 45, 155, 44);
		});
		RenderSystem.disableBlend();
		this.minecraft.getTextureManager().bind(EDITION_LOCATION);
		blit(poseStack, l + 88, m + 37, 0.0F, 0.0F, 98, 14, 128, 16);
		RenderSystem.disableAlphaTest();
		int n = m + 100;

		for (int o = 0; o < this.lines.size(); o++) {
			if (o == this.lines.size() - 1) {
				float h = (float)n + g - (float)(this.height / 2 - 6);
				if (h < 0.0F) {
					RenderSystem.translatef(0.0F, -h, 0.0F);
				}
			}

			if ((float)n + g + 12.0F + 8.0F > 0.0F && (float)n + g < (float)this.height) {
				FormattedCharSequence formattedCharSequence = (FormattedCharSequence)this.lines.get(o);
				if (this.centeredLines.contains(o)) {
					this.font.drawShadow(poseStack, formattedCharSequence, (float)(l + (274 - this.font.width(formattedCharSequence)) / 2), (float)n, 16777215);
				} else {
					this.font.random.setSeed((long)((float)((long)o * 4238972211L) + this.time / 4.0F));
					this.font.drawShadow(poseStack, formattedCharSequence, (float)l, (float)n, 16777215);
				}
			}

			n += 12;
		}

		RenderSystem.popMatrix();
		this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
		int o = this.width;
		int p = this.height;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)p, (double)this.getBlitOffset()).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)o, (double)p, (double)this.getBlitOffset()).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)o, 0.0, (double)this.getBlitOffset()).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
		super.render(poseStack, i, j, f);
	}
}
