/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VISIBILITY_DEPTH = 2;
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)AdvancementProgress.class), new AdvancementProgress.Serializer()).registerTypeAdapter((Type)((Object)ResourceLocation.class), new ResourceLocation.Serializer()).setPrettyPrinting().create();
    private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>(){};
    private final DataFixer dataFixer;
    private final PlayerList playerList;
    private final File file;
    private final Map<Advancement, AdvancementProgress> advancements = Maps.newLinkedHashMap();
    private final Set<Advancement> visible = Sets.newLinkedHashSet();
    private final Set<Advancement> visibilityChanged = Sets.newLinkedHashSet();
    private final Set<Advancement> progressChanged = Sets.newLinkedHashSet();
    private ServerPlayer player;
    @Nullable
    private Advancement lastSelectedTab;
    private boolean isFirstPacket = true;

    public PlayerAdvancements(DataFixer dataFixer, PlayerList playerList, ServerAdvancementManager serverAdvancementManager, File file, ServerPlayer serverPlayer) {
        this.dataFixer = dataFixer;
        this.playerList = playerList;
        this.file = file;
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
        this.advancements.clear();
        this.visible.clear();
        this.visibilityChanged.clear();
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

    private void ensureAllVisible() {
        ArrayList<Advancement> list = Lists.newArrayList();
        for (Map.Entry<Advancement, AdvancementProgress> entry : this.advancements.entrySet()) {
            if (!entry.getValue().isDone()) continue;
            list.add(entry.getKey());
            this.progressChanged.add(entry.getKey());
        }
        for (Advancement advancement : list) {
            this.ensureVisibility(advancement);
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
        if (this.file.isFile()) {
            try (JsonReader jsonReader = new JsonReader(new StringReader(Files.toString(this.file, StandardCharsets.UTF_8)));){
                jsonReader.setLenient(false);
                Dynamic<JsonElement> dynamic = new Dynamic<JsonElement>(JsonOps.INSTANCE, Streams.parse(jsonReader));
                if (!dynamic.get("DataVersion").asNumber().result().isPresent()) {
                    dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
                }
                dynamic = this.dataFixer.update(DataFixTypes.ADVANCEMENTS.getType(), dynamic, dynamic.get("DataVersion").asInt(0), SharedConstants.getCurrentVersion().getWorldVersion());
                dynamic = dynamic.remove("DataVersion");
                Map<ResourceLocation, AdvancementProgress> map = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(dynamic.getValue());
                if (map == null) {
                    throw new JsonParseException("Found null for advancements");
                }
                Stream<Map.Entry> stream = map.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue));
                for (Map.Entry entry : stream.collect(Collectors.toList())) {
                    Advancement advancement = serverAdvancementManager.getAdvancement((ResourceLocation)entry.getKey());
                    if (advancement == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", entry.getKey(), (Object)this.file);
                        continue;
                    }
                    this.startProgress(advancement, (AdvancementProgress)entry.getValue());
                }
            } catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse player advancements in {}", (Object)this.file, (Object)jsonParseException);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't access player advancements in {}", (Object)this.file, (Object)iOException);
            }
        }
        this.checkForAutomaticTriggers(serverAdvancementManager);
        this.ensureAllVisible();
        this.registerListeners(serverAdvancementManager);
    }

    public void save() {
        HashMap<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();
        for (Map.Entry<Advancement, AdvancementProgress> entry : this.advancements.entrySet()) {
            AdvancementProgress advancementProgress = entry.getValue();
            if (!advancementProgress.hasProgress()) continue;
            map.put(entry.getKey().getId(), advancementProgress);
        }
        if (this.file.getParentFile() != null) {
            this.file.getParentFile().mkdirs();
        }
        JsonElement jsonElement = GSON.toJsonTree(map);
        jsonElement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        try (FileOutputStream outputStream = new FileOutputStream(this.file);
             OutputStreamWriter writer = new OutputStreamWriter((OutputStream)outputStream, Charsets.UTF_8.newEncoder());){
            GSON.toJson(jsonElement, (Appendable)writer);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save player advancements to {}", (Object)this.file, (Object)iOException);
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
                    this.playerList.broadcastSystemMessage(Component.translatable("chat.type.advancement." + advancement.getDisplay().getFrame().getName(), this.player.getDisplayName(), advancement.getChatComponent()), ChatType.SYSTEM);
                }
            }
        }
        if (advancementProgress.isDone()) {
            this.ensureVisibility(advancement);
        }
        return bl;
    }

    public boolean revoke(Advancement advancement, String string) {
        boolean bl = false;
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
        if (advancementProgress.revokeProgress(string)) {
            this.registerListeners(advancement);
            this.progressChanged.add(advancement);
            bl = true;
        }
        if (!advancementProgress.hasProgress()) {
            this.ensureVisibility(advancement);
        }
        return bl;
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
        if (this.isFirstPacket || !this.visibilityChanged.isEmpty() || !this.progressChanged.isEmpty()) {
            HashMap<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();
            LinkedHashSet<Advancement> set = Sets.newLinkedHashSet();
            LinkedHashSet<ResourceLocation> set2 = Sets.newLinkedHashSet();
            for (Advancement advancement : this.progressChanged) {
                if (!this.visible.contains(advancement)) continue;
                map.put(advancement.getId(), this.advancements.get(advancement));
            }
            for (Advancement advancement : this.visibilityChanged) {
                if (this.visible.contains(advancement)) {
                    set.add(advancement);
                    continue;
                }
                set2.add(advancement.getId());
            }
            if (this.isFirstPacket || !map.isEmpty() || !set.isEmpty() || !set2.isEmpty()) {
                serverPlayer.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, set, set2, map));
                this.visibilityChanged.clear();
                this.progressChanged.clear();
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
        AdvancementProgress advancementProgress = this.advancements.get(advancement);
        if (advancementProgress == null) {
            advancementProgress = new AdvancementProgress();
            this.startProgress(advancement, advancementProgress);
        }
        return advancementProgress;
    }

    private void startProgress(Advancement advancement, AdvancementProgress advancementProgress) {
        advancementProgress.update(advancement.getCriteria(), advancement.getRequirements());
        this.advancements.put(advancement, advancementProgress);
    }

    private void ensureVisibility(Advancement advancement) {
        boolean bl = this.shouldBeVisible(advancement);
        boolean bl2 = this.visible.contains(advancement);
        if (bl && !bl2) {
            this.visible.add(advancement);
            this.visibilityChanged.add(advancement);
            if (this.advancements.containsKey(advancement)) {
                this.progressChanged.add(advancement);
            }
        } else if (!bl && bl2) {
            this.visible.remove(advancement);
            this.visibilityChanged.add(advancement);
        }
        if (bl != bl2 && advancement.getParent() != null) {
            this.ensureVisibility(advancement.getParent());
        }
        for (Advancement advancement2 : advancement.getChildren()) {
            this.ensureVisibility(advancement2);
        }
    }

    private boolean shouldBeVisible(Advancement advancement) {
        for (int i = 0; advancement != null && i <= 2; advancement = advancement.getParent(), ++i) {
            if (i == 0 && this.hasCompletedChildrenOrSelf(advancement)) {
                return true;
            }
            if (advancement.getDisplay() == null) {
                return false;
            }
            AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
            if (advancementProgress.isDone()) {
                return true;
            }
            if (!advancement.getDisplay().isHidden()) continue;
            return false;
        }
        return false;
    }

    private boolean hasCompletedChildrenOrSelf(Advancement advancement) {
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
        if (advancementProgress.isDone()) {
            return true;
        }
        for (Advancement advancement2 : advancement.getChildren()) {
            if (!this.hasCompletedChildrenOrSelf(advancement2)) continue;
            return true;
        }
        return false;
    }
}

