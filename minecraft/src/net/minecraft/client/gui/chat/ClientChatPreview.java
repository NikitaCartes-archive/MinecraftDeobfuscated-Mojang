package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ClientChatPreview {
	private static final long PREVIEW_VALID_AFTER_MS = 200L;
	@Nullable
	private String lastQuery;
	@Nullable
	private String scheduledRequest;
	private final ChatPreviewRequests requests;
	@Nullable
	private ClientChatPreview.Preview preview;

	public ClientChatPreview(Minecraft minecraft) {
		this.requests = new ChatPreviewRequests(minecraft);
	}

	public void tick() {
		String string = this.scheduledRequest;
		if (string != null && this.requests.trySendRequest(string, Util.getMillis())) {
			this.scheduledRequest = null;
		}
	}

	public void update(String string) {
		string = normalizeQuery(string);
		if (!string.isEmpty()) {
			if (!string.equals(this.lastQuery)) {
				this.lastQuery = string;
				this.sendOrScheduleRequest(string);
			}
		} else {
			this.clear();
		}
	}

	private void sendOrScheduleRequest(String string) {
		if (!this.requests.trySendRequest(string, Util.getMillis())) {
			this.scheduledRequest = string;
		} else {
			this.scheduledRequest = null;
		}
	}

	public void disable() {
		this.clear();
	}

	private void clear() {
		this.lastQuery = null;
		this.scheduledRequest = null;
		this.preview = null;
		this.requests.clear();
	}

	public void handleResponse(int i, @Nullable Component component) {
		String string = this.requests.handleResponse(i);
		if (string != null) {
			this.preview = new ClientChatPreview.Preview(Util.getMillis(), string, component);
		}
	}

	public boolean hasScheduledRequest() {
		return this.scheduledRequest != null || this.preview != null && !this.preview.isPreviewValid();
	}

	public boolean queryEquals(String string) {
		return normalizeQuery(string).equals(this.lastQuery);
	}

	@Nullable
	public ClientChatPreview.Preview peek() {
		return this.preview;
	}

	@Nullable
	public ClientChatPreview.Preview pull(String string) {
		if (this.preview != null && this.preview.canPull(string)) {
			ClientChatPreview.Preview preview = this.preview;
			this.preview = null;
			return preview;
		} else {
			return null;
		}
	}

	static String normalizeQuery(String string) {
		return StringUtils.normalizeSpace(string.trim());
	}

	@Environment(EnvType.CLIENT)
	public static record Preview(long receivedTimeStamp, String query, @Nullable Component response) {
		public Preview(long receivedTimeStamp, String query, @Nullable Component response) {
			query = ClientChatPreview.normalizeQuery(query);
			this.receivedTimeStamp = receivedTimeStamp;
			this.query = query;
			this.response = response;
		}

		private boolean queryEquals(String string) {
			return this.query.equals(ClientChatPreview.normalizeQuery(string));
		}

		boolean canPull(String string) {
			return this.queryEquals(string) ? this.isPreviewValid() : false;
		}

		boolean isPreviewValid() {
			long l = this.receivedTimeStamp + 200L;
			return Util.getMillis() >= l;
		}
	}
}
