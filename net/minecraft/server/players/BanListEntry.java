/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.StoredUserEntry;
import org.jetbrains.annotations.Nullable;

public abstract class BanListEntry<T>
extends StoredUserEntry<T> {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    protected final Date created;
    protected final String source;
    protected final Date expires;
    protected final String reason;

    public BanListEntry(T object, @Nullable Date date, @Nullable String string, @Nullable Date date2, @Nullable String string2) {
        super(object);
        this.created = date == null ? new Date() : date;
        this.source = string == null ? "(Unknown)" : string;
        this.expires = date2;
        this.reason = string2 == null ? "Banned by an operator." : string2;
    }

    protected BanListEntry(T object, JsonObject jsonObject) {
        super(object);
        Date date2;
        Date date;
        try {
            date = jsonObject.has("created") ? DATE_FORMAT.parse(jsonObject.get("created").getAsString()) : new Date();
        } catch (ParseException parseException) {
            date = new Date();
        }
        this.created = date;
        this.source = jsonObject.has("source") ? jsonObject.get("source").getAsString() : "(Unknown)";
        try {
            date2 = jsonObject.has("expires") ? DATE_FORMAT.parse(jsonObject.get("expires").getAsString()) : null;
        } catch (ParseException parseException2) {
            date2 = null;
        }
        this.expires = date2;
        this.reason = jsonObject.has("reason") ? jsonObject.get("reason").getAsString() : "Banned by an operator.";
    }

    public String getSource() {
        return this.source;
    }

    public Date getExpires() {
        return this.expires;
    }

    public String getReason() {
        return this.reason;
    }

    public abstract Component getDisplayName();

    @Override
    boolean hasExpired() {
        if (this.expires == null) {
            return false;
        }
        return this.expires.before(new Date());
    }

    @Override
    protected void serialize(JsonObject jsonObject) {
        jsonObject.addProperty("created", DATE_FORMAT.format(this.created));
        jsonObject.addProperty("source", this.source);
        jsonObject.addProperty("expires", this.expires == null ? "forever" : DATE_FORMAT.format(this.expires));
        jsonObject.addProperty("reason", this.reason);
    }
}

