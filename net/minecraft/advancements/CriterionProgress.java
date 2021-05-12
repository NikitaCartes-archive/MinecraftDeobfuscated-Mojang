/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.network.FriendlyByteBuf;

public class CriterionProgress {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
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

    public Date getObtained() {
        return this.obtained;
    }

    public String toString() {
        return "CriterionProgress{obtained=" + (Serializable)(this.obtained == null ? "false" : this.obtained) + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.obtained != null);
        if (this.obtained != null) {
            friendlyByteBuf.writeDate(this.obtained);
        }
    }

    public JsonElement serializeToJson() {
        if (this.obtained != null) {
            return new JsonPrimitive(DATE_FORMAT.format(this.obtained));
        }
        return JsonNull.INSTANCE;
    }

    public static CriterionProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        CriterionProgress criterionProgress = new CriterionProgress();
        if (friendlyByteBuf.readBoolean()) {
            criterionProgress.obtained = friendlyByteBuf.readDate();
        }
        return criterionProgress;
    }

    public static CriterionProgress fromJson(String string) {
        CriterionProgress criterionProgress = new CriterionProgress();
        try {
            criterionProgress.obtained = DATE_FORMAT.parse(string);
        } catch (ParseException parseException) {
            throw new JsonSyntaxException("Invalid datetime: " + string, parseException);
        }
        return criterionProgress;
    }
}

