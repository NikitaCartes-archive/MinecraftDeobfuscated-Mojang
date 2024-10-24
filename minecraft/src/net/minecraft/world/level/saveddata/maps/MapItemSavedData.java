package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class MapItemSavedData extends SavedData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAP_SIZE = 128;
	private static final int HALF_MAP_SIZE = 64;
	public static final int MAX_SCALE = 4;
	public static final int TRACKED_DECORATION_LIMIT = 256;
	private static final String FRAME_PREFIX = "frame-";
	public final int centerX;
	public final int centerZ;
	public final ResourceKey<Level> dimension;
	private final boolean trackingPosition;
	private final boolean unlimitedTracking;
	public final byte scale;
	public byte[] colors = new byte[16384];
	public final boolean locked;
	private final List<MapItemSavedData.HoldingPlayer> carriedBy = Lists.<MapItemSavedData.HoldingPlayer>newArrayList();
	private final Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers = Maps.<Player, MapItemSavedData.HoldingPlayer>newHashMap();
	private final Map<String, MapBanner> bannerMarkers = Maps.<String, MapBanner>newHashMap();
	final Map<String, MapDecoration> decorations = Maps.<String, MapDecoration>newLinkedHashMap();
	private final Map<String, MapFrame> frameMarkers = Maps.<String, MapFrame>newHashMap();
	private int trackedDecorationCount;

	public static SavedData.Factory<MapItemSavedData> factory() {
		return new SavedData.Factory<>(() -> {
			throw new IllegalStateException("Should never create an empty map saved data");
		}, MapItemSavedData::load, DataFixTypes.SAVED_DATA_MAP_DATA);
	}

	private MapItemSavedData(int i, int j, byte b, boolean bl, boolean bl2, boolean bl3, ResourceKey<Level> resourceKey) {
		this.scale = b;
		this.centerX = i;
		this.centerZ = j;
		this.dimension = resourceKey;
		this.trackingPosition = bl;
		this.unlimitedTracking = bl2;
		this.locked = bl3;
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

	public static MapItemSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		ResourceKey<Level> resourceKey = (ResourceKey<Level>)DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("dimension")))
			.resultOrPartial(LOGGER::error)
			.orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + compoundTag.get("dimension")));
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

		RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);

		for (MapBanner mapBanner : (List)MapBanner.LIST_CODEC
			.parse(registryOps, compoundTag.get("banners"))
			.resultOrPartial(string -> LOGGER.warn("Failed to parse map banner: '{}'", string))
			.orElse(List.of())) {
			mapItemSavedData.bannerMarkers.put(mapBanner.getId(), mapBanner);
			mapItemSavedData.addDecoration(
				mapBanner.getDecoration(),
				null,
				mapBanner.getId(),
				(double)mapBanner.pos().getX(),
				(double)mapBanner.pos().getZ(),
				180.0,
				(Component)mapBanner.name().orElse(null)
			);
		}

		ListTag listTag = compoundTag.getList("frames", 10);

		for (int k = 0; k < listTag.size(); k++) {
			MapFrame mapFrame = MapFrame.load(listTag.getCompound(k));
			if (mapFrame != null) {
				mapItemSavedData.frameMarkers.put(mapFrame.getId(), mapFrame);
				mapItemSavedData.addDecoration(
					MapDecorationTypes.FRAME,
					null,
					getFrameKey(mapFrame.getEntityId()),
					(double)mapFrame.getPos().getX(),
					(double)mapFrame.getPos().getZ(),
					(double)mapFrame.getRotation(),
					null
				);
			}
		}

		return mapItemSavedData;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
		ResourceLocation.CODEC
			.encodeStart(NbtOps.INSTANCE, this.dimension.location())
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("dimension", tag));
		compoundTag.putInt("xCenter", this.centerX);
		compoundTag.putInt("zCenter", this.centerZ);
		compoundTag.putByte("scale", this.scale);
		compoundTag.putByteArray("colors", this.colors);
		compoundTag.putBoolean("trackingPosition", this.trackingPosition);
		compoundTag.putBoolean("unlimitedTracking", this.unlimitedTracking);
		compoundTag.putBoolean("locked", this.locked);
		RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
		compoundTag.put("banners", MapBanner.LIST_CODEC.encodeStart(registryOps, List.copyOf(this.bannerMarkers.values())).getOrThrow());
		ListTag listTag = new ListTag();

		for (MapFrame mapFrame : this.frameMarkers.values()) {
			listTag.add(mapFrame.save());
		}

		compoundTag.put("frames", listTag);
		return compoundTag;
	}

	public MapItemSavedData locked() {
		MapItemSavedData mapItemSavedData = new MapItemSavedData(
			this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension
		);
		mapItemSavedData.bannerMarkers.putAll(this.bannerMarkers);
		mapItemSavedData.decorations.putAll(this.decorations);
		mapItemSavedData.trackedDecorationCount = this.trackedDecorationCount;
		System.arraycopy(this.colors, 0, mapItemSavedData.colors, 0, this.colors.length);
		return mapItemSavedData;
	}

	public MapItemSavedData scaled() {
		return createFresh(
			(double)this.centerX, (double)this.centerZ, (byte)Mth.clamp(this.scale + 1, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension
		);
	}

	private static Predicate<ItemStack> mapMatcher(ItemStack itemStack) {
		MapId mapId = itemStack.get(DataComponents.MAP_ID);
		return itemStack2 -> itemStack2 == itemStack ? true : itemStack2.is(itemStack.getItem()) && Objects.equals(mapId, itemStack2.get(DataComponents.MAP_ID));
	}

	public void tickCarriedBy(Player player, ItemStack itemStack) {
		if (!this.carriedByPlayers.containsKey(player)) {
			MapItemSavedData.HoldingPlayer holdingPlayer = new MapItemSavedData.HoldingPlayer(player);
			this.carriedByPlayers.put(player, holdingPlayer);
			this.carriedBy.add(holdingPlayer);
		}

		Predicate<ItemStack> predicate = mapMatcher(itemStack);
		if (!player.getInventory().contains(predicate)) {
			this.removeDecoration(player.getName().getString());
		}

		for (int i = 0; i < this.carriedBy.size(); i++) {
			MapItemSavedData.HoldingPlayer holdingPlayer2 = (MapItemSavedData.HoldingPlayer)this.carriedBy.get(i);
			Player player2 = holdingPlayer2.player;
			String string = player2.getName().getString();
			if (!player2.isRemoved() && (player2.getInventory().contains(predicate) || itemStack.isFramed())) {
				if (!itemStack.isFramed() && player2.level().dimension() == this.dimension && this.trackingPosition) {
					this.addDecoration(MapDecorationTypes.PLAYER, player2.level(), string, player2.getX(), player2.getZ(), (double)player2.getYRot(), null);
				}
			} else {
				this.carriedByPlayers.remove(player2);
				this.carriedBy.remove(holdingPlayer2);
				this.removeDecoration(string);
			}

			if (!player2.equals(player) && hasMapInvisibilityItemEquipped(player2)) {
				this.removeDecoration(string);
			}
		}

		if (itemStack.isFramed() && this.trackingPosition) {
			ItemFrame itemFrame = itemStack.getFrame();
			BlockPos blockPos = itemFrame.getPos();
			MapFrame mapFrame = (MapFrame)this.frameMarkers.get(MapFrame.frameId(blockPos));
			if (mapFrame != null && itemFrame.getId() != mapFrame.getEntityId() && this.frameMarkers.containsKey(mapFrame.getId())) {
				this.removeDecoration(getFrameKey(mapFrame.getEntityId()));
			}

			MapFrame mapFrame2 = new MapFrame(blockPos, itemFrame.getDirection().get2DDataValue() * 90, itemFrame.getId());
			this.addDecoration(
				MapDecorationTypes.FRAME,
				player.level(),
				getFrameKey(itemFrame.getId()),
				(double)blockPos.getX(),
				(double)blockPos.getZ(),
				(double)(itemFrame.getDirection().get2DDataValue() * 90),
				null
			);
			this.frameMarkers.put(mapFrame2.getId(), mapFrame2);
		}

		MapDecorations mapDecorations = itemStack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
		if (!this.decorations.keySet().containsAll(mapDecorations.decorations().keySet())) {
			mapDecorations.decorations().forEach((stringx, entry) -> {
				if (!this.decorations.containsKey(stringx)) {
					this.addDecoration(entry.type(), player.level(), stringx, entry.x(), entry.z(), (double)entry.rotation(), null);
				}
			});
		}
	}

	private static boolean hasMapInvisibilityItemEquipped(Player player) {
		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			if (equipmentSlot != EquipmentSlot.MAINHAND
				&& equipmentSlot != EquipmentSlot.OFFHAND
				&& player.getItemBySlot(equipmentSlot).is(ItemTags.MAP_INVISIBILITY_EQUIPMENT)) {
				return true;
			}
		}

		return false;
	}

	private void removeDecoration(String string) {
		MapDecoration mapDecoration = (MapDecoration)this.decorations.remove(string);
		if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
			this.trackedDecorationCount--;
		}

		this.setDecorationsDirty();
	}

	public static void addTargetDecoration(ItemStack itemStack, BlockPos blockPos, String string, Holder<MapDecorationType> holder) {
		MapDecorations.Entry entry = new MapDecorations.Entry(holder, (double)blockPos.getX(), (double)blockPos.getZ(), 180.0F);
		itemStack.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, mapDecorations -> mapDecorations.withDecoration(string, entry));
		if (holder.value().hasMapColor()) {
			itemStack.set(DataComponents.MAP_COLOR, new MapItemColor(holder.value().mapColor()));
		}
	}

	private void addDecoration(
		Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, String string, double d, double e, double f, @Nullable Component component
	) {
		int i = 1 << this.scale;
		float g = (float)(d - (double)this.centerX) / (float)i;
		float h = (float)(e - (double)this.centerZ) / (float)i;
		MapItemSavedData.MapDecorationLocation mapDecorationLocation = this.calculateDecorationLocationAndType(holder, levelAccessor, f, g, h);
		if (mapDecorationLocation == null) {
			this.removeDecoration(string);
		} else {
			MapDecoration mapDecoration = new MapDecoration(
				mapDecorationLocation.type(), mapDecorationLocation.x(), mapDecorationLocation.y(), mapDecorationLocation.rot(), Optional.ofNullable(component)
			);
			MapDecoration mapDecoration2 = (MapDecoration)this.decorations.put(string, mapDecoration);
			if (!mapDecoration.equals(mapDecoration2)) {
				if (mapDecoration2 != null && mapDecoration2.type().value().trackCount()) {
					this.trackedDecorationCount--;
				}

				if (mapDecorationLocation.type().value().trackCount()) {
					this.trackedDecorationCount++;
				}

				this.setDecorationsDirty();
			}
		}
	}

	@Nullable
	private MapItemSavedData.MapDecorationLocation calculateDecorationLocationAndType(
		Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, double d, float f, float g
	) {
		byte b = clampMapCoordinate(f);
		byte c = clampMapCoordinate(g);
		if (holder.is(MapDecorationTypes.PLAYER)) {
			Pair<Holder<MapDecorationType>, Byte> pair = this.playerDecorationTypeAndRotation(holder, levelAccessor, d, f, g);
			return pair == null ? null : new MapItemSavedData.MapDecorationLocation(pair.getFirst(), b, c, pair.getSecond());
		} else {
			return !isInsideMap(f, g) && !this.unlimitedTracking
				? null
				: new MapItemSavedData.MapDecorationLocation(holder, b, c, this.calculateRotation(levelAccessor, d));
		}
	}

	@Nullable
	private Pair<Holder<MapDecorationType>, Byte> playerDecorationTypeAndRotation(
		Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, double d, float f, float g
	) {
		if (isInsideMap(f, g)) {
			return Pair.of(holder, this.calculateRotation(levelAccessor, d));
		} else {
			Holder<MapDecorationType> holder2 = this.decorationTypeForPlayerOutsideMap(f, g);
			return holder2 == null ? null : Pair.of(holder2, (byte)0);
		}
	}

	private byte calculateRotation(@Nullable LevelAccessor levelAccessor, double d) {
		if (this.dimension == Level.NETHER && levelAccessor != null) {
			int i = (int)(levelAccessor.getLevelData().getDayTime() / 10L);
			return (byte)(i * i * 34187121 + i * 121 >> 15 & 15);
		} else {
			double e = d < 0.0 ? d - 8.0 : d + 8.0;
			return (byte)((int)(e * 16.0 / 360.0));
		}
	}

	private static boolean isInsideMap(float f, float g) {
		int i = 63;
		return f >= -63.0F && g >= -63.0F && f <= 63.0F && g <= 63.0F;
	}

	@Nullable
	private Holder<MapDecorationType> decorationTypeForPlayerOutsideMap(float f, float g) {
		int i = 320;
		boolean bl = Math.abs(f) < 320.0F && Math.abs(g) < 320.0F;
		if (bl) {
			return MapDecorationTypes.PLAYER_OFF_MAP;
		} else {
			return this.unlimitedTracking ? MapDecorationTypes.PLAYER_OFF_LIMITS : null;
		}
	}

	private static byte clampMapCoordinate(float f) {
		int i = 63;
		if (f <= -63.0F) {
			return -128;
		} else {
			return f >= 63.0F ? 127 : (byte)((int)((double)(f * 2.0F) + 0.5));
		}
	}

	@Nullable
	public Packet<?> getUpdatePacket(MapId mapId, Player player) {
		MapItemSavedData.HoldingPlayer holdingPlayer = (MapItemSavedData.HoldingPlayer)this.carriedByPlayers.get(player);
		return holdingPlayer == null ? null : holdingPlayer.nextUpdatePacket(mapId);
	}

	private void setColorsDirty(int i, int j) {
		this.setDirty();

		for (MapItemSavedData.HoldingPlayer holdingPlayer : this.carriedBy) {
			holdingPlayer.markColorsDirty(i, j);
		}
	}

	private void setDecorationsDirty() {
		this.setDirty();
		this.carriedBy.forEach(MapItemSavedData.HoldingPlayer::markDecorationsDirty);
	}

	public MapItemSavedData.HoldingPlayer getHoldingPlayer(Player player) {
		MapItemSavedData.HoldingPlayer holdingPlayer = (MapItemSavedData.HoldingPlayer)this.carriedByPlayers.get(player);
		if (holdingPlayer == null) {
			holdingPlayer = new MapItemSavedData.HoldingPlayer(player);
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
				this.addDecoration(mapBanner.getDecoration(), levelAccessor, mapBanner.getId(), d, e, 180.0, (Component)mapBanner.name().orElse(null));
				return true;
			}
		}

		return false;
	}

	public void checkBanners(BlockGetter blockGetter, int i, int j) {
		Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();

		while (iterator.hasNext()) {
			MapBanner mapBanner = (MapBanner)iterator.next();
			if (mapBanner.pos().getX() == i && mapBanner.pos().getZ() == j) {
				MapBanner mapBanner2 = MapBanner.fromWorld(blockGetter, mapBanner.pos());
				if (!mapBanner.equals(mapBanner2)) {
					iterator.remove();
					this.removeDecoration(mapBanner.getId());
				}
			}
		}
	}

	public Collection<MapBanner> getBanners() {
		return this.bannerMarkers.values();
	}

	public void removedFromFrame(BlockPos blockPos, int i) {
		this.removeDecoration(getFrameKey(i));
		this.frameMarkers.remove(MapFrame.frameId(blockPos));
		this.setDirty();
	}

	public boolean updateColor(int i, int j, byte b) {
		byte c = this.colors[i + j * 128];
		if (c != b) {
			this.setColor(i, j, b);
			return true;
		} else {
			return false;
		}
	}

	public void setColor(int i, int j, byte b) {
		this.colors[i + j * 128] = b;
		this.setColorsDirty(i, j);
	}

	public boolean isExplorationMap() {
		for (MapDecoration mapDecoration : this.decorations.values()) {
			if (mapDecoration.type().value().explorationMapElement()) {
				return true;
			}
		}

		return false;
	}

	public void addClientSideDecorations(List<MapDecoration> list) {
		this.decorations.clear();
		this.trackedDecorationCount = 0;

		for (int i = 0; i < list.size(); i++) {
			MapDecoration mapDecoration = (MapDecoration)list.get(i);
			this.decorations.put("icon-" + i, mapDecoration);
			if (mapDecoration.type().value().trackCount()) {
				this.trackedDecorationCount++;
			}
		}
	}

	public Iterable<MapDecoration> getDecorations() {
		return this.decorations.values();
	}

	public boolean isTrackedCountOverLimit(int i) {
		return this.trackedDecorationCount >= i;
	}

	private static String getFrameKey(int i) {
		return "frame-" + i;
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

		HoldingPlayer(final Player player) {
			this.player = player;
		}

		private MapItemSavedData.MapPatch createPatch() {
			int i = this.minDirtyX;
			int j = this.minDirtyY;
			int k = this.maxDirtyX + 1 - this.minDirtyX;
			int l = this.maxDirtyY + 1 - this.minDirtyY;
			byte[] bs = new byte[k * l];

			for (int m = 0; m < k; m++) {
				for (int n = 0; n < l; n++) {
					bs[m + n * k] = MapItemSavedData.this.colors[i + m + (j + n) * 128];
				}
			}

			return new MapItemSavedData.MapPatch(i, j, k, l, bs);
		}

		@Nullable
		Packet<?> nextUpdatePacket(MapId mapId) {
			MapItemSavedData.MapPatch mapPatch;
			if (this.dirtyData) {
				this.dirtyData = false;
				mapPatch = this.createPatch();
			} else {
				mapPatch = null;
			}

			Collection<MapDecoration> collection;
			if (this.dirtyDecorations && this.tick++ % 5 == 0) {
				this.dirtyDecorations = false;
				collection = MapItemSavedData.this.decorations.values();
			} else {
				collection = null;
			}

			return collection == null && mapPatch == null
				? null
				: new ClientboundMapItemDataPacket(mapId, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapPatch);
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

	static record MapDecorationLocation(Holder<MapDecorationType> type, byte x, byte y, byte rot) {
	}

	public static record MapPatch(int startX, int startY, int width, int height, byte[] mapColors) {
		public static final StreamCodec<ByteBuf, Optional<MapItemSavedData.MapPatch>> STREAM_CODEC = StreamCodec.of(
			MapItemSavedData.MapPatch::write, MapItemSavedData.MapPatch::read
		);

		private static void write(ByteBuf byteBuf, Optional<MapItemSavedData.MapPatch> optional) {
			if (optional.isPresent()) {
				MapItemSavedData.MapPatch mapPatch = (MapItemSavedData.MapPatch)optional.get();
				byteBuf.writeByte(mapPatch.width);
				byteBuf.writeByte(mapPatch.height);
				byteBuf.writeByte(mapPatch.startX);
				byteBuf.writeByte(mapPatch.startY);
				FriendlyByteBuf.writeByteArray(byteBuf, mapPatch.mapColors);
			} else {
				byteBuf.writeByte(0);
			}
		}

		private static Optional<MapItemSavedData.MapPatch> read(ByteBuf byteBuf) {
			int i = byteBuf.readUnsignedByte();
			if (i > 0) {
				int j = byteBuf.readUnsignedByte();
				int k = byteBuf.readUnsignedByte();
				int l = byteBuf.readUnsignedByte();
				byte[] bs = FriendlyByteBuf.readByteArray(byteBuf);
				return Optional.of(new MapItemSavedData.MapPatch(k, l, i, j, bs));
			} else {
				return Optional.empty();
			}
		}

		public void applyToMap(MapItemSavedData mapItemSavedData) {
			for (int i = 0; i < this.width; i++) {
				for (int j = 0; j < this.height; j++) {
					mapItemSavedData.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
				}
			}
		}
	}
}
