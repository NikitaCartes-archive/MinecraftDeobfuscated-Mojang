package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem extends ComplexItem {
	public static final int IMAGE_WIDTH = 128;
	public static final int IMAGE_HEIGHT = 128;
	private static final int DEFAULT_MAP_COLOR = -12173266;
	private static final String TAG_MAP = "map";

	public MapItem(Item.Properties properties) {
		super(properties);
	}

	public static ItemStack create(Level level, int i, int j, byte b, boolean bl, boolean bl2) {
		ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
		createAndStoreSavedData(itemStack, level, i, j, b, bl, bl2, level.dimension());
		return itemStack;
	}

	@Nullable
	public static MapItemSavedData getSavedData(@Nullable Integer integer, Level level) {
		return integer == null ? null : level.getMapData(makeKey(integer));
	}

	@Nullable
	public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
		Integer integer = getMapId(itemStack);
		return getSavedData(integer, level);
	}

	@Nullable
	public static Integer getMapId(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && compoundTag.contains("map", 99) ? compoundTag.getInt("map") : null;
	}

	private static int createNewSavedData(Level level, int i, int j, int k, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
		MapItemSavedData mapItemSavedData = MapItemSavedData.createFresh((double)i, (double)j, (byte)k, bl, bl2, resourceKey);
		int l = level.getFreeMapId();
		level.setMapData(makeKey(l), mapItemSavedData);
		return l;
	}

	private static void storeMapData(ItemStack itemStack, int i) {
		itemStack.getOrCreateTag().putInt("map", i);
	}

	private static void createAndStoreSavedData(ItemStack itemStack, Level level, int i, int j, int k, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
		int l = createNewSavedData(level, i, j, k, bl, bl2, resourceKey);
		storeMapData(itemStack, l);
	}

	public static String makeKey(int i) {
		return "map_" + i;
	}

	public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
		if (level.dimension() == mapItemSavedData.dimension && entity instanceof Player) {
			int i = 1 << mapItemSavedData.scale;
			int j = mapItemSavedData.x;
			int k = mapItemSavedData.z;
			int l = Mth.floor(entity.getX() - (double)j) / i + 64;
			int m = Mth.floor(entity.getZ() - (double)k) / i + 64;
			int n = 128 / i;
			if (level.dimensionType().hasCeiling()) {
				n /= 2;
			}

			MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player)entity);
			holdingPlayer.step++;
			boolean bl = false;

			for (int o = l - n + 1; o < l + n; o++) {
				if ((o & 15) == (holdingPlayer.step & 15) || bl) {
					bl = false;
					double d = 0.0;

					for (int p = m - n - 1; p < m + n; p++) {
						if (o >= 0 && p >= -1 && o < 128 && p < 128) {
							int q = o - l;
							int r = p - m;
							boolean bl2 = q * q + r * r > (n - 2) * (n - 2);
							int s = (j / i + o - 64) * i;
							int t = (k / i + p - 64) * i;
							Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
							LevelChunk levelChunk = level.getChunkAt(new BlockPos(s, 0, t));
							if (!levelChunk.isEmpty()) {
								ChunkPos chunkPos = levelChunk.getPos();
								int u = s & 15;
								int v = t & 15;
								int w = 0;
								double e = 0.0;
								if (level.dimensionType().hasCeiling()) {
									int x = s + t * 231871;
									x = x * x * 31287121 + x * 11;
									if ((x >> 20 & 1) == 0) {
										multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
									} else {
										multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
									}

									e = 100.0;
								} else {
									BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
									BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

									for (int y = 0; y < i; y++) {
										for (int z = 0; z < i; z++) {
											int aa = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, y + u, z + v) + 1;
											BlockState blockState;
											if (aa <= level.getMinBuildHeight() + 1) {
												blockState = Blocks.BEDROCK.defaultBlockState();
											} else {
												do {
													mutableBlockPos.set(chunkPos.getMinBlockX() + y + u, --aa, chunkPos.getMinBlockZ() + z + v);
													blockState = levelChunk.getBlockState(mutableBlockPos);
												} while (blockState.getMapColor(level, mutableBlockPos) == MaterialColor.NONE && aa > level.getMinBuildHeight());

												if (aa > level.getMinBuildHeight() && !blockState.getFluidState().isEmpty()) {
													int ab = aa - 1;
													mutableBlockPos2.set(mutableBlockPos);

													BlockState blockState2;
													do {
														mutableBlockPos2.setY(ab--);
														blockState2 = levelChunk.getBlockState(mutableBlockPos2);
														w++;
													} while (ab > level.getMinBuildHeight() && !blockState2.getFluidState().isEmpty());

													blockState = this.getCorrectStateForFluidBlock(level, blockState, mutableBlockPos);
												}
											}

											mapItemSavedData.checkBanners(level, chunkPos.getMinBlockX() + y + u, chunkPos.getMinBlockZ() + z + v);
											e += (double)aa / (double)(i * i);
											multiset.add(blockState.getMapColor(level, mutableBlockPos));
										}
									}
								}

								w /= i * i;
								double f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4;
								int y = 1;
								if (f > 0.6) {
									y = 2;
								}

								if (f < -0.6) {
									y = 0;
								}

								MaterialColor materialColor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE);
								if (materialColor == MaterialColor.WATER) {
									f = (double)w * 0.1 + (double)(o + p & 1) * 0.2;
									y = 1;
									if (f < 0.5) {
										y = 2;
									}

									if (f > 0.9) {
										y = 0;
									}
								}

								d = e;
								if (p >= 0 && q * q + r * r < n * n && (!bl2 || (o + p & 1) != 0)) {
									bl |= mapItemSavedData.updateColor(o, p, (byte)(materialColor.id * 4 + y));
								}
							}
						}
					}
				}
			}
		}
	}

	private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos) {
		FluidState fluidState = blockState.getFluidState();
		return !fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP) ? fluidState.createLegacyBlock() : blockState;
	}

	private static boolean isLand(Biome[] biomes, int i, int j, int k) {
		return true;
	}

	public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
		MapItemSavedData mapItemSavedData = getSavedData(itemStack, serverLevel);
		if (mapItemSavedData != null) {
			if (serverLevel.dimension() == mapItemSavedData.dimension) {
				int i = 1 << mapItemSavedData.scale;
				int j = mapItemSavedData.x;
				int k = mapItemSavedData.z;
				Biome[] biomes = new Biome[128 * i * 128 * i];

				for (int l = 0; l < 128 * i; l++) {
					for (int m = 0; m < 128 * i; m++) {
						biomes[l * 128 * i + m] = serverLevel.getBiome(new BlockPos((j / i - 64) * i + m, 0, (k / i - 64) * i + l));
					}
				}

				for (int l = 0; l < 128; l++) {
					for (int m = 0; m < 128; m++) {
						if (l > 0 && m > 0 && l < 127 && m < 127) {
							Biome biome = biomes[l * i + m * i * 128 * i];
							int n = 8;
							if (isLand(biomes, i, l - 1, m - 1)) {
								n--;
							}

							if (isLand(biomes, i, l - 1, m + 1)) {
								n--;
							}

							if (isLand(biomes, i, l - 1, m)) {
								n--;
							}

							if (isLand(biomes, i, l + 1, m - 1)) {
								n--;
							}

							if (isLand(biomes, i, l + 1, m + 1)) {
								n--;
							}

							if (isLand(biomes, i, l + 1, m)) {
								n--;
							}

							if (isLand(biomes, i, l, m - 1)) {
								n--;
							}

							if (isLand(biomes, i, l, m + 1)) {
								n--;
							}

							int o = 3;
							MaterialColor materialColor = MaterialColor.NONE;
							if (n > 0) {
								materialColor = MaterialColor.COLOR_BROWN;
								if (n > 3) {
									o = 1;
								} else {
									o = 3;
								}
							}

							if (materialColor != MaterialColor.NONE) {
								mapItemSavedData.setColor(l, m, (byte)(materialColor.id * 4 + o));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
		if (!level.isClientSide) {
			MapItemSavedData mapItemSavedData = getSavedData(itemStack, level);
			if (mapItemSavedData != null) {
				if (entity instanceof Player player) {
					mapItemSavedData.tickCarriedBy(player, itemStack);
				}

				if (!mapItemSavedData.locked && (bl || entity instanceof Player && ((Player)entity).getOffhandItem() == itemStack)) {
					this.update(level, entity, mapItemSavedData);
				}
			}
		}
	}

	@Nullable
	@Override
	public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
		Integer integer = getMapId(itemStack);
		MapItemSavedData mapItemSavedData = getSavedData(integer, level);
		return mapItemSavedData != null ? mapItemSavedData.getUpdatePacket(integer, player) : null;
	}

	@Override
	public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("map_scale_direction", 99)) {
			scaleMap(itemStack, level, compoundTag.getInt("map_scale_direction"));
			compoundTag.remove("map_scale_direction");
		} else if (compoundTag != null && compoundTag.contains("map_to_lock", 1) && compoundTag.getBoolean("map_to_lock")) {
			lockMap(level, itemStack);
			compoundTag.remove("map_to_lock");
		}
	}

	private static void scaleMap(ItemStack itemStack, Level level, int i) {
		MapItemSavedData mapItemSavedData = getSavedData(itemStack, level);
		if (mapItemSavedData != null) {
			int j = level.getFreeMapId();
			level.setMapData(makeKey(j), mapItemSavedData.scaled(i));
			storeMapData(itemStack, j);
		}
	}

	public static void lockMap(Level level, ItemStack itemStack) {
		MapItemSavedData mapItemSavedData = getSavedData(itemStack, level);
		if (mapItemSavedData != null) {
			int i = level.getFreeMapId();
			String string = makeKey(i);
			MapItemSavedData mapItemSavedData2 = mapItemSavedData.locked();
			level.setMapData(string, mapItemSavedData2);
			storeMapData(itemStack, i);
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		Integer integer = getMapId(itemStack);
		MapItemSavedData mapItemSavedData = level == null ? null : getSavedData(integer, level);
		if (mapItemSavedData != null && mapItemSavedData.locked) {
			list.add(new TranslatableComponent("filled_map.locked", integer).withStyle(ChatFormatting.GRAY));
		}

		if (tooltipFlag.isAdvanced()) {
			if (mapItemSavedData != null) {
				list.add(new TranslatableComponent("filled_map.id", integer).withStyle(ChatFormatting.GRAY));
				list.add(new TranslatableComponent("filled_map.scale", 1 << mapItemSavedData.scale).withStyle(ChatFormatting.GRAY));
				list.add(new TranslatableComponent("filled_map.level", mapItemSavedData.scale, 4).withStyle(ChatFormatting.GRAY));
			} else {
				list.add(new TranslatableComponent("filled_map.unknown").withStyle(ChatFormatting.GRAY));
			}
		}
	}

	public static int getColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTagElement("display");
		if (compoundTag != null && compoundTag.contains("MapColor", 99)) {
			int i = compoundTag.getInt("MapColor");
			return 0xFF000000 | i & 16777215;
		} else {
			return -12173266;
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		BlockState blockState = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
		if (blockState.is(BlockTags.BANNERS)) {
			if (!useOnContext.getLevel().isClientSide) {
				MapItemSavedData mapItemSavedData = getSavedData(useOnContext.getItemInHand(), useOnContext.getLevel());
				if (mapItemSavedData != null && !mapItemSavedData.toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos())) {
					return InteractionResult.FAIL;
				}
			}

			return InteractionResult.sidedSuccess(useOnContext.getLevel().isClientSide);
		} else {
			return super.useOn(useOnContext);
		}
	}
}
