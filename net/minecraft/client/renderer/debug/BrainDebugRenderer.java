/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BrainDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean SHOW_NAME_FOR_ALL = true;
    private static final boolean SHOW_PROFESSION_FOR_ALL = false;
    private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
    private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
    private static final boolean SHOW_INVENTORY_FOR_ALL = false;
    private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
    private static final boolean SHOW_PATH_FOR_ALL = false;
    private static final boolean SHOW_HEALTH_FOR_ALL = false;
    private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
    private static final boolean SHOW_NAME_FOR_SELECTED = true;
    private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
    private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
    private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
    private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
    private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
    private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
    private static final boolean SHOW_PATH_FOR_SELECTED = true;
    private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
    private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
    private static final boolean SHOW_POI_INFO = true;
    private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
    private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final float TEXT_SCALE = 0.02f;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int CYAN = -16711681;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private static final int ORANGE = -23296;
    private final Minecraft minecraft;
    private final Map<BlockPos, PoiInfo> pois = Maps.newHashMap();
    private final Map<UUID, BrainDump> brainDumpsPerEntity = Maps.newHashMap();
    @Nullable
    private UUID lastLookedAtUuid;

    public BrainDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void clear() {
        this.pois.clear();
        this.brainDumpsPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addPoi(PoiInfo poiInfo) {
        this.pois.put(poiInfo.pos, poiInfo);
    }

    public void removePoi(BlockPos blockPos) {
        this.pois.remove(blockPos);
    }

    public void setFreeTicketCount(BlockPos blockPos, int i) {
        PoiInfo poiInfo = this.pois.get(blockPos);
        if (poiInfo == null) {
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)blockPos);
            return;
        }
        poiInfo.freeTicketCount = i;
    }

    public void addOrUpdateBrainDump(BrainDump brainDump) {
        this.brainDumpsPerEntity.put(brainDump.uuid, brainDump);
    }

    public void removeBrainDump(int i) {
        this.brainDumpsPerEntity.values().removeIf(brainDump -> brainDump.id == i);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.clearRemovedEntities();
        this.doRender(d, e, f);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }
    }

    private void clearRemovedEntities() {
        this.brainDumpsPerEntity.entrySet().removeIf(entry -> {
            Entity entity = this.minecraft.level.getEntity(((BrainDump)entry.getValue()).id);
            return entity == null || entity.isRemoved();
        });
    }

    private void doRender(double d, double e, double f) {
        BlockPos blockPos = new BlockPos(d, e, f);
        this.brainDumpsPerEntity.values().forEach(brainDump -> {
            if (this.isPlayerCloseEnoughToMob((BrainDump)brainDump)) {
                this.renderBrainInfo((BrainDump)brainDump, d, e, f);
            }
        });
        for (BlockPos blockPos22 : this.pois.keySet()) {
            if (!blockPos.closerThan(blockPos22, 30.0)) continue;
            BrainDebugRenderer.highlightPoi(blockPos22);
        }
        this.pois.values().forEach(poiInfo -> {
            if (blockPos.closerThan(poiInfo.pos, 30.0)) {
                this.renderPoiInfo((PoiInfo)poiInfo);
            }
        });
        this.getGhostPois().forEach((blockPos2, list) -> {
            if (blockPos.closerThan((Vec3i)blockPos2, 30.0)) {
                this.renderGhostPoi((BlockPos)blockPos2, (List<String>)list);
            }
        });
    }

    private static void highlightPoi(BlockPos blockPos) {
        float f = 0.05f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.renderFilledBox(blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void renderGhostPoi(BlockPos blockPos, List<String> list) {
        float f = 0.05f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DebugRenderer.renderFilledBox(blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        BrainDebugRenderer.renderTextOverPos("" + list, blockPos, 0, -256);
        BrainDebugRenderer.renderTextOverPos("Ghost POI", blockPos, 1, -65536);
    }

    private void renderPoiInfo(PoiInfo poiInfo) {
        int i = 0;
        Set<String> set = this.getTicketHolderNames(poiInfo);
        if (set.size() < 4) {
            BrainDebugRenderer.renderTextOverPoi("Owners: " + set, poiInfo, i, -256);
        } else {
            BrainDebugRenderer.renderTextOverPoi("" + set.size() + " ticket holders", poiInfo, i, -256);
        }
        ++i;
        Set<String> set2 = this.getPotentialTicketHolderNames(poiInfo);
        if (set2.size() < 4) {
            BrainDebugRenderer.renderTextOverPoi("Candidates: " + set2, poiInfo, i, -23296);
        } else {
            BrainDebugRenderer.renderTextOverPoi("" + set2.size() + " potential owners", poiInfo, i, -23296);
        }
        BrainDebugRenderer.renderTextOverPoi("Free tickets: " + poiInfo.freeTicketCount, poiInfo, ++i, -256);
        BrainDebugRenderer.renderTextOverPoi(poiInfo.type, poiInfo, ++i, -1);
    }

    private void renderPath(BrainDump brainDump, double d, double e, double f) {
        if (brainDump.path != null) {
            PathfindingRenderer.renderPath(brainDump.path, 0.5f, false, false, d, e, f);
        }
    }

    private void renderBrainInfo(BrainDump brainDump, double d, double e, double f) {
        boolean bl = this.isMobSelected(brainDump);
        int i = 0;
        BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, brainDump.name, -1, 0.03f);
        ++i;
        if (bl) {
            BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, brainDump.profession + " " + brainDump.xp + " xp", -1, 0.02f);
            ++i;
        }
        if (bl) {
            int j = brainDump.health < brainDump.maxHealth ? -23296 : -1;
            BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, "health: " + String.format("%.1f", Float.valueOf(brainDump.health)) + " / " + String.format("%.1f", Float.valueOf(brainDump.maxHealth)), j, 0.02f);
            ++i;
        }
        if (bl && !brainDump.inventory.equals("")) {
            BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, brainDump.inventory, -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brainDump.behaviors) {
                BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -16711681, 0.02f);
                ++i;
            }
        }
        if (bl) {
            for (String string : brainDump.activities) {
                BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -16711936, 0.02f);
                ++i;
            }
        }
        if (brainDump.wantsGolem) {
            BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, "Wants Golem", -23296, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brainDump.gossips) {
                if (string.startsWith(brainDump.name)) {
                    BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -1, 0.02f);
                } else {
                    BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -23296, 0.02f);
                }
                ++i;
            }
        }
        if (bl) {
            for (String string : Lists.reverse(brainDump.memories)) {
                BrainDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -3355444, 0.02f);
                ++i;
            }
        }
        if (bl) {
            this.renderPath(brainDump, d, e, f);
        }
    }

    private static void renderTextOverPoi(String string, PoiInfo poiInfo, int i, int j) {
        BlockPos blockPos = poiInfo.pos;
        BrainDebugRenderer.renderTextOverPos(string, blockPos, i, j);
    }

    private static void renderTextOverPos(String string, BlockPos blockPos, int i, int j) {
        double d = 1.3;
        double e = 0.2;
        double f = (double)blockPos.getX() + 0.5;
        double g = (double)blockPos.getY() + 1.3 + (double)i * 0.2;
        double h = (double)blockPos.getZ() + 0.5;
        DebugRenderer.renderFloatingText(string, f, g, h, j, 0.02f, true, 0.0f, true);
    }

    private static void renderTextOverMob(Position position, int i, String string, int j, float f) {
        double d = 2.4;
        double e = 0.25;
        BlockPos blockPos = new BlockPos(position);
        double g = (double)blockPos.getX() + 0.5;
        double h = position.y() + 2.4 + (double)i * 0.25;
        double k = (double)blockPos.getZ() + 0.5;
        float l = 0.5f;
        DebugRenderer.renderFloatingText(string, g, h, k, j, f, false, 0.5f, true);
    }

    private Set<String> getTicketHolderNames(PoiInfo poiInfo) {
        return this.getTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private Set<String> getPotentialTicketHolderNames(PoiInfo poiInfo) {
        return this.getPotentialTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private boolean isMobSelected(BrainDump brainDump) {
        return Objects.equals(this.lastLookedAtUuid, brainDump.uuid);
    }

    private boolean isPlayerCloseEnoughToMob(BrainDump brainDump) {
        LocalPlayer player = this.minecraft.player;
        BlockPos blockPos = new BlockPos(player.getX(), brainDump.pos.y(), player.getZ());
        BlockPos blockPos2 = new BlockPos(brainDump.pos);
        return blockPos.closerThan(blockPos2, 30.0);
    }

    private Collection<UUID> getTicketHolders(BlockPos blockPos) {
        return this.brainDumpsPerEntity.values().stream().filter(brainDump -> ((BrainDump)brainDump).hasPoi(blockPos)).map(BrainDump::getUuid).collect(Collectors.toSet());
    }

    private Collection<UUID> getPotentialTicketHolders(BlockPos blockPos) {
        return this.brainDumpsPerEntity.values().stream().filter(brainDump -> ((BrainDump)brainDump).hasPotentialPoi(blockPos)).map(BrainDump::getUuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPois() {
        HashMap<BlockPos, List<String>> map = Maps.newHashMap();
        for (BrainDump brainDump : this.brainDumpsPerEntity.values()) {
            for (BlockPos blockPos2 : Iterables.concat(brainDump.pois, brainDump.potentialPois)) {
                if (this.pois.containsKey(blockPos2)) continue;
                map.computeIfAbsent(blockPos2, blockPos -> Lists.newArrayList()).add(brainDump.name);
            }
        }
        return map;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> {
            this.lastLookedAtUuid = entity.getUUID();
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class BrainDump {
        public final UUID uuid;
        public final int id;
        public final String name;
        public final String profession;
        public final int xp;
        public final float health;
        public final float maxHealth;
        public final Position pos;
        public final String inventory;
        public final Path path;
        public final boolean wantsGolem;
        public final List<String> activities = Lists.newArrayList();
        public final List<String> behaviors = Lists.newArrayList();
        public final List<String> memories = Lists.newArrayList();
        public final List<String> gossips = Lists.newArrayList();
        public final Set<BlockPos> pois = Sets.newHashSet();
        public final Set<BlockPos> potentialPois = Sets.newHashSet();

        public BrainDump(UUID uUID, int i, String string, String string2, int j, float f, float g, Position position, String string3, @Nullable Path path, boolean bl) {
            this.uuid = uUID;
            this.id = i;
            this.name = string;
            this.profession = string2;
            this.xp = j;
            this.health = f;
            this.maxHealth = g;
            this.pos = position;
            this.inventory = string3;
            this.path = path;
            this.wantsGolem = bl;
        }

        private boolean hasPoi(BlockPos blockPos) {
            return this.pois.stream().anyMatch(blockPos::equals);
        }

        private boolean hasPotentialPoi(BlockPos blockPos) {
            return this.potentialPois.contains(blockPos);
        }

        public UUID getUuid() {
            return this.uuid;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class PoiInfo {
        public final BlockPos pos;
        public String type;
        public int freeTicketCount;

        public PoiInfo(BlockPos blockPos, String string, int i) {
            this.pos = blockPos;
            this.type = string;
            this.freeTicketCount = i;
        }
    }
}

