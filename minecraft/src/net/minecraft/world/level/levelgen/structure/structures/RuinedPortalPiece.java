package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class RuinedPortalPiece extends TemplateStructurePiece {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final float PROBABILITY_OF_GOLD_GONE = 0.3F;
	private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07F;
	private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2F;
	private final RuinedPortalPiece.VerticalPlacement verticalPlacement;
	private final RuinedPortalPiece.Properties properties;

	public RuinedPortalPiece(
		StructureTemplateManager structureTemplateManager,
		BlockPos blockPos,
		RuinedPortalPiece.VerticalPlacement verticalPlacement,
		RuinedPortalPiece.Properties properties,
		ResourceLocation resourceLocation,
		StructureTemplate structureTemplate,
		Rotation rotation,
		Mirror mirror,
		BlockPos blockPos2
	) {
		super(
			StructurePieceType.RUINED_PORTAL,
			0,
			structureTemplateManager,
			resourceLocation,
			resourceLocation.toString(),
			makeSettings(mirror, rotation, verticalPlacement, blockPos2, properties),
			blockPos
		);
		this.verticalPlacement = verticalPlacement;
		this.properties = properties;
	}

	public RuinedPortalPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
		super(
			StructurePieceType.RUINED_PORTAL,
			compoundTag,
			structureTemplateManager,
			resourceLocation -> makeSettings(structureTemplateManager, compoundTag, resourceLocation)
		);
		this.verticalPlacement = RuinedPortalPiece.VerticalPlacement.byName(compoundTag.getString("VerticalPlacement"));
		this.properties = RuinedPortalPiece.Properties.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("Properties"))).getOrThrow(true, LOGGER::error);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
		super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
		compoundTag.putString("Rotation", this.placeSettings.getRotation().name());
		compoundTag.putString("Mirror", this.placeSettings.getMirror().name());
		compoundTag.putString("VerticalPlacement", this.verticalPlacement.getName());
		RuinedPortalPiece.Properties.CODEC
			.encodeStart(NbtOps.INSTANCE, this.properties)
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("Properties", tag));
	}

	private static StructurePlaceSettings makeSettings(
		StructureTemplateManager structureTemplateManager, CompoundTag compoundTag, ResourceLocation resourceLocation
	) {
		StructureTemplate structureTemplate = structureTemplateManager.getOrCreate(resourceLocation);
		BlockPos blockPos = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
		return makeSettings(
			Mirror.valueOf(compoundTag.getString("Mirror")),
			Rotation.valueOf(compoundTag.getString("Rotation")),
			RuinedPortalPiece.VerticalPlacement.byName(compoundTag.getString("VerticalPlacement")),
			blockPos,
			RuinedPortalPiece.Properties.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("Properties"))).getOrThrow(true, LOGGER::error)
		);
	}

	private static StructurePlaceSettings makeSettings(
		Mirror mirror, Rotation rotation, RuinedPortalPiece.VerticalPlacement verticalPlacement, BlockPos blockPos, RuinedPortalPiece.Properties properties
	) {
		BlockIgnoreProcessor blockIgnoreProcessor = properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
		List<ProcessorRule> list = Lists.<ProcessorRule>newArrayList();
		list.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
		list.add(getLavaProcessorRule(verticalPlacement, properties));
		if (!properties.cold) {
			list.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
		}

		StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings()
			.setRotation(rotation)
			.setMirror(mirror)
			.setRotationPivot(blockPos)
			.addProcessor(blockIgnoreProcessor)
			.addProcessor(new RuleProcessor(list))
			.addProcessor(new BlockAgeProcessor(properties.mossiness))
			.addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))
			.addProcessor(new LavaSubmergedBlockProcessor());
		if (properties.replaceWithBlackstone) {
			structurePlaceSettings.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
		}

		return structurePlaceSettings;
	}

	private static ProcessorRule getLavaProcessorRule(RuinedPortalPiece.VerticalPlacement verticalPlacement, RuinedPortalPiece.Properties properties) {
		if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR) {
			return getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
		} else {
			return properties.cold ? getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK) : getBlockReplaceRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK);
		}
	}

	@Override
	public void postProcess(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	) {
		BoundingBox boundingBox2 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
		if (boundingBox.isInside(boundingBox2.getCenter())) {
			boundingBox.encapsulate(boundingBox2);
			super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
			this.spreadNetherrack(randomSource, worldGenLevel);
			this.addNetherrackDripColumnsBelowPortal(randomSource, worldGenLevel);
			if (this.properties.vines || this.properties.overgrown) {
				BlockPos.betweenClosedStream(this.getBoundingBox()).forEach(blockPosx -> {
					if (this.properties.vines) {
						this.maybeAddVines(randomSource, worldGenLevel, blockPosx);
					}

					if (this.properties.overgrown) {
						this.maybeAddLeavesAbove(randomSource, worldGenLevel, blockPosx);
					}
				});
			}
		}
	}

	@Override
	protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
	}

	private void maybeAddVines(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (!blockState.isAir() && !blockState.is(Blocks.VINE)) {
			Direction direction = getRandomHorizontalDirection(randomSource);
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
			if (blockState2.isAir()) {
				if (Block.isFaceFull(blockState.getCollisionShape(levelAccessor, blockPos), direction)) {
					BooleanProperty booleanProperty = VineBlock.getPropertyForFace(direction.getOpposite());
					levelAccessor.setBlock(blockPos2, Blocks.VINE.defaultBlockState().setValue(booleanProperty, Boolean.valueOf(true)), 3);
				}
			}
		}
	}

	private void maybeAddLeavesAbove(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
		if (randomSource.nextFloat() < 0.5F && levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK) && levelAccessor.getBlockState(blockPos.above()).isAir()) {
			levelAccessor.setBlock(blockPos.above(), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, Boolean.valueOf(true)), 3);
		}
	}

	private void addNetherrackDripColumnsBelowPortal(RandomSource randomSource, LevelAccessor levelAccessor) {
		for (int i = this.boundingBox.minX() + 1; i < this.boundingBox.maxX(); i++) {
			for (int j = this.boundingBox.minZ() + 1; j < this.boundingBox.maxZ(); j++) {
				BlockPos blockPos = new BlockPos(i, this.boundingBox.minY(), j);
				if (levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK)) {
					this.addNetherrackDripColumn(randomSource, levelAccessor, blockPos.below());
				}
			}
		}
	}

	private void addNetherrackDripColumn(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		this.placeNetherrackOrMagma(randomSource, levelAccessor, mutableBlockPos);
		int i = 8;

		while (i > 0 && randomSource.nextFloat() < 0.5F) {
			mutableBlockPos.move(Direction.DOWN);
			i--;
			this.placeNetherrackOrMagma(randomSource, levelAccessor, mutableBlockPos);
		}
	}

	private void spreadNetherrack(RandomSource randomSource, LevelAccessor levelAccessor) {
		boolean bl = this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE
			|| this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
		BlockPos blockPos = this.boundingBox.getCenter();
		int i = blockPos.getX();
		int j = blockPos.getZ();
		float[] fs = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
		int k = fs.length;
		int l = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
		int m = randomSource.nextInt(Math.max(1, 8 - l / 2));
		int n = 3;
		BlockPos.MutableBlockPos mutableBlockPos = BlockPos.ZERO.mutable();

		for (int o = i - k; o <= i + k; o++) {
			for (int p = j - k; p <= j + k; p++) {
				int q = Math.abs(o - i) + Math.abs(p - j);
				int r = Math.max(0, q + m);
				if (r < k) {
					float f = fs[r];
					if (randomSource.nextDouble() < (double)f) {
						int s = getSurfaceY(levelAccessor, o, p, this.verticalPlacement);
						int t = bl ? s : Math.min(this.boundingBox.minY(), s);
						mutableBlockPos.set(o, t, p);
						if (Math.abs(t - this.boundingBox.minY()) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma(levelAccessor, mutableBlockPos)) {
							this.placeNetherrackOrMagma(randomSource, levelAccessor, mutableBlockPos);
							if (this.properties.overgrown) {
								this.maybeAddLeavesAbove(randomSource, levelAccessor, mutableBlockPos);
							}

							this.addNetherrackDripColumn(randomSource, levelAccessor, mutableBlockPos.below());
						}
					}
				}
			}
		}
	}

	private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		return !blockState.is(Blocks.AIR)
			&& !blockState.is(Blocks.OBSIDIAN)
			&& !blockState.is(BlockTags.FEATURES_CANNOT_REPLACE)
			&& (this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER || !blockState.is(Blocks.LAVA));
	}

	private void placeNetherrackOrMagma(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
		if (!this.properties.cold && randomSource.nextFloat() < 0.07F) {
			levelAccessor.setBlock(blockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
		} else {
			levelAccessor.setBlock(blockPos, Blocks.NETHERRACK.defaultBlockState(), 3);
		}
	}

	private static int getSurfaceY(LevelAccessor levelAccessor, int i, int j, RuinedPortalPiece.VerticalPlacement verticalPlacement) {
		return levelAccessor.getHeight(getHeightMapType(verticalPlacement), i, j) - 1;
	}

	public static Heightmap.Types getHeightMapType(RuinedPortalPiece.VerticalPlacement verticalPlacement) {
		return verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
	}

	private static ProcessorRule getBlockReplaceRule(Block block, float f, Block block2) {
		return new ProcessorRule(new RandomBlockMatchTest(block, f), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
	}

	private static ProcessorRule getBlockReplaceRule(Block block, Block block2) {
		return new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
	}

	public static class Properties {
		public static final Codec<RuinedPortalPiece.Properties> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.BOOL.fieldOf("cold").forGetter(properties -> properties.cold),
						Codec.FLOAT.fieldOf("mossiness").forGetter(properties -> properties.mossiness),
						Codec.BOOL.fieldOf("air_pocket").forGetter(properties -> properties.airPocket),
						Codec.BOOL.fieldOf("overgrown").forGetter(properties -> properties.overgrown),
						Codec.BOOL.fieldOf("vines").forGetter(properties -> properties.vines),
						Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(properties -> properties.replaceWithBlackstone)
					)
					.apply(instance, RuinedPortalPiece.Properties::new)
		);
		public boolean cold;
		public float mossiness;
		public boolean airPocket;
		public boolean overgrown;
		public boolean vines;
		public boolean replaceWithBlackstone;

		public Properties() {
		}

		public Properties(boolean bl, float f, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
			this.cold = bl;
			this.mossiness = f;
			this.airPocket = bl2;
			this.overgrown = bl3;
			this.vines = bl4;
			this.replaceWithBlackstone = bl5;
		}
	}

	public static enum VerticalPlacement implements StringRepresentable {
		ON_LAND_SURFACE("on_land_surface"),
		PARTLY_BURIED("partly_buried"),
		ON_OCEAN_FLOOR("on_ocean_floor"),
		IN_MOUNTAIN("in_mountain"),
		UNDERGROUND("underground"),
		IN_NETHER("in_nether");

		public static final StringRepresentable.EnumCodec<RuinedPortalPiece.VerticalPlacement> CODEC = StringRepresentable.fromEnum(
			RuinedPortalPiece.VerticalPlacement::values
		);
		private final String name;

		private VerticalPlacement(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		public static RuinedPortalPiece.VerticalPlacement byName(String string) {
			return (RuinedPortalPiece.VerticalPlacement)CODEC.byName(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
