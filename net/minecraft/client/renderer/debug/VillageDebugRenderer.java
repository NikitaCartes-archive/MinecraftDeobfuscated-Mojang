/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
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
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugVillagerNameGenerator;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VillageDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final Map<BlockPos, PoiInfo> pois = Maps.newHashMap();
    private final Set<SectionPos> villageSections = Sets.newHashSet();
    private final Map<UUID, BrainDump> brainDumpsPerEntity = Maps.newHashMap();
    private UUID lastLookedAtUuid;

    public VillageDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void clear() {
        this.pois.clear();
        this.villageSections.clear();
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
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: " + blockPos);
            return;
        }
        poiInfo.freeTicketCount = i;
    }

    public void setVillageSection(SectionPos sectionPos) {
        this.villageSections.add(sectionPos);
    }

    public void setNotVillageSection(SectionPos sectionPos) {
        this.villageSections.remove(sectionPos);
    }

    public void addOrUpdateBrainDump(BrainDump brainDump) {
        this.brainDumpsPerEntity.put(brainDump.uuid, brainDump);
    }

    @Override
    public void render(long l) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture();
        this.doRender();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }
    }

    private void doRender() {
        BlockPos blockPos = this.getCamera().getBlockPosition();
        this.villageSections.forEach(sectionPos -> {
            if (blockPos.closerThan(sectionPos.center(), 60.0)) {
                VillageDebugRenderer.highlightVillageSection(sectionPos);
            }
        });
        this.brainDumpsPerEntity.values().forEach(brainDump -> {
            if (this.isPlayerCloseEnoughToMob((BrainDump)brainDump)) {
                this.renderVillagerInfo((BrainDump)brainDump);
            }
        });
        for (BlockPos blockPos22 : this.pois.keySet()) {
            if (!blockPos.closerThan(blockPos22, 30.0)) continue;
            VillageDebugRenderer.highlightPoi(blockPos22);
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

    private static void highlightVillageSection(SectionPos sectionPos) {
        float f = 1.0f;
        BlockPos blockPos = sectionPos.center();
        BlockPos blockPos2 = blockPos.offset(-1.0, -1.0, -1.0);
        BlockPos blockPos3 = blockPos.offset(1.0, 1.0, 1.0);
        DebugRenderer.renderFilledBox(blockPos2, blockPos3, 0.2f, 1.0f, 0.2f, 0.15f);
    }

    private static void highlightPoi(BlockPos blockPos) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void renderGhostPoi(BlockPos blockPos, List<String> list) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        VillageDebugRenderer.renderTextOverPos("" + list, blockPos, 0, -256);
        VillageDebugRenderer.renderTextOverPos("Ghost POI", blockPos, 1, -65536);
    }

    private void renderPoiInfo(PoiInfo poiInfo) {
        int i = 0;
        if (this.getTicketHolderNames(poiInfo).size() < 4) {
            VillageDebugRenderer.renderTextOverPoi("" + this.getTicketHolderNames(poiInfo), poiInfo, i, -256);
        } else {
            VillageDebugRenderer.renderTextOverPoi("" + this.getTicketHolderNames(poiInfo).size() + " ticket holders", poiInfo, i, -256);
        }
        VillageDebugRenderer.renderTextOverPoi("Free tickets: " + poiInfo.freeTicketCount, poiInfo, ++i, -256);
        VillageDebugRenderer.renderTextOverPoi(poiInfo.type, poiInfo, ++i, -1);
    }

    private void renderPath(BrainDump brainDump) {
        if (brainDump.path != null) {
            PathfindingRenderer.renderPath(this.getCamera(), brainDump.path, 0.5f, false, false);
        }
    }

    private void renderVillagerInfo(BrainDump brainDump) {
        boolean bl = this.isVillagerSelected(brainDump);
        int i = 0;
        VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, brainDump.name, -1, 0.03f);
        ++i;
        if (bl) {
            VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, brainDump.profession + " " + brainDump.xp + "xp", -1, 0.02f);
            ++i;
        }
        if (bl && !brainDump.inventory.equals("")) {
            VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, brainDump.inventory, -98404, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brainDump.behaviors) {
                VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -16711681, 0.02f);
                ++i;
            }
        }
        if (bl) {
            for (String string : brainDump.activities) {
                VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -16711936, 0.02f);
                ++i;
            }
        }
        if (brainDump.wantsGolem) {
            VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, "Wants Golem", -23296, 0.02f);
            ++i;
        }
        if (bl) {
            for (String string : brainDump.gossips) {
                if (string.startsWith(brainDump.name)) {
                    VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -1, 0.02f);
                } else {
                    VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -23296, 0.02f);
                }
                ++i;
            }
        }
        if (bl) {
            for (String string : Lists.reverse(brainDump.memories)) {
                VillageDebugRenderer.renderTextOverMob(brainDump.pos, i, string, -3355444, 0.02f);
                ++i;
            }
        }
        if (bl) {
            this.renderPath(brainDump);
        }
    }

    private static void renderTextOverPoi(String string, PoiInfo poiInfo, int i, int j) {
        BlockPos blockPos = poiInfo.pos;
        VillageDebugRenderer.renderTextOverPos(string, blockPos, i, j);
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

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }

    private Set<String> getTicketHolderNames(PoiInfo poiInfo) {
        return this.getTicketHolders(poiInfo.pos).stream().map(DebugVillagerNameGenerator::getVillagerName).collect(Collectors.toSet());
    }

    private boolean isVillagerSelected(BrainDump brainDump) {
        return Objects.equals(this.lastLookedAtUuid, brainDump.uuid);
    }

    private boolean isPlayerCloseEnoughToMob(BrainDump brainDump) {
        LocalPlayer player = this.minecraft.player;
        BlockPos blockPos = new BlockPos(player.x, brainDump.pos.y(), player.z);
        BlockPos blockPos2 = new BlockPos(brainDump.pos);
        return blockPos.closerThan(blockPos2, 30.0);
    }

    private Collection<UUID> getTicketHolders(BlockPos blockPos) {
        return this.brainDumpsPerEntity.values().stream().filter(brainDump -> ((BrainDump)brainDump).hasPoi(blockPos)).map(BrainDump::getUuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPois() {
        HashMap<BlockPos, List<String>> map = Maps.newHashMap();
        for (BrainDump brainDump : this.brainDumpsPerEntity.values()) {
            for (BlockPos blockPos : brainDump.pois) {
                if (this.pois.containsKey(blockPos)) continue;
                ArrayList<String> list = (ArrayList<String>)map.get(blockPos);
                if (list == null) {
                    list = Lists.newArrayList();
                    map.put(blockPos, list);
                }
                list.add(brainDump.name);
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
        public final Position pos;
        public final String inventory;
        public final Path path;
        public final boolean wantsGolem;
        public final List<String> activities = Lists.newArrayList();
        public final List<String> behaviors = Lists.newArrayList();
        public final List<String> memories = Lists.newArrayList();
        public final List<String> gossips = Lists.newArrayList();
        public final Set<BlockPos> pois = Sets.newHashSet();

        public BrainDump(UUID uUID, int i, String string, String string2, int j, Position position, String string3, @Nullable Path path, boolean bl) {
            this.uuid = uUID;
            this.id = i;
            this.name = string;
            this.profession = string2;
            this.xp = j;
            this.pos = position;
            this.inventory = string3;
            this.path = path;
            this.wantsGolem = bl;
        }

        private boolean hasPoi(BlockPos blockPos) {
            return this.pois.stream().anyMatch(blockPos::equals);
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

