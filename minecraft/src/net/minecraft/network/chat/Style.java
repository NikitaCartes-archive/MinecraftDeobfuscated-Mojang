package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class Style {
	public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
	public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft", "default");
	@Nullable
	final TextColor color;
	@Nullable
	final Boolean bold;
	@Nullable
	final Boolean italic;
	@Nullable
	final Boolean underlined;
	@Nullable
	final Boolean strikethrough;
	@Nullable
	final Boolean obfuscated;
	@Nullable
	final ClickEvent clickEvent;
	@Nullable
	final HoverEvent hoverEvent;
	@Nullable
	final String insertion;
	@Nullable
	final ResourceLocation font;

	private static Style create(
		Optional<TextColor> optional,
		Optional<Boolean> optional2,
		Optional<Boolean> optional3,
		Optional<Boolean> optional4,
		Optional<Boolean> optional5,
		Optional<Boolean> optional6,
		Optional<ClickEvent> optional7,
		Optional<HoverEvent> optional8,
		Optional<String> optional9,
		Optional<ResourceLocation> optional10
	) {
		Style style = new Style(
			(TextColor)optional.orElse(null),
			(Boolean)optional2.orElse(null),
			(Boolean)optional3.orElse(null),
			(Boolean)optional4.orElse(null),
			(Boolean)optional5.orElse(null),
			(Boolean)optional6.orElse(null),
			(ClickEvent)optional7.orElse(null),
			(HoverEvent)optional8.orElse(null),
			(String)optional9.orElse(null),
			(ResourceLocation)optional10.orElse(null)
		);
		return style.equals(EMPTY) ? EMPTY : style;
	}

	private Style(
		@Nullable TextColor textColor,
		@Nullable Boolean boolean_,
		@Nullable Boolean boolean2,
		@Nullable Boolean boolean3,
		@Nullable Boolean boolean4,
		@Nullable Boolean boolean5,
		@Nullable ClickEvent clickEvent,
		@Nullable HoverEvent hoverEvent,
		@Nullable String string,
		@Nullable ResourceLocation resourceLocation
	) {
		this.color = textColor;
		this.bold = boolean_;
		this.italic = boolean2;
		this.underlined = boolean3;
		this.strikethrough = boolean4;
		this.obfuscated = boolean5;
		this.clickEvent = clickEvent;
		this.hoverEvent = hoverEvent;
		this.insertion = string;
		this.font = resourceLocation;
	}

	@Nullable
	public TextColor getColor() {
		return this.color;
	}

	public boolean isBold() {
		return this.bold == Boolean.TRUE;
	}

	public boolean isItalic() {
		return this.italic == Boolean.TRUE;
	}

	public boolean isStrikethrough() {
		return this.strikethrough == Boolean.TRUE;
	}

	public boolean isUnderlined() {
		return this.underlined == Boolean.TRUE;
	}

	public boolean isObfuscated() {
		return this.obfuscated == Boolean.TRUE;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}

	@Nullable
	public ClickEvent getClickEvent() {
		return this.clickEvent;
	}

	@Nullable
	public HoverEvent getHoverEvent() {
		return this.hoverEvent;
	}

	@Nullable
	public String getInsertion() {
		return this.insertion;
	}

	public ResourceLocation getFont() {
		return this.font != null ? this.font : DEFAULT_FONT;
	}

	private static <T> Style checkEmptyAfterChange(Style style, @Nullable T object, @Nullable T object2) {
		return object != null && object2 == null && style.equals(EMPTY) ? EMPTY : style;
	}

	public Style withColor(@Nullable TextColor textColor) {
		return Objects.equals(this.color, textColor)
			? this
			: checkEmptyAfterChange(
				new Style(
					textColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
				),
				this.color,
				textColor
			);
	}

	public Style withColor(@Nullable ChatFormatting chatFormatting) {
		return this.withColor(chatFormatting != null ? TextColor.fromLegacyFormat(chatFormatting) : null);
	}

	public Style withColor(int i) {
		return this.withColor(TextColor.fromRgb(i));
	}

	public Style withBold(@Nullable Boolean boolean_) {
		return Objects.equals(this.bold, boolean_)
			? this
			: checkEmptyAfterChange(
				new Style(
					this.color, boolean_, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
				),
				this.bold,
				boolean_
			);
	}

	public Style withItalic(@Nullable Boolean boolean_) {
		return Objects.equals(this.italic, boolean_)
			? this
			: checkEmptyAfterChange(
				new Style(
					this.color, this.bold, boolean_, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font
				),
				this.italic,
				boolean_
			);
	}

	public Style withUnderlined(@Nullable Boolean boolean_) {
		return Objects.equals(this.underlined, boolean_)
			? this
			: checkEmptyAfterChange(
				new Style(this.color, this.bold, this.italic, boolean_, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font),
				this.underlined,
				boolean_
			);
	}

	public Style withStrikethrough(@Nullable Boolean boolean_) {
		return Objects.equals(this.strikethrough, boolean_)
			? this
			: checkEmptyAfterChange(
				new Style(this.color, this.bold, this.italic, this.underlined, boolean_, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font),
				this.strikethrough,
				boolean_
			);
	}

	public Style withObfuscated(@Nullable Boolean boolean_) {
		return Objects.equals(this.obfuscated, boolean_)
			? this
			: checkEmptyAfterChange(
				new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, boolean_, this.clickEvent, this.hoverEvent, this.insertion, this.font),
				this.obfuscated,
				boolean_
			);
	}

	public Style withClickEvent(@Nullable ClickEvent clickEvent) {
		return Objects.equals(this.clickEvent, clickEvent)
			? this
			: checkEmptyAfterChange(
				new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font),
				this.clickEvent,
				clickEvent
			);
	}

	public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
		return Objects.equals(this.hoverEvent, hoverEvent)
			? this
			: checkEmptyAfterChange(
				new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font),
				this.hoverEvent,
				hoverEvent
			);
	}

	public Style withInsertion(@Nullable String string) {
		return Objects.equals(this.insertion, string)
			? this
			: checkEmptyAfterChange(
				new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, string, this.font),
				this.insertion,
				string
			);
	}

	public Style withFont(@Nullable ResourceLocation resourceLocation) {
		return Objects.equals(this.font, resourceLocation)
			? this
			: checkEmptyAfterChange(
				new Style(
					this.color,
					this.bold,
					this.italic,
					this.underlined,
					this.strikethrough,
					this.obfuscated,
					this.clickEvent,
					this.hoverEvent,
					this.insertion,
					resourceLocation
				),
				this.font,
				resourceLocation
			);
	}

	public Style applyFormat(ChatFormatting chatFormatting) {
		TextColor textColor = this.color;
		Boolean boolean_ = this.bold;
		Boolean boolean2 = this.italic;
		Boolean boolean3 = this.strikethrough;
		Boolean boolean4 = this.underlined;
		Boolean boolean5 = this.obfuscated;
		switch (chatFormatting) {
			case OBFUSCATED:
				boolean5 = true;
				break;
			case BOLD:
				boolean_ = true;
				break;
			case STRIKETHROUGH:
				boolean3 = true;
				break;
			case UNDERLINE:
				boolean4 = true;
				break;
			case ITALIC:
				boolean2 = true;
				break;
			case RESET:
				return EMPTY;
			default:
				textColor = TextColor.fromLegacyFormat(chatFormatting);
		}

		return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
	}

	public Style applyLegacyFormat(ChatFormatting chatFormatting) {
		TextColor textColor = this.color;
		Boolean boolean_ = this.bold;
		Boolean boolean2 = this.italic;
		Boolean boolean3 = this.strikethrough;
		Boolean boolean4 = this.underlined;
		Boolean boolean5 = this.obfuscated;
		switch (chatFormatting) {
			case OBFUSCATED:
				boolean5 = true;
				break;
			case BOLD:
				boolean_ = true;
				break;
			case STRIKETHROUGH:
				boolean3 = true;
				break;
			case UNDERLINE:
				boolean4 = true;
				break;
			case ITALIC:
				boolean2 = true;
				break;
			case RESET:
				return EMPTY;
			default:
				boolean5 = false;
				boolean_ = false;
				boolean3 = false;
				boolean4 = false;
				boolean2 = false;
				textColor = TextColor.fromLegacyFormat(chatFormatting);
		}

		return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
	}

	public Style applyFormats(ChatFormatting... chatFormattings) {
		TextColor textColor = this.color;
		Boolean boolean_ = this.bold;
		Boolean boolean2 = this.italic;
		Boolean boolean3 = this.strikethrough;
		Boolean boolean4 = this.underlined;
		Boolean boolean5 = this.obfuscated;

		for (ChatFormatting chatFormatting : chatFormattings) {
			switch (chatFormatting) {
				case OBFUSCATED:
					boolean5 = true;
					break;
				case BOLD:
					boolean_ = true;
					break;
				case STRIKETHROUGH:
					boolean3 = true;
					break;
				case UNDERLINE:
					boolean4 = true;
					break;
				case ITALIC:
					boolean2 = true;
					break;
				case RESET:
					return EMPTY;
				default:
					textColor = TextColor.fromLegacyFormat(chatFormatting);
			}
		}

		return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
	}

	public Style applyTo(Style style) {
		if (this == EMPTY) {
			return style;
		} else {
			return style == EMPTY
				? this
				: new Style(
					this.color != null ? this.color : style.color,
					this.bold != null ? this.bold : style.bold,
					this.italic != null ? this.italic : style.italic,
					this.underlined != null ? this.underlined : style.underlined,
					this.strikethrough != null ? this.strikethrough : style.strikethrough,
					this.obfuscated != null ? this.obfuscated : style.obfuscated,
					this.clickEvent != null ? this.clickEvent : style.clickEvent,
					this.hoverEvent != null ? this.hoverEvent : style.hoverEvent,
					this.insertion != null ? this.insertion : style.insertion,
					this.font != null ? this.font : style.font
				);
		}
	}

	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder("{");

		class Collector {
			private boolean isNotFirst;

			private void prependSeparator() {
				if (this.isNotFirst) {
					stringBuilder.append(',');
				}

				this.isNotFirst = true;
			}

			void addFlagString(String string, @Nullable Boolean boolean_) {
				if (boolean_ != null) {
					this.prependSeparator();
					if (!boolean_) {
						stringBuilder.append('!');
					}

					stringBuilder.append(string);
				}
			}

			void addValueString(String string, @Nullable Object object) {
				if (object != null) {
					this.prependSeparator();
					stringBuilder.append(string);
					stringBuilder.append('=');
					stringBuilder.append(object);
				}
			}
		}

		Collector lv = new Collector();
		lv.addValueString("color", this.color);
		lv.addFlagString("bold", this.bold);
		lv.addFlagString("italic", this.italic);
		lv.addFlagString("underlined", this.underlined);
		lv.addFlagString("strikethrough", this.strikethrough);
		lv.addFlagString("obfuscated", this.obfuscated);
		lv.addValueString("clickEvent", this.clickEvent);
		lv.addValueString("hoverEvent", this.hoverEvent);
		lv.addValueString("insertion", this.insertion);
		lv.addValueString("font", this.font);
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof Style style)
				? false
				: this.bold == style.bold
					&& Objects.equals(this.getColor(), style.getColor())
					&& this.italic == style.italic
					&& this.obfuscated == style.obfuscated
					&& this.strikethrough == style.strikethrough
					&& this.underlined == style.underlined
					&& Objects.equals(this.clickEvent, style.clickEvent)
					&& Objects.equals(this.hoverEvent, style.hoverEvent)
					&& Objects.equals(this.insertion, style.insertion)
					&& Objects.equals(this.font, style.font);
		}
	}

	public int hashCode() {
		return Objects.hash(
			new Object[]{this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion}
		);
	}

	public static class Serializer {
		public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(TextColor.CODEC, "color").forGetter(style -> Optional.ofNullable(style.color)),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "bold").forGetter(style -> Optional.ofNullable(style.bold)),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "italic").forGetter(style -> Optional.ofNullable(style.italic)),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "underlined").forGetter(style -> Optional.ofNullable(style.underlined)),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)),
						ExtraCodecs.strictOptionalField(ClickEvent.CODEC, "clickEvent").forGetter(style -> Optional.ofNullable(style.clickEvent)),
						ExtraCodecs.strictOptionalField(HoverEvent.CODEC, "hoverEvent").forGetter(style -> Optional.ofNullable(style.hoverEvent)),
						ExtraCodecs.strictOptionalField(Codec.STRING, "insertion").forGetter(style -> Optional.ofNullable(style.insertion)),
						ExtraCodecs.strictOptionalField(ResourceLocation.CODEC, "font").forGetter(style -> Optional.ofNullable(style.font))
					)
					.apply(instance, Style::create)
		);
		public static final Codec<Style> CODEC = MAP_CODEC.codec();
	}
}
