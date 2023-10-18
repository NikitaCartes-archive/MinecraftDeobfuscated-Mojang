package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;

public class MutableComponent implements Component {
	private final ComponentContents contents;
	private final List<Component> siblings;
	private Style style;
	private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
	@Nullable
	private Language decomposedWith;

	MutableComponent(ComponentContents componentContents, List<Component> list, Style style) {
		this.contents = componentContents;
		this.siblings = list;
		this.style = style;
	}

	public static MutableComponent create(ComponentContents componentContents) {
		return new MutableComponent(componentContents, Lists.<Component>newArrayList(), Style.EMPTY);
	}

	@Override
	public ComponentContents getContents() {
		return this.contents;
	}

	@Override
	public List<Component> getSiblings() {
		return this.siblings;
	}

	public MutableComponent setStyle(Style style) {
		this.style = style;
		return this;
	}

	@Override
	public Style getStyle() {
		return this.style;
	}

	public MutableComponent append(String string) {
		return this.append(Component.literal(string));
	}

	public MutableComponent append(Component component) {
		this.siblings.add(component);
		return this;
	}

	public MutableComponent withStyle(UnaryOperator<Style> unaryOperator) {
		this.setStyle((Style)unaryOperator.apply(this.getStyle()));
		return this;
	}

	public MutableComponent withStyle(Style style) {
		this.setStyle(style.applyTo(this.getStyle()));
		return this;
	}

	public MutableComponent withStyle(ChatFormatting... chatFormattings) {
		this.setStyle(this.getStyle().applyFormats(chatFormattings));
		return this;
	}

	public MutableComponent withStyle(ChatFormatting chatFormatting) {
		this.setStyle(this.getStyle().applyFormat(chatFormatting));
		return this;
	}

	public MutableComponent withColor(int i) {
		this.setStyle(this.getStyle().withColor(i));
		return this;
	}

	@Override
	public FormattedCharSequence getVisualOrderText() {
		Language language = Language.getInstance();
		if (this.decomposedWith != language) {
			this.visualOrderText = language.getVisualOrder(this);
			this.decomposedWith = language;
		}

		return this.visualOrderText;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof MutableComponent mutableComponent)
				? false
				: this.contents.equals(mutableComponent.contents) && this.style.equals(mutableComponent.style) && this.siblings.equals(mutableComponent.siblings);
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.contents, this.style, this.siblings});
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder(this.contents.toString());
		boolean bl = !this.style.isEmpty();
		boolean bl2 = !this.siblings.isEmpty();
		if (bl || bl2) {
			stringBuilder.append('[');
			if (bl) {
				stringBuilder.append("style=");
				stringBuilder.append(this.style);
			}

			if (bl && bl2) {
				stringBuilder.append(", ");
			}

			if (bl2) {
				stringBuilder.append("siblings=");
				stringBuilder.append(this.siblings);
			}

			stringBuilder.append(']');
		}

		return stringBuilder.toString();
	}
}
