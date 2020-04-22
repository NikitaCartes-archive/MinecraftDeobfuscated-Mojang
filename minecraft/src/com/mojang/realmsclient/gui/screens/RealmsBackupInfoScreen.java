package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.dto.Backup;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
	private final Screen lastScreen;
	private final Backup backup;
	private final List<String> keys = Lists.<String>newArrayList();
	private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

	public RealmsBackupInfoScreen(Screen screen, Backup backup) {
		this.lastScreen = screen;
		this.backup = backup;
		if (backup.changeList != null) {
			for (Entry<String, String> entry : backup.changeList.entrySet()) {
				this.keys.add(entry.getKey());
			}
		}
	}

	@Override
	public void tick() {
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.addButton(
			new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen))
		);
		this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
		this.addWidget(this.backupInfoList);
		this.magicalSpecialHackyFocus(this.backupInfoList);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, "Changes from last backup", this.width / 2, 10, 16777215);
		this.backupInfoList.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}

	private Component checkForSpecificMetadata(String string, String string2) {
		String string3 = string.toLowerCase(Locale.ROOT);
		if (string3.contains("game") && string3.contains("mode")) {
			return this.gameModeMetadata(string2);
		} else {
			return (Component)(string3.contains("game") && string3.contains("difficulty") ? this.gameDifficultyMetadata(string2) : new TextComponent(string2));
		}
	}

	private Component gameDifficultyMetadata(String string) {
		try {
			return RealmsSlotOptionsScreen.DIFFICULTIES[Integer.parseInt(string)];
		} catch (Exception var3) {
			return new TextComponent("UNKNOWN");
		}
	}

	private Component gameModeMetadata(String string) {
		try {
			return RealmsSlotOptionsScreen.GAME_MODES[Integer.parseInt(string)];
		} catch (Exception var3) {
			return new TextComponent("UNKNOWN");
		}
	}

	@Environment(EnvType.CLIENT)
	class BackupInfoList extends ScrolledSelectionList {
		public BackupInfoList(Minecraft minecraft) {
			super(minecraft, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
		}

		@Override
		public int getItemCount() {
			return RealmsBackupInfoScreen.this.backup.changeList.size();
		}

		@Override
		protected void renderItem(PoseStack poseStack, int i, int j, int k, int l, int m, int n, float f) {
			String string = (String)RealmsBackupInfoScreen.this.keys.get(i);
			Font font = this.minecraft.font;
			this.drawString(poseStack, font, string, this.width / 2 - 40, k, 10526880);
			String string2 = (String)RealmsBackupInfoScreen.this.backup.changeList.get(string);
			this.drawString(poseStack, font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(string, string2), this.width / 2 - 40, k + 12, 16777215);
		}

		@Override
		public boolean isSelectedItem(int i) {
			return false;
		}

		@Override
		public void renderBackground() {
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, float f) {
			if (this.visible) {
				this.renderBackground();
				int k = this.getScrollbarPosition();
				int l = k + 6;
				this.capYPosition();
				RenderSystem.disableFog();
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				int m = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
				int n = this.y0 + 4 - (int)this.yo;
				if (this.renderHeader) {
					this.renderHeader(m, n, tesselator);
				}

				this.renderList(poseStack, m, n, i, j, f);
				RenderSystem.disableDepthTest();
				this.renderHoleBackground(0, this.y0, 255, 255);
				this.renderHoleBackground(this.y1, this.height, 255, 255);
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(
					GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
				);
				RenderSystem.disableAlphaTest();
				RenderSystem.shadeModel(7425);
				RenderSystem.disableTexture();
				int o = this.getMaxScroll();
				if (o > 0) {
					int p = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getMaxPosition();
					p = Mth.clamp(p, 32, this.y1 - this.y0 - 8);
					int q = (int)this.yo * (this.y1 - this.y0 - p) / o + this.y0;
					if (q < this.y0) {
						q = this.y0;
					}

					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
					bufferBuilder.vertex((double)k, (double)this.y1, 0.0).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
					bufferBuilder.vertex((double)l, (double)this.y1, 0.0).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
					bufferBuilder.vertex((double)l, (double)this.y0, 0.0).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
					bufferBuilder.vertex((double)k, (double)this.y0, 0.0).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
					tesselator.end();
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
					bufferBuilder.vertex((double)k, (double)(q + p), 0.0).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
					bufferBuilder.vertex((double)l, (double)(q + p), 0.0).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
					bufferBuilder.vertex((double)l, (double)q, 0.0).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
					bufferBuilder.vertex((double)k, (double)q, 0.0).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
					tesselator.end();
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
					bufferBuilder.vertex((double)k, (double)(q + p - 1), 0.0).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
					bufferBuilder.vertex((double)(l - 1), (double)(q + p - 1), 0.0).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
					bufferBuilder.vertex((double)(l - 1), (double)q, 0.0).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
					bufferBuilder.vertex((double)k, (double)q, 0.0).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
					tesselator.end();
				}

				this.renderDecorations(i, j);
				RenderSystem.enableTexture();
				RenderSystem.shadeModel(7424);
				RenderSystem.enableAlphaTest();
				RenderSystem.disableBlend();
			}
		}
	}
}
