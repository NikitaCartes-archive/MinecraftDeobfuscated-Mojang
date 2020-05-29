package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.locale.Language;

public class TextComponent extends BaseComponent {
	public static final Component EMPTY = new TextComponent("");
	private final String text;
	@Nullable
	private Language decomposedWith;
	private String reorderedText;

	public TextComponent(String string) {
		this.text = string;
		this.reorderedText = string;
	}

	public String getText() {
		return this.text;
	}

	@Override
	public String getContents() {
		if (this.text.isEmpty()) {
			return this.text;
		} else {
			Language language = Language.getInstance();
			if (this.decomposedWith != language) {
				this.reorderedText = language.reorder(this.text, false);
				this.decomposedWith = language;
			}

			return this.reorderedText;
		}
	}

	public TextComponent toMutable() {
		TextComponent textComponent = new TextComponent(this.text);
		textComponent.setStyle(this.getStyle());
		return textComponent;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof TextComponent)) {
			return false;
		} else {
			TextComponent textComponent = (TextComponent)object;
			return this.text.equals(textComponent.getText()) && super.equals(object);
		}
	}

	@Override
	public String toString() {
		return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
	}
}
