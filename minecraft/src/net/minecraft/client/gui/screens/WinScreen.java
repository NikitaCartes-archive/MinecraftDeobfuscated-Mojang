package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class WinScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/minecraft.png");
	private static final ResourceLocation EDITION_LOCATION = new ResourceLocation("textures/gui/title/edition.png");
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static final Component SECTION_HEADING = new TextComponent("============").withStyle(ChatFormatting.WHITE);
	private static final String NAME_PREFIX = "           ";
	private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
	private static final int LOGO_WIDTH = 274;
	private static final float SPEEDUP_FACTOR = 5.0F;
	private static final float SPEEDUP_FACTOR_FAST = 15.0F;
	private final boolean poem;
	private final Runnable onFinished;
	private float scroll;
	private List<FormattedCharSequence> lines;
	private IntSet centeredLines;
	private int totalScrollLength;
	private boolean speedupActive;
	private final IntSet speedupModifiers = new IntOpenHashSet();
	private float scrollSpeed;
	private final float unmodifiedScrollSpeed;

	public WinScreen(boolean bl, Runnable runnable) {
		super(NarratorChatListener.NO_TITLE);
		this.poem = bl;
		this.onFinished = runnable;
		if (!bl) {
			this.unmodifiedScrollSpeed = 0.75F;
		} else {
			this.unmodifiedScrollSpeed = 0.5F;
		}

		this.scrollSpeed = this.unmodifiedScrollSpeed;
	}

	private float calculateScrollSpeed() {
		return this.speedupActive ? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) : this.unmodifiedScrollSpeed;
	}

	@Override
	public void tick() {
		this.minecraft.getMusicManager().tick();
		this.minecraft.getSoundManager().tick(false);
		float f = (float)(this.totalScrollLength + this.height + this.height + 24);
		if (this.scroll > f) {
			this.respawn();
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 341 || i == 345) {
			this.speedupModifiers.add(i);
		} else if (i == 32) {
			this.speedupActive = true;
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyPressed(i, j, k);
	}

	@Override
	public boolean keyReleased(int i, int j, int k) {
		if (i == 32) {
			this.speedupActive = false;
		} else if (i == 341 || i == 345) {
			this.speedupModifiers.remove(i);
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyReleased(i, j, k);
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
				if (this.poem) {
					resource = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
					InputStream inputStream = resource.getInputStream();
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
					Random random = new Random(8124371L);

					String string;
					while ((string = bufferedReader.readLine()) != null) {
						string = string.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());

						int i;
						while ((i = string.indexOf(OBFUSCATE_TOKEN)) != -1) {
							String string2 = string.substring(0, i);
							String string3 = string.substring(i + OBFUSCATE_TOKEN.length());
							string = string2 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + string3;
						}

						this.addPoemLines(string);
						this.addEmptyLine();
					}

					inputStream.close();

					for (int i = 0; i < 8; i++) {
						this.addEmptyLine();
					}
				}

				resource = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/credits.json"));
				JsonArray jsonArray = GsonHelper.parseArray(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

				for (JsonElement jsonElement : jsonArray.getAsJsonArray()) {
					JsonObject jsonObject = jsonElement.getAsJsonObject();
					String string2 = jsonObject.get("section").getAsString();
					this.addCreditsLine(SECTION_HEADING, true);
					this.addCreditsLine(new TextComponent(string2).withStyle(ChatFormatting.YELLOW), true);
					this.addCreditsLine(SECTION_HEADING, true);
					this.addEmptyLine();
					this.addEmptyLine();

					for (JsonElement jsonElement2 : jsonObject.getAsJsonArray("titles")) {
						JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
						String string4 = jsonObject2.get("title").getAsString();
						JsonArray jsonArray4 = jsonObject2.getAsJsonArray("names");
						this.addCreditsLine(new TextComponent(string4).withStyle(ChatFormatting.GRAY), false);

						for (JsonElement jsonElement3 : jsonArray4) {
							String string5 = jsonElement3.getAsString();
							this.addCreditsLine(new TextComponent("           ").append(string5).withStyle(ChatFormatting.WHITE), false);
						}

						this.addEmptyLine();
						this.addEmptyLine();
					}
				}

				this.totalScrollLength = this.lines.size() * 12;
			} catch (Exception var20) {
				LOGGER.error("Couldn't load credits", (Throwable)var20);
			} finally {
				IOUtils.closeQuietly(resource);
			}
		}
	}

	private void addEmptyLine() {
		this.lines.add(FormattedCharSequence.EMPTY);
	}

	private void addPoemLines(String string) {
		this.lines.addAll(this.minecraft.font.split(new TextComponent(string), 274));
	}

	private void addCreditsLine(Component component, boolean bl) {
		if (bl) {
			this.centeredLines.add(this.lines.size());
		}

		this.lines.add(component.getVisualOrderText());
	}

	private void renderBg() {
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
		int i = this.width;
		float f = -this.scroll * 0.5F;
		float g = (float)this.height - 0.5F * this.scroll;
		float h = 0.015625F;
		float j = this.scroll / this.unmodifiedScrollSpeed;
		float k = j * 0.02F;
		float l = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
		float m = (l - 20.0F - j) * 0.005F;
		if (m < k) {
			k = m;
		}

		if (k > 1.0F) {
			k = 1.0F;
		}

		k *= k;
		k = k * 96.0F / 255.0F;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)this.height, (double)this.getBlitOffset()).uv(0.0F, f * 0.015625F).color(k, k, k, 1.0F).endVertex();
		bufferBuilder.vertex((double)i, (double)this.height, (double)this.getBlitOffset()).uv((float)i * 0.015625F, f * 0.015625F).color(k, k, k, 1.0F).endVertex();
		bufferBuilder.vertex((double)i, 0.0, (double)this.getBlitOffset()).uv((float)i * 0.015625F, g * 0.015625F).color(k, k, k, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, g * 0.015625F).color(k, k, k, 1.0F).endVertex();
		tesselator.end();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.scroll = this.scroll + f * this.scrollSpeed;
		this.renderBg();
		int k = this.width / 2 - 137;
		int l = this.height + 50;
		float g = -this.scroll;
		poseStack.pushPose();
		poseStack.translate(0.0, (double)g, 0.0);
		RenderSystem.setShaderTexture(0, LOGO_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		this.blitOutlineBlack(k, l, (integer, integer2) -> {
			this.blit(poseStack, integer + 0, integer2, 0, 0, 155, 44);
			this.blit(poseStack, integer + 155, integer2, 0, 45, 155, 44);
		});
		RenderSystem.disableBlend();
		RenderSystem.setShaderTexture(0, EDITION_LOCATION);
		blit(poseStack, k + 88, l + 37, 0.0F, 0.0F, 98, 14, 128, 16);
		int m = l + 100;

		for (int n = 0; n < this.lines.size(); n++) {
			if (n == this.lines.size() - 1) {
				float h = (float)m + g - (float)(this.height / 2 - 6);
				if (h < 0.0F) {
					poseStack.translate(0.0, (double)(-h), 0.0);
				}
			}

			if ((float)m + g + 12.0F + 8.0F > 0.0F && (float)m + g < (float)this.height) {
				FormattedCharSequence formattedCharSequence = (FormattedCharSequence)this.lines.get(n);
				if (this.centeredLines.contains(n)) {
					this.font.drawShadow(poseStack, formattedCharSequence, (float)(k + (274 - this.font.width(formattedCharSequence)) / 2), (float)m, 16777215);
				} else {
					this.font.drawShadow(poseStack, formattedCharSequence, (float)k, (float)m, 16777215);
				}
			}

			m += 12;
		}

		poseStack.popPose();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
		int n = this.width;
		int o = this.height;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)o, (double)this.getBlitOffset()).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)n, (double)o, (double)this.getBlitOffset()).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)n, 0.0, (double)this.getBlitOffset()).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
		super.render(poseStack, i, j, f);
	}
}
