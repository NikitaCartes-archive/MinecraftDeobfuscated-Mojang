package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.TranslatableComponent;

public class WrappedMinMaxBounds {
	public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds(null, null);
	public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(new TranslatableComponent("argument.range.ints"));
	private final Float min;
	private final Float max;

	public WrappedMinMaxBounds(@Nullable Float float_, @Nullable Float float2) {
		this.min = float_;
		this.max = float2;
	}

	@Nullable
	public Float getMin() {
		return this.min;
	}

	@Nullable
	public Float getMax() {
		return this.max;
	}

	public static WrappedMinMaxBounds fromReader(StringReader stringReader, boolean bl, Function<Float, Float> function) throws CommandSyntaxException {
		if (!stringReader.canRead()) {
			throw MinMaxBounds.ERROR_EMPTY.createWithContext(stringReader);
		} else {
			int i = stringReader.getCursor();
			Float float_ = optionallyFormat(readNumber(stringReader, bl), function);
			Float float2;
			if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
				stringReader.skip();
				stringReader.skip();
				float2 = optionallyFormat(readNumber(stringReader, bl), function);
				if (float_ == null && float2 == null) {
					stringReader.setCursor(i);
					throw MinMaxBounds.ERROR_EMPTY.createWithContext(stringReader);
				}
			} else {
				if (!bl && stringReader.canRead() && stringReader.peek() == '.') {
					stringReader.setCursor(i);
					throw ERROR_INTS_ONLY.createWithContext(stringReader);
				}

				float2 = float_;
			}

			if (float_ == null && float2 == null) {
				stringReader.setCursor(i);
				throw MinMaxBounds.ERROR_EMPTY.createWithContext(stringReader);
			} else {
				return new WrappedMinMaxBounds(float_, float2);
			}
		}
	}

	@Nullable
	private static Float readNumber(StringReader stringReader, boolean bl) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && isAllowedNumber(stringReader, bl)) {
			stringReader.skip();
		}

		String string = stringReader.getString().substring(i, stringReader.getCursor());
		if (string.isEmpty()) {
			return null;
		} else {
			try {
				return Float.parseFloat(string);
			} catch (NumberFormatException var5) {
				if (bl) {
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(stringReader, string);
				} else {
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(stringReader, string);
				}
			}
		}
	}

	private static boolean isAllowedNumber(StringReader stringReader, boolean bl) {
		char c = stringReader.peek();
		if ((c < '0' || c > '9') && c != '-') {
			return bl && c == '.' ? !stringReader.canRead(2) || stringReader.peek(1) != '.' : false;
		} else {
			return true;
		}
	}

	@Nullable
	private static Float optionallyFormat(@Nullable Float float_, Function<Float, Float> function) {
		return float_ == null ? null : (Float)function.apply(float_);
	}
}
