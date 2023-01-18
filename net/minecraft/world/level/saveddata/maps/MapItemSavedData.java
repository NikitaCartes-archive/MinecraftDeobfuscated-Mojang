/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MapItemSavedData
extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final int MAX_SCALE = 4;
    public static final int TRACKED_DECORATION_LIMIT = 256;
    public final int centerX;
    public final int centerZ;
    public final ResourceKey<Level> dimension;
    private final boolean trackingPosition;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
    private int trackedDecorationCount;

    private MapItemSavedData(int i, int j, byte b, boolean bl, boolean bl2, boolean bl3, ResourceKey<Level> resourceKey) {
        this.scale = b;
        this.centerX = i;
        this.centerZ = j;
        this.dimension = resourceKey;
        this.trackingPosition = bl;
        this.unlimitedTracking = bl2;
        this.locked = bl3;
        this.setDirty();
    }

    public static MapItemSavedData createFresh(double d, double e, byte b, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        int i = 128 * (1 << b);
        int j = Mth.floor((d + 64.0) / (double)i);
        int k = Mth.floor((e + 64.0) / (double)i);
        int l = j * i + i / 2 - 64;
        int m = k * i + i / 2 - 64;
        return new MapItemSavedData(l, m, b, bl, bl2, false, resourceKey);
    }

    public static MapItemSavedData createForClient(byte b, boolean bl, ResourceKey<Level> resourceKey) {
        return new MapItemSavedData(0, 0, b, false, false, bl, resourceKey);
    }

    public static MapItemSavedData load(CompoundTag compoundTag) {
        ResourceKey<Level> resourceKey = DimensionType.parseLegacy(new Dynamic<Tag>(NbtOps.INSTANCE, compoundTag.get("dimension"))).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + compoundTag.get("dimension")));
        int i = compoundTag.getInt("xCenter");
        int j = compoundTag.getInt("zCenter");
        byte b = (byte)Mth.clamp(compoundTag.getByte("scale"), 0, 4);
        boolean bl = !compoundTag.contains("trackingPosition", 1) || compoundTag.getBoolean("trackingPosition");
        boolean bl2 = compoundTag.getBoolean("unlimitedTracking");
        boolean bl3 = compoundTag.getBoolean("locked");
        MapItemSavedData mapItemSavedData = new MapItemSavedData(i, j, b, bl, bl2, bl3, resourceKey);
        byte[] bs = compoundTag.getByteArray("colors");
        if (bs.length == 16384) {
            mapItemSavedData.colors = bs;
        }
        ListTag listTag = compoundTag.getList("banners", 10);
        for (int k = 0; k < listTag.size(); ++k) {
            MapBanner mapBanner = MapBanner.load(listTag.getCompound(k));
            mapItemSavedData.bannerMarkers.put(mapBanner.getId(), mapBanner);
            mapItemSavedData.addDecoration(mapBanner.getDecoration(), null, mapBanner.getId(), mapBanner.getPos().getX(), mapBanner.getPos().getZ(), 180.0, mapBanner.getName());
        }
        ListTag listTag2 = compoundTag.getList("frames", 10);
        for (int l = 0; l < listTag2.size(); ++l) {
            MapFrame mapFrame = MapFrame.load(listTag2.getCompound(l));
            mapItemSavedData.frameMarkers.put(mapFrame.getId(), mapFrame);
            mapItemSavedData.addDecoration(MapDecoration.Type.FRAME, null, "frame-" + mapFrame.getEntityId(), mapFrame.getPos().getX(), mapFrame.getPos().getZ(), mapFrame.getRotation(), null);
        }
        return mapItemSavedData;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.location()).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("dimension", (Tag)tag));
        compoundTag.putInt("xCenter", this.centerX);
        compoundTag.putInt("zCenter", this.centerZ);
        compoundTag.putByte("scale", this.scale);
        compoundTag.putByteArray("colors", this.colors);
        compoundTag.putBoolean("trackingPosition", this.trackingPosition);
        compoundTag.putBoolean("unlimitedTracking", this.unlimitedTracking);
        compoundTag.putBoolean("locked", this.locked);
        ListTag listTag = new ListTag();
        for (MapBanner mapBanner : this.bannerMarkers.values()) {
            listTag.add(mapBanner.save());
        }
        compoundTag.put("banners", listTag);
        ListTag listTag2 = new ListTag();
        for (MapFrame mapFrame : this.frameMarkers.values()) {
            listTag2.add(mapFrame.save());
        }
        compoundTag.put("frames", listTag2);
        return compoundTag;
    }

    public MapItemSavedData locked() {
        MapItemSavedData mapItemSavedData = new MapItemSavedData(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);
        mapItemSavedData.bannerMarkers.putAll(this.bannerMarkers);
        mapItemSavedData.decorations.putAll(this.decorations);
        mapItemSavedData.trackedDecorationCount = this.trackedDecorationCount;
        System.arraycopy(this.colors, 0, mapItemSavedData.colors, 0, this.colors.length);
        mapItemSavedData.setDirty();
        return mapItemSavedData;
    }

    public MapItemSavedData scaled(int i) {
        return MapItemSavedData.createFresh(this.centerX, this.centerZ, (byte)Mth.clamp(this.scale + i, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
    }

    public void tickCarriedBy(Player player, ItemStack itemStack) {
        CompoundTag compoundTag;
        if (!this.carriedByPlayers.containsKey(player)) {
            HoldingPlayer holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        if (!player.getInventory().contains(itemStack)) {
            this.removeDecoration(player.getName().getString());
        }
        for (int i = 0; i < this.carriedBy.size(); ++i) {
            HoldingPlayer holdingPlayer2 = this.carriedBy.get(i);
            String string = holdingPlayer2.player.getName().getString();
            if (holdingPlayer2.player.isRemoved() || !holdingPlayer2.player.getInventory().contains(itemStack) && !itemStack.isFramed()) {
                this.carriedByPlayers.remove(holdingPlayer2.player);
                this.carriedBy.remove(holdingPlayer2);
                this.removeDecoration(string);
                continue;
            }
            if (itemStack.isFramed() || holdingPlayer2.player.level.dimension() != this.dimension || !this.trackingPosition) continue;
            this.addDecoration(MapDecoration.Type.PLAYER, holdingPlayer2.player.level, string, holdingPlayer2.player.getX(), holdingPlayer2.player.getZ(), holdingPlayer2.player.getYRot(), null);
        }
        if (itemStack.isFramed() && this.trackingPosition) {
            ItemFrame itemFrame = itemStack.getFrame();
            BlockPos blockPos = itemFrame.getPos();
            MapFrame mapFrame = this.frameMarkers.get(MapFrame.frameId(blockPos));
            if (mapFrame != null && itemFrame.getId() != mapFrame.getEntityId() && this.frameMarkers.containsKey(mapFrame.getId())) {
                this.removeDecoration("frame-" + mapFrame.getEntityId());
            }
            MapFrame mapFrame2 = new MapFrame(blockPos, itemFrame.getDirection().get2DDataValue() * 90, itemFrame.getId());
            this.addDecoration(MapDecoration.Type.FRAME, player.level, "frame-" + itemFrame.getId(), blockPos.getX(), blockPos.getZ(), itemFrame.getDirection().get2DDataValue() * 90, null);
            this.frameMarkers.put(mapFrame2.getId(), mapFrame2);
        }
        if ((compoundTag = itemStack.getTag()) != null && compoundTag.contains("Decorations", 9)) {
            ListTag listTag = compoundTag.getList("Decorations", 10);
            for (int j = 0; j < listTag.size(); ++j) {
                CompoundTag compoundTag2 = listTag.getCompound(j);
                if (this.decorations.containsKey(compoundTag2.getString("id"))) continue;
                this.addDecoration(MapDecoration.Type.byIcon(compoundTag2.getByte("type")), player.level, compoundTag2.getString("id"), compoundTag2.getDouble("x"), compoundTag2.getDouble("z"), compoundTag2.getDouble("rot"), null);
            }
        }
    }

    private void removeDecoration(String string) {
        MapDecoration mapDecoration = this.decorations.remove(string);
        if (mapDecoration != null && mapDecoration.getType().shouldTrackCount()) {
            --this.trackedDecorationCount;
        }
        this.setDecorationsDirty();
    }

    public static void addTargetDecoration(ItemStack itemStack, BlockPos blockPos, String string, MapDecoration.Type type) {
        ListTag listTag;
        if (itemStack.hasTag() && itemStack.getTag().contains("Decorations", 9)) {
            listTag = itemStack.getTag().getList("Decorations", 10);
        } else {
            listTag = new ListTag();
            itemStack.addTagElement("Decorations", listTag);
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putByte("type", type.getIcon());
        compoundTag.putString("id", string);
        compoundTag.putDouble("x", blockPos.getX());
        compoundTag.putDouble("z", blockPos.getZ());
        compoundTag.putDouble("rot", 180.0);
        listTag.add(compoundTag);
        if (type.hasMapColor()) {
            CompoundTag compoundTag2 = itemStack.getOrCreateTagElement("display");
            compoundTag2.putInt("MapColor", type.getMapColor());
        }
    }

    private void addDecoration(MapDecoration.Type type, @Nullable LevelAccessor levelAccessor, String string, double d, double e, double f, @Nullable Component component) {
        MapDecoration mapDecoration2;
        MapDecoration mapDecoration;
        byte k;
        int i = 1 << this.scale;
        float g = (float)(d - (double)this.centerX) / (float)i;
        float h = (float)(e - (double)this.centerZ) / (float)i;
        byte b = (byte)((double)(g * 2.0f) + 0.5);
        byte c = (byte)((double)(h * 2.0f) + 0.5);
        int j = 63;
        if (g >= -63.0f && h >= -63.0f && g <= 63.0f && h <= 63.0f) {
            k = (byte)((f += f < 0.0 ? -8.0 : 8.0) * 16.0 / 360.0);
            if (this.dimension == Level.NETHER && levelAccessor != null) {
                l = (int)(levelAccessor.getLevelData().getDayTime() / 10L);
                k = (byte)(l * l * 34187121 + l * 121 >> 15 & 0xF);
            }
        } else if (type == MapDecoration.Type.PLAYER) {
            l = 320;
            if (Math.abs(g) < 320.0f && Math.abs(h) < 320.0f) {
                type = MapDecoration.Type.PLAYER_OFF_MAP;
            } else if (this.unlimitedTracking) {
                type = MapDecoration.Type.PLAYER_OFF_LIMITS;
            } else {
                this.removeDecoration(string);
                return;
            }
            k = 0;
            if (g <= -63.0f) {
                b = -128;
            }
            if (h <= -63.0f) {
                c = -128;
            }
            if (g >= 63.0f) {
                b = 127;
            }
            if (h >= 63.0f) {
                c = 127;
            }
        } else {
            this.removeDecoration(string);
            return;
        }
        if (!(mapDecoration = new MapDecoration(type, b, c, k, component)).equals(mapDecoration2 = this.decorations.put(string, mapDecoration))) {
            if (mapDecoration2 != null && mapDecoration2.getType().shouldTrackCount()) {
                --this.trackedDecorationCount;
            }
            if (type.shouldTrackCount()) {
                ++this.trackedDecorationCount;
            }
            this.setDecorationsDirty();
        }
    }

    @Nullable
    public Packet<?> getUpdatePacket(int i, Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            return null;
        }
        return holdingPlayer.nextUpdatePacket(i);
    }

    private void setColorsDirty(int i, int j) {
        this.setDirty();
        for (HoldingPlayer holdingPlayer : this.carriedBy) {
            holdingPlayer.markColorsDirty(i, j);
        }
    }

    private void setDecorationsDirty() {
        this.setDirty();
        this.carriedBy.forEach(HoldingPlayer::markDecorationsDirty);
    }

    public HoldingPlayer getHoldingPlayer(Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        return holdingPlayer;
    }

    public boolean toggleBanner(LevelAccessor levelAccessor, BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getZ() + 0.5;
        int i = 1 << this.scale;
        double f = (d - (double)this.centerX) / (double)i;
        double g = (e - (double)this.centerZ) / (double)i;
        int j = 63;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapBanner mapBanner = MapBanner.fromWorld(levelAccessor, blockPos);
            if (mapBanner == null) {
                return false;
            }
            if (this.bannerMarkers.remove(mapBanner.getId(), mapBanner)) {
                this.removeDecoration(mapBanner.getId());
                return true;
            }
            if (!this.isTrackedCountOverLimit(256)) {
                this.bannerMarkers.put(mapBanner.getId(), mapBanner);
                this.addDecoration(mapBanner.getDecoration(), levelAccessor, mapBanner.getId(), d, e, 180.0, mapBanner.getName());
                return true;
            }
        }
        return false;
    }

    public void checkBanners(BlockGetter blockGetter, int i, int j) {
        Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();
        while (iterator.hasNext()) {
            MapBanner mapBanner2;
            MapBanner mapBanner = iterator.next();
            if (mapBanner.getPos().getX() != i || mapBanner.getPos().getZ() != j || mapBanner.equals(mapBanner2 = MapBanner.fromWorld(blockGetter, mapBanner.getPos()))) continue;
            iterator.remove();
            this.removeDecoration(mapBanner.getId());
        }
    }

    public Collection<MapBanner> getBanners() {
        return this.bannerMarkers.values();
    }

    public void removedFromFrame(BlockPos blockPos, int i) {
        this.removeDecoration("frame-" + i);
        this.frameMarkers.remove(MapFrame.frameId(blockPos));
    }

    public boolean updateColor(int i, int j, byte b) {
        byte c = this.colors[i + j * 128];
        if (c != b) {
            this.setColor(i, j, b);
            return true;
        }
        return false;
    }

    public void setColor(int i, int j, byte b) {
        this.colors[i + j * 128] = b;
        this.setColorsDirty(i, j);
    }

    public boolean isExplorationMap() {
        for (MapDecoration mapDecoration : this.decorations.values()) {
            if (mapDecoration.getType() != MapDecoration.Type.MANSION && mapDecoration.getType() != MapDecoration.Type.MONUMENT) continue;
            return true;
        }
        return false;
    }

    public void addClientSideDecorations(List<MapDecoration> list) {
        this.decorations.clear();
        this.trackedDecorationCount = 0;
        for (int i = 0; i < list.size(); ++i) {
            MapDecoration mapDecoration = list.get(i);
            this.decorations.put("icon-" + i, mapDecoration);
            if (!mapDecoration.getType().shouldTrackCount()) continue;
            ++this.trackedDecorationCount;
        }
    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean isTrackedCountOverLimit(int i) {
        return this.trackedDecorationCount >= i;
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private boolean dirtyDecorations = true;
        private int tick;
        public int step;

        HoldingPlayer(Player player) {
            this.player = player;
        }

        private MapPatch createPatch() {
            int i = this.minDirtyX;
            int j = this.minDirtyY;
            int k = this.maxDirtyX + 1 - this.minDirtyX;
            int l = this.maxDirtyY + 1 - this.minDirtyY;
            byte[] bs = new byte[k * l];
            for (int m = 0; m < k; ++m) {
                for (int n = 0; n < l; ++n) {
                    bs[m + n * k] = MapItemSavedData.this.colors[i + m + (j + n) * 128];
                }
            }
            return new MapPatch(i, j, k, l, bs);
        }

        @Nullable
        Packet<?> nextUpdatePacket(int i) {
            Collection<MapDecoration> collection;
            MapPatch mapPatch;
            if (this.dirtyData) {
                this.dirtyData = false;
                mapPatch = this.createPatch();
            } else {
                mapPatch = null;
            }
            if (this.dirtyDecorations && this.tick++ % 5 == 0) {
                this.dirtyDecorations = false;
                collection = MapItemSavedData.this.decorations.values();
            } else {
                collection = null;
            }
            if (collection != null || mapPatch != null) {
                return new ClientboundMapItemDataPacket(i, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapPatch);
            }
            return null;
        }

        void markColorsDirty(int i, int j) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, i);
                this.minDirtyY = Math.min(this.minDirtyY, j);
                this.maxDirtyX = Math.max(this.maxDirtyX, i);
                this.maxDirtyY = Math.max(this.maxDirtyY, j);
            } else {
                this.dirtyData = true;
                this.minDirtyX = i;
                this.minDirtyY = j;
                this.maxDirtyX = i;
                this.maxDirtyY = j;
            }
        }

        private void markDecorationsDirty() {
            this.dirtyDecorations = true;
        }
    }

    public static class MapPatch {
        public final int startX;
        public final int startY;
        public final int width;
        public final int height;
        public final byte[] mapColors;

        public MapPatch(int i, int j, int k, int l, byte[] bs) {
            this.startX = i;
            this.startY = j;
            this.width = k;
            this.height = l;
            this.mapColors = bs;
        }

        public void applyToMap(MapItemSavedData mapItemSavedData) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapItemSavedData.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
                }
            }
        }
    }
}

