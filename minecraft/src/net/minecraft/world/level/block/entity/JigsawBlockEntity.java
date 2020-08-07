package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class JigsawBlockEntity extends BlockEntity {
	private ResourceLocation name = new ResourceLocation("empty");
	private ResourceLocation target = new ResourceLocation("empty");
	private ResourceLocation pool = new ResourceLocation("empty");
	private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
	private String finalState = "minecraft:air";

	public JigsawBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	public JigsawBlockEntity() {
		this(BlockEntityType.JIGSAW);
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getName() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getTarget() {
		return this.target;
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getPool() {
		return this.pool;
	}

	@Environment(EnvType.CLIENT)
	public String getFinalState() {
		return this.finalState;
	}

	@Environment(EnvType.CLIENT)
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
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putString("name", this.name.toString());
		compoundTag.putString("target", this.target.toString());
		compoundTag.putString("pool", this.pool.toString());
		compoundTag.putString("final_state", this.finalState);
		compoundTag.putString("joint", this.joint.getSerializedName());
		return compoundTag;
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
		this.name = new ResourceLocation(compoundTag.getString("name"));
		this.target = new ResourceLocation(compoundTag.getString("target"));
		this.pool = new ResourceLocation(compoundTag.getString("pool"));
		this.finalState = compoundTag.getString("final_state");
		this.joint = (JigsawBlockEntity.JointType)JigsawBlockEntity.JointType.byName(compoundTag.getString("joint"))
			.orElseGet(
				() -> JigsawBlock.getFrontFacing(blockState).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE
			);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 12, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	public void generate(ServerLevel serverLevel, int i, boolean bl) {
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		StructureManager structureManager = serverLevel.getStructureManager();
		StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
		Random random = serverLevel.getRandom();
		BlockPos blockPos = this.getBlockPos();
		List<PoolElementStructurePiece> list = Lists.<PoolElementStructurePiece>newArrayList();
		StructureTemplate structureTemplate = new StructureTemplate();
		structureTemplate.fillFromWorld(serverLevel, blockPos, new BlockPos(1, 1, 1), false, null);
		StructurePoolElement structurePoolElement = new SinglePoolElement(structureTemplate);
		PoolElementStructurePiece poolElementStructurePiece = new PoolElementStructurePiece(
			structureManager, structurePoolElement, blockPos, 1, Rotation.NONE, new BoundingBox(blockPos, blockPos)
		);
		JigsawPlacement.addPieces(
			serverLevel.registryAccess(), poolElementStructurePiece, i, PoolElementStructurePiece::new, chunkGenerator, structureManager, list, random
		);

		for (PoolElementStructurePiece poolElementStructurePiece2 : list) {
			poolElementStructurePiece2.place(serverLevel, structureFeatureManager, chunkGenerator, random, BoundingBox.infinite(), blockPos, bl);
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
	}
}
