/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class EndCityPieces {
    private static final int MAX_GEN_DEPTH = 8;
    static final SectionGenerator HOUSE_TOWER_GENERATOR = new SectionGenerator(){

        @Override
        public void init() {
        }

        @Override
        public boolean generate(StructureTemplateManager structureTemplateManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, RandomSource randomSource) {
            if (i > 8) {
                return false;
            }
            Rotation rotation = endCityPiece.placeSettings().getRotation();
            EndCityPiece endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece, blockPos, "base_floor", rotation, true));
            int j = randomSource.nextInt(3);
            if (j == 0) {
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 4, -1), "base_roof", rotation, true));
            } else if (j == 1) {
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false));
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 8, -1), "second_roof", rotation, false));
                EndCityPieces.recursiveChildren(structureTemplateManager, TOWER_GENERATOR, i + 1, endCityPiece2, null, list, randomSource);
            } else if (j == 2) {
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false));
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 4, -1), "third_floor_2", rotation, false));
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
                EndCityPieces.recursiveChildren(structureTemplateManager, TOWER_GENERATOR, i + 1, endCityPiece2, null, list, randomSource);
            }
            return true;
        }
    };
    static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(new Tuple<Rotation, BlockPos>(Rotation.NONE, new BlockPos(1, -1, 0)), new Tuple<Rotation, BlockPos>(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Tuple<Rotation, BlockPos>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Tuple<Rotation, BlockPos>(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6)));
    static final SectionGenerator TOWER_GENERATOR = new SectionGenerator(){

        @Override
        public void init() {
        }

        @Override
        public boolean generate(StructureTemplateManager structureTemplateManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, RandomSource randomSource) {
            Rotation rotation = endCityPiece.placeSettings().getRotation();
            EndCityPiece endCityPiece2 = endCityPiece;
            endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(3 + randomSource.nextInt(2), -3, 3 + randomSource.nextInt(2)), "tower_base", rotation, true));
            endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, 7, 0), "tower_piece", rotation, true));
            EndCityPiece endCityPiece3 = randomSource.nextInt(3) == 0 ? endCityPiece2 : null;
            int j = 1 + randomSource.nextInt(3);
            for (int k = 0; k < j; ++k) {
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, 4, 0), "tower_piece", rotation, true));
                if (k >= j - 1 || !randomSource.nextBoolean()) continue;
                endCityPiece3 = endCityPiece2;
            }
            if (endCityPiece3 != null) {
                for (Tuple<Rotation, BlockPos> tuple : TOWER_BRIDGES) {
                    if (!randomSource.nextBoolean()) continue;
                    EndCityPiece endCityPiece4 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece3, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true));
                    EndCityPieces.recursiveChildren(structureTemplateManager, TOWER_BRIDGE_GENERATOR, i + 1, endCityPiece4, null, list, randomSource);
                }
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
            } else if (i == 7) {
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
            } else {
                return EndCityPieces.recursiveChildren(structureTemplateManager, FAT_TOWER_GENERATOR, i + 1, endCityPiece2, null, list, randomSource);
            }
            return true;
        }
    };
    static final SectionGenerator TOWER_BRIDGE_GENERATOR = new SectionGenerator(){
        public boolean shipCreated;

        @Override
        public void init() {
            this.shipCreated = false;
        }

        @Override
        public boolean generate(StructureTemplateManager structureTemplateManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, RandomSource randomSource) {
            Rotation rotation = endCityPiece.placeSettings().getRotation();
            int j = randomSource.nextInt(4) + 1;
            EndCityPiece endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece, new BlockPos(0, 0, -4), "bridge_piece", rotation, true));
            endCityPiece2.setGenDepth(-1);
            int k = 0;
            for (int l = 0; l < j; ++l) {
                if (randomSource.nextBoolean()) {
                    endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, k, -4), "bridge_piece", rotation, true));
                    k = 0;
                    continue;
                }
                endCityPiece2 = randomSource.nextBoolean() ? EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, k, -4), "bridge_steep_stairs", rotation, true)) : EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, k, -8), "bridge_gentle_stairs", rotation, true));
                k = 4;
            }
            if (this.shipCreated || randomSource.nextInt(10 - i) != 0) {
                if (!EndCityPieces.recursiveChildren(structureTemplateManager, HOUSE_TOWER_GENERATOR, i + 1, endCityPiece2, new BlockPos(-3, k + 1, -11), list, randomSource)) {
                    return false;
                }
            } else {
                EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-8 + randomSource.nextInt(8), k, -70 + randomSource.nextInt(10)), "ship", rotation, true));
                this.shipCreated = true;
            }
            endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(4, k, 0), "bridge_end", rotation.getRotated(Rotation.CLOCKWISE_180), true));
            endCityPiece2.setGenDepth(-1);
            return true;
        }
    };
    static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(new Tuple<Rotation, BlockPos>(Rotation.NONE, new BlockPos(4, -1, 0)), new Tuple<Rotation, BlockPos>(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Tuple<Rotation, BlockPos>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Tuple<Rotation, BlockPos>(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12)));
    static final SectionGenerator FAT_TOWER_GENERATOR = new SectionGenerator(){

        @Override
        public void init() {
        }

        @Override
        public boolean generate(StructureTemplateManager structureTemplateManager, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, RandomSource randomSource) {
            Rotation rotation = endCityPiece.placeSettings().getRotation();
            EndCityPiece endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece, new BlockPos(-3, 4, -3), "fat_tower_base", rotation, true));
            endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, 4, 0), "fat_tower_middle", rotation, true));
            for (int j = 0; j < 2 && randomSource.nextInt(3) != 0; ++j) {
                endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(0, 8, 0), "fat_tower_middle", rotation, true));
                for (Tuple<Rotation, BlockPos> tuple : FAT_TOWER_BRIDGES) {
                    if (!randomSource.nextBoolean()) continue;
                    EndCityPiece endCityPiece3 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true));
                    EndCityPieces.recursiveChildren(structureTemplateManager, TOWER_BRIDGE_GENERATOR, i + 1, endCityPiece3, null, list, randomSource);
                }
            }
            endCityPiece2 = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece2, new BlockPos(-2, 8, -2), "fat_tower_top", rotation, true));
            return true;
        }
    };

    static EndCityPiece addPiece(StructureTemplateManager structureTemplateManager, EndCityPiece endCityPiece, BlockPos blockPos, String string, Rotation rotation, boolean bl) {
        EndCityPiece endCityPiece2 = new EndCityPiece(structureTemplateManager, string, endCityPiece.templatePosition(), rotation, bl);
        BlockPos blockPos2 = endCityPiece.template().calculateConnectedPosition(endCityPiece.placeSettings(), blockPos, endCityPiece2.placeSettings(), BlockPos.ZERO);
        endCityPiece2.move(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
        return endCityPiece2;
    }

    public static void startHouseTower(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, RandomSource randomSource) {
        FAT_TOWER_GENERATOR.init();
        HOUSE_TOWER_GENERATOR.init();
        TOWER_BRIDGE_GENERATOR.init();
        TOWER_GENERATOR.init();
        EndCityPiece endCityPiece = EndCityPieces.addHelper(list, new EndCityPiece(structureTemplateManager, "base_floor", blockPos, rotation, true));
        endCityPiece = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece, new BlockPos(-1, 0, -1), "second_floor_1", rotation, false));
        endCityPiece = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece, new BlockPos(-1, 4, -1), "third_floor_1", rotation, false));
        endCityPiece = EndCityPieces.addHelper(list, EndCityPieces.addPiece(structureTemplateManager, endCityPiece, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
        EndCityPieces.recursiveChildren(structureTemplateManager, TOWER_GENERATOR, 1, endCityPiece, null, list, randomSource);
    }

    static EndCityPiece addHelper(List<StructurePiece> list, EndCityPiece endCityPiece) {
        list.add(endCityPiece);
        return endCityPiece;
    }

    static boolean recursiveChildren(StructureTemplateManager structureTemplateManager, SectionGenerator sectionGenerator, int i, EndCityPiece endCityPiece, BlockPos blockPos, List<StructurePiece> list, RandomSource randomSource) {
        if (i > 8) {
            return false;
        }
        ArrayList<StructurePiece> list2 = Lists.newArrayList();
        if (sectionGenerator.generate(structureTemplateManager, i, endCityPiece, blockPos, list2, randomSource)) {
            boolean bl = false;
            int j = randomSource.nextInt();
            for (StructurePiece structurePiece : list2) {
                structurePiece.setGenDepth(j);
                StructurePiece structurePiece2 = StructurePiece.findCollisionPiece(list, structurePiece.getBoundingBox());
                if (structurePiece2 == null || structurePiece2.getGenDepth() == endCityPiece.getGenDepth()) continue;
                bl = true;
                break;
            }
            if (!bl) {
                list.addAll(list2);
                return true;
            }
        }
        return false;
    }

    public static class EndCityPiece
    extends TemplateStructurePiece {
        public EndCityPiece(StructureTemplateManager structureTemplateManager, String string, BlockPos blockPos, Rotation rotation, boolean bl) {
            super(StructurePieceType.END_CITY_PIECE, 0, structureTemplateManager, EndCityPiece.makeResourceLocation(string), string, EndCityPiece.makeSettings(bl, rotation), blockPos);
        }

        public EndCityPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
            super(StructurePieceType.END_CITY_PIECE, compoundTag, structureTemplateManager, resourceLocation -> EndCityPiece.makeSettings(compoundTag.getBoolean("OW"), Rotation.valueOf(compoundTag.getString("Rot"))));
        }

        private static StructurePlaceSettings makeSettings(boolean bl, Rotation rotation) {
            BlockIgnoreProcessor blockIgnoreProcessor = bl ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
            return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(blockIgnoreProcessor).setRotation(rotation);
        }

        @Override
        protected ResourceLocation makeTemplateLocation() {
            return EndCityPiece.makeResourceLocation(this.templateName);
        }

        private static ResourceLocation makeResourceLocation(String string) {
            return new ResourceLocation("end_city/" + string);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putString("Rot", this.placeSettings.getRotation().name());
            compoundTag.putBoolean("OW", this.placeSettings.getProcessors().get(0) == BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
            if (string.startsWith("Chest")) {
                BlockPos blockPos2 = blockPos.below();
                if (boundingBox.isInside(blockPos2)) {
                    RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, randomSource, blockPos2, BuiltInLootTables.END_CITY_TREASURE);
                }
            } else if (boundingBox.isInside(blockPos) && Level.isInSpawnableBounds(blockPos)) {
                if (string.startsWith("Sentry")) {
                    Shulker shulker = EntityType.SHULKER.create(serverLevelAccessor.getLevel());
                    if (shulker != null) {
                        shulker.setPos((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5);
                        serverLevelAccessor.addFreshEntity(shulker);
                    }
                } else if (string.startsWith("Elytra")) {
                    ItemFrame itemFrame = new ItemFrame(serverLevelAccessor.getLevel(), blockPos, this.placeSettings.getRotation().rotate(Direction.SOUTH));
                    itemFrame.setItem(new ItemStack(Items.ELYTRA), false);
                    serverLevelAccessor.addFreshEntity(itemFrame);
                }
            }
        }
    }

    static interface SectionGenerator {
        public void init();

        public boolean generate(StructureTemplateManager var1, int var2, EndCityPiece var3, BlockPos var4, List<StructurePiece> var5, RandomSource var6);
    }
}

