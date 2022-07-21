package net.minecraft.client.multiplayer.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RollingMemoryChatLog implements ChatLog {
	private final LoggedChatEvent[] buffer;
	private int newestId = -1;
	private int oldestId = -1;

	public RollingMemoryChatLog(int i) {
		this.buffer = new LoggedChatEvent[i];
	}

	@Override
	public void push(LoggedChatEvent loggedChatEvent) {
		int i = this.nextId();
		this.buffer[this.index(i)] = loggedChatEvent;
	}

	private int nextId() {
		int i = ++this.newestId;
		if (i >= this.buffer.length) {
			this.oldestId++;
		} else {
			this.oldestId = 0;
		}

		return i;
	}

	@Nullable
	@Override
	public LoggedChatEvent lookup(int i) {
		return this.contains(i) ? this.buffer[this.index(i)] : null;
	}

	private int index(int i) {
		return i % this.buffer.length;
	}

	@Override
	public boolean contains(int i) {
		return i >= this.oldestId && i <= this.newestId;
	}

	@Override
	public int offset(int i, int j) {
		int k = i + j;
		return this.contains(k) ? k : -1;
	}

	@Override
	public int newest() {
		return this.newestId;
	}

	@Override
	public int oldest() {
		return this.oldestId;
	}
}
