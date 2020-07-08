package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalFeature extends StructureFeature<RuinedPortalConfiguration> {
	private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{
		"ruined_portal/portal_1",
		"ruined_portal/portal_2",
		"ruined_portal/portal_3",
		"ruined_portal/portal_4",
		"ruined_portal/portal_5",
		"ruined_portal/portal_6",
		"ruined_portal/portal_7",
		"ruined_portal/portal_8",
		"ruined_portal/portal_9",
		"ruined_portal/portal_10"
	};
	private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{
		"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"
	};

	public RuinedPortalFeature(Codec<RuinedPortalConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<RuinedPortalConfiguration> getStartFactory() {
		return RuinedPortalFeature.FeatureStart::new;
	}

	private static boolean isCold(BlockPos blockPos, Biome biome) {
		return biome.getTemperature(blockPos) < 0.15F;
	}

	private static int findSuitableY(
		Random random, ChunkGenerator chunkGenerator, RuinedPortalPiece.VerticalPlacement verticalPlacement, boolean bl, int i, int j, BoundingBox boundingBox
	) {
		int k;
		if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
			if (bl) {
				k = randomIntInclusive(random, 32, 100);
			} else if (random.nextFloat() < 0.5F) {
				k = randomIntInclusive(random, 27, 29);
			} else {
				k = randomIntInclusive(random, 29, 100);
			}
		} else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
			int l = i - j;
			k = getRandomWithinInterval(random, 70, l);
		} else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
			int l = i - j;
			k = getRandomWithinInterval(random, 15, l);
		} else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
			k = i - j + randomIntInclusive(random, 2, 8);
		} else {
			k = i;
		}

		List<BlockPos> list = ImmutableList.of(
			new BlockPos(boundingBox.x0, 0, boundingBox.z0),
			new BlockPos(boundingBox.x1, 0, boundingBox.z0),
			new BlockPos(boundingBox.x0, 0, boundingBox.z1),
			new BlockPos(boundingBox.x1, 0, boundingBox.z1)
		);
		List<BlockGetter> list2 = (List<BlockGetter>)list.stream()
			.map(blockPos -> chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ()))
			.collect(Collectors.toList());
		Heightmap.Types types = verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR
			? Heightmap.Types.OCEAN_FLOOR_WG
			: Heightmap.Types.WORLD_SURFACE_WG;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		int m;
		for (m = k; m > 15; m--) {
			int n = 0;
			mutableBlockPos.set(0, m, 0);

			for (BlockGetter blockGetter : list2) {
				BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
				if (blockState != null && types.isOpaque().test(blockState)) {
					if (++n == 3) {
						return m;
					}
				}
			}
		}

		return m;
	}

	private static int randomIntInclusive(Random random, int i, int j) {
		return random.nextInt(j - i + 1) + i;
	}

	private static int getRandomWithinInterval(Random random, int i, int j) {
		return i < j ? randomIntInclusive(random, i, j) : j;
	}

	public static class FeatureStart extends StructureStart<RuinedPortalConfiguration> {
		protected FeatureStart(StructureFeature<RuinedPortalConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			RuinedPortalConfiguration ruinedPortalConfiguration
		) {
			RuinedPortalPiece.Properties properties = new RuinedPortalPiece.Properties();
			RuinedPortalPiece.VerticalPlacement verticalPlacement;
			if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.DESERT) {
				verticalPlacement = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
				properties.airPocket = false;
				properties.mossiness = 0.0F;
			} else if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.JUNGLE) {
				verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
				properties.airPocket = this.random.nextFloat() < 0.5F;
				properties.mossiness = 0.8F;
				properties.overgrown = true;
				properties.vines = true;
			} else if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.SWAMP) {
				verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
				properties.airPocket = false;
				properties.mossiness = 0.5F;
				properties.vines = true;
			} else if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.MOUNTAIN) {
				boolean bl = this.random.nextFloat() < 0.5F;
				verticalPlacement = bl ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
				properties.airPocket = bl || this.random.nextFloat() < 0.5F;
			} else if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.OCEAN) {
				verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
				properties.airPocket = false;
				properties.mossiness = 0.8F;
			} else if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.NETHER) {
				verticalPlacement = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
				properties.airPocket = this.random.nextFloat() < 0.5F;
				properties.mossiness = 0.0F;
				properties.replaceWithBlackstone = true;
			} else {
				boolean bl = this.random.nextFloat() < 0.5F;
				verticalPlacement = bl ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
				properties.airPocket = bl || this.random.nextFloat() < 0.5F;
			}

			ResourceLocation resourceLocation;
			if (this.random.nextFloat() < 0.05F) {
				resourceLocation = new ResourceLocation(
					RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS.length)]
				);
			} else {
				resourceLocation = new ResourceLocation(
					RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS.length)]
				);
			}

			StructureTemplate structureTemplate = structureManager.getOrCreate(resourceLocation);
			Rotation rotation = Util.getRandom(Rotation.values(), this.random);
			Mirror mirror = this.random.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
			BlockPos blockPos = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
			BlockPos blockPos2 = new ChunkPos(i, j).getWorldPosition();
			BoundingBox boundingBox = structureTemplate.getBoundingBox(blockPos2, rotation, blockPos, mirror);
			Vec3i vec3i = boundingBox.getCenter();
			int k = vec3i.getX();
			int l = vec3i.getZ();
			int m = chunkGenerator.getBaseHeight(k, l, RuinedPortalPiece.getHeightMapType(verticalPlacement)) - 1;
			int n = RuinedPortalFeature.findSuitableY(this.random, chunkGenerator, verticalPlacement, properties.airPocket, m, boundingBox.getYSpan(), boundingBox);
			BlockPos blockPos3 = new BlockPos(blockPos2.getX(), n, blockPos2.getZ());
			if (ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.MOUNTAIN
				|| ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.OCEAN
				|| ruinedPortalConfiguration.portalType == RuinedPortalFeature.Type.STANDARD) {
				properties.cold = RuinedPortalFeature.isCold(blockPos3, biome);
			}

			this.pieces.add(new RuinedPortalPiece(blockPos3, verticalPlacement, properties, resourceLocation, structureTemplate, rotation, mirror, blockPos));
			this.calculateBoundingBox();
		}
	}

	public static enum Type implements StringRepresentable {
		STANDARD("standard"),
		DESERT("desert"),
		JUNGLE("jungle"),
		SWAMP("swamp"),
		MOUNTAIN("mountain"),
		OCEAN("ocean"),
		NETHER("nether");

		public static final Codec<RuinedPortalFeature.Type> CODEC = StringRepresentable.fromEnum(RuinedPortalFeature.Type::values, RuinedPortalFeature.Type::byName);
		private static final Map<String, RuinedPortalFeature.Type> BY_NAME = (Map<String, RuinedPortalFeature.Type>)Arrays.stream(values())
			.collect(Collectors.toMap(RuinedPortalFeature.Type::getName, type -> type));
		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		public static RuinedPortalFeature.Type byName(String string) {
			return (RuinedPortalFeature.Type)BY_NAME.get(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
