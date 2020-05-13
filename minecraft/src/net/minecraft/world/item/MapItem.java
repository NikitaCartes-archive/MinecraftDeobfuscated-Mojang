package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem extends ComplexItem {
	public MapItem(Item.Properties properties) {
		super(properties);
	}

	public static ItemStack create(Level level, int i, int j, byte b, boolean bl, boolean bl2) {
		ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
		createAndStoreSavedData(itemStack, level, i, j, b, bl, bl2, level.dimensionType());
		return itemStack;
	}

	@Nullable
	public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
		return level.getMapData(makeKey(getMapId(itemStack)));
	}

	@Nullable
	public static MapItemSavedData getOrCreateSavedData(ItemStack itemStack, Level level) {
		MapItemSavedData mapItemSavedData = getSavedData(itemStack, level);
		if (mapItemSavedData == null && level instanceof ServerLevel) {
			mapItemSavedData = createAndStoreSavedData(
				itemStack, level, level.getLevelData().getXSpawn(), level.getLevelData().getZSpawn(), 3, false, false, level.dimensionType()
			);
		}

		return mapItemSavedData;
	}

	public static int getMapId(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && compoundTag.contains("map", 99) ? compoundTag.getInt("map") : 0;
	}

	private static MapItemSavedData createAndStoreSavedData(
		ItemStack itemStack, Level level, int i, int j, int k, boolean bl, boolean bl2, DimensionType dimensionType
	) {
		int l = level.getFreeMapId();
		MapItemSavedData mapItemSavedData = new MapItemSavedData(makeKey(l));
		mapItemSavedData.setProperties(i, j, k, bl, bl2, dimensionType);
		level.setMapData(mapItemSavedData);
		itemStack.getOrCreateTag().putInt("map", l);
		return mapItemSavedData;
	}

	public static String makeKey(int i) {
		return "map_" + i;
	}

	public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
		if (level.dimensionType() == mapItemSavedData.dimension && entity instanceof Player) {
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
											if (aa <= 1) {
												blockState = Blocks.BEDROCK.defaultBlockState();
											} else {
												do {
													mutableBlockPos.set(chunkPos.getMinBlockX() + y + u, --aa, chunkPos.getMinBlockZ() + z + v);
													blockState = levelChunk.getBlockState(mutableBlockPos);
												} while (blockState.getMapColor(level, mutableBlockPos) == MaterialColor.NONE && aa > 0);

												if (aa > 0 && !blockState.getFluidState().isEmpty()) {
													int ab = aa - 1;
													mutableBlockPos2.set(mutableBlockPos);

													BlockState blockState2;
													do {
														mutableBlockPos2.setY(ab--);
														blockState2 = levelChunk.getBlockState(mutableBlockPos2);
														w++;
													} while (ab > 0 && !blockState2.getFluidState().isEmpty());

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
									byte b = mapItemSavedData.colors[o + p * 128];
									byte c = (byte)(materialColor.id * 4 + y);
									if (b != c) {
										mapItemSavedData.colors[o + p * 128] = c;
										mapItemSavedData.setDirty(o, p);
										bl = true;
									}
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
		return biomes[j * i + k * i * 128 * i].getDepth() >= 0.0F;
	}

	public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
		MapItemSavedData mapItemSavedData = getOrCreateSavedData(itemStack, serverLevel);
		if (mapItemSavedData != null) {
			if (serverLevel.dimensionType() == mapItemSavedData.dimension) {
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
							if (biome.getDepth() < 0.0F) {
								materialColor = MaterialColor.COLOR_ORANGE;
								if (n > 7 && m % 2 == 0) {
									o = (l + (int)(Mth.sin((float)m + 0.0F) * 7.0F)) / 8 % 5;
									if (o == 3) {
										o = 1;
									} else if (o == 4) {
										o = 0;
									}
								} else if (n > 7) {
									materialColor = MaterialColor.NONE;
								} else if (n > 5) {
									o = 1;
								} else if (n > 3) {
									o = 0;
								} else if (n > 1) {
									o = 0;
								}
							} else if (n > 0) {
								materialColor = MaterialColor.COLOR_BROWN;
								if (n > 3) {
									o = 1;
								} else {
									o = 3;
								}
							}

							if (materialColor != MaterialColor.NONE) {
								mapItemSavedData.colors[l + m * 128] = (byte)(materialColor.id * 4 + o);
								mapItemSavedData.setDirty(l, m);
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
			MapItemSavedData mapItemSavedData = getOrCreateSavedData(itemStack, level);
			if (mapItemSavedData != null) {
				if (entity instanceof Player) {
					Player player = (Player)entity;
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
		return getOrCreateSavedData(itemStack, level).getUpdatePacket(itemStack, level, player);
	}

	@Override
	public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("map_scale_direction", 99)) {
			scaleMap(itemStack, level, compoundTag.getInt("map_scale_direction"));
			compoundTag.remove("map_scale_direction");
		}
	}

	protected static void scaleMap(ItemStack itemStack, Level level, int i) {
		MapItemSavedData mapItemSavedData = getOrCreateSavedData(itemStack, level);
		if (mapItemSavedData != null) {
			createAndStoreSavedData(
				itemStack,
				level,
				mapItemSavedData.x,
				mapItemSavedData.z,
				Mth.clamp(mapItemSavedData.scale + i, 0, 4),
				mapItemSavedData.trackingPosition,
				mapItemSavedData.unlimitedTracking,
				mapItemSavedData.dimension
			);
		}
	}

	@Nullable
	public static ItemStack lockMap(Level level, ItemStack itemStack) {
		MapItemSavedData mapItemSavedData = getOrCreateSavedData(itemStack, level);
		if (mapItemSavedData != null) {
			ItemStack itemStack2 = itemStack.copy();
			MapItemSavedData mapItemSavedData2 = createAndStoreSavedData(
				itemStack2, level, 0, 0, mapItemSavedData.scale, mapItemSavedData.trackingPosition, mapItemSavedData.unlimitedTracking, mapItemSavedData.dimension
			);
			mapItemSavedData2.lockData(mapItemSavedData);
			return itemStack2;
		} else {
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		MapItemSavedData mapItemSavedData = level == null ? null : getOrCreateSavedData(itemStack, level);
		if (mapItemSavedData != null && mapItemSavedData.locked) {
			list.add(new TranslatableComponent("filled_map.locked", getMapId(itemStack)).withStyle(ChatFormatting.GRAY));
		}

		if (tooltipFlag.isAdvanced()) {
			if (mapItemSavedData != null) {
				list.add(new TranslatableComponent("filled_map.id", getMapId(itemStack)).withStyle(ChatFormatting.GRAY));
				list.add(new TranslatableComponent("filled_map.scale", 1 << mapItemSavedData.scale).withStyle(ChatFormatting.GRAY));
				list.add(new TranslatableComponent("filled_map.level", mapItemSavedData.scale, 4).withStyle(ChatFormatting.GRAY));
			} else {
				list.add(new TranslatableComponent("filled_map.unknown").withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Environment(EnvType.CLIENT)
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
			if (!useOnContext.level.isClientSide) {
				MapItemSavedData mapItemSavedData = getOrCreateSavedData(useOnContext.getItemInHand(), useOnContext.getLevel());
				mapItemSavedData.toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos());
			}

			return InteractionResult.SUCCESS;
		} else {
			return super.useOn(useOnContext);
		}
	}
}
