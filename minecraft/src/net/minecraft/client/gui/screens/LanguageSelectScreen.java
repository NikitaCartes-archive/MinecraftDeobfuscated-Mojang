package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
	private static final Component WARNING_LABEL = new TextComponent("(")
		.append(new TranslatableComponent("options.languageWarning"))
		.append(")")
		.withStyle(ChatFormatting.GRAY);
	private LanguageSelectScreen.LanguageSelectionList packSelectionList;
	final LanguageManager languageManager;

	public LanguageSelectScreen(Screen screen, Options options, LanguageManager languageManager) {
		super(screen, options, new TranslatableComponent("options.language"));
		this.languageManager = languageManager;
	}

	@Override
	protected void init() {
		this.packSelectionList = new LanguageSelectScreen.LanguageSelectionList(this.minecraft);
		this.addWidget(this.packSelectionList);
		this.addRenderableWidget(Option.FORCE_UNICODE_FONT.createButton(this.options, this.width / 2 - 155, this.height - 38, 150));
		this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, CommonComponents.GUI_DONE, button -> {
			LanguageSelectScreen.LanguageSelectionList.Entry entry = this.packSelectionList.getSelected();
			if (entry != null && !entry.language.getCode().equals(this.languageManager.getSelected().getCode())) {
				this.languageManager.setSelected(entry.language);
				this.options.languageCode = entry.language.getCode();
				this.minecraft.reloadResourcePacks();
				this.options.save();
			}

			this.minecraft.setScreen(this.lastScreen);
		}));
		super.init();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.packSelectionList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
		drawCenteredString(poseStack, this.font, WARNING_LABEL, this.width / 2, this.height - 56, 8421504);
		super.render(poseStack, i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
		public LanguageSelectionList(Minecraft minecraft) {
			super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);

			for (LanguageInfo languageInfo : LanguageSelectScreen.this.languageManager.getLanguages()) {
				LanguageSelectScreen.LanguageSelectionList.Entry entry = new LanguageSelectScreen.LanguageSelectionList.Entry(languageInfo);
				this.addEntry(entry);
				if (LanguageSelectScreen.this.languageManager.getSelected().getCode().equals(languageInfo.getCode())) {
					this.setSelected(entry);
				}
			}

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

		@Override
		protected void renderBackground(PoseStack poseStack) {
			LanguageSelectScreen.this.renderBackground(poseStack);
		}

		@Override
		protected boolean isFocused() {
			return LanguageSelectScreen.this.getFocused() == this;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
			final LanguageInfo language;

			public Entry(LanguageInfo languageInfo) {
				this.language = languageInfo;
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				String string = this.language.toString();
				LanguageSelectScreen.this.font
					.drawShadow(
						poseStack, string, (float)(LanguageSelectionList.this.width / 2 - LanguageSelectScreen.this.font.width(string) / 2), (float)(j + 1), 16777215, true
					);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					this.select();
					return true;
				} else {
					return false;
				}
			}

			private void select() {
				LanguageSelectionList.this.setSelected(this);
			}

			@Override
			public Component getNarration() {
				return new TranslatableComponent("narrator.select", this.language);
			}
		}
	}
}
