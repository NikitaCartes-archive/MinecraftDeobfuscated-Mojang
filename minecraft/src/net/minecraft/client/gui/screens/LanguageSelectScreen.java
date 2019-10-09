package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.Language;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
	private LanguageSelectScreen.LanguageSelectionList packSelectionList;
	private final LanguageManager languageManager;
	private OptionButton forceUnicodeButton;
	private Button doneButton;

	public LanguageSelectScreen(Screen screen, Options options, LanguageManager languageManager) {
		super(screen, options, new TranslatableComponent("options.language"));
		this.languageManager = languageManager;
	}

	@Override
	protected void init() {
		this.packSelectionList = new LanguageSelectScreen.LanguageSelectionList(this.minecraft);
		this.children.add(this.packSelectionList);
		this.forceUnicodeButton = this.addButton(
			new OptionButton(this.width / 2 - 155, this.height - 38, 150, 20, Option.FORCE_UNICODE_FONT, Option.FORCE_UNICODE_FONT.getMessage(this.options), button -> {
				Option.FORCE_UNICODE_FONT.toggle(this.options);
				this.options.save();
				button.setMessage(Option.FORCE_UNICODE_FONT.getMessage(this.options));
				this.minecraft.resizeDisplay();
			})
		);
		this.doneButton = this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, I18n.get("gui.done"), button -> {
			LanguageSelectScreen.LanguageSelectionList.Entry entry = this.packSelectionList.getSelected();
			if (entry != null && !entry.language.getCode().equals(this.languageManager.getSelected().getCode())) {
				this.languageManager.setSelected(entry.language);
				this.options.languageCode = entry.language.getCode();
				this.minecraft.reloadResourcePacks();
				this.font.setBidirectional(this.languageManager.isBidirectional());
				this.doneButton.setMessage(I18n.get("gui.done"));
				this.forceUnicodeButton.setMessage(Option.FORCE_UNICODE_FONT.getMessage(this.options));
				this.options.save();
			}

			this.minecraft.setScreen(this.lastScreen);
		}));
		super.init();
	}

	@Override
	public void render(int i, int j, float f) {
		this.packSelectionList.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 16, 16777215);
		this.drawCenteredString(this.font, "(" + I18n.get("options.languageWarning") + ")", this.width / 2, this.height - 56, 8421504);
		super.render(i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
		public LanguageSelectionList(Minecraft minecraft) {
			super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);

			for (Language language : LanguageSelectScreen.this.languageManager.getLanguages()) {
				LanguageSelectScreen.LanguageSelectionList.Entry entry = new LanguageSelectScreen.LanguageSelectionList.Entry(language);
				this.addEntry(entry);
				if (LanguageSelectScreen.this.languageManager.getSelected().getCode().equals(language.getCode())) {
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

		public void setSelected(@Nullable LanguageSelectScreen.LanguageSelectionList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", entry.language).getString());
			}
		}

		@Override
		protected void renderBackground() {
			LanguageSelectScreen.this.renderBackground();
		}

		@Override
		protected boolean isFocused() {
			return LanguageSelectScreen.this.getFocused() == this;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
			private final Language language;

			public Entry(Language language) {
				this.language = language;
			}

			@Override
			public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				LanguageSelectScreen.this.font.setBidirectional(true);
				LanguageSelectionList.this.drawCenteredString(
					LanguageSelectScreen.this.font, this.language.toString(), LanguageSelectionList.this.width / 2, j + 1, 16777215
				);
				LanguageSelectScreen.this.font.setBidirectional(LanguageSelectScreen.this.languageManager.getSelected().isBidirectional());
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
		}
	}
}
