/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.ProcessorMailbox;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TextFilterClient
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
        return thread;
    };
    private static final String DEFAULT_ENDPOINT = "v1/chat";
    private final URL chatEndpoint;
    private final MessageEncoder chatEncoder;
    final URL joinEndpoint;
    final JoinOrLeaveEncoder joinEncoder;
    final URL leaveEndpoint;
    final JoinOrLeaveEncoder leaveEncoder;
    private final String authKey;
    final IgnoreStrategy chatIgnoreStrategy;
    final ExecutorService workerPool;

    private TextFilterClient(URL uRL, MessageEncoder messageEncoder, URL uRL2, JoinOrLeaveEncoder joinOrLeaveEncoder, URL uRL3, JoinOrLeaveEncoder joinOrLeaveEncoder2, String string, IgnoreStrategy ignoreStrategy, int i) {
        this.authKey = string;
        this.chatIgnoreStrategy = ignoreStrategy;
        this.chatEndpoint = uRL;
        this.chatEncoder = messageEncoder;
        this.joinEndpoint = uRL2;
        this.joinEncoder = joinOrLeaveEncoder;
        this.leaveEndpoint = uRL3;
        this.leaveEncoder = joinOrLeaveEncoder2;
        this.workerPool = Executors.newFixedThreadPool(i, THREAD_FACTORY);
    }

    private static URL getEndpoint(URI uRI, @Nullable JsonObject jsonObject, String string, String string2) throws MalformedURLException {
        String string3 = TextFilterClient.getEndpointFromConfig(jsonObject, string, string2);
        return uRI.resolve("/" + string3).toURL();
    }

    private static String getEndpointFromConfig(@Nullable JsonObject jsonObject, String string, String string2) {
        return jsonObject != null ? GsonHelper.getAsString(jsonObject, string, string2) : string2;
    }

    @Nullable
    public static TextFilterClient createFromConfig(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return null;
        }
        try {
            MessageEncoder messageEncoder;
            JsonObject jsonObject = GsonHelper.parse(string);
            URI uRI = new URI(GsonHelper.getAsString(jsonObject, "apiServer"));
            String string2 = GsonHelper.getAsString(jsonObject, "apiKey");
            if (string2.isEmpty()) {
                throw new IllegalArgumentException("Missing API key");
            }
            int i = GsonHelper.getAsInt(jsonObject, "ruleId", 1);
            String string32 = GsonHelper.getAsString(jsonObject, "serverId", "");
            String string42 = GsonHelper.getAsString(jsonObject, "roomId", "Java:Chat");
            int j = GsonHelper.getAsInt(jsonObject, "hashesToDrop", -1);
            int k = GsonHelper.getAsInt(jsonObject, "maxConcurrentRequests", 7);
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "endpoints", null);
            String string5 = TextFilterClient.getEndpointFromConfig(jsonObject2, "chat", DEFAULT_ENDPOINT);
            boolean bl = string5.equals(DEFAULT_ENDPOINT);
            URL uRL = uRI.resolve("/" + string5).toURL();
            URL uRL2 = TextFilterClient.getEndpoint(uRI, jsonObject2, "join", "v1/join");
            URL uRL3 = TextFilterClient.getEndpoint(uRI, jsonObject2, "leave", "v1/leave");
            JoinOrLeaveEncoder joinOrLeaveEncoder = gameProfile -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("server", string32);
                jsonObject.addProperty("room", string42);
                jsonObject.addProperty("user_id", gameProfile.getId().toString());
                jsonObject.addProperty("user_display_name", gameProfile.getName());
                return jsonObject;
            };
            if (bl) {
                messageEncoder = (gameProfile, string3) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rule", i);
                    jsonObject.addProperty("server", string32);
                    jsonObject.addProperty("room", string42);
                    jsonObject.addProperty("player", gameProfile.getId().toString());
                    jsonObject.addProperty("player_display_name", gameProfile.getName());
                    jsonObject.addProperty("text", string3);
                    jsonObject.addProperty("language", "*");
                    return jsonObject;
                };
            } else {
                String string6 = String.valueOf(i);
                messageEncoder = (gameProfile, string4) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rule_id", string6);
                    jsonObject.addProperty("category", string32);
                    jsonObject.addProperty("subcategory", string42);
                    jsonObject.addProperty("user_id", gameProfile.getId().toString());
                    jsonObject.addProperty("user_display_name", gameProfile.getName());
                    jsonObject.addProperty("text", string4);
                    jsonObject.addProperty("language", "*");
                    return jsonObject;
                };
            }
            IgnoreStrategy ignoreStrategy = IgnoreStrategy.select(j);
            String string7 = Base64.getEncoder().encodeToString(string2.getBytes(StandardCharsets.US_ASCII));
            return new TextFilterClient(uRL, messageEncoder, uRL2, joinOrLeaveEncoder, uRL3, joinOrLeaveEncoder, string7, ignoreStrategy, k);
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse chat filter config {}", (Object)string, (Object)exception);
            return null;
        }
    }

    void processJoinOrLeave(GameProfile gameProfile, URL uRL, JoinOrLeaveEncoder joinOrLeaveEncoder, Executor executor) {
        executor.execute(() -> {
            JsonObject jsonObject = joinOrLeaveEncoder.encode(gameProfile);
            try {
                this.processRequest(jsonObject, uRL);
            } catch (Exception exception) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", uRL, gameProfile, exception);
            }
        });
    }

    CompletableFuture<FilteredText<String>> requestMessageProcessing(GameProfile gameProfile, String string, IgnoreStrategy ignoreStrategy, Executor executor) {
        if (string.isEmpty()) {
            return CompletableFuture.completedFuture(FilteredText.EMPTY_STRING);
        }
        return CompletableFuture.supplyAsync(() -> {
            JsonObject jsonObject = this.chatEncoder.encode(gameProfile, string);
            try {
                JsonObject jsonObject2 = this.processRequestResponse(jsonObject, this.chatEndpoint);
                boolean bl = GsonHelper.getAsBoolean(jsonObject2, "response", false);
                if (bl) {
                    return FilteredText.passThrough(string);
                }
                String string2 = GsonHelper.getAsString(jsonObject2, "hashed", null);
                if (string2 == null) {
                    return FilteredText.fullyFiltered(string);
                }
                int i = GsonHelper.getAsJsonArray(jsonObject2, "hashes").size();
                return ignoreStrategy.shouldIgnore(string2, i) ? FilteredText.fullyFiltered(string) : new FilteredText<String>(string, string2);
            } catch (Exception exception) {
                LOGGER.warn("Failed to validate message '{}'", (Object)string, (Object)exception);
                return FilteredText.fullyFiltered(string);
            }
        }, executor);
    }

    @Override
    public void close() {
        this.workerPool.shutdownNow();
    }

    private void drainStream(InputStream inputStream) throws IOException {
        byte[] bs = new byte[1024];
        while (inputStream.read(bs) != -1) {
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonObject processRequestResponse(JsonObject jsonObject, URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
        try (InputStream inputStream = httpURLConnection.getInputStream();){
            JsonObject jsonObject2;
            if (httpURLConnection.getResponseCode() == 204) {
                JsonObject jsonObject3 = new JsonObject();
                return jsonObject3;
            }
            try {
                jsonObject2 = Streams.parse(new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))).getAsJsonObject();
            } catch (Throwable throwable) {
                this.drainStream(inputStream);
                throw throwable;
            }
            this.drainStream(inputStream);
            return jsonObject2;
        }
    }

    private void processRequest(JsonObject jsonObject, URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
        try (InputStream inputStream = httpURLConnection.getInputStream();){
            this.drainStream(inputStream);
        }
    }

    private HttpURLConnection makeRequest(JsonObject jsonObject, URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(2000);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Basic " + this.authKey);
        httpURLConnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().getName());
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), StandardCharsets.UTF_8);
             JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);){
            Streams.write(jsonObject, jsonWriter);
        }
        int i = httpURLConnection.getResponseCode();
        if (i < 200 || i >= 300) {
            throw new RequestFailedException(i + " " + httpURLConnection.getResponseMessage());
        }
        return httpURLConnection;
    }

    public TextFilter createContext(GameProfile gameProfile) {
        return new PlayerContext(gameProfile);
    }

    @FunctionalInterface
    public static interface IgnoreStrategy {
        public static final IgnoreStrategy NEVER_IGNORE = (string, i) -> false;
        public static final IgnoreStrategy IGNORE_FULLY_FILTERED = (string, i) -> string.length() == i;

        public static IgnoreStrategy ignoreOverThreshold(int i) {
            return (string, j) -> j >= i;
        }

        public static IgnoreStrategy select(int i) {
            return switch (i) {
                case -1 -> NEVER_IGNORE;
                case 0 -> IGNORE_FULLY_FILTERED;
                default -> IgnoreStrategy.ignoreOverThreshold(i);
            };
        }

        public boolean shouldIgnore(String var1, int var2);
    }

    @FunctionalInterface
    static interface MessageEncoder {
        public JsonObject encode(GameProfile var1, String var2);
    }

    @FunctionalInterface
    static interface JoinOrLeaveEncoder {
        public JsonObject encode(GameProfile var1);
    }

    public static class RequestFailedException
    extends RuntimeException {
        RequestFailedException(String string) {
            super(string);
        }
    }

    class PlayerContext
    implements TextFilter {
        private final GameProfile profile;
        private final Executor streamExecutor;

        PlayerContext(GameProfile gameProfile) {
            this.profile = gameProfile;
            ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(TextFilterClient.this.workerPool, "chat stream for " + gameProfile.getName());
            this.streamExecutor = processorMailbox::tell;
        }

        @Override
        public void join() {
            TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.joinEndpoint, TextFilterClient.this.joinEncoder, this.streamExecutor);
        }

        @Override
        public void leave() {
            TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.leaveEndpoint, TextFilterClient.this.leaveEncoder, this.streamExecutor);
        }

        @Override
        public CompletableFuture<List<FilteredText<String>>> processMessageBundle(List<String> list) {
            List list2 = list.stream().map(string -> TextFilterClient.this.requestMessageProcessing(this.profile, (String)string, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor)).collect(ImmutableList.toImmutableList());
            return Util.sequenceFailFast(list2).exceptionally(throwable -> ImmutableList.of());
        }

        @Override
        public CompletableFuture<FilteredText<String>> processStreamMessage(String string) {
            return TextFilterClient.this.requestMessageProcessing(this.profile, string, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor);
        }
    }
}

