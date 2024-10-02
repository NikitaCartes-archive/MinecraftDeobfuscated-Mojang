package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class JigsawBlockEntity extends BlockEntity {
	public static final String TARGET = "target";
	public static final String POOL = "pool";
	public static final String JOINT = "joint";
	public static final String PLACEMENT_PRIORITY = "placement_priority";
	public static final String SELECTION_PRIORITY = "selection_priority";
	public static final String NAME = "name";
	public static final String FINAL_STATE = "final_state";
	private ResourceLocation name = ResourceLocation.withDefaultNamespace("empty");
	private ResourceLocation target = ResourceLocation.withDefaultNamespace("empty");
	private ResourceKey<StructureTemplatePool> pool = ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.withDefaultNamespace("empty"));
	private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
	private String finalState = "minecraft:air";
	private int placementPriority;
	private int selectionPriority;

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

	public int getPlacementPriority() {
		return this.placementPriority;
	}

	public int getSelectionPriority() {
		return this.selectionPriority;
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

	public void setPlacementPriority(int i) {
		this.placementPriority = i;
	}

	public void setSelectionPriority(int i) {
		this.selectionPriority = i;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putString("name", this.name.toString());
		compoundTag.putString("target", this.target.toString());
		compoundTag.putString("pool", this.pool.location().toString());
		compoundTag.putString("final_state", this.finalState);
		compoundTag.putString("joint", this.joint.getSerializedName());
		compoundTag.putInt("placement_priority", this.placementPriority);
		compoundTag.putInt("selection_priority", this.selectionPriority);
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.name = ResourceLocation.parse(compoundTag.getString("name"));
		this.target = ResourceLocation.parse(compoundTag.getString("target"));
		this.pool = ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.parse(compoundTag.getString("pool")));
		this.finalState = compoundTag.getString("final_state");
		this.joint = StructureTemplate.getJointType(compoundTag, this.getBlockState());
		this.placementPriority = compoundTag.getInt("placement_priority");
		this.selectionPriority = compoundTag.getInt("selection_priority");
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveCustomOnly(provider);
	}

	public void generate(ServerLevel serverLevel, int i, boolean bl) {
		BlockPos blockPos = this.getBlockPos().relative(((FrontAndTop)this.getBlockState().getValue(JigsawBlock.ORIENTATION)).front());
		Registry<StructureTemplatePool> registry = serverLevel.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
		Holder<StructureTemplatePool> holder = registry.getOrThrow(this.pool);
		JigsawPlacement.generateJigsaw(serverLevel, holder, this.target, i, blockPos, bl);
	}

	public static enum JointType implements StringRepresentable {
		ROLLABLE("rollable"),
		ALIGNED("aligned");

		public static final StringRepresentable.EnumCodec<JigsawBlockEntity.JointType> CODEC = StringRepresentable.fromEnum(JigsawBlockEntity.JointType::values);
		private final String name;

		private JointType(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public Component getTranslatedName() {
			return Component.translatable("jigsaw_block.joint." + this.name);
		}
	}
}
