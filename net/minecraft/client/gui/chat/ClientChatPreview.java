/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientChatPreview {
    private static final long MIN_REQUEST_INTERVAL_MS = 100L;
    private static final long MAX_REQUEST_INTERVAL_MS = 1000L;
    private static final long PREVIEW_VALID_AFTER_MS = 200L;
    private final Minecraft minecraft;
    private final QueryIdGenerator queryIdGenerator = new QueryIdGenerator();
    @Nullable
    private PendingPreview scheduledPreview;
    @Nullable
    private PendingPreview pendingPreview;
    private long lastRequestTime;
    @Nullable
    private Preview preview;

    public ClientChatPreview(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void tick() {
        PendingPreview pendingPreview = this.scheduledPreview;
        if (pendingPreview == null) {
            return;
        }
        long l = Util.getMillis();
        if (this.isRequestReady(l)) {
            this.sendRequest(pendingPreview, l);
            this.scheduledPreview = null;
        }
    }

    private void sendRequest(PendingPreview pendingPreview, long l) {
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
        }
        if (l >= this.getEarliestNextRequest()) {
            return this.pendingPreview == null || l >= this.getLatestNextRequest();
        }
        return false;
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
        PendingPreview pendingPreview;
        if ((string = ClientChatPreview.normalizeQuery(string)).isEmpty()) {
            this.preview = new Preview(Util.getMillis(), string, null);
            this.scheduledPreview = null;
            this.pendingPreview = null;
            return;
        }
        PendingPreview pendingPreview2 = pendingPreview = this.scheduledPreview != null ? this.scheduledPreview : this.pendingPreview;
        if (pendingPreview == null || !pendingPreview.matches(string)) {
            this.scheduledPreview = new PendingPreview(this.queryIdGenerator.next(), string);
        }
    }

    public void handleResponse(int i, @Nullable Component component) {
        if (this.scheduledPreview == null && this.pendingPreview == null) {
            return;
        }
        if (this.pendingPreview != null && this.pendingPreview.matches(i)) {
            Component component2 = component != null ? component : Component.literal(this.pendingPreview.query());
            this.preview = new Preview(Util.getMillis(), this.pendingPreview.query(), component2);
            this.pendingPreview = null;
        } else {
            this.preview = null;
        }
    }

    @Nullable
    public Component peek() {
        return Util.mapNullable(this.preview, Preview::response);
    }

    @Nullable
    public Component pull(String string) {
        if (this.preview != null && this.preview.canPull(string)) {
            Component component = this.preview.response();
            this.preview = null;
            return component;
        }
        return null;
    }

    public boolean isActive() {
        return this.preview != null || this.scheduledPreview != null || this.pendingPreview != null;
    }

    static String normalizeQuery(String string) {
        return StringUtils.normalizeSpace(string.trim());
    }

    @Environment(value=EnvType.CLIENT)
    static class QueryIdGenerator {
        private static final int MAX_STEP = 100;
        private final RandomSource random = RandomSource.createNewThreadLocalInstance();
        private int lastId;

        QueryIdGenerator() {
        }

        public int next() {
            int i;
            this.lastId = i = this.lastId + this.random.nextInt(100);
            return i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record PendingPreview(int id, String query) {
        public boolean matches(int i) {
            return this.id == i;
        }

        public boolean matches(String string) {
            return this.query.equals(string);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Preview(long receivedTimeStamp, String query, @Nullable Component response) {
        public Preview {
            string = ClientChatPreview.normalizeQuery(string);
        }

        public boolean canPull(String string) {
            if (this.query.equals(ClientChatPreview.normalizeQuery(string))) {
                long l = this.receivedTimeStamp + 200L;
                return Util.getMillis() >= l;
            }
            return false;
        }

        @Nullable
        public Component response() {
            return this.response;
        }
    }
}

