package com.mojang.realmsclient;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class KeyCombo {
	private final char[] chars;
	private int matchIndex;
	private final Runnable onCompletion;

	public KeyCombo(char[] cs, Runnable runnable) {
		this.onCompletion = runnable;
		if (cs.length < 1) {
			throw new IllegalArgumentException("Must have at least one char");
		} else {
			this.chars = cs;
		}
	}

	public KeyCombo(char[] cs) {
		this(cs, () -> {
		});
	}

	public boolean keyPressed(char c) {
		if (c == this.chars[this.matchIndex++]) {
			if (this.matchIndex == this.chars.length) {
				this.reset();
				this.onCompletion.run();
				return true;
			}
		} else {
			this.reset();
		}

		return false;
	}

	public void reset() {
		this.matchIndex = 0;
	}

	public String toString() {
		return "KeyCombo{chars=" + Arrays.toString(this.chars) + ", matchIndex=" + this.matchIndex + '}';
	}
}
