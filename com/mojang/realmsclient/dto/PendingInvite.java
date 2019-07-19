/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PendingInvite
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public String invitationId;
    public String worldName;
    public String worldOwnerName;
    public String worldOwnerUuid;
    public Date date;

    public static PendingInvite parse(JsonObject jsonObject) {
        PendingInvite pendingInvite = new PendingInvite();
        try {
            pendingInvite.invitationId = JsonUtils.getStringOr("invitationId", jsonObject, "");
            pendingInvite.worldName = JsonUtils.getStringOr("worldName", jsonObject, "");
            pendingInvite.worldOwnerName = JsonUtils.getStringOr("worldOwnerName", jsonObject, "");
            pendingInvite.worldOwnerUuid = JsonUtils.getStringOr("worldOwnerUuid", jsonObject, "");
            pendingInvite.date = JsonUtils.getDateOr("date", jsonObject);
        } catch (Exception exception) {
            LOGGER.error("Could not parse PendingInvite: " + exception.getMessage());
        }
        return pendingInvite;
    }
}

