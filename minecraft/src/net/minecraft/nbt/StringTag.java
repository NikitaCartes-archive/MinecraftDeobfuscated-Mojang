package net.minecraft.nbt;

import com.mojang.logging.LogUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Objects;
import net.minecraft.Util;
import org.slf4j.Logger;

public class StringTag implements Tag {
	private static final int SELF_SIZE_IN_BITS = 288;
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>() {
		public StringTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(288L);
			String string = dataInput.readUTF();
			nbtAccounter.accountBits((long)(16 * string.length()));
			return StringTag.valueOf(string);
		}

		@Override
		public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
			return streamTagVisitor.visit(dataInput.readUTF());
		}

		@Override
		public void skip(DataInput dataInput) throws IOException {
			StringTag.skipString(dataInput);
		}

		@Override
		public String getName() {
			return "STRING";
		}

		@Override
		public String getPrettyName() {
			return "TAG_String";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	private static final StringTag EMPTY = new StringTag("");
	private static final char DOUBLE_QUOTE = '"';
	private static final char SINGLE_QUOTE = '\'';
	private static final char ESCAPE = '\\';
	private static final char NOT_SET = '\u0000';
	private final String data;

	public static void skipString(DataInput dataInput) throws IOException {
		dataInput.skipBytes(dataInput.readUnsignedShort());
	}

	private StringTag(String string) {
		Objects.requireNonNull(string, "Null string not allowed");
		this.data = string;
	}

	public static StringTag valueOf(String string) {
		return string.isEmpty() ? EMPTY : new StringTag(string);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		try {
			dataOutput.writeUTF(this.data);
		} catch (UTFDataFormatException var3) {
			Util.logAndPauseIfInIde("Failed to write NBT String", var3);
			dataOutput.writeUTF("");
		}
	}

	@Override
	public byte getId() {
		return 8;
	}

	@Override
	public TagType<StringTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return Tag.super.getAsString();
	}

	public StringTag copy() {
		return this;
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof StringTag && Objects.equals(this.data, ((StringTag)object).data);
	}

	public int hashCode() {
		return this.data.hashCode();
	}

	@Override
	public String getAsString() {
		return this.data;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitString(this);
	}

	public static String quoteAndEscape(String string) {
		StringBuilder stringBuilder = new StringBuilder(" ");
		char c = 0;

		for (int i = 0; i < string.length(); i++) {
			char d = string.charAt(i);
			if (d == '\\') {
				stringBuilder.append('\\');
			} else if (d == '"' || d == '\'') {
				if (c == 0) {
					c = (char)(d == '"' ? 39 : 34);
				}

				if (c == d) {
					stringBuilder.append('\\');
				}
			}

			stringBuilder.append(d);
		}

		if (c == 0) {
			c = '"';
		}

		stringBuilder.setCharAt(0, c);
		stringBuilder.append(c);
		return stringBuilder.toString();
	}

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		return streamTagVisitor.visit(this.data);
	}
}
