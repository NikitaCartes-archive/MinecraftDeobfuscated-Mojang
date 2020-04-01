package net.minecraft.world.level.dimension.special;

import com.mojang.math.OctahedralGroup;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.CharFeature;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.phys.Vec3;

public class G07 extends SpecialDimensionBase {
	private static final String[] credits = readCredits();

	public G07(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	private static String[] readCredits() {
		try {
			InputStream inputStream = G07.class.getResourceAsStream("/credits.txt");
			Throwable var1 = null;

			String[] var6;
			try {
				Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				Throwable var3 = null;

				try {
					BufferedReader bufferedReader = new BufferedReader(reader);
					Throwable var5 = null;

					try {
						var6 = (String[])bufferedReader.lines().toArray(String[]::new);
					} catch (Throwable var53) {
						var5 = var53;
						throw var53;
					} finally {
						if (bufferedReader != null) {
							if (var5 != null) {
								try {
									bufferedReader.close();
								} catch (Throwable var52) {
									var5.addSuppressed(var52);
								}
							} else {
								bufferedReader.close();
							}
						}
					}
				} catch (Throwable var55) {
					var3 = var55;
					throw var55;
				} finally {
					if (reader != null) {
						if (var3 != null) {
							try {
								reader.close();
							} catch (Throwable var51) {
								var3.addSuppressed(var51);
							}
						} else {
							reader.close();
						}
					}
				}
			} catch (Throwable var57) {
				var1 = var57;
				throw var57;
			} finally {
				if (inputStream != null) {
					if (var1 != null) {
						try {
							inputStream.close();
						} catch (Throwable var50) {
							var1.addSuppressed(var50);
						}
					} else {
						inputStream.close();
					}
				}
			}

			return var6;
		} catch (IOException var59) {
			return new String[0];
		}
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G07.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 12000.0F;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return false;
	}

	public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
		final SimpleStateProvider provider = new SimpleStateProvider(Blocks.SPONGE.defaultBlockState());

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
		}

		@Override
		public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		}

		@Override
		public int getSpawnHeight() {
			return 30;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
			ChunkPos chunkPos = chunkAccess.getPos();
			int i = chunkPos.x * 2;
			int j = chunkPos.z * 2;
			this.tryPrintChar(chunkAccess, j, i, 0, 0);
			this.tryPrintChar(chunkAccess, j, i, 1, 0);
			this.tryPrintChar(chunkAccess, j, i, 0, 1);
			this.tryPrintChar(chunkAccess, j, i, 1, 1);
		}

		private void tryPrintChar(ChunkAccess chunkAccess, int i, int j, int k, int l) {
			int m = j + k;
			int n = i + l;
			if (n >= 0 && n < G07.credits.length) {
				String string = G07.credits[n];
				if (m >= 0 && m < string.length()) {
					char c = string.charAt(m);
					CharFeature.place(
						new BlockPos(8 * k, 20, 8 * l),
						new CharConfiguration(this.provider, c, OctahedralGroup.ROT_90_X_NEG),
						blockPos -> chunkAccess.setBlockState(blockPos, Blocks.NETHERITE_BLOCK.defaultBlockState(), false)
					);
				}
			}
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
		}

		@Override
		public int getBaseHeight(int i, int j, Heightmap.Types types) {
			return 0;
		}

		@Override
		public BlockGetter getBaseColumn(int i, int j) {
			return EmptyBlockGetter.INSTANCE;
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T07;
		}
	}
}
