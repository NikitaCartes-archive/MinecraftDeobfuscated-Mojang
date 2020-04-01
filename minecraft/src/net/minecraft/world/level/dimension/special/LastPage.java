package net.minecraft.world.level.dimension.special;

import com.mojang.math.OctahedralGroup;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
import net.minecraft.world.level.dimension.DimHash;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.CharFeature;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LastPage extends SpecialDimensionBase {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final byte[] DATA = new byte[]{
		72,
		-71,
		33,
		-116,
		61,
		25,
		-105,
		61,
		69,
		-34,
		22,
		-96,
		83,
		-41,
		4,
		100,
		49,
		-120,
		-76,
		-32,
		-24,
		105,
		-103,
		57,
		-101,
		-101,
		114,
		39,
		45,
		-48,
		-58,
		106,
		-83,
		72,
		-120,
		-98,
		14,
		111,
		-73,
		38,
		-43,
		-29,
		-17,
		-64,
		-48,
		-21,
		-63,
		-14,
		7,
		-65,
		-115,
		61,
		-62,
		-121,
		108,
		-2,
		24,
		84,
		-62,
		117,
		115,
		52,
		-88,
		18,
		30,
		115,
		44,
		-113,
		-123,
		88,
		77,
		-122,
		15,
		-13,
		123,
		85,
		-118,
		-12,
		-30,
		7,
		104,
		-75,
		-84,
		-57,
		-124,
		-113,
		-38,
		84,
		52,
		6,
		-94,
		67,
		76,
		59,
		105,
		82,
		92,
		-65,
		-52,
		-26,
		-46,
		45,
		94,
		47,
		10,
		-14,
		-86,
		-12,
		1,
		111,
		-107,
		-119,
		-115,
		32,
		-60,
		-92,
		107,
		-2,
		73,
		109,
		-128,
		-107,
		-52,
		-7,
		-6,
		126,
		98,
		-71,
		-92,
		12,
		-41,
		83,
		-124,
		14,
		-51,
		-15,
		4,
		-3,
		-65,
		-36,
		99,
		63,
		119,
		64,
		46,
		21,
		10,
		30,
		-23,
		-10,
		-90,
		-36,
		-4,
		-106,
		-102,
		84,
		-17,
		58,
		59,
		-76,
		-103,
		-28,
		-95,
		4,
		112,
		18,
		3,
		-78,
		125,
		-79,
		11,
		120,
		-59,
		-64,
		-37,
		-47,
		19,
		-21,
		90,
		-9,
		-65,
		109,
		70,
		-83,
		-4,
		34,
		41,
		-109,
		27,
		-20,
		29,
		60,
		109,
		-117,
		74,
		-112,
		-58,
		76,
		96,
		9,
		-65,
		86,
		63,
		62,
		112,
		-88,
		96,
		-35,
		64,
		57,
		35,
		89,
		-24,
		-40,
		121,
		106,
		-102,
		-103,
		-24,
		-73,
		103,
		-110,
		56,
		97,
		-82,
		55,
		-53,
		-100,
		22,
		-68,
		104,
		8,
		98,
		-120,
		-65,
		-30,
		38,
		114,
		-59,
		30,
		66,
		-119,
		59,
		-93,
		107,
		-50,
		115,
		40,
		80,
		77,
		-61,
		-102,
		-62,
		-110,
		-80,
		-85,
		19,
		123,
		-120,
		70,
		-119,
		11,
		63,
		30,
		92,
		73,
		81,
		-19,
		-14,
		122,
		-103,
		-108,
		38,
		-116,
		-100,
		50,
		-121,
		-7,
		-125,
		61,
		-44,
		-38,
		-117,
		16,
		14,
		-101,
		79,
		-96,
		89,
		12,
		84,
		-36,
		42,
		-21,
		-109,
		-7,
		117,
		64,
		38,
		18,
		-97,
		-58,
		73,
		2,
		41,
		70,
		-85,
		75,
		6,
		123,
		76,
		-66,
		53,
		-41,
		25,
		-14,
		-104,
		-19,
		67,
		-28,
		-9,
		-111,
		59,
		-109,
		35,
		57,
		108,
		100,
		40,
		116,
		-106,
		-128,
		2,
		109,
		-75,
		3,
		19,
		87,
		-120,
		59,
		-20,
		-15,
		74,
		-40,
		106,
		-3,
		-122,
		19,
		-94,
		53,
		-103,
		-60,
		-36,
		2,
		52,
		31,
		63,
		17,
		-32,
		-61,
		-116,
		5,
		9,
		117,
		-72,
		-28,
		-125,
		99,
		-54,
		-126,
		96,
		21,
		29,
		38,
		35,
		90,
		-32,
		89,
		48,
		108,
		10,
		-52,
		-117,
		2,
		-74,
		-122,
		-21,
		119,
		126,
		-110,
		-115,
		57,
		-119,
		-53,
		43,
		-128,
		10,
		97,
		122,
		126,
		-111,
		103,
		113,
		90,
		101,
		44,
		9,
		5,
		102,
		88,
		-24,
		-108,
		-8,
		42,
		65,
		46
	};
	private static final byte[] IVS = new byte[]{-114, 123, -36, 36, 6, 2, 31, 116, -76, -125, -62, -61, -41, -121, 82, -106};

	public LastPage(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new LastPage.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE, DimHash.getLastPasshphrase());
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
		private final String[] decodedMessage;
		final SimpleStateProvider provider = new SimpleStateProvider(Blocks.SPONGE.defaultBlockState());

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			this(levelAccessor, biomeSource, noneGeneratorSettings, "");
		}

		private Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings, String string) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
			this.decodedMessage = decodeMessage(string);
		}

		private static String[] decodeMessage(String string) {
			try {
				SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec keySpec = new PBEKeySpec(string.toCharArray(), "pinch_of_salt".getBytes(StandardCharsets.UTF_8), 65536, 128);
				SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
				SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				IvParameterSpec ivParameterSpec = new IvParameterSpec(LastPage.IVS);
				cipher.init(2, secretKeySpec, ivParameterSpec);
				byte[] bs = cipher.doFinal(LastPage.DATA);
				return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bs)).toString().split("\n");
			} catch (Exception var8) {
				LastPage.LOGGER.warn("No.", (Throwable)var8);
				return new String[]{"Uh uh uh! You didn't say the magic word!"};
			}
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
			if (n >= 0 && n < this.decodedMessage.length) {
				String string = this.decodedMessage[n];
				if (m >= 0 && m < string.length()) {
					char c = string.charAt(m);
					CharFeature.place(
						new BlockPos(8 * k, 20, 8 * l),
						new CharConfiguration(this.provider, c, OctahedralGroup.ROT_90_X_NEG),
						blockPos -> chunkAccess.setBlockState(blockPos, Blocks.GRASS_BLOCK.defaultBlockState(), false)
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
			return ChunkGeneratorType.T27;
		}
	}
}
