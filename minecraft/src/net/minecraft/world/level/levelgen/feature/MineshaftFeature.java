package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
	public MineshaftFeature(Codec<MineshaftConfiguration> codec) {
		super(codec);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		ChunkPos chunkPos2,
		MineshaftConfiguration mineshaftConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
		double d = (double)mineshaftConfiguration.probability;
		return worldgenRandom.nextDouble() < d;
	}

	@Override
	public StructureFeature.StructureStartFactory<MineshaftConfiguration> getStartFactory() {
		return MineshaftFeature.MineShaftStart::new;
	}

	public static class MineShaftStart extends StructureStart<MineshaftConfiguration> {
		public MineShaftStart(StructureFeature<MineshaftConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			MineshaftConfiguration mineshaftConfiguration,
			LevelHeightAccessor levelHeightAccessor,
			Predicate<Biome> predicate
		) {
			if (predicate.test(
				chunkGenerator.getNoiseBiome(QuartPos.fromBlock(chunkPos.getMiddleBlockX()), QuartPos.fromBlock(50), QuartPos.fromBlock(chunkPos.getMiddleBlockZ()))
			)) {
				MineShaftPieces.MineShaftRoom mineShaftRoom = new MineShaftPieces.MineShaftRoom(
					0, this.random, chunkPos.getBlockX(2), chunkPos.getBlockZ(2), mineshaftConfiguration.type
				);
				this.addPiece(mineShaftRoom);
				mineShaftRoom.addChildren(mineShaftRoom, this, this.random);
				if (mineshaftConfiguration.type == MineshaftFeature.Type.MESA) {
					int i = -5;
					BoundingBox boundingBox = this.getBoundingBox();
					int j = chunkGenerator.getSeaLevel() - boundingBox.maxY() + boundingBox.getYSpan() / 2 - -5;
					this.offsetPiecesVertically(j);
				} else {
					this.moveBelowSeaLevel(chunkGenerator.getSeaLevel(), chunkGenerator.getMinY(), this.random, 10);
				}
			}
		}
	}

	public static enum Type implements StringRepresentable {
		NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
		MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

		public static final Codec<MineshaftFeature.Type> CODEC = StringRepresentable.fromEnum(MineshaftFeature.Type::values, MineshaftFeature.Type::byName);
		private static final Map<String, MineshaftFeature.Type> BY_NAME = (Map<String, MineshaftFeature.Type>)Arrays.stream(values())
			.collect(Collectors.toMap(MineshaftFeature.Type::getName, type -> type));
		private final String name;
		private final BlockState woodState;
		private final BlockState planksState;
		private final BlockState fenceState;

		private Type(String string2, Block block, Block block2, Block block3) {
			this.name = string2;
			this.woodState = block.defaultBlockState();
			this.planksState = block2.defaultBlockState();
			this.fenceState = block3.defaultBlockState();
		}

		public String getName() {
			return this.name;
		}

		private static MineshaftFeature.Type byName(String string) {
			return (MineshaftFeature.Type)BY_NAME.get(string);
		}

		public static MineshaftFeature.Type byId(int i) {
			return i >= 0 && i < values().length ? values()[i] : NORMAL;
		}

		public BlockState getWoodState() {
			return this.woodState;
		}

		public BlockState getPlanksState() {
			return this.planksState;
		}

		public BlockState getFenceState() {
			return this.fenceState;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
