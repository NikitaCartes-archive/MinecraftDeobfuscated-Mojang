package net.minecraft.client.gui.screens.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
	private static final Component WARNING_LABEL = Component.translatable("options.languageAccuracyWarning").withColor(-4539718);
	private static final int FOOTER_HEIGHT = 53;
	private LanguageSelectScreen.LanguageSelectionList languageSelectionList;
	final LanguageManager languageManager;

	public LanguageSelectScreen(Screen screen, Options options, LanguageManager languageManager) {
		super(screen, options, Component.translatable("options.language.title"));
		this.languageManager = languageManager;
		this.layout.setFooterHeight(53);
	}

	@Override
	protected void addContents() {
		this.languageSelectionList = this.layout.addToContents(new LanguageSelectScreen.LanguageSelectionList(this.minecraft));
	}

	@Override
	protected void addOptions() {
	}

	@Override
	protected void addFooter() {
		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.vertical()).spacing(8);
		linearLayout.defaultCellSetting().alignHorizontallyCenter();
		linearLayout.addChild(new StringWidget(WARNING_LABEL, this.font));
		LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal().spacing(8));
		linearLayout2.addChild(
			Button.builder(Component.translatable("options.font"), button -> this.minecraft.setScreen(new FontOptionsScreen(this, this.options))).build()
		);
		linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).build());
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		this.languageSelectionList.updateSize(this.width, this.layout);
	}

	void onDone() {
		LanguageSelectScreen.LanguageSelectionList.Entry entry = this.languageSelectionList.getSelected();
		if (entry != null && !entry.code.equals(this.languageManager.getSelected())) {
			this.languageManager.setSelected(entry.code);
			this.options.languageCode = entry.code;
			this.minecraft.reloadResourcePacks();
		}

		this.minecraft.setScreen(this.lastScreen);
	}

	@Environment(EnvType.CLIENT)
	class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
		public LanguageSelectionList(final Minecraft minecraft) {
			super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height - 33 - 53, 33, 18);
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
		public int getRowWidth() {
			return super.getRowWidth() + 50;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
			final String code;
			private final Component language;
			private long lastClickTime;

			public Entry(final String string, final LanguageInfo languageInfo) {
				this.code = string;
				this.language = languageInfo.toComponent();
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				guiGraphics.drawCenteredString(LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, j + m / 2 - 9 / 2, -1);
			}

			@Override
			public boolean keyPressed(int i, int j, int k) {
				if (CommonInputs.selected(i)) {
					this.select();
					LanguageSelectScreen.this.onDone();
					return true;
				} else {
					return super.keyPressed(i, j, k);
				}
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				this.select();
				if (Util.getMillis() - this.lastClickTime < 250L) {
					LanguageSelectScreen.this.onDone();
				}

				this.lastClickTime = Util.getMillis();
				return super.mouseClicked(d, e, i);
			}

			private void select() {
				LanguageSelectionList.this.setSelected(this);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.language);
			}
		}
	}
}
