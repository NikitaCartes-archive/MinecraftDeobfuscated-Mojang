package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
	private static final Logger LOGGER = LogManager.getLogger();
	protected final String templateName;
	protected StructureTemplate template;
	protected StructurePlaceSettings placeSettings;
	protected BlockPos templatePosition;

	public TemplateStructurePiece(
		StructurePieceType structurePieceType,
		int i,
		StructureManager structureManager,
		ResourceLocation resourceLocation,
		String string,
		StructurePlaceSettings structurePlaceSettings,
		BlockPos blockPos
	) {
		super(structurePieceType, i, structureManager.getOrCreate(resourceLocation).getBoundingBox(structurePlaceSettings, blockPos));
		this.setOrientation(Direction.NORTH);
		this.templateName = string;
		this.templatePosition = blockPos;
		this.template = structureManager.getOrCreate(resourceLocation);
		this.placeSettings = structurePlaceSettings;
	}

	public TemplateStructurePiece(
		StructurePieceType structurePieceType, CompoundTag compoundTag, ServerLevel serverLevel, Function<ResourceLocation, StructurePlaceSettings> function
	) {
		super(structurePieceType, compoundTag);
		this.setOrientation(Direction.NORTH);
		this.templateName = compoundTag.getString("Template");
		this.templatePosition = new BlockPos(compoundTag.getInt("TPX"), compoundTag.getInt("TPY"), compoundTag.getInt("TPZ"));
		ResourceLocation resourceLocation = this.makeTemplateLocation();
		this.template = serverLevel.getStructureManager().getOrCreate(resourceLocation);
		this.placeSettings = (StructurePlaceSettings)function.apply(resourceLocation);
		this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
	}

	protected ResourceLocation makeTemplateLocation() {
		return new ResourceLocation(this.templateName);
	}

	@Override
	protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
		compoundTag.putInt("TPX", this.templatePosition.getX());
		compoundTag.putInt("TPY", this.templatePosition.getY());
		compoundTag.putInt("TPZ", this.templatePosition.getZ());
		compoundTag.putString("Template", this.templateName);
	}

	@Override
	public boolean postProcess(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	) {
		this.placeSettings.setBoundingBox(boundingBox);
		this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
		if (this.template.placeInWorld(worldGenLevel, this.templatePosition, blockPos, this.placeSettings, random, 2)) {
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
				if (structureBlockInfo.nbt != null) {
					StructureMode structureMode = StructureMode.valueOf(structureBlockInfo.nbt.getString("mode"));
					if (structureMode == StructureMode.DATA) {
						this.handleDataMarker(structureBlockInfo.nbt.getString("metadata"), structureBlockInfo.pos, worldGenLevel, random, boundingBox);
					}
				}
			}

			for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
				if (structureBlockInfo2.nbt != null) {
					String string = structureBlockInfo2.nbt.getString("final_state");
					BlockStateParser blockStateParser = new BlockStateParser(new StringReader(string), false);
					BlockState blockState = Blocks.AIR.defaultBlockState();

					try {
						blockStateParser.parse(true);
						BlockState blockState2 = blockStateParser.getState();
						if (blockState2 != null) {
							blockState = blockState2;
						} else {
							LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", string, structureBlockInfo2.pos);
						}
					} catch (CommandSyntaxException var16) {
						LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", string, structureBlockInfo2.pos);
					}

					worldGenLevel.setBlock(structureBlockInfo2.pos, blockState, 3);
				}
			}
		}

		return true;
	}

	protected abstract void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox);

	@Override
	public void move(int i, int j, int k) {
		super.move(i, j, k);
		this.templatePosition = this.templatePosition.offset(i, j, k);
	}

	@Override
	public Rotation getRotation() {
		return this.placeSettings.getRotation();
	}
}
