package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public final class JigsawStructure extends Structure {
	public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
	public static final Codec<JigsawStructure> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						settingsCodec(instance),
						StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(jigsawStructure -> jigsawStructure.startPool),
						ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(jigsawStructure -> jigsawStructure.startJigsawName),
						Codec.intRange(0, 7).fieldOf("size").forGetter(jigsawStructure -> jigsawStructure.maxDepth),
						HeightProvider.CODEC.fieldOf("start_height").forGetter(jigsawStructure -> jigsawStructure.startHeight),
						Codec.BOOL.fieldOf("use_expansion_hack").forGetter(jigsawStructure -> jigsawStructure.useExpansionHack),
						Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(jigsawStructure -> jigsawStructure.projectStartToHeightmap),
						Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(jigsawStructure -> jigsawStructure.maxDistanceFromCenter)
					)
					.apply(instance, JigsawStructure::new)
		)
		.<JigsawStructure>flatXmap(verifyRange(), verifyRange())
		.codec();
	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int maxDepth;
	private final HeightProvider startHeight;
	private final boolean useExpansionHack;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;

	private static Function<JigsawStructure, DataResult<JigsawStructure>> verifyRange() {
		return jigsawStructure -> {
			int i = switch (jigsawStructure.terrainAdaptation()) {
				case NONE -> 0;
				case BURY, BEARD_THIN, BEARD_BOX -> 12;
			};
			return jigsawStructure.maxDistanceFromCenter + i > 128
				? DataResult.error("Structure size including terrain adaptation must not exceed 128")
				: DataResult.success(jigsawStructure);
		};
	}

	public JigsawStructure(
		Structure.StructureSettings structureSettings,
		Holder<StructureTemplatePool> holder,
		Optional<ResourceLocation> optional,
		int i,
		HeightProvider heightProvider,
		boolean bl,
		Optional<Heightmap.Types> optional2,
		int j
	) {
		super(structureSettings);
		this.startPool = holder;
		this.startJigsawName = optional;
		this.maxDepth = i;
		this.startHeight = heightProvider;
		this.useExpansionHack = bl;
		this.projectStartToHeightmap = optional2;
		this.maxDistanceFromCenter = j;
	}

	public JigsawStructure(
		Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> holder, int i, HeightProvider heightProvider, boolean bl, Heightmap.Types types
	) {
		this(structureSettings, holder, Optional.empty(), i, heightProvider, bl, Optional.of(types), 80);
	}

	public JigsawStructure(Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> holder, int i, HeightProvider heightProvider, boolean bl) {
		this(structureSettings, holder, Optional.empty(), i, heightProvider, bl, Optional.empty(), 80);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		ChunkPos chunkPos = generationContext.chunkPos();
		int i = this.startHeight
			.sample(generationContext.random(), new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor()));
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), i, chunkPos.getMinBlockZ());
		Pools.bootstrap();
		return JigsawPlacement.addPieces(
			generationContext,
			this.startPool,
			this.startJigsawName,
			this.maxDepth,
			blockPos,
			this.useExpansionHack,
			this.projectStartToHeightmap,
			this.maxDistanceFromCenter
		);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.JIGSAW;
	}
}
