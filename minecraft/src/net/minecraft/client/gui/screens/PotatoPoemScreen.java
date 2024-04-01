package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class PotatoPoemScreen extends Screen {
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
	private static final float SPEEDUP_FACTOR = 5.0F;
	private static final float SPEEDUP_FACTOR_FAST = 15.0F;
	private final Runnable onFinished;
	private float scroll;
	private List<FormattedCharSequence> lines;
	private IntSet centeredLines;
	private int totalScrollLength;
	private boolean speedupActive;
	private final IntSet speedupModifiers = new IntOpenHashSet();
	private float scrollSpeed;
	private final float unmodifiedScrollSpeed;
	private int direction;
	private final LogoRenderer logoRenderer = new LogoRenderer(false);

	public PotatoPoemScreen(Runnable runnable) {
		super(GameNarrator.NO_TITLE);
		this.onFinished = runnable;
		this.unmodifiedScrollSpeed = 0.5F;
		this.direction = 1;
		this.scrollSpeed = this.unmodifiedScrollSpeed;
	}

	private float calculateScrollSpeed() {
		return this.speedupActive
			? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) * (float)this.direction
			: this.unmodifiedScrollSpeed * (float)this.direction;
	}

	@Override
	public void tick() {
		this.minecraft.getMusicManager().tick();
		this.minecraft.getSoundManager().tick(false);
		float f = (float)(this.totalScrollLength + this.height + 50);
		if (this.scroll > f) {
			this.respawn();
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 265) {
			this.direction = -1;
		} else if (i == 341 || i == 345) {
			this.speedupModifiers.add(i);
		} else if (i == 32) {
			this.speedupActive = true;
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyPressed(i, j, k);
	}

	@Override
	public boolean keyReleased(int i, int j, int k) {
		if (i == 265) {
			this.direction = 1;
		}

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
	}

	@Override
	protected void init() {
		if (this.lines == null) {
			this.lines = Lists.<FormattedCharSequence>newArrayList();
			this.centeredLines = new IntOpenHashSet();
			this.wrapCreditsIO("texts/potato.txt", this::addPoemFile);
			this.totalScrollLength = this.lines.size() * 12;
		}
	}

	private void wrapCreditsIO(String string, PotatoPoemScreen.CreditsReader creditsReader) {
		try {
			Reader reader = this.minecraft.getResourceManager().openAsReader(new ResourceLocation(string));

			try {
				creditsReader.read(reader);
			} catch (Throwable var7) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (Exception var8) {
		}
	}

	private void addPoemFile(Reader reader) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		RandomSource randomSource = RandomSource.create(8124371L);

		String string;
		while ((string = bufferedReader.readLine()) != null) {
			string = string.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());

			int i;
			while ((i = string.indexOf(OBFUSCATE_TOKEN)) != -1) {
				String string2 = string.substring(0, i);
				String string3 = string.substring(i + OBFUSCATE_TOKEN.length());
				string = string2 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, randomSource.nextInt(4) + 3) + string3;
			}

			this.addPoemLines(string);
			this.addEmptyLine();
		}

		for (int i = 0; i < 8; i++) {
			this.addEmptyLine();
		}
	}

	private void addEmptyLine() {
		this.lines.add(FormattedCharSequence.EMPTY);
	}

	private void addPoemLines(String string) {
		this.lines.addAll(this.minecraft.font.split(Component.literal(string), 256));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderVignette(guiGraphics);
		this.scroll = Math.max(0.0F, this.scroll + f * this.scrollSpeed);
		int k = this.width / 2 - 128;
		int l = this.height + 50;
		float g = -this.scroll;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, g, 0.0F);
		this.logoRenderer.renderLogo(guiGraphics, this.width, 1.0F, l);
		int m = l + 100;

		for (int n = 0; n < this.lines.size(); n++) {
			if (n == this.lines.size() - 1) {
				float h = (float)m + g - (float)(this.height / 2 - 6);
				if (h < 0.0F) {
					guiGraphics.pose().translate(0.0F, -h, 0.0F);
				}
			}

			if ((float)m + g + 12.0F + 8.0F > 0.0F && (float)m + g < (float)this.height) {
				FormattedCharSequence formattedCharSequence = (FormattedCharSequence)this.lines.get(n);
				if (this.centeredLines.contains(n)) {
					guiGraphics.drawCenteredString(this.font, formattedCharSequence, k + 128, m, -1);
				} else {
					guiGraphics.drawString(this.font, formattedCharSequence, k, m, -1);
				}
			}

			m += 12;
		}

		guiGraphics.pose().popPose();
	}

	private void renderVignette(GuiGraphics guiGraphics) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
		guiGraphics.blit(VIGNETTE_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.fillRenderType(RenderType.poisonousPotato(), 0, 0, this.width, this.height, 0);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		this.minecraft.getMusicManager().stopPlaying(Musics.CREDITS);
	}

	@Override
	public Music getBackgroundMusic() {
		return Musics.CREDITS;
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	interface CreditsReader {
		void read(Reader reader) throws IOException;
	}
}
