package net.minecraft.network.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum ChatType {
	CHAT((byte)0, false),
	SYSTEM((byte)1, true),
	GAME_INFO((byte)2, true);

	private final byte index;
	private final boolean interrupt;

	private ChatType(byte b, boolean bl) {
		this.index = b;
		this.interrupt = bl;
	}

	public byte getIndex() {
		return this.index;
	}

	public static ChatType getForIndex(byte b) {
		for (ChatType chatType : values()) {
			if (b == chatType.index) {
				return chatType;
			}
		}

		return CHAT;
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldInterrupt() {
		return this.interrupt;
	}
}
