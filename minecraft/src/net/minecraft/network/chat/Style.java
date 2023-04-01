package net.minecraft.network.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Style {
	public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null, null);
	public static final Codec<Style> FORMATTING_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					TextColor.CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.color)),
					Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)),
					Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic)),
					Codec.BOOL.optionalFieldOf("underlined").forGetter(style -> Optional.ofNullable(style.underlined)),
					Codec.BOOL.optionalFieldOf("strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)),
					Codec.BOOL.optionalFieldOf("obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)),
					Codec.STRING.optionalFieldOf("insertion").forGetter(style -> Optional.ofNullable(style.insertion)),
					ResourceLocation.CODEC.optionalFieldOf("font").forGetter(style -> Optional.ofNullable(style.font)),
					Codec.BOOL.optionalFieldOf("reversed").forGetter(style -> Optional.ofNullable(style.reversed))
				)
				.apply(instance, Style::create)
	);
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
	@Nullable
	final Boolean reversed;

	private static Style create(
		Optional<TextColor> optional,
		Optional<Boolean> optional2,
		Optional<Boolean> optional3,
		Optional<Boolean> optional4,
		Optional<Boolean> optional5,
		Optional<Boolean> optional6,
		Optional<String> optional7,
		Optional<ResourceLocation> optional8,
		Optional<Boolean> optional9
	) {
		return new Style(
			(TextColor)optional.orElse(null),
			(Boolean)optional2.orElse(null),
			(Boolean)optional3.orElse(null),
			(Boolean)optional4.orElse(null),
			(Boolean)optional5.orElse(null),
			(Boolean)optional6.orElse(null),
			null,
			null,
			(String)optional7.orElse(null),
			(ResourceLocation)optional8.orElse(null),
			(Boolean)optional9.orElse(null)
		);
	}

	Style(
		@Nullable TextColor textColor,
		@Nullable Boolean boolean_,
		@Nullable Boolean boolean2,
		@Nullable Boolean boolean3,
		@Nullable Boolean boolean4,
		@Nullable Boolean boolean5,
		@Nullable ClickEvent clickEvent,
		@Nullable HoverEvent hoverEvent,
		@Nullable String string,
		@Nullable ResourceLocation resourceLocation,
		@Nullable Boolean boolean6
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
		this.reversed = boolean6;
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

	public boolean isReversed() {
		return this.reversed == Boolean.TRUE;
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

	public Style withColor(@Nullable TextColor textColor) {
		return new Style(
			textColor,
			this.bold,
			this.italic,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withColor(@Nullable ChatFormatting chatFormatting) {
		return this.withColor(chatFormatting != null ? TextColor.fromLegacyFormat(chatFormatting) : null);
	}

	public Style withColor(int i) {
		return this.withColor(TextColor.fromRgb(i));
	}

	public Style withBold(@Nullable Boolean boolean_) {
		return new Style(
			this.color,
			boolean_,
			this.italic,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withItalic(@Nullable Boolean boolean_) {
		return new Style(
			this.color,
			this.bold,
			boolean_,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withUnderlined(@Nullable Boolean boolean_) {
		return new Style(
			this.color,
			this.bold,
			this.italic,
			boolean_,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withStrikethrough(@Nullable Boolean boolean_) {
		return new Style(
			this.color, this.bold, this.italic, this.underlined, boolean_, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font, this.reversed
		);
	}

	public Style withObfuscated(@Nullable Boolean boolean_) {
		return new Style(
			this.color,
			this.bold,
			this.italic,
			this.underlined,
			this.strikethrough,
			boolean_,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withClickEvent(@Nullable ClickEvent clickEvent) {
		return new Style(
			this.color,
			this.bold,
			this.italic,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
		return new Style(
			this.color,
			this.bold,
			this.italic,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			hoverEvent,
			this.insertion,
			this.font,
			this.reversed
		);
	}

	public Style withInsertion(@Nullable String string) {
		return new Style(
			this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, string, this.font, this.reversed
		);
	}

	public Style withFont(@Nullable ResourceLocation resourceLocation) {
		return new Style(
			this.color,
			this.bold,
			this.italic,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			resourceLocation,
			this.reversed
		);
	}

	public Style withReversed(@Nullable Boolean boolean_) {
		return new Style(
			this.color,
			this.bold,
			this.italic,
			this.underlined,
			this.strikethrough,
			this.obfuscated,
			this.clickEvent,
			this.hoverEvent,
			this.insertion,
			this.font,
			boolean_
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

		return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font, this.reversed);
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

		return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font, this.reversed);
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

		return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font, this.reversed);
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
					this.font != null ? this.font : style.font,
					this.reversed != null ? this.reversed : style.reversed
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
		lv.addValueString("reversed", this.reversed);
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof Style style)
				? false
				: this.isBold() == style.isBold()
					&& Objects.equals(this.getColor(), style.getColor())
					&& this.isItalic() == style.isItalic()
					&& this.isObfuscated() == style.isObfuscated()
					&& this.isStrikethrough() == style.isStrikethrough()
					&& this.isUnderlined() == style.isUnderlined()
					&& Objects.equals(this.getClickEvent(), style.getClickEvent())
					&& Objects.equals(this.getHoverEvent(), style.getHoverEvent())
					&& Objects.equals(this.getInsertion(), style.getInsertion())
					&& Objects.equals(this.getFont(), style.getFont())
					&& this.isReversed() == style.isReversed();
		}
	}

	public int hashCode() {
		return Objects.hash(
			new Object[]{
				this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.reversed
			}
		);
	}

	public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
		@Nullable
		public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				if (jsonObject == null) {
					return null;
				} else {
					Boolean boolean_ = getOptionalFlag(jsonObject, "bold");
					Boolean boolean2 = getOptionalFlag(jsonObject, "italic");
					Boolean boolean3 = getOptionalFlag(jsonObject, "underlined");
					Boolean boolean4 = getOptionalFlag(jsonObject, "strikethrough");
					Boolean boolean5 = getOptionalFlag(jsonObject, "obfuscated");
					TextColor textColor = getTextColor(jsonObject);
					String string = getInsertion(jsonObject);
					ClickEvent clickEvent = getClickEvent(jsonObject);
					HoverEvent hoverEvent = getHoverEvent(jsonObject);
					ResourceLocation resourceLocation = getFont(jsonObject);
					Boolean boolean6 = getOptionalFlag(jsonObject, "reversed");
					return new Style(textColor, boolean_, boolean2, boolean3, boolean4, boolean5, clickEvent, hoverEvent, string, resourceLocation, boolean6);
				}
			} else {
				return null;
			}
		}

		@Nullable
		private static ResourceLocation getFont(JsonObject jsonObject) {
			if (jsonObject.has("font")) {
				String string = GsonHelper.getAsString(jsonObject, "font");

				try {
					return new ResourceLocation(string);
				} catch (ResourceLocationException var3) {
					throw new JsonSyntaxException("Invalid font name: " + string);
				}
			} else {
				return null;
			}
		}

		@Nullable
		private static HoverEvent getHoverEvent(JsonObject jsonObject) {
			if (jsonObject.has("hoverEvent")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "hoverEvent");
				HoverEvent hoverEvent = HoverEvent.deserialize(jsonObject2);
				if (hoverEvent != null && hoverEvent.getAction().isAllowedFromServer()) {
					return hoverEvent;
				}
			}

			return null;
		}

		@Nullable
		private static ClickEvent getClickEvent(JsonObject jsonObject) {
			if (jsonObject.has("clickEvent")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "clickEvent");
				String string = GsonHelper.getAsString(jsonObject2, "action", null);
				ClickEvent.Action action = string == null ? null : ClickEvent.Action.getByName(string);
				String string2 = GsonHelper.getAsString(jsonObject2, "value", null);
				if (action != null && string2 != null && action.isAllowedFromServer()) {
					return new ClickEvent(action, string2);
				}
			}

			return null;
		}

		@Nullable
		private static String getInsertion(JsonObject jsonObject) {
			return GsonHelper.getAsString(jsonObject, "insertion", null);
		}

		@Nullable
		private static TextColor getTextColor(JsonObject jsonObject) {
			if (jsonObject.has("color")) {
				String string = GsonHelper.getAsString(jsonObject, "color");
				return TextColor.parseColor(string);
			} else {
				return null;
			}
		}

		@Nullable
		private static Boolean getOptionalFlag(JsonObject jsonObject, String string) {
			return jsonObject.has(string) ? jsonObject.get(string).getAsBoolean() : null;
		}

		@Nullable
		public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
			if (style.isEmpty()) {
				return null;
			} else {
				JsonObject jsonObject = new JsonObject();
				if (style.bold != null) {
					jsonObject.addProperty("bold", style.bold);
				}

				if (style.italic != null) {
					jsonObject.addProperty("italic", style.italic);
				}

				if (style.underlined != null) {
					jsonObject.addProperty("underlined", style.underlined);
				}

				if (style.strikethrough != null) {
					jsonObject.addProperty("strikethrough", style.strikethrough);
				}

				if (style.obfuscated != null) {
					jsonObject.addProperty("obfuscated", style.obfuscated);
				}

				if (style.color != null) {
					jsonObject.addProperty("color", style.color.serialize());
				}

				if (style.insertion != null) {
					jsonObject.add("insertion", jsonSerializationContext.serialize(style.insertion));
				}

				if (style.clickEvent != null) {
					JsonObject jsonObject2 = new JsonObject();
					jsonObject2.addProperty("action", style.clickEvent.getAction().getName());
					jsonObject2.addProperty("value", style.clickEvent.getValue());
					jsonObject.add("clickEvent", jsonObject2);
				}

				if (style.hoverEvent != null) {
					jsonObject.add("hoverEvent", style.hoverEvent.serialize());
				}

				if (style.font != null) {
					jsonObject.addProperty("font", style.font.toString());
				}

				if (style.reversed != null) {
					jsonObject.addProperty("reversed", style.reversed);
				}

				return jsonObject;
			}
		}
	}
}
