/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.ChatPreviewRequests;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientChatPreview {
    private static final long PREVIEW_VALID_AFTER_MS = 200L;
    private boolean enabled;
    @Nullable
    private String lastQuery;
    @Nullable
    private String scheduledRequest;
    private final ChatPreviewRequests requests;
    @Nullable
    private Preview preview;

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
        this.enabled = true;
        if (!(string = ClientChatPreview.normalizeQuery(string)).isEmpty()) {
            if (!string.equals(this.lastQuery)) {
                this.lastQuery = string;
                this.sendOrScheduleRequest(string);
            }
        } else {
            this.clear();
        }
    }

    private void sendOrScheduleRequest(String string) {
        this.scheduledRequest = !this.requests.trySendRequest(string, Util.getMillis()) ? string : null;
    }

    public void disable() {
        this.enabled = false;
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
            Component component2 = component != null ? component : Component.literal(string);
            this.preview = new Preview(Util.getMillis(), string, component2);
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

    public boolean isEnabled() {
        return this.enabled;
    }

    static String normalizeQuery(String string) {
        return StringUtils.normalizeSpace(string.trim());
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

