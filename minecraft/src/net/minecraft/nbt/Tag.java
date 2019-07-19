package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface Tag {
	String[] TAG_NAMES = new String[]{"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]", "LONG[]"};
	ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
	ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
	ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
	ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;

	void write(DataOutput dataOutput) throws IOException;

	void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException;

	String toString();

	byte getId();

	static Tag newTag(byte b) {
		switch (b) {
			case 0:
				return new EndTag();
			case 1:
				return new ByteTag();
			case 2:
				return new ShortTag();
			case 3:
				return new IntTag();
			case 4:
				return new LongTag();
			case 5:
				return new FloatTag();
			case 6:
				return new DoubleTag();
			case 7:
				return new ByteArrayTag();
			case 8:
				return new StringTag();
			case 9:
				return new ListTag();
			case 10:
				return new CompoundTag();
			case 11:
				return new IntArrayTag();
			case 12:
				return new LongArrayTag();
			default:
				return null;
		}
	}

	static String getTagTypeName(int i) {
		switch (i) {
			case 0:
				return "TAG_End";
			case 1:
				return "TAG_Byte";
			case 2:
				return "TAG_Short";
			case 3:
				return "TAG_Int";
			case 4:
				return "TAG_Long";
			case 5:
				return "TAG_Float";
			case 6:
				return "TAG_Double";
			case 7:
				return "TAG_Byte_Array";
			case 8:
				return "TAG_String";
			case 9:
				return "TAG_List";
			case 10:
				return "TAG_Compound";
			case 11:
				return "TAG_Int_Array";
			case 12:
				return "TAG_Long_Array";
			case 99:
				return "Any Numeric Tag";
			default:
				return "UNKNOWN";
		}
	}

	Tag copy();

	default String getAsString() {
		return this.toString();
	}

	default Component getPrettyDisplay() {
		return this.getPrettyDisplay("", 0);
	}

	Component getPrettyDisplay(String string, int i);
}
