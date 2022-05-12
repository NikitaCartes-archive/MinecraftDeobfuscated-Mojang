package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ClientChatPreview {
	private static final long MIN_REQUEST_INTERVAL_MS = 100L;
	private static final long MAX_REQUEST_INTERVAL_MS = 1000L;
	private static final long PREVIEW_VALID_AFTER_MS = 200L;
	private final Minecraft minecraft;
	private final ClientChatPreview.QueryIdGenerator queryIdGenerator = new ClientChatPreview.QueryIdGenerator();
	@Nullable
	private ClientChatPreview.PendingPreview scheduledPreview;
	@Nullable
	private ClientChatPreview.PendingPreview pendingPreview;
	private long lastRequestTime;
	@Nullable
	private ClientChatPreview.Preview preview;

	public ClientChatPreview(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void tick() {
		ClientChatPreview.PendingPreview pendingPreview = this.scheduledPreview;
		if (pendingPreview != null) {
			long l = Util.getMillis();
			if (this.isRequestReady(l)) {
				this.sendRequest(pendingPreview, l);
				this.scheduledPreview = null;
			}
		}
	}

	private void sendRequest(ClientChatPreview.PendingPreview pendingPreview, long l) {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.send(new ServerboundChatPreviewPacket(pendingPreview.id(), pendingPreview.query()));
			this.pendingPreview = pendingPreview;
		} else {
			this.pendingPreview = null;
		}

		this.lastRequestTime = l;
	}

	private boolean isRequestReady(long l) {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener == null) {
			return true;
		} else {
			return l < this.getEarliestNextRequest() ? false : this.pendingPreview == null || l >= this.getLatestNextRequest();
		}
	}

	private long getEarliestNextRequest() {
		return this.lastRequestTime + 100L;
	}

	private long getLatestNextRequest() {
		return this.lastRequestTime + 1000L;
	}

	public void clear() {
		this.preview = null;
		this.scheduledPreview = null;
		this.pendingPreview = null;
	}

	public void request(String string) {
		string = normalizeQuery(string);
		if (string.isEmpty()) {
			this.preview = new ClientChatPreview.Preview(Util.getMillis(), string, null);
			this.scheduledPreview = null;
			this.pendingPreview = null;
		} else {
			ClientChatPreview.PendingPreview pendingPreview = this.scheduledPreview != null ? this.scheduledPreview : this.pendingPreview;
			if (pendingPreview == null || !pendingPreview.matches(string)) {
				this.scheduledPreview = new ClientChatPreview.PendingPreview(this.queryIdGenerator.next(), string);
			}
		}
	}

	public void handleResponse(int i, @Nullable Component component) {
		if (this.scheduledPreview != null || this.pendingPreview != null) {
			if (this.pendingPreview != null && this.pendingPreview.matches(i)) {
				Component component2 = (Component)(component != null ? component : Component.literal(this.pendingPreview.query()));
				this.preview = new ClientChatPreview.Preview(Util.getMillis(), this.pendingPreview.query(), component2);
				this.pendingPreview = null;
			} else {
				this.preview = null;
			}
		}
	}

	@Nullable
	public Component peek() {
		return Util.mapNullable(this.preview, ClientChatPreview.Preview::response);
	}

	@Nullable
	public Component pull(String string) {
		if (this.preview != null && this.preview.canPull(string)) {
			Component component = this.preview.response();
			this.preview = null;
			return component;
		} else {
			return null;
		}
	}

	public boolean isActive() {
		return this.preview != null || this.scheduledPreview != null || this.pendingPreview != null;
	}

	static String normalizeQuery(String string) {
		return StringUtils.normalizeSpace(string.trim());
	}

	@Environment(EnvType.CLIENT)
	static record PendingPreview(int id, String query) {
		public boolean matches(int i) {
			return this.id == i;
		}

		public boolean matches(String string) {
			return this.query.equals(string);
		}
	}

	@Environment(EnvType.CLIENT)
	static record Preview(long receivedTimeStamp, String query, @Nullable Component response) {
		public Preview(long receivedTimeStamp, String query, @Nullable Component response) {
			query = ClientChatPreview.normalizeQuery(query);
			this.receivedTimeStamp = receivedTimeStamp;
			this.query = query;
			this.response = response;
		}

		public boolean canPull(String string) {
			if (this.query.equals(ClientChatPreview.normalizeQuery(string))) {
				long l = this.receivedTimeStamp + 200L;
				return Util.getMillis() >= l;
			} else {
				return false;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class QueryIdGenerator {
		private static final int MAX_STEP = 100;
		private final RandomSource random = RandomSource.createNewThreadLocalInstance();
		private int lastId;

		public int next() {
			int i = this.lastId + this.random.nextInt(100);
			this.lastId = i;
			return i;
		}
	}
}
