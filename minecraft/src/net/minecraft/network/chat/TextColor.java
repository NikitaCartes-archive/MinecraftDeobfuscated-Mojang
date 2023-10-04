package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;

public final class TextColor {
	private static final String CUSTOM_COLOR_PREFIX = "#";
	public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(TextColor::parseColor, TextColor::serialize);
	private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = (Map<ChatFormatting, TextColor>)Stream.of(ChatFormatting.values())
		.filter(ChatFormatting::isColor)
		.collect(ImmutableMap.toImmutableMap(Function.identity(), chatFormatting -> new TextColor(chatFormatting.getColor(), chatFormatting.getName())));
	private static final Map<String, TextColor> NAMED_COLORS = (Map<String, TextColor>)LEGACY_FORMAT_TO_COLOR.values()
		.stream()
		.collect(ImmutableMap.toImmutableMap(textColor -> textColor.name, Function.identity()));
	private final int value;
	@Nullable
	private final String name;

	private TextColor(int i, String string) {
		this.value = i & 16777215;
		this.name = string;
	}

	private TextColor(int i) {
		this.value = i & 16777215;
		this.name = null;
	}

	public int getValue() {
		return this.value;
	}

	public String serialize() {
		return this.name != null ? this.name : this.formatValue();
	}

	private String formatValue() {
		return String.format(Locale.ROOT, "#%06X", this.value);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			TextColor textColor = (TextColor)object;
			return this.value == textColor.value;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.value, this.name});
	}

	public String toString() {
		return this.serialize();
	}

	@Nullable
	public static TextColor fromLegacyFormat(ChatFormatting chatFormatting) {
		return (TextColor)LEGACY_FORMAT_TO_COLOR.get(chatFormatting);
	}

	public static TextColor fromRgb(int i) {
		return new TextColor(i);
	}

	public static DataResult<TextColor> parseColor(String string) {
		if (string.startsWith("#")) {
			try {
				int i = Integer.parseInt(string.substring(1), 16);
				return i >= 0 && i <= 16777215 ? DataResult.success(fromRgb(i), Lifecycle.stable()) : DataResult.error(() -> "Color value out of range: " + string);
			} catch (NumberFormatException var2) {
				return DataResult.error(() -> "Invalid color value: " + string);
			}
		} else {
			TextColor textColor = (TextColor)NAMED_COLORS.get(string);
			return textColor == null ? DataResult.error(() -> "Invalid color name: " + string) : DataResult.success(textColor, Lifecycle.stable());
		}
	}
}
