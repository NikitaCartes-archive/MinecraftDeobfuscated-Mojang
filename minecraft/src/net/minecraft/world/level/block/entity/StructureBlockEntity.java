package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureBlockEntity extends BlockEntity {
	private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
	public static final int MAX_OFFSET_PER_AXIS = 48;
	public static final int MAX_SIZE_PER_AXIS = 48;
	public static final String AUTHOR_TAG = "author";
	private ResourceLocation structureName;
	private String author = "";
	private String metaData = "";
	private BlockPos structurePos = new BlockPos(0, 1, 0);
	private Vec3i structureSize = Vec3i.ZERO;
	private Mirror mirror = Mirror.NONE;
	private Rotation rotation = Rotation.NONE;
	private StructureMode mode;
	private boolean ignoreEntities = true;
	private boolean powered;
	private boolean showAir;
	private boolean showBoundingBox = true;
	private float integrity = 1.0F;
	private long seed;

	public StructureBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.STRUCTURE_BLOCK, blockPos, blockState);
		this.mode = blockState.getValue(StructureBlock.MODE);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		compoundTag.putString("name", this.getStructureName());
		compoundTag.putString("author", this.author);
		compoundTag.putString("metadata", this.metaData);
		compoundTag.putInt("posX", this.structurePos.getX());
		compoundTag.putInt("posY", this.structurePos.getY());
		compoundTag.putInt("posZ", this.structurePos.getZ());
		compoundTag.putInt("sizeX", this.structureSize.getX());
		compoundTag.putInt("sizeY", this.structureSize.getY());
		compoundTag.putInt("sizeZ", this.structureSize.getZ());
		compoundTag.putString("rotation", this.rotation.toString());
		compoundTag.putString("mirror", this.mirror.toString());
		compoundTag.putString("mode", this.mode.toString());
		compoundTag.putBoolean("ignoreEntities", this.ignoreEntities);
		compoundTag.putBoolean("powered", this.powered);
		compoundTag.putBoolean("showair", this.showAir);
		compoundTag.putBoolean("showboundingbox", this.showBoundingBox);
		compoundTag.putFloat("integrity", this.integrity);
		compoundTag.putLong("seed", this.seed);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.setStructureName(compoundTag.getString("name"));
		this.author = compoundTag.getString("author");
		this.metaData = compoundTag.getString("metadata");
		int i = Mth.clamp(compoundTag.getInt("posX"), -48, 48);
		int j = Mth.clamp(compoundTag.getInt("posY"), -48, 48);
		int k = Mth.clamp(compoundTag.getInt("posZ"), -48, 48);
		this.structurePos = new BlockPos(i, j, k);
		int l = Mth.clamp(compoundTag.getInt("sizeX"), 0, 48);
		int m = Mth.clamp(compoundTag.getInt("sizeY"), 0, 48);
		int n = Mth.clamp(compoundTag.getInt("sizeZ"), 0, 48);
		this.structureSize = new Vec3i(l, m, n);

		try {
			this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
		} catch (IllegalArgumentException var11) {
			this.rotation = Rotation.NONE;
		}

		try {
			this.mirror = Mirror.valueOf(compoundTag.getString("mirror"));
		} catch (IllegalArgumentException var10) {
			this.mirror = Mirror.NONE;
		}

		try {
			this.mode = StructureMode.valueOf(compoundTag.getString("mode"));
		} catch (IllegalArgumentException var9) {
			this.mode = StructureMode.DATA;
		}

		this.ignoreEntities = compoundTag.getBoolean("ignoreEntities");
		this.powered = compoundTag.getBoolean("powered");
		this.showAir = compoundTag.getBoolean("showair");
		this.showBoundingBox = compoundTag.getBoolean("showboundingbox");
		if (compoundTag.contains("integrity")) {
			this.integrity = compoundTag.getFloat("integrity");
		} else {
			this.integrity = 1.0F;
		}

		this.seed = compoundTag.getLong("seed");
		this.updateBlockState();
	}

	private void updateBlockState() {
		if (this.level != null) {
			BlockPos blockPos = this.getBlockPos();
			BlockState blockState = this.level.getBlockState(blockPos);
			if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
				this.level.setBlock(blockPos, blockState.setValue(StructureBlock.MODE, this.mode), 2);
			}
		}
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public boolean usedBy(Player player) {
		if (!player.canUseGameMasterBlocks()) {
			return false;
		} else {
			if (player.getCommandSenderWorld().isClientSide) {
				player.openStructureBlock(this);
			}

			return true;
		}
	}

	public String getStructureName() {
		return this.structureName == null ? "" : this.structureName.toString();
	}

	public String getStructurePath() {
		return this.structureName == null ? "" : this.structureName.getPath();
	}

	public boolean hasStructureName() {
		return this.structureName != null;
	}

	public void setStructureName(@Nullable String string) {
		this.setStructureName(StringUtil.isNullOrEmpty(string) ? null : ResourceLocation.tryParse(string));
	}

	public void setStructureName(@Nullable ResourceLocation resourceLocation) {
		this.structureName = resourceLocation;
	}

	public void createdBy(LivingEntity livingEntity) {
		this.author = livingEntity.getName().getString();
	}

	public BlockPos getStructurePos() {
		return this.structurePos;
	}

	public void setStructurePos(BlockPos blockPos) {
		this.structurePos = blockPos;
	}

	public Vec3i getStructureSize() {
		return this.structureSize;
	}

	public void setStructureSize(Vec3i vec3i) {
		this.structureSize = vec3i;
	}

	public Mirror getMirror() {
		return this.mirror;
	}

	public void setMirror(Mirror mirror) {
		this.mirror = mirror;
	}

	public Rotation getRotation() {
		return this.rotation;
	}

	public void setRotation(Rotation rotation) {
		this.rotation = rotation;
	}

	public String getMetaData() {
		return this.metaData;
	}

	public void setMetaData(String string) {
		this.metaData = string;
	}

	public StructureMode getMode() {
		return this.mode;
	}

	public void setMode(StructureMode structureMode) {
		this.mode = structureMode;
		BlockState blockState = this.level.getBlockState(this.getBlockPos());
		if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
			this.level.setBlock(this.getBlockPos(), blockState.setValue(StructureBlock.MODE, structureMode), 2);
		}
	}

	public boolean isIgnoreEntities() {
		return this.ignoreEntities;
	}

	public void setIgnoreEntities(boolean bl) {
		this.ignoreEntities = bl;
	}

	public float getIntegrity() {
		return this.integrity;
	}

	public void setIntegrity(float f) {
		this.integrity = f;
	}

	public long getSeed() {
		return this.seed;
	}

	public void setSeed(long l) {
		this.seed = l;
	}

	public boolean detectSize() {
		if (this.mode != StructureMode.SAVE) {
			return false;
		} else {
			BlockPos blockPos = this.getBlockPos();
			int i = 80;
			BlockPos blockPos2 = new BlockPos(blockPos.getX() - 80, this.level.getMinBuildHeight(), blockPos.getZ() - 80);
			BlockPos blockPos3 = new BlockPos(blockPos.getX() + 80, this.level.getMaxBuildHeight() - 1, blockPos.getZ() + 80);
			Stream<BlockPos> stream = this.getRelatedCorners(blockPos2, blockPos3);
			return calculateEnclosingBoundingBox(blockPos, stream)
				.filter(
					boundingBox -> {
						int ix = boundingBox.maxX() - boundingBox.minX();
						int j = boundingBox.maxY() - boundingBox.minY();
						int k = boundingBox.maxZ() - boundingBox.minZ();
						if (ix > 1 && j > 1 && k > 1) {
							this.structurePos = new BlockPos(
								boundingBox.minX() - blockPos.getX() + 1, boundingBox.minY() - blockPos.getY() + 1, boundingBox.minZ() - blockPos.getZ() + 1
							);
							this.structureSize = new Vec3i(ix - 1, j - 1, k - 1);
							this.setChanged();
							BlockState blockState = this.level.getBlockState(blockPos);
							this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
							return true;
						} else {
							return false;
						}
					}
				)
				.isPresent();
		}
	}

	private Stream<BlockPos> getRelatedCorners(BlockPos blockPos, BlockPos blockPos2) {
		return BlockPos.betweenClosedStream(blockPos, blockPos2)
			.filter(blockPosx -> this.level.getBlockState(blockPosx).is(Blocks.STRUCTURE_BLOCK))
			.map(this.level::getBlockEntity)
			.filter(blockEntity -> blockEntity instanceof StructureBlockEntity)
			.map(blockEntity -> (StructureBlockEntity)blockEntity)
			.filter(structureBlockEntity -> structureBlockEntity.mode == StructureMode.CORNER && Objects.equals(this.structureName, structureBlockEntity.structureName))
			.map(BlockEntity::getBlockPos);
	}

	private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos blockPos, Stream<BlockPos> stream) {
		Iterator<BlockPos> iterator = stream.iterator();
		if (!iterator.hasNext()) {
			return Optional.empty();
		} else {
			BlockPos blockPos2 = (BlockPos)iterator.next();
			BoundingBox boundingBox = new BoundingBox(blockPos2);
			if (iterator.hasNext()) {
				iterator.forEachRemaining(boundingBox::encapsulate);
			} else {
				boundingBox.encapsulate(blockPos);
			}

			return Optional.of(boundingBox);
		}
	}

	public boolean saveStructure() {
		return this.saveStructure(true);
	}

	public boolean saveStructure(boolean bl) {
		if (this.mode == StructureMode.SAVE && !this.level.isClientSide && this.structureName != null) {
			BlockPos blockPos = this.getBlockPos().offset(this.structurePos);
			ServerLevel serverLevel = (ServerLevel)this.level;
			StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();

			StructureTemplate structureTemplate;
			try {
				structureTemplate = structureTemplateManager.getOrCreate(this.structureName);
			} catch (ResourceLocationException var8) {
				return false;
			}

			structureTemplate.fillFromWorld(this.level, blockPos, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
			structureTemplate.setAuthor(this.author);
			if (bl) {
				try {
					return structureTemplateManager.save(this.structureName);
				} catch (ResourceLocationException var7) {
					return false;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public boolean loadStructure(ServerLevel serverLevel) {
		return this.loadStructure(serverLevel, true);
	}

	private static RandomSource createRandom(long l) {
		return l == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(l);
	}

	public boolean loadStructure(ServerLevel serverLevel, boolean bl) {
		if (this.mode == StructureMode.LOAD && this.structureName != null) {
			StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();

			Optional<StructureTemplate> optional;
			try {
				optional = structureTemplateManager.get(this.structureName);
			} catch (ResourceLocationException var6) {
				return false;
			}

			return !optional.isPresent() ? false : this.loadStructure(serverLevel, bl, (StructureTemplate)optional.get());
		} else {
			return false;
		}
	}

	public boolean loadStructure(ServerLevel serverLevel, boolean bl, StructureTemplate structureTemplate) {
		BlockPos blockPos = this.getBlockPos();
		if (!StringUtil.isNullOrEmpty(structureTemplate.getAuthor())) {
			this.author = structureTemplate.getAuthor();
		}

		Vec3i vec3i = structureTemplate.getSize();
		boolean bl2 = this.structureSize.equals(vec3i);
		if (!bl2) {
			this.structureSize = vec3i;
			this.setChanged();
			BlockState blockState = serverLevel.getBlockState(blockPos);
			serverLevel.sendBlockUpdated(blockPos, blockState, blockState, 3);
		}

		if (bl && !bl2) {
			return false;
		} else {
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings()
				.setMirror(this.mirror)
				.setRotation(this.rotation)
				.setIgnoreEntities(this.ignoreEntities);
			if (this.integrity < 1.0F) {
				structurePlaceSettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
			}

			BlockPos blockPos2 = blockPos.offset(this.structurePos);
			structureTemplate.placeInWorld(serverLevel, blockPos2, blockPos2, structurePlaceSettings, createRandom(this.seed), 2);
			return true;
		}
	}

	public void unloadStructure() {
		if (this.structureName != null) {
			ServerLevel serverLevel = (ServerLevel)this.level;
			StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
			structureTemplateManager.remove(this.structureName);
		}
	}

	public boolean isStructureLoadable() {
		if (this.mode == StructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
			ServerLevel serverLevel = (ServerLevel)this.level;
			StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();

			try {
				return structureTemplateManager.get(this.structureName).isPresent();
			} catch (ResourceLocationException var4) {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isPowered() {
		return this.powered;
	}

	public void setPowered(boolean bl) {
		this.powered = bl;
	}

	public boolean getShowAir() {
		return this.showAir;
	}

	public void setShowAir(boolean bl) {
		this.showAir = bl;
	}

	public boolean getShowBoundingBox() {
		return this.showBoundingBox;
	}

	public void setShowBoundingBox(boolean bl) {
		this.showBoundingBox = bl;
	}

	public static enum UpdateType {
		UPDATE_DATA,
		SAVE_AREA,
		LOAD_AREA,
		SCAN_AREA;
	}
}
