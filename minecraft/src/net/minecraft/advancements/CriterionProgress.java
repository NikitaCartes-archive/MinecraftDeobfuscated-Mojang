package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class CriterionProgress {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
	@Nullable
	private Date obtained;

	public boolean isDone() {
		return this.obtained != null;
	}

	public void grant() {
		this.obtained = new Date();
	}

	public void revoke() {
		this.obtained = null;
	}

	@Nullable
	public Date getObtained() {
		return this.obtained;
	}

	public String toString() {
		return "CriterionProgress{obtained=" + (this.obtained == null ? "false" : this.obtained) + "}";
	}

	public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeNullable(this.obtained, FriendlyByteBuf::writeDate);
	}

	public JsonElement serializeToJson() {
		return (JsonElement)(this.obtained != null ? new JsonPrimitive(DATE_FORMAT.format(this.obtained)) : JsonNull.INSTANCE);
	}

	public static CriterionProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		CriterionProgress criterionProgress = new CriterionProgress();
		criterionProgress.obtained = friendlyByteBuf.readNullable(FriendlyByteBuf::readDate);
		return criterionProgress;
	}

	public static CriterionProgress fromJson(String string) {
		CriterionProgress criterionProgress = new CriterionProgress();

		try {
			criterionProgress.obtained = DATE_FORMAT.parse(string);
			return criterionProgress;
		} catch (ParseException var3) {
			throw new JsonSyntaxException("Invalid datetime: " + string, var3);
		}
	}
}
