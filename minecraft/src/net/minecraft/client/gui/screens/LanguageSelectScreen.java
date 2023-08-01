package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
	private static final Component WARNING_LABEL = Component.literal("(")
		.append(Component.translatable("options.languageWarning"))
		.append(")")
		.withStyle(ChatFormatting.GRAY);
	private LanguageSelectScreen.LanguageSelectionList packSelectionList;
	final LanguageManager languageManager;

	public LanguageSelectScreen(Screen screen, Options options, LanguageManager languageManager) {
		super(screen, options, Component.translatable("options.language"));
		this.languageManager = languageManager;
	}

	@Override
	protected void init() {
		this.packSelectionList = new LanguageSelectScreen.LanguageSelectionList(this.minecraft);
		this.addWidget(this.packSelectionList);
		this.addRenderableWidget(this.options.forceUnicodeFont().createButton(this.options, this.width / 2 - 155, this.height - 38, 150));
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 155 + 160, this.height - 38, 150, 20).build()
		);
		super.init();
	}

	void onDone() {
		LanguageSelectScreen.LanguageSelectionList.Entry entry = this.packSelectionList.getSelected();
		if (entry != null && !entry.code.equals(this.languageManager.getSelected())) {
			this.languageManager.setSelected(entry.code);
			this.options.languageCode = entry.code;
			this.minecraft.reloadResourcePacks();
			this.options.save();
		}

		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (CommonInputs.selected(i)) {
			LanguageSelectScreen.LanguageSelectionList.Entry entry = this.packSelectionList.getSelected();
			if (entry != null) {
				entry.select();
				this.onDone();
				return true;
			}
		}

		return super.keyPressed(i, j, k);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.packSelectionList.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
		guiGraphics.drawCenteredString(this.font, WARNING_LABEL, this.width / 2, this.height - 56, -8355712);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
	}

	@Environment(EnvType.CLIENT)
	class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
		public LanguageSelectionList(Minecraft minecraft) {
			super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);
			String string = LanguageSelectScreen.this.languageManager.getSelected();
			LanguageSelectScreen.this.languageManager.getLanguages().forEach((string2, languageInfo) -> {
				LanguageSelectScreen.LanguageSelectionList.Entry entry = new LanguageSelectScreen.LanguageSelectionList.Entry(string2, languageInfo);
				this.addEntry(entry);
				if (string.equals(string2)) {
					this.setSelected(entry);
				}
			});
			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}
		}

		@Override
		protected int getScrollbarPosition() {
			return super.getScrollbarPosition() + 20;
		}

		@Override
		public int getRowWidth() {
			return super.getRowWidth() + 50;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
			final String code;
			private final Component language;
			private long lastClickTime;

			public Entry(String string, LanguageInfo languageInfo) {
				this.code = string;
				this.language = languageInfo.toComponent();
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				guiGraphics.drawCenteredString(LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, j + 1, 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					this.select();
					if (Util.getMillis() - this.lastClickTime < 250L) {
						LanguageSelectScreen.this.onDone();
					}

					this.lastClickTime = Util.getMillis();
					return true;
				} else {
					this.lastClickTime = Util.getMillis();
					return false;
				}
			}

			void select() {
				LanguageSelectionList.this.setSelected(this);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.language);
			}
		}
	}
}
