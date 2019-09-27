package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface Tag {
	ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
	ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
	ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
	ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;

	void write(DataOutput dataOutput) throws IOException;

	String toString();

	byte getId();

	TagType<?> getType();

	Tag copy();

	default String getAsString() {
		return this.toString();
	}

	default Component getPrettyDisplay() {
		return this.getPrettyDisplay("", 0);
	}

	Component getPrettyDisplay(String string, int i);
}
