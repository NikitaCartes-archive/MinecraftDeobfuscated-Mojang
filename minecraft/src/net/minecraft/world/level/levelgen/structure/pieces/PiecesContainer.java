package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.slf4j.Logger;

public record PiecesContainer(List<StructurePiece> pieces) {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation JIGSAW_RENAME = ResourceLocation.withDefaultNamespace("jigsaw");
	private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder()
		.put(ResourceLocation.withDefaultNamespace("nvi"), JIGSAW_RENAME)
		.put(ResourceLocation.withDefaultNamespace("pcp"), JIGSAW_RENAME)
		.put(ResourceLocation.withDefaultNamespace("bastionremnant"), JIGSAW_RENAME)
		.put(ResourceLocation.withDefaultNamespace("runtime"), JIGSAW_RENAME)
		.build();

	public PiecesContainer(final List<StructurePiece> pieces) {
		this.pieces = List.copyOf(pieces);
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
			ResourceLocation resourceLocation = ResourceLocation.parse(string);
			ResourceLocation resourceLocation2 = (ResourceLocation)RENAMES.getOrDefault(resourceLocation, resourceLocation);
			StructurePieceType structurePieceType = BuiltInRegistries.STRUCTURE_PIECE.getValue(resourceLocation2);
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
