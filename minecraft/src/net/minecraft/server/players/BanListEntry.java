package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public abstract class BanListEntry<T> extends StoredUserEntry<T> {
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	public static final String EXPIRES_NEVER = "forever";
	protected final Date created;
	protected final String source;
	@Nullable
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

		Date date;
		try {
			date = jsonObject.has("created") ? DATE_FORMAT.parse(jsonObject.get("created").getAsString()) : new Date();
		} catch (ParseException var7) {
			date = new Date();
		}

		this.created = date;
		this.source = jsonObject.has("source") ? jsonObject.get("source").getAsString() : "(Unknown)";

		Date date2;
		try {
			date2 = jsonObject.has("expires") ? DATE_FORMAT.parse(jsonObject.get("expires").getAsString()) : null;
		} catch (ParseException var6) {
			date2 = null;
		}

		this.expires = date2;
		this.reason = jsonObject.has("reason") ? jsonObject.get("reason").getAsString() : "Banned by an operator.";
	}

	public Date getCreated() {
		return this.created;
	}

	public String getSource() {
		return this.source;
	}

	@Nullable
	public Date getExpires() {
		return this.expires;
	}

	public String getReason() {
		return this.reason;
	}

	public abstract Component getDisplayName();

	@Override
	boolean hasExpired() {
		return this.expires == null ? false : this.expires.before(new Date());
	}

	@Override
	protected void serialize(JsonObject jsonObject) {
		jsonObject.addProperty("created", DATE_FORMAT.format(this.created));
		jsonObject.addProperty("source", this.source);
		jsonObject.addProperty("expires", this.expires == null ? "forever" : DATE_FORMAT.format(this.expires));
		jsonObject.addProperty("reason", this.reason);
	}
}
