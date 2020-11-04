package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureBlockEntity extends BlockEntity {
	private ResourceLocation structureName;
	private String author = "";
	private String metaData = "";
	private BlockPos structurePos = new BlockPos(0, 1, 0);
	private BlockPos structureSize = BlockPos.ZERO;
	private Mirror mirror = Mirror.NONE;
	private Rotation rotation = Rotation.NONE;
	private StructureMode mode = StructureMode.DATA;
	private boolean ignoreEntities = true;
	private boolean powered;
	private boolean showAir;
	private boolean showBoundingBox = true;
	private float integrity = 1.0F;
	private long seed;

	public StructureBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.STRUCTURE_BLOCK, blockPos, blockState);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public double getViewDistance() {
		return 96.0;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
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
		return compoundTag;
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
		this.structureSize = new BlockPos(l, m, n);

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

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 7, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
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

	@Environment(EnvType.CLIENT)
	public BlockPos getStructurePos() {
		return this.structurePos;
	}

	public void setStructurePos(BlockPos blockPos) {
		this.structurePos = blockPos;
	}

	public BlockPos getStructureSize() {
		return this.structureSize;
	}

	public void setStructureSize(BlockPos blockPos) {
		this.structureSize = blockPos;
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	public void nextMode() {
		switch (this.getMode()) {
			case SAVE:
				this.setMode(StructureMode.LOAD);
				break;
			case LOAD:
				this.setMode(StructureMode.CORNER);
				break;
			case CORNER:
				this.setMode(StructureMode.DATA);
				break;
			case DATA:
				this.setMode(StructureMode.SAVE);
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean isIgnoreEntities() {
		return this.ignoreEntities;
	}

	public void setIgnoreEntities(boolean bl) {
		this.ignoreEntities = bl;
	}

	@Environment(EnvType.CLIENT)
	public float getIntegrity() {
		return this.integrity;
	}

	public void setIntegrity(float f) {
		this.integrity = f;
	}

	@Environment(EnvType.CLIENT)
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
			BlockPos blockPos2 = new BlockPos(blockPos.getX() - 80, 0, blockPos.getZ() - 80);
			BlockPos blockPos3 = new BlockPos(blockPos.getX() + 80, this.level.getMaxBuildHeight() - 1, blockPos.getZ() + 80);
			List<StructureBlockEntity> list = this.getNearbyCornerBlocks(blockPos2, blockPos3);
			List<StructureBlockEntity> list2 = this.filterRelatedCornerBlocks(list);
			if (list2.size() < 1) {
				return false;
			} else {
				BoundingBox boundingBox = this.calculateEnclosingBoundingBox(blockPos, list2);
				if (boundingBox.x1 - boundingBox.x0 > 1 && boundingBox.y1 - boundingBox.y0 > 1 && boundingBox.z1 - boundingBox.z0 > 1) {
					this.structurePos = new BlockPos(boundingBox.x0 - blockPos.getX() + 1, boundingBox.y0 - blockPos.getY() + 1, boundingBox.z0 - blockPos.getZ() + 1);
					this.structureSize = new BlockPos(boundingBox.x1 - boundingBox.x0 - 1, boundingBox.y1 - boundingBox.y0 - 1, boundingBox.z1 - boundingBox.z0 - 1);
					this.setChanged();
					BlockState blockState = this.level.getBlockState(blockPos);
					this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
					return true;
				} else {
					return false;
				}
			}
		}
	}

	private List<StructureBlockEntity> filterRelatedCornerBlocks(List<StructureBlockEntity> list) {
		Predicate<StructureBlockEntity> predicate = structureBlockEntity -> structureBlockEntity.mode == StructureMode.CORNER
				&& Objects.equals(this.structureName, structureBlockEntity.structureName);
		return (List<StructureBlockEntity>)list.stream().filter(predicate).collect(Collectors.toList());
	}

	private List<StructureBlockEntity> getNearbyCornerBlocks(BlockPos blockPos, BlockPos blockPos2) {
		List<StructureBlockEntity> list = Lists.<StructureBlockEntity>newArrayList();

		for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
			BlockState blockState = this.level.getBlockState(blockPos3);
			if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
				BlockEntity blockEntity = this.level.getBlockEntity(blockPos3);
				if (blockEntity != null && blockEntity instanceof StructureBlockEntity) {
					list.add((StructureBlockEntity)blockEntity);
				}
			}
		}

		return list;
	}

	private BoundingBox calculateEnclosingBoundingBox(BlockPos blockPos, List<StructureBlockEntity> list) {
		BoundingBox boundingBox;
		if (list.size() > 1) {
			BlockPos blockPos2 = ((StructureBlockEntity)list.get(0)).getBlockPos();
			boundingBox = new BoundingBox(blockPos2, blockPos2);
		} else {
			boundingBox = new BoundingBox(blockPos, blockPos);
		}

		for (StructureBlockEntity structureBlockEntity : list) {
			BlockPos blockPos3 = structureBlockEntity.getBlockPos();
			if (blockPos3.getX() < boundingBox.x0) {
				boundingBox.x0 = blockPos3.getX();
			} else if (blockPos3.getX() > boundingBox.x1) {
				boundingBox.x1 = blockPos3.getX();
			}

			if (blockPos3.getY() < boundingBox.y0) {
				boundingBox.y0 = blockPos3.getY();
			} else if (blockPos3.getY() > boundingBox.y1) {
				boundingBox.y1 = blockPos3.getY();
			}

			if (blockPos3.getZ() < boundingBox.z0) {
				boundingBox.z0 = blockPos3.getZ();
			} else if (blockPos3.getZ() > boundingBox.z1) {
				boundingBox.z1 = blockPos3.getZ();
			}
		}

		return boundingBox;
	}

	public boolean saveStructure() {
		return this.saveStructure(true);
	}

	public boolean saveStructure(boolean bl) {
		if (this.mode == StructureMode.SAVE && !this.level.isClientSide && this.structureName != null) {
			BlockPos blockPos = this.getBlockPos().offset(this.structurePos);
			ServerLevel serverLevel = (ServerLevel)this.level;
			StructureManager structureManager = serverLevel.getStructureManager();

			StructureTemplate structureTemplate;
			try {
				structureTemplate = structureManager.getOrCreate(this.structureName);
			} catch (ResourceLocationException var8) {
				return false;
			}

			structureTemplate.fillFromWorld(this.level, blockPos, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
			structureTemplate.setAuthor(this.author);
			if (bl) {
				try {
					return structureManager.save(this.structureName);
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

	private static Random createRandom(long l) {
		return l == 0L ? new Random(Util.getMillis()) : new Random(l);
	}

	public boolean loadStructure(ServerLevel serverLevel, boolean bl) {
		if (this.mode == StructureMode.LOAD && this.structureName != null) {
			StructureManager structureManager = serverLevel.getStructureManager();

			StructureTemplate structureTemplate;
			try {
				structureTemplate = structureManager.get(this.structureName);
			} catch (ResourceLocationException var6) {
				return false;
			}

			return structureTemplate == null ? false : this.loadStructure(serverLevel, bl, structureTemplate);
		} else {
			return false;
		}
	}

	public boolean loadStructure(ServerLevel serverLevel, boolean bl, StructureTemplate structureTemplate) {
		BlockPos blockPos = this.getBlockPos();
		if (!StringUtil.isNullOrEmpty(structureTemplate.getAuthor())) {
			this.author = structureTemplate.getAuthor();
		}

		BlockPos blockPos2 = structureTemplate.getSize();
		boolean bl2 = this.structureSize.equals(blockPos2);
		if (!bl2) {
			this.structureSize = blockPos2;
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
				.setIgnoreEntities(this.ignoreEntities)
				.setChunkPos(null);
			if (this.integrity < 1.0F) {
				structurePlaceSettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
			}

			BlockPos blockPos3 = blockPos.offset(this.structurePos);
			structureTemplate.placeInWorldChunk(serverLevel, blockPos3, structurePlaceSettings, createRandom(this.seed));
			return true;
		}
	}

	public void unloadStructure() {
		if (this.structureName != null) {
			ServerLevel serverLevel = (ServerLevel)this.level;
			StructureManager structureManager = serverLevel.getStructureManager();
			structureManager.remove(this.structureName);
		}
	}

	public boolean isStructureLoadable() {
		if (this.mode == StructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
			ServerLevel serverLevel = (ServerLevel)this.level;
			StructureManager structureManager = serverLevel.getStructureManager();

			try {
				return structureManager.get(this.structureName) != null;
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

	@Environment(EnvType.CLIENT)
	public boolean getShowAir() {
		return this.showAir;
	}

	public void setShowAir(boolean bl) {
		this.showAir = bl;
	}

	@Environment(EnvType.CLIENT)
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
