/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.players.BanListEntry;
import org.jetbrains.annotations.Nullable;

public class IpBanListEntry
extends BanListEntry<String> {
    public IpBanListEntry(String string) {
        this(string, (Date)null, (String)null, (Date)null, (String)null);
    }

    public IpBanListEntry(String string, @Nullable Date date, @Nullable String string2, @Nullable Date date2, @Nullable String string3) {
        super(string, date, string2, date2, string3);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent((String)this.getUser());
    }

    public IpBanListEntry(JsonObject jsonObject) {
        super(IpBanListEntry.createIpInfo(jsonObject), jsonObject);
    }

    private static String createIpInfo(JsonObject jsonObject) {
        return jsonObject.has("ip") ? jsonObject.get("ip").getAsString() : null;
    }

    @Override
    protected void serialize(JsonObject jsonObject) {
        if (this.getUser() == null) {
            return;
        }
        jsonObject.addProperty("ip", (String)this.getUser());
        super.serialize(jsonObject);
    }
}

