/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class UploadInfo
extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern URI_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
    private final boolean worldClosed;
    @Nullable
    private final String token;
    private final URI uploadEndpoint;

    private UploadInfo(boolean bl, @Nullable String string, URI uRI) {
        this.worldClosed = bl;
        this.token = string;
        this.uploadEndpoint = uRI;
    }

    @Nullable
    public static UploadInfo parse(String string) {
        try {
            int i;
            URI uRI;
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
            String string2 = JsonUtils.getStringOr("uploadEndpoint", jsonObject, null);
            if (string2 != null && (uRI = UploadInfo.assembleUri(string2, i = JsonUtils.getIntOr("port", jsonObject, -1))) != null) {
                boolean bl = JsonUtils.getBooleanOr("worldClosed", jsonObject, false);
                String string3 = JsonUtils.getStringOr("token", jsonObject, null);
                return new UploadInfo(bl, string3, uRI);
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse UploadInfo: " + exception.getMessage());
        }
        return null;
    }

    @Nullable
    @VisibleForTesting
    public static URI assembleUri(String string, int i) {
        Matcher matcher = URI_SCHEMA_PATTERN.matcher(string);
        String string2 = UploadInfo.ensureEndpointSchema(string, matcher);
        try {
            URI uRI = new URI(string2);
            int j = UploadInfo.selectPortOrDefault(i, uRI.getPort());
            if (j != uRI.getPort()) {
                return new URI(uRI.getScheme(), uRI.getUserInfo(), uRI.getHost(), j, uRI.getPath(), uRI.getQuery(), uRI.getFragment());
            }
            return uRI;
        } catch (URISyntaxException uRISyntaxException) {
            LOGGER.warn("Failed to parse URI {}", (Object)string2, (Object)uRISyntaxException);
            return null;
        }
    }

    private static int selectPortOrDefault(int i, int j) {
        if (i != -1) {
            return i;
        }
        if (j != -1) {
            return j;
        }
        return 8080;
    }

    private static String ensureEndpointSchema(String string, Matcher matcher) {
        if (matcher.find()) {
            return string;
        }
        return "http://" + string;
    }

    public static String createRequest(@Nullable String string) {
        JsonObject jsonObject = new JsonObject();
        if (string != null) {
            jsonObject.addProperty("token", string);
        }
        return jsonObject.toString();
    }

    @Nullable
    public String getToken() {
        return this.token;
    }

    public URI getUploadEndpoint() {
        return this.uploadEndpoint;
    }

    public boolean isWorldClosed() {
        return this.worldClosed;
    }
}

