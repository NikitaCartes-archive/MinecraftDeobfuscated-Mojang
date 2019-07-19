package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.chat.TranslatableComponent;

public class TagParser {
	public static final SimpleCommandExceptionType ERROR_TRAILING_DATA = new SimpleCommandExceptionType(new TranslatableComponent("argument.nbt.trailing"));
	public static final SimpleCommandExceptionType ERROR_EXPECTED_KEY = new SimpleCommandExceptionType(new TranslatableComponent("argument.nbt.expected.key"));
	public static final SimpleCommandExceptionType ERROR_EXPECTED_VALUE = new SimpleCommandExceptionType(new TranslatableComponent("argument.nbt.expected.value"));
	public static final Dynamic2CommandExceptionType ERROR_INSERT_MIXED_LIST = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.nbt.list.mixed", object, object2)
	);
	public static final Dynamic2CommandExceptionType ERROR_INSERT_MIXED_ARRAY = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.nbt.array.mixed", object, object2)
	);
	public static final DynamicCommandExceptionType ERROR_INVALID_ARRAY = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.nbt.array.invalid", object)
	);
	private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
	private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
	private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
	private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
	private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
	private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
	private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
	private final StringReader reader;

	public static CompoundTag parseTag(String string) throws CommandSyntaxException {
		return new TagParser(new StringReader(string)).readSingleStruct();
	}

	@VisibleForTesting
	CompoundTag readSingleStruct() throws CommandSyntaxException {
		CompoundTag compoundTag = this.readStruct();
		this.reader.skipWhitespace();
		if (this.reader.canRead()) {
			throw ERROR_TRAILING_DATA.createWithContext(this.reader);
		} else {
			return compoundTag;
		}
	}

	public TagParser(StringReader stringReader) {
		this.reader = stringReader;
	}

	protected String readKey() throws CommandSyntaxException {
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
		} else {
			return this.reader.readString();
		}
	}

	protected Tag readTypedValue() throws CommandSyntaxException {
		this.reader.skipWhitespace();
		int i = this.reader.getCursor();
		if (StringReader.isQuotedStringStart(this.reader.peek())) {
			return new StringTag(this.reader.readQuotedString());
		} else {
			String string = this.reader.readUnquotedString();
			if (string.isEmpty()) {
				this.reader.setCursor(i);
				throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
			} else {
				return this.type(string);
			}
		}
	}

	private Tag type(String string) {
		try {
			if (FLOAT_PATTERN.matcher(string).matches()) {
				return new FloatTag(Float.parseFloat(string.substring(0, string.length() - 1)));
			}

			if (BYTE_PATTERN.matcher(string).matches()) {
				return new ByteTag(Byte.parseByte(string.substring(0, string.length() - 1)));
			}

			if (LONG_PATTERN.matcher(string).matches()) {
				return new LongTag(Long.parseLong(string.substring(0, string.length() - 1)));
			}

			if (SHORT_PATTERN.matcher(string).matches()) {
				return new ShortTag(Short.parseShort(string.substring(0, string.length() - 1)));
			}

			if (INT_PATTERN.matcher(string).matches()) {
				return new IntTag(Integer.parseInt(string));
			}

			if (DOUBLE_PATTERN.matcher(string).matches()) {
				return new DoubleTag(Double.parseDouble(string.substring(0, string.length() - 1)));
			}

			if (DOUBLE_PATTERN_NOSUFFIX.matcher(string).matches()) {
				return new DoubleTag(Double.parseDouble(string));
			}

			if ("true".equalsIgnoreCase(string)) {
				return new ByteTag((byte)1);
			}

			if ("false".equalsIgnoreCase(string)) {
				return new ByteTag((byte)0);
			}
		} catch (NumberFormatException var3) {
		}

		return new StringTag(string);
	}

	public Tag readValue() throws CommandSyntaxException {
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
		} else {
			char c = this.reader.peek();
			if (c == '{') {
				return this.readStruct();
			} else {
				return c == '[' ? this.readList() : this.readTypedValue();
			}
		}
	}

	protected Tag readList() throws CommandSyntaxException {
		return this.reader.canRead(3) && !StringReader.isQuotedStringStart(this.reader.peek(1)) && this.reader.peek(2) == ';'
			? this.readArrayTag()
			: this.readListTag();
	}

	public CompoundTag readStruct() throws CommandSyntaxException {
		this.expect('{');
		CompoundTag compoundTag = new CompoundTag();
		this.reader.skipWhitespace();

		while (this.reader.canRead() && this.reader.peek() != '}') {
			int i = this.reader.getCursor();
			String string = this.readKey();
			if (string.isEmpty()) {
				this.reader.setCursor(i);
				throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
			}

			this.expect(':');
			compoundTag.put(string, this.readValue());
			if (!this.hasElementSeparator()) {
				break;
			}

			if (!this.reader.canRead()) {
				throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
			}
		}

		this.expect('}');
		return compoundTag;
	}

	private Tag readListTag() throws CommandSyntaxException {
		this.expect('[');
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
		} else {
			ListTag listTag = new ListTag();
			int i = -1;

			while (this.reader.peek() != ']') {
				int j = this.reader.getCursor();
				Tag tag = this.readValue();
				int k = tag.getId();
				if (i < 0) {
					i = k;
				} else if (k != i) {
					this.reader.setCursor(j);
					throw ERROR_INSERT_MIXED_LIST.createWithContext(this.reader, Tag.getTagTypeName(k), Tag.getTagTypeName(i));
				}

				listTag.add(tag);
				if (!this.hasElementSeparator()) {
					break;
				}

				if (!this.reader.canRead()) {
					throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
				}
			}

			this.expect(']');
			return listTag;
		}
	}

	private Tag readArrayTag() throws CommandSyntaxException {
		this.expect('[');
		int i = this.reader.getCursor();
		char c = this.reader.read();
		this.reader.read();
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
		} else if (c == 'B') {
			return new ByteArrayTag(this.readArray((byte)7, (byte)1));
		} else if (c == 'L') {
			return new LongArrayTag(this.readArray((byte)12, (byte)4));
		} else if (c == 'I') {
			return new IntArrayTag(this.readArray((byte)11, (byte)3));
		} else {
			this.reader.setCursor(i);
			throw ERROR_INVALID_ARRAY.createWithContext(this.reader, String.valueOf(c));
		}
	}

	private <T extends Number> List<T> readArray(byte b, byte c) throws CommandSyntaxException {
		List<T> list = Lists.<T>newArrayList();

		while (this.reader.peek() != ']') {
			int i = this.reader.getCursor();
			Tag tag = this.readValue();
			int j = tag.getId();
			if (j != c) {
				this.reader.setCursor(i);
				throw ERROR_INSERT_MIXED_ARRAY.createWithContext(this.reader, Tag.getTagTypeName(j), Tag.getTagTypeName(b));
			}

			if (c == 1) {
				list.add(((NumericTag)tag).getAsByte());
			} else if (c == 4) {
				list.add(((NumericTag)tag).getAsLong());
			} else {
				list.add(((NumericTag)tag).getAsInt());
			}

			if (!this.hasElementSeparator()) {
				break;
			}

			if (!this.reader.canRead()) {
				throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
			}
		}

		this.expect(']');
		return list;
	}

	private boolean hasElementSeparator() {
		this.reader.skipWhitespace();
		if (this.reader.canRead() && this.reader.peek() == ',') {
			this.reader.skip();
			this.reader.skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	private void expect(char c) throws CommandSyntaxException {
		this.reader.skipWhitespace();
		this.reader.expect(c);
	}
}
