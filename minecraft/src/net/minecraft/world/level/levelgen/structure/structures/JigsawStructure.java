package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.Products.P4;
import com.mojang.datafixers.Products.P5;
import com.mojang.datafixers.Products.P9;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public final class JigsawStructure extends Structure {
	public static final Codec<JigsawStructure> CODEC = RecordCodecBuilder.create(instance -> jigsawCodec(instance).apply(instance, JigsawStructure::new));
	private final Holder<StructureTemplatePool> startPool;
	private final int maxDepth;
	private final HeightProvider startHeight;
	private final boolean useExpansionHack;
	private final Optional<Heightmap.Types> projectStartToHeightmap;

	public static P9<Mu<JigsawStructure>, HolderSet<Biome>, Map<MobCategory, StructureSpawnOverride>, GenerationStep.Decoration, Boolean, Holder<StructureTemplatePool>, Integer, HeightProvider, Boolean, Optional<Heightmap.Types>> jigsawCodec(
		Instance<JigsawStructure> instance
	) {
		P4<Mu<JigsawStructure>, HolderSet<Biome>, Map<MobCategory, StructureSpawnOverride>, GenerationStep.Decoration, Boolean> p4 = codec(instance);
		P5<Mu<JigsawStructure>, Holder<StructureTemplatePool>, Integer, HeightProvider, Boolean, Optional<Heightmap.Types>> p5 = instance.group(
			StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(jigsawStructure -> jigsawStructure.startPool),
			Codec.intRange(0, 7).fieldOf("size").forGetter(jigsawStructure -> jigsawStructure.maxDepth),
			HeightProvider.CODEC.fieldOf("start_height").forGetter(jigsawStructure -> jigsawStructure.startHeight),
			Codec.BOOL.fieldOf("use_expansion_hack").forGetter(jigsawStructure -> jigsawStructure.useExpansionHack),
			Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(jigsawStructure -> jigsawStructure.projectStartToHeightmap)
		);
		return new P9<>(p4.t1(), p4.t2(), p4.t3(), p4.t4(), p5.t1(), p5.t2(), p5.t3(), p5.t4(), p5.t5());
	}

	public JigsawStructure(
		HolderSet<Biome> holderSet,
		Map<MobCategory, StructureSpawnOverride> map,
		GenerationStep.Decoration decoration,
		boolean bl,
		Holder<StructureTemplatePool> holder,
		int i,
		HeightProvider heightProvider,
		boolean bl2,
		Optional<Heightmap.Types> optional
	) {
		super(holderSet, map, decoration, bl);
		this.startPool = holder;
		this.maxDepth = i;
		this.startHeight = heightProvider;
		this.useExpansionHack = bl2;
		this.projectStartToHeightmap = optional;
	}

	public JigsawStructure(
		HolderSet<Biome> holderSet,
		Map<MobCategory, StructureSpawnOverride> map,
		GenerationStep.Decoration decoration,
		boolean bl,
		Holder<StructureTemplatePool> holder,
		int i,
		HeightProvider heightProvider,
		boolean bl2,
		Heightmap.Types types
	) {
		this(holderSet, map, decoration, bl, holder, i, heightProvider, bl2, Optional.of(types));
	}

	public JigsawStructure(
		HolderSet<Biome> holderSet,
		Map<MobCategory, StructureSpawnOverride> map,
		GenerationStep.Decoration decoration,
		boolean bl,
		Holder<StructureTemplatePool> holder,
		int i,
		HeightProvider heightProvider,
		boolean bl2
	) {
		this(holderSet, map, decoration, bl, holder, i, heightProvider, bl2, Optional.empty());
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		ChunkPos chunkPos = generationContext.chunkPos();
		int i = this.startHeight
			.sample(generationContext.random(), new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor()));
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), i, chunkPos.getMinBlockZ());
		Pools.bootstrap();
		return JigsawPlacement.addPieces(
			generationContext, this.startPool, this.maxDepth, PoolElementStructurePiece::new, blockPos, this.useExpansionHack, this.projectStartToHeightmap
		);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.JIGSAW;
	}
}
