package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record PiecesContainer() {
	private final List<StructurePiece> pieces;
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
	private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder()
		.put(new ResourceLocation("nvi"), JIGSAW_RENAME)
		.put(new ResourceLocation("pcp"), JIGSAW_RENAME)
		.put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME)
		.put(new ResourceLocation("runtime"), JIGSAW_RENAME)
		.build();

	public PiecesContainer(List<StructurePiece> list) {
		this.pieces = List.copyOf(list);
	}

	public boolean isEmpty() {
		return this.pieces.isEmpty();
	}

	public boolean isInsidePiece(BlockPos blockPos) {
		for (StructurePiece structurePiece : this.pieces) {
			if (structurePiece.getBoundingBox().isInside(blockPos)) {
				return true;
			}
		}

		return false;
	}

	public Tag save(StructurePieceSerializationContext structurePieceSerializationContext) {
		ListTag listTag = new ListTag();

		for (StructurePiece structurePiece : this.pieces) {
			listTag.add(structurePiece.createTag(structurePieceSerializationContext));
		}

		return listTag;
	}

	public static PiecesContainer load(ListTag listTag, StructurePieceSerializationContext structurePieceSerializationContext) {
		List<StructurePiece> list = Lists.<StructurePiece>newArrayList();

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			String string = compoundTag.getString("id").toLowerCase(Locale.ROOT);
			ResourceLocation resourceLocation = new ResourceLocation(string);
			ResourceLocation resourceLocation2 = (ResourceLocation)RENAMES.getOrDefault(resourceLocation, resourceLocation);
			StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(resourceLocation2);
			if (structurePieceType == null) {
				LOGGER.error("Unknown structure piece id: {}", resourceLocation2);
			} else {
				try {
					StructurePiece structurePiece = structurePieceType.load(structurePieceSerializationContext, compoundTag);
					list.add(structurePiece);
				} catch (Exception var10) {
					LOGGER.error("Exception loading structure piece with id {}", resourceLocation2, var10);
				}
			}
		}

		return new PiecesContainer(list);
	}

	public BoundingBox calculateBoundingBox() {
		return StructurePiece.createBoundingBox(this.pieces.stream());
	}
}
