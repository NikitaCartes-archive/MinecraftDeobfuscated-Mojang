/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)AdvancementProgress.class), new AdvancementProgress.Serializer()).registerTypeAdapter((Type)((Object)ResourceLocation.class), new ResourceLocation.Serializer()).setPrettyPrinting().create();
    private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>(){};
    private final DataFixer dataFixer;
    private final PlayerList playerList;
    private final Path playerSavePath;
    private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<Advancement, AdvancementProgress>();
    private final Set<Advancement> visible = new HashSet<Advancement>();
    private final Set<Advancement> progressChanged = new HashSet<Advancement>();
    private final Set<Advancement> rootsToUpdate = new HashSet<Advancement>();
    private ServerPlayer player;
    @Nullable
    private Advancement lastSelectedTab;
    private boolean isFirstPacket = true;

    public PlayerAdvancements(DataFixer dataFixer, PlayerList playerList, ServerAdvancementManager serverAdvancementManager, Path path, ServerPlayer serverPlayer) {
        this.dataFixer = dataFixer;
        this.playerList = playerList;
        this.playerSavePath = path;
        this.player = serverPlayer;
        this.load(serverAdvancementManager);
    }

    public void setPlayer(ServerPlayer serverPlayer) {
        this.player = serverPlayer;
    }

    public void stopListening() {
        for (CriterionTrigger<?> criterionTrigger : CriteriaTriggers.all()) {
            criterionTrigger.removePlayerListeners(this);
        }
    }

    public void reload(ServerAdvancementManager serverAdvancementManager) {
        this.stopListening();
        this.progress.clear();
        this.visible.clear();
        this.rootsToUpdate.clear();
        this.progressChanged.clear();
        this.isFirstPacket = true;
        this.lastSelectedTab = null;
        this.load(serverAdvancementManager);
    }

    private void registerListeners(ServerAdvancementManager serverAdvancementManager) {
        for (Advancement advancement : serverAdvancementManager.getAllAdvancements()) {
            this.registerListeners(advancement);
        }
    }

    private void checkForAutomaticTriggers(ServerAdvancementManager serverAdvancementManager) {
        for (Advancement advancement : serverAdvancementManager.getAllAdvancements()) {
            if (!advancement.getCriteria().isEmpty()) continue;
            this.award(advancement, "");
            advancement.getRewards().grant(this.player);
        }
    }

    private void load(ServerAdvancementManager serverAdvancementManager) {
        if (Files.isRegularFile(this.playerSavePath, new LinkOption[0])) {
            try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8));){
                jsonReader.setLenient(false);
                Dynamic<JsonElement> dynamic = new Dynamic<JsonElement>(JsonOps.INSTANCE, Streams.parse(jsonReader));
                int i = dynamic.get("DataVersion").asInt(1343);
                dynamic = dynamic.remove("DataVersion");
                dynamic = DataFixTypes.ADVANCEMENTS.updateToCurrentVersion(this.dataFixer, dynamic, i);
                Map<ResourceLocation, AdvancementProgress> map = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(dynamic.getValue());
                if (map == null) {
                    throw new JsonParseException("Found null for advancements");
                }
                map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
                    Advancement advancement = serverAdvancementManager.getAdvancement((ResourceLocation)entry.getKey());
                    if (advancement == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", entry.getKey(), (Object)this.playerSavePath);
                        return;
                    }
                    this.startProgress(advancement, (AdvancementProgress)entry.getValue());
                    this.progressChanged.add(advancement);
                    this.markForVisibilityUpdate(advancement);
                });
            } catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse player advancements in {}", (Object)this.playerSavePath, (Object)jsonParseException);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't access player advancements in {}", (Object)this.playerSavePath, (Object)iOException);
            }
        }
        this.checkForAutomaticTriggers(serverAdvancementManager);
        this.registerListeners(serverAdvancementManager);
    }

    public void save() {
        LinkedHashMap<ResourceLocation, AdvancementProgress> map = new LinkedHashMap<ResourceLocation, AdvancementProgress>();
        for (Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            AdvancementProgress advancementProgress = entry.getValue();
            if (!advancementProgress.hasProgress()) continue;
            map.put(entry.getKey().getId(), advancementProgress);
        }
        JsonElement jsonElement = GSON.toJsonTree(map);
        jsonElement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        try {
            FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(jsonElement, (Appendable)writer);
            }
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save player advancements to {}", (Object)this.playerSavePath, (Object)iOException);
        }
    }

    public boolean award(Advancement advancement, String string) {
        boolean bl = false;
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
        boolean bl2 = advancementProgress.isDone();
        if (advancementProgress.grantProgress(string)) {
            this.unregisterListeners(advancement);
            this.progressChanged.add(advancement);
            bl = true;
            if (!bl2 && advancementProgress.isDone()) {
                advancement.getRewards().grant(this.player);
                if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat() && this.player.level.getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
                    this.playerList.broadcastSystemMessage(Component.translatable("chat.type.advancement." + advancement.getDisplay().getFrame().getName(), this.player.getDisplayName(), advancement.getChatComponent()), false);
                }
            }
        }
        if (!bl2 && advancementProgress.isDone()) {
            this.markForVisibilityUpdate(advancement);
        }
        return bl;
    }

    public boolean revoke(Advancement advancement, String string) {
        boolean bl = false;
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
        boolean bl2 = advancementProgress.isDone();
        if (advancementProgress.revokeProgress(string)) {
            this.registerListeners(advancement);
            this.progressChanged.add(advancement);
            bl = true;
        }
        if (bl2 && !advancementProgress.isDone()) {
            this.markForVisibilityUpdate(advancement);
        }
        return bl;
    }

    private void markForVisibilityUpdate(Advancement advancement) {
        this.rootsToUpdate.add(advancement.getRoot());
    }

    private void registerListeners(Advancement advancement) {
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
        if (advancementProgress.isDone()) {
            return;
        }
        for (Map.Entry<String, Criterion> entry : advancement.getCriteria().entrySet()) {
            CriterionTrigger<CriterionTriggerInstance> criterionTrigger;
            CriterionTriggerInstance criterionTriggerInstance;
            CriterionProgress criterionProgress = advancementProgress.getCriterion(entry.getKey());
            if (criterionProgress == null || criterionProgress.isDone() || (criterionTriggerInstance = entry.getValue().getTrigger()) == null || (criterionTrigger = CriteriaTriggers.getCriterion(criterionTriggerInstance.getCriterion())) == null) continue;
            criterionTrigger.addPlayerListener(this, new CriterionTrigger.Listener<CriterionTriggerInstance>(criterionTriggerInstance, advancement, entry.getKey()));
        }
    }

    private void unregisterListeners(Advancement advancement) {
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
        for (Map.Entry<String, Criterion> entry : advancement.getCriteria().entrySet()) {
            CriterionTrigger<CriterionTriggerInstance> criterionTrigger;
            CriterionTriggerInstance criterionTriggerInstance;
            CriterionProgress criterionProgress = advancementProgress.getCriterion(entry.getKey());
            if (criterionProgress == null || !criterionProgress.isDone() && !advancementProgress.isDone() || (criterionTriggerInstance = entry.getValue().getTrigger()) == null || (criterionTrigger = CriteriaTriggers.getCriterion(criterionTriggerInstance.getCriterion())) == null) continue;
            criterionTrigger.removePlayerListener(this, new CriterionTrigger.Listener<CriterionTriggerInstance>(criterionTriggerInstance, advancement, entry.getKey()));
        }
    }

    public void flushDirty(ServerPlayer serverPlayer) {
        if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
            HashMap<ResourceLocation, AdvancementProgress> map = new HashMap<ResourceLocation, AdvancementProgress>();
            HashSet<Advancement> set = new HashSet<Advancement>();
            HashSet<ResourceLocation> set2 = new HashSet<ResourceLocation>();
            for (Advancement advancement : this.rootsToUpdate) {
                this.updateTreeVisibility(advancement, set, set2);
            }
            this.rootsToUpdate.clear();
            for (Advancement advancement : this.progressChanged) {
                if (!this.visible.contains(advancement)) continue;
                map.put(advancement.getId(), this.progress.get(advancement));
            }
            this.progressChanged.clear();
            if (!(map.isEmpty() && set.isEmpty() && set2.isEmpty())) {
                serverPlayer.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, set, set2, map));
            }
        }
        this.isFirstPacket = false;
    }

    public void setSelectedTab(@Nullable Advancement advancement) {
        Advancement advancement2 = this.lastSelectedTab;
        this.lastSelectedTab = advancement != null && advancement.getParent() == null && advancement.getDisplay() != null ? advancement : null;
        if (advancement2 != this.lastSelectedTab) {
            this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.getId()));
        }
    }

    public AdvancementProgress getOrStartProgress(Advancement advancement) {
        AdvancementProgress advancementProgress = this.progress.get(advancement);
        if (advancementProgress == null) {
            advancementProgress = new AdvancementProgress();
            this.startProgress(advancement, advancementProgress);
        }
        return advancementProgress;
    }

    private void startProgress(Advancement advancement, AdvancementProgress advancementProgress) {
        advancementProgress.update(advancement.getCriteria(), advancement.getRequirements());
        this.progress.put(advancement, advancementProgress);
    }

    private void updateTreeVisibility(Advancement advancement2, Set<Advancement> set, Set<ResourceLocation> set2) {
        AdvancementVisibilityEvaluator.evaluateVisibility(advancement2, advancement -> this.getOrStartProgress((Advancement)advancement).isDone(), (advancement, bl) -> {
            if (bl) {
                if (this.visible.add(advancement)) {
                    set.add(advancement);
                    if (this.progress.containsKey(advancement)) {
                        this.progressChanged.add(advancement);
                    }
                }
            } else if (this.visible.remove(advancement)) {
                set2.add(advancement.getId());
            }
        });
    }
}

