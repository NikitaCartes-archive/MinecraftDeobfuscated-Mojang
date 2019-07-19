package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class EndTag implements Tag {
	@Override
	public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBits(64L);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
	}

	@Override
	public byte getId() {
		return 0;
	}

	@Override
	public String toString() {
		return "END";
	}

	public EndTag copy() {
		return new EndTag();
	}

	@Override
	public Component getPrettyDisplay(String string, int i) {
		return new TextComponent("");
	}

	public boolean equals(Object object) {
		return object instanceof EndTag;
	}

	public int hashCode() {
		return this.getId();
	}
}
