package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class ChatPreviewRequests {
	private static final long MIN_REQUEST_INTERVAL_MS = 100L;
	private static final long MAX_REQUEST_INTERVAL_MS = 1000L;
	private final Minecraft minecraft;
	private final ChatPreviewRequests.QueryIdGenerator queryIdGenerator = new ChatPreviewRequests.QueryIdGenerator();
	@Nullable
	private ChatPreviewRequests.PendingPreview pending;
	private long lastRequestTime;

	public ChatPreviewRequests(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public boolean trySendRequest(String string, long l) {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener == null) {
			this.clear();
			return true;
		} else if (this.pending != null && this.pending.matches(string)) {
			return true;
		} else if (this.isRequestReady(l)) {
			ChatPreviewRequests.PendingPreview pendingPreview = new ChatPreviewRequests.PendingPreview(this.queryIdGenerator.next(), string);
			this.pending = pendingPreview;
			this.lastRequestTime = l;
			clientPacketListener.send(new ServerboundChatPreviewPacket(pendingPreview.id(), pendingPreview.query()));
			return true;
		} else {
			return false;
		}
	}

	@Nullable
	public String handleResponse(int i) {
		if (this.pending != null && this.pending.matches(i)) {
			String string = this.pending.query;
			this.pending = null;
			return string;
		} else {
			return null;
		}
	}

	private boolean isRequestReady(long l) {
		long m = this.lastRequestTime + 100L;
		if (l < m) {
			return false;
		} else {
			long n = this.lastRequestTime + 1000L;
			return this.pending == null || l >= n;
		}
	}

	public void clear() {
		this.pending = null;
		this.lastRequestTime = 0L;
	}

	public boolean isPending() {
		return this.pending != null;
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
