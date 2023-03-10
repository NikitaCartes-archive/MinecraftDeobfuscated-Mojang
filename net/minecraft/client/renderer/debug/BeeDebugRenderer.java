/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BeeDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
    private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
    private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
    private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
    private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
    private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_MEMBERS = true;
    private static final boolean SHOW_BLACKLISTS = true;
    private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
    private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final int HIVE_TIMEOUT = 20;
    private static final float TEXT_SCALE = 0.02f;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int ORANGE = -23296;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private final Minecraft minecraft;
    private final Map<BlockPos, HiveInfo> hives = Maps.newHashMap();
    private final Map<UUID, BeeInfo> beeInfosPerEntity = Maps.newHashMap();
    private UUID lastLookedAtUuid;

    public BeeDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void clear() {
        this.hives.clear();
        this.beeInfosPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addOrUpdateHiveInfo(HiveInfo hiveInfo) {
        this.hives.put(hiveInfo.pos, hiveInfo);
    }

    public void addOrUpdateBeeInfo(BeeInfo beeInfo) {
        this.beeInfosPerEntity.put(beeInfo.uuid, beeInfo);
    }

    public void removeBeeInfo(int i) {
        this.beeInfosPerEntity.values().removeIf(beeInfo -> beeInfo.id == i);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        this.clearRemovedHives();
        this.clearRemovedBees();
        this.doRender(poseStack, multiBufferSource);
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }
    }

    private void clearRemovedBees() {
        this.beeInfosPerEntity.entrySet().removeIf(entry -> this.minecraft.level.getEntity(((BeeInfo)entry.getValue()).id) == null);
    }

    private void clearRemovedHives() {
        long l = this.minecraft.level.getGameTime() - 20L;
        this.hives.entrySet().removeIf(entry -> ((HiveInfo)entry.getValue()).lastSeen < l);
    }

    private void doRender(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        BlockPos blockPos = this.getCamera().getBlockPosition();
        this.beeInfosPerEntity.values().forEach(beeInfo -> {
            if (this.isPlayerCloseEnoughToMob((BeeInfo)beeInfo)) {
                this.renderBeeInfo(poseStack, multiBufferSource, (BeeInfo)beeInfo);
            }
        });
        this.renderFlowerInfos(poseStack, multiBufferSource);
        for (BlockPos blockPos22 : this.hives.keySet()) {
            if (!blockPos.closerThan(blockPos22, 30.0)) continue;
            BeeDebugRenderer.highlightHive(poseStack, multiBufferSource, blockPos22);
        }
        Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
        this.hives.values().forEach(hiveInfo -> {
            if (blockPos.closerThan(hiveInfo.pos, 30.0)) {
                Set set = (Set)map.get(hiveInfo.pos);
                this.renderHiveInfo(poseStack, multiBufferSource, (HiveInfo)hiveInfo, set == null ? Sets.newHashSet() : set);
            }
        });
        this.getGhostHives().forEach((blockPos2, list) -> {
            if (blockPos.closerThan((Vec3i)blockPos2, 30.0)) {
                this.renderGhostHive(poseStack, multiBufferSource, (BlockPos)blockPos2, (List<String>)list);
            }
        });
    }

    private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
        HashMap<BlockPos, Set<UUID>> map = Maps.newHashMap();
        this.beeInfosPerEntity.values().forEach(beeInfo -> beeInfo.blacklistedHives.forEach(blockPos2 -> map.computeIfAbsent((BlockPos)blockPos2, blockPos -> Sets.newHashSet()).add(beeInfo.getUuid())));
        return map;
    }

    private void renderFlowerInfos(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        HashMap map = Maps.newHashMap();
        this.beeInfosPerEntity.values().stream().filter(BeeInfo::hasFlower).forEach(beeInfo -> map.computeIfAbsent(beeInfo.flowerPos, blockPos -> Sets.newHashSet()).add(beeInfo.getUuid()));
        map.entrySet().forEach(entry -> {
            BlockPos blockPos = (BlockPos)entry.getKey();
            Set set = (Set)entry.getValue();
            Set set2 = set.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
            int i = 1;
            BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, set2.toString(), blockPos, i++, -256);
            BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, "Flower", blockPos, i++, -1);
            float f = 0.05f;
            DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.8f, 0.8f, 0.0f, 0.3f);
        });
    }

    private static String getBeeUuidsAsString(Collection<UUID> collection) {
        if (collection.isEmpty()) {
            return "-";
        }
        if (collection.size() > 3) {
            return collection.size() + " bees";
        }
        return collection.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
    }

    private static void highlightHive(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void renderGhostHive(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, List<String> list) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, "" + list, blockPos, 0, -256);
        BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, "Ghost Hive", blockPos, 1, -65536);
    }

    private void renderHiveInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, HiveInfo hiveInfo, Collection<UUID> collection) {
        int i = 0;
        if (!collection.isEmpty()) {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "Blacklisted by " + BeeDebugRenderer.getBeeUuidsAsString(collection), hiveInfo, i++, -65536);
        }
        BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "Out: " + BeeDebugRenderer.getBeeUuidsAsString(this.getHiveMembers(hiveInfo.pos)), hiveInfo, i++, -3355444);
        if (hiveInfo.occupantCount == 0) {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "In: -", hiveInfo, i++, -256);
        } else if (hiveInfo.occupantCount == 1) {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "In: 1 bee", hiveInfo, i++, -256);
        } else {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "In: " + hiveInfo.occupantCount + " bees", hiveInfo, i++, -256);
        }
        BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "Honey: " + hiveInfo.honeyLevel, hiveInfo, i++, -23296);
        BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, hiveInfo.hiveType + (hiveInfo.sedated ? " (sedated)" : ""), hiveInfo, i++, -1);
    }

    private void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, BeeInfo beeInfo) {
        if (beeInfo.path != null) {
            PathfindingRenderer.renderPath(poseStack, multiBufferSource, beeInfo.path, 0.5f, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z());
        }
    }

    private void renderBeeInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, BeeInfo beeInfo) {
        boolean bl = this.isBeeSelected(beeInfo);
        int i = 0;
        BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, beeInfo.toString(), -1, 0.03f);
        if (beeInfo.hivePos == null) {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, "No hive", -98404, 0.02f);
        } else {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, "Hive: " + this.getPosDescription(beeInfo, beeInfo.hivePos), -256, 0.02f);
        }
        if (beeInfo.flowerPos == null) {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, "No flower", -98404, 0.02f);
        } else {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, "Flower: " + this.getPosDescription(beeInfo, beeInfo.flowerPos), -256, 0.02f);
        }
        for (String string : beeInfo.goals) {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, string, -16711936, 0.02f);
        }
        if (bl) {
            this.renderPath(poseStack, multiBufferSource, beeInfo);
        }
        if (beeInfo.travelTicks > 0) {
            int j = beeInfo.travelTicks < 600 ? -3355444 : -23296;
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos, i++, "Travelling: " + beeInfo.travelTicks + " ticks", j, 0.02f);
        }
    }

    private static void renderTextOverHive(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, HiveInfo hiveInfo, int i, int j) {
        BlockPos blockPos = hiveInfo.pos;
        BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, string, blockPos, i, j);
    }

    private static void renderTextOverPos(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, BlockPos blockPos, int i, int j) {
        double d = 1.3;
        double e = 0.2;
        double f = (double)blockPos.getX() + 0.5;
        double g = (double)blockPos.getY() + 1.3 + (double)i * 0.2;
        double h = (double)blockPos.getZ() + 0.5;
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, f, g, h, j, 0.02f, true, 0.0f, true);
    }

    private static void renderTextOverMob(PoseStack poseStack, MultiBufferSource multiBufferSource, Position position, int i, String string, int j, float f) {
        double d = 2.4;
        double e = 0.25;
        BlockPos blockPos = BlockPos.containing(position);
        double g = (double)blockPos.getX() + 0.5;
        double h = position.y() + 2.4 + (double)i * 0.25;
        double k = (double)blockPos.getZ() + 0.5;
        float l = 0.5f;
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, g, h, k, j, f, false, 0.5f, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }

    private Set<String> getHiveMemberNames(HiveInfo hiveInfo) {
        return this.getHiveMembers(hiveInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private String getPosDescription(BeeInfo beeInfo, BlockPos blockPos) {
        double d = Math.sqrt(blockPos.distToCenterSqr(beeInfo.pos));
        double e = (double)Math.round(d * 10.0) / 10.0;
        return blockPos.toShortString() + " (dist " + e + ")";
    }

    private boolean isBeeSelected(BeeInfo beeInfo) {
        return Objects.equals(this.lastLookedAtUuid, beeInfo.uuid);
    }

    private boolean isPlayerCloseEnoughToMob(BeeInfo beeInfo) {
        LocalPlayer player = this.minecraft.player;
        BlockPos blockPos = BlockPos.containing(player.getX(), beeInfo.pos.y(), player.getZ());
        BlockPos blockPos2 = BlockPos.containing(beeInfo.pos);
        return blockPos.closerThan(blockPos2, 30.0);
    }

    private Collection<UUID> getHiveMembers(BlockPos blockPos) {
        return this.beeInfosPerEntity.values().stream().filter(beeInfo -> beeInfo.hasHive(blockPos)).map(BeeInfo::getUuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostHives() {
        HashMap<BlockPos, List<String>> map = Maps.newHashMap();
        for (BeeInfo beeInfo : this.beeInfosPerEntity.values()) {
            if (beeInfo.hivePos == null || this.hives.containsKey(beeInfo.hivePos)) continue;
            map.computeIfAbsent(beeInfo.hivePos, blockPos -> Lists.newArrayList()).add(beeInfo.getName());
        }
        return map;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> {
            this.lastLookedAtUuid = entity.getUUID();
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class HiveInfo {
        public final BlockPos pos;
        public final String hiveType;
        public final int occupantCount;
        public final int honeyLevel;
        public final boolean sedated;
        public final long lastSeen;

        public HiveInfo(BlockPos blockPos, String string, int i, int j, boolean bl, long l) {
            this.pos = blockPos;
            this.hiveType = string;
            this.occupantCount = i;
            this.honeyLevel = j;
            this.sedated = bl;
            this.lastSeen = l;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class BeeInfo {
        public final UUID uuid;
        public final int id;
        public final Position pos;
        @Nullable
        public final Path path;
        @Nullable
        public final BlockPos hivePos;
        @Nullable
        public final BlockPos flowerPos;
        public final int travelTicks;
        public final List<String> goals = Lists.newArrayList();
        public final Set<BlockPos> blacklistedHives = Sets.newHashSet();

        public BeeInfo(UUID uUID, int i, Position position, @Nullable Path path, @Nullable BlockPos blockPos, @Nullable BlockPos blockPos2, int j) {
            this.uuid = uUID;
            this.id = i;
            this.pos = position;
            this.path = path;
            this.hivePos = blockPos;
            this.flowerPos = blockPos2;
            this.travelTicks = j;
        }

        public boolean hasHive(BlockPos blockPos) {
            return this.hivePos != null && this.hivePos.equals(blockPos);
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        public String toString() {
            return this.getName();
        }

        public boolean hasFlower() {
            return this.flowerPos != null;
        }
    }
}

