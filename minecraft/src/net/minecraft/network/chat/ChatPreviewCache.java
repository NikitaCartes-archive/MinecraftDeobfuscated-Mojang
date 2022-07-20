package net.minecraft.network.chat;

import javax.annotation.Nullable;

public class ChatPreviewCache {
	@Nullable
	private ChatPreviewCache.Result result;

	public void set(String string, Component component) {
		this.result = new ChatPreviewCache.Result(string, component);
	}

	@Nullable
	public Component pull(String string) {
		ChatPreviewCache.Result result = this.result;
		if (result != null && result.matches(string)) {
			this.result = null;
			return result.preview();
		} else {
			return null;
		}
	}

	static record Result(String query, Component preview) {
		public boolean matches(String string) {
			return this.query.equals(string);
		}
	}
}
