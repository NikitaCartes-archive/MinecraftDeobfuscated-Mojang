package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class JigsawBlockEntity extends BlockEntity {
	public static final String TARGET = "target";
	public static final String POOL = "pool";
	public static final String JOINT = "joint";
	public static final String NAME = "name";
	public static final String FINAL_STATE = "final_state";
	private ResourceLocation name = new ResourceLocation("empty");
	private ResourceLocation target = new ResourceLocation("empty");
	private ResourceLocation pool = new ResourceLocation("empty");
	private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
	private String finalState = "minecraft:air";

	public JigsawBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.JIGSAW, blockPos, blockState);
	}

	public ResourceLocation getName() {
		return this.name;
	}

	public ResourceLocation getTarget() {
		return this.target;
	}

	public ResourceLocation getPool() {
		return this.pool;
	}

	public String getFinalState() {
		return this.finalState;
	}

	public JigsawBlockEntity.JointType getJoint() {
		return this.joint;
	}

	public void setName(ResourceLocation resourceLocation) {
		this.name = resourceLocation;
	}

	public void setTarget(ResourceLocation resourceLocation) {
		this.target = resourceLocation;
	}

	public void setPool(ResourceLocation resourceLocation) {
		this.pool = resourceLocation;
	}

	public void setFinalState(String string) {
		this.finalState = string;
	}

	public void setJoint(JigsawBlockEntity.JointType jointType) {
		this.joint = jointType;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		compoundTag.putString("name", this.name.toString());
		compoundTag.putString("target", this.target.toString());
		compoundTag.putString("pool", this.pool.toString());
		compoundTag.putString("final_state", this.finalState);
		compoundTag.putString("joint", this.joint.getSerializedName());
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.name = new ResourceLocation(compoundTag.getString("name"));
		this.target = new ResourceLocation(compoundTag.getString("target"));
		this.pool = new ResourceLocation(compoundTag.getString("pool"));
		this.finalState = compoundTag.getString("final_state");
		this.joint = (JigsawBlockEntity.JointType)JigsawBlockEntity.JointType.byName(compoundTag.getString("joint"))
			.orElseGet(
				() -> JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal()
						? JigsawBlockEntity.JointType.ALIGNED
						: JigsawBlockEntity.JointType.ROLLABLE
			);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public void generate(ServerLevel serverLevel, int i, boolean bl) {
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
		StructureManager structureManager = serverLevel.structureManager();
		RandomSource randomSource = serverLevel.getRandom();
		Registry<StructureTemplatePool> registry = serverLevel.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		ResourceKey<StructureTemplatePool> resourceKey = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, this.pool);
		Holder<StructureTemplatePool> holder = registry.getHolderOrThrow(resourceKey);
		BlockPos blockPos = this.getBlockPos().relative(((FrontAndTop)this.getBlockState().getValue(JigsawBlock.ORIENTATION)).front());
		Structure.GenerationContext generationContext = new Structure.GenerationContext(
			serverLevel.registryAccess(),
			chunkGenerator,
			chunkGenerator.getBiomeSource(),
			serverLevel.getChunkSource().randomState(),
			structureTemplateManager,
			serverLevel.getSeed(),
			new ChunkPos(blockPos),
			serverLevel,
			holderx -> true
		);
		Optional<Structure.GenerationStub> optional = JigsawPlacement.addPieces(
			generationContext, holder, Optional.of(this.target), i, blockPos, false, Optional.empty(), 128
		);
		if (optional.isPresent()) {
			StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
			((Structure.GenerationStub)optional.get()).generator().accept(structurePiecesBuilder);

			for (StructurePiece structurePiece : structurePiecesBuilder.build().pieces()) {
				if (structurePiece instanceof PoolElementStructurePiece poolElementStructurePiece) {
					poolElementStructurePiece.place(serverLevel, structureManager, chunkGenerator, randomSource, BoundingBox.infinite(), blockPos, bl);
				}
			}
		}
	}

	public static enum JointType implements StringRepresentable {
		ROLLABLE("rollable"),
		ALIGNED("aligned");

		private final String name;

		private JointType(String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<JigsawBlockEntity.JointType> byName(String string) {
			return Arrays.stream(values()).filter(jointType -> jointType.getSerializedName().equals(string)).findFirst();
		}

		public Component getTranslatedName() {
			return Component.translatable("jigsaw_block.joint." + this.name);
		}
	}
}
