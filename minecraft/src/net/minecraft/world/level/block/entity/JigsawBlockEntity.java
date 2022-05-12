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
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class JigsawBlockEntity extends BlockEntity {
	public static final String TARGET = "target";
	public static final String POOL = "pool";
	public static final String JOINT = "joint";
	public static final String NAME = "name";
	public static final String FINAL_STATE = "final_state";
	private ResourceLocation name = new ResourceLocation("empty");
	private ResourceLocation target = new ResourceLocation("empty");
	private ResourceKey<StructureTemplatePool> pool = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation("empty"));
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

	public ResourceKey<StructureTemplatePool> getPool() {
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

	public void setPool(ResourceKey<StructureTemplatePool> resourceKey) {
		this.pool = resourceKey;
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
		compoundTag.putString("pool", this.pool.location().toString());
		compoundTag.putString("final_state", this.finalState);
		compoundTag.putString("joint", this.joint.getSerializedName());
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.name = new ResourceLocation(compoundTag.getString("name"));
		this.target = new ResourceLocation(compoundTag.getString("target"));
		this.pool = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation(compoundTag.getString("pool")));
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
		BlockPos blockPos = this.getBlockPos().relative(((FrontAndTop)this.getBlockState().getValue(JigsawBlock.ORIENTATION)).front());
		Registry<StructureTemplatePool> registry = serverLevel.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		Holder<StructureTemplatePool> holder = registry.getHolderOrThrow(this.pool);
		JigsawPlacement.generateJigsaw(serverLevel, holder, this.target, i, blockPos, bl);
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
