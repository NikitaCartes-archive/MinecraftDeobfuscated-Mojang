/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Dynamic;
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
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class MapItemSavedData
extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    public int x;
    public int z;
    public ResourceKey<Level> dimension;
    public boolean trackingPosition;
    public boolean unlimitedTracking;
    public byte scale;
    public byte[] colors = new byte[16384];
    public boolean locked;
    public final List<HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    public final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();

    public MapItemSavedData(String string) {
        super(string);
    }

    public void setProperties(int i, int j, int k, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        this.scale = (byte)k;
        this.setOrigin(i, j, this.scale);
        this.dimension = resourceKey;
        this.trackingPosition = bl;
        this.unlimitedTracking = bl2;
        this.setDirty();
    }

    public void setOrigin(double d, double e, int i) {
        int j = 128 * (1 << i);
        int k = Mth.floor((d + 64.0) / (double)j);
        int l = Mth.floor((e + 64.0) / (double)j);
        this.x = k * j + j / 2 - 64;
        this.z = l * j + j / 2 - 64;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.dimension = DimensionType.parseLegacy(new Dynamic<Tag>(NbtOps.INSTANCE, compoundTag.get("dimension"))).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + compoundTag.get("dimension")));
        this.x = compoundTag.getInt("xCenter");
        this.z = compoundTag.getInt("zCenter");
        this.scale = (byte)Mth.clamp(compoundTag.getByte("scale"), 0, 4);
        this.trackingPosition = !compoundTag.contains("trackingPosition", 1) || compoundTag.getBoolean("trackingPosition");
        this.unlimitedTracking = compoundTag.getBoolean("unlimitedTracking");
        this.locked = compoundTag.getBoolean("locked");
        this.colors = compoundTag.getByteArray("colors");
        if (this.colors.length != 16384) {
            this.colors = new byte[16384];
        }
        ListTag listTag = compoundTag.getList("banners", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            MapBanner mapBanner = MapBanner.load(listTag.getCompound(i));
            this.bannerMarkers.put(mapBanner.getId(), mapBanner);
            this.addDecoration(mapBanner.getDecoration(), null, mapBanner.getId(), mapBanner.getPos().getX(), mapBanner.getPos().getZ(), 180.0, mapBanner.getName());
        }
        ListTag listTag2 = compoundTag.getList("frames", 10);
        for (int j = 0; j < listTag2.size(); ++j) {
            MapFrame mapFrame = MapFrame.load(listTag2.getCompound(j));
            this.frameMarkers.put(mapFrame.getId(), mapFrame);
            this.addDecoration(MapDecoration.Type.FRAME, null, "frame-" + mapFrame.getEntityId(), mapFrame.getPos().getX(), mapFrame.getPos().getZ(), mapFrame.getRotation(), null);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.location()).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("dimension", (Tag)tag));
        compoundTag.putInt("xCenter", this.x);
        compoundTag.putInt("zCenter", this.z);
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

    public void lockData(MapItemSavedData mapItemSavedData) {
        this.locked = true;
        this.x = mapItemSavedData.x;
        this.z = mapItemSavedData.z;
        this.bannerMarkers.putAll(mapItemSavedData.bannerMarkers);
        this.decorations.putAll(mapItemSavedData.decorations);
        System.arraycopy(mapItemSavedData.colors, 0, this.colors, 0, mapItemSavedData.colors.length);
        this.setDirty();
    }

    public void tickCarriedBy(Player player, ItemStack itemStack) {
        CompoundTag compoundTag;
        if (!this.carriedByPlayers.containsKey(player)) {
            HoldingPlayer holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        if (!player.getInventory().contains(itemStack)) {
            this.decorations.remove(player.getName().getString());
        }
        for (int i = 0; i < this.carriedBy.size(); ++i) {
            HoldingPlayer holdingPlayer2 = this.carriedBy.get(i);
            String string = holdingPlayer2.player.getName().getString();
            if (holdingPlayer2.player.isRemoved() || !holdingPlayer2.player.getInventory().contains(itemStack) && !itemStack.isFramed()) {
                this.carriedByPlayers.remove(holdingPlayer2.player);
                this.carriedBy.remove(holdingPlayer2);
                this.decorations.remove(string);
                continue;
            }
            if (itemStack.isFramed() || holdingPlayer2.player.level.dimension() != this.dimension || !this.trackingPosition) continue;
            this.addDecoration(MapDecoration.Type.PLAYER, holdingPlayer2.player.level, string, holdingPlayer2.player.getX(), holdingPlayer2.player.getZ(), holdingPlayer2.player.yRot, null);
        }
        if (itemStack.isFramed() && this.trackingPosition) {
            ItemFrame itemFrame = itemStack.getFrame();
            BlockPos blockPos = itemFrame.getPos();
            MapFrame mapFrame = this.frameMarkers.get(MapFrame.frameId(blockPos));
            if (mapFrame != null && itemFrame.getId() != mapFrame.getEntityId() && this.frameMarkers.containsKey(mapFrame.getId())) {
                this.decorations.remove("frame-" + mapFrame.getEntityId());
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
        byte k;
        int i = 1 << this.scale;
        float g = (float)(d - (double)this.x) / (float)i;
        float h = (float)(e - (double)this.z) / (float)i;
        byte b = (byte)((double)(g * 2.0f) + 0.5);
        byte c = (byte)((double)(h * 2.0f) + 0.5);
        int j = 63;
        if (g >= -63.0f && h >= -63.0f && g <= 63.0f && h <= 63.0f) {
            k = (byte)((f += f < 0.0 ? -8.0 : 8.0) * 16.0 / 360.0);
            if (this.dimension == Level.NETHER && levelAccessor != null) {
                int l = (int)(levelAccessor.getLevelData().getDayTime() / 10L);
                k = (byte)(l * l * 34187121 + l * 121 >> 15 & 0xF);
            }
        } else if (type == MapDecoration.Type.PLAYER) {
            int l = 320;
            if (Math.abs(g) < 320.0f && Math.abs(h) < 320.0f) {
                type = MapDecoration.Type.PLAYER_OFF_MAP;
            } else if (this.unlimitedTracking) {
                type = MapDecoration.Type.PLAYER_OFF_LIMITS;
            } else {
                this.decorations.remove(string);
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
            this.decorations.remove(string);
            return;
        }
        this.decorations.put(string, new MapDecoration(type, b, c, k, component));
    }

    @Nullable
    public Packet<?> getUpdatePacket(ItemStack itemStack, BlockGetter blockGetter, Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            return null;
        }
        return holdingPlayer.nextUpdatePacket(itemStack);
    }

    public void setDirty(int i, int j) {
        this.setDirty();
        for (HoldingPlayer holdingPlayer : this.carriedBy) {
            holdingPlayer.markDirty(i, j);
        }
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

    public void toggleBanner(LevelAccessor levelAccessor, BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getZ() + 0.5;
        int i = 1 << this.scale;
        double f = (d - (double)this.x) / (double)i;
        double g = (e - (double)this.z) / (double)i;
        int j = 63;
        boolean bl = false;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapBanner mapBanner = MapBanner.fromWorld(levelAccessor, blockPos);
            if (mapBanner == null) {
                return;
            }
            boolean bl2 = true;
            if (this.bannerMarkers.containsKey(mapBanner.getId()) && this.bannerMarkers.get(mapBanner.getId()).equals(mapBanner)) {
                this.bannerMarkers.remove(mapBanner.getId());
                this.decorations.remove(mapBanner.getId());
                bl2 = false;
                bl = true;
            }
            if (bl2) {
                this.bannerMarkers.put(mapBanner.getId(), mapBanner);
                this.addDecoration(mapBanner.getDecoration(), levelAccessor, mapBanner.getId(), d, e, 180.0, mapBanner.getName());
                bl = true;
            }
            if (bl) {
                this.setDirty();
            }
        }
    }

    public void checkBanners(BlockGetter blockGetter, int i, int j) {
        Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();
        while (iterator.hasNext()) {
            MapBanner mapBanner2;
            MapBanner mapBanner = iterator.next();
            if (mapBanner.getPos().getX() != i || mapBanner.getPos().getZ() != j || mapBanner.equals(mapBanner2 = MapBanner.fromWorld(blockGetter, mapBanner.getPos()))) continue;
            iterator.remove();
            this.decorations.remove(mapBanner.getId());
        }
    }

    public void removedFromFrame(BlockPos blockPos, int i) {
        this.decorations.remove("frame-" + i);
        this.frameMarkers.remove(MapFrame.frameId(blockPos));
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private int tick;
        public int step;

        public HoldingPlayer(Player player) {
            this.player = player;
        }

        @Nullable
        public Packet<?> nextUpdatePacket(ItemStack itemStack) {
            if (this.dirtyData) {
                this.dirtyData = false;
                return new ClientboundMapItemDataPacket(MapItem.getMapId(itemStack), MapItemSavedData.this.scale, MapItemSavedData.this.trackingPosition, MapItemSavedData.this.locked, MapItemSavedData.this.decorations.values(), MapItemSavedData.this.colors, this.minDirtyX, this.minDirtyY, this.maxDirtyX + 1 - this.minDirtyX, this.maxDirtyY + 1 - this.minDirtyY);
            }
            if (this.tick++ % 5 == 0) {
                return new ClientboundMapItemDataPacket(MapItem.getMapId(itemStack), MapItemSavedData.this.scale, MapItemSavedData.this.trackingPosition, MapItemSavedData.this.locked, MapItemSavedData.this.decorations.values(), MapItemSavedData.this.colors, 0, 0, 0, 0);
            }
            return null;
        }

        public void markDirty(int i, int j) {
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
    }
}

