package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class StructureTemplatePool {
	public static final StructureTemplatePool EMPTY = new StructureTemplatePool(
		new ResourceLocation("empty"), new ResourceLocation("empty"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID
	);
	public static final StructureTemplatePool INVALID = new StructureTemplatePool(
		new ResourceLocation("invalid"), new ResourceLocation("invalid"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID
	);
	private final ResourceLocation name;
	private final ImmutableList<Pair<StructurePoolElement, Integer>> rawTemplates;
	private final List<StructurePoolElement> templates;
	private final ResourceLocation fallback;
	private final StructureTemplatePool.Projection projection;
	private int maxSize = Integer.MIN_VALUE;

	public StructureTemplatePool(
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		List<Pair<StructurePoolElement, Integer>> list,
		StructureTemplatePool.Projection projection
	) {
		this.name = resourceLocation;
		this.rawTemplates = ImmutableList.copyOf(list);
		this.templates = Lists.<StructurePoolElement>newArrayList();

		for (Pair<StructurePoolElement, Integer> pair : list) {
			for (Integer integer = 0; integer < pair.getSecond(); integer = integer + 1) {
				this.templates.add(pair.getFirst().setProjection(projection));
			}
		}

		this.fallback = resourceLocation2;
		this.projection = projection;
	}

	public int getMaxSize(StructureManager structureManager) {
		if (this.maxSize == Integer.MIN_VALUE) {
			this.maxSize = this.templates
				.stream()
				.mapToInt(structurePoolElement -> structurePoolElement.getBoundingBox(structureManager, BlockPos.ZERO, Rotation.NONE).getYSpan())
				.max()
				.orElse(0);
		}

		return this.maxSize;
	}

	public ResourceLocation getFallback() {
		return this.fallback;
	}

	public StructurePoolElement getRandomTemplate(Random random) {
		return (StructurePoolElement)this.templates.get(random.nextInt(this.templates.size()));
	}

	public List<StructurePoolElement> getShuffledTemplates(Random random) {
		return ImmutableList.copyOf(ObjectArrays.shuffle(this.templates.toArray(new StructurePoolElement[0]), random));
	}

	public ResourceLocation getName() {
		return this.name;
	}

	public int size() {
		return this.templates.size();
	}

	public static enum Projection {
		TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
		RIGID("rigid", ImmutableList.of());

		private static final Map<String, StructureTemplatePool.Projection> BY_NAME = (Map<String, StructureTemplatePool.Projection>)Arrays.stream(values())
			.collect(Collectors.toMap(StructureTemplatePool.Projection::getName, projection -> projection));
		private final String name;
		private final ImmutableList<StructureProcessor> processors;

		private Projection(String string2, ImmutableList<StructureProcessor> immutableList) {
			this.name = string2;
			this.processors = immutableList;
		}

		public String getName() {
			return this.name;
		}

		public static StructureTemplatePool.Projection byName(String string) {
			return (StructureTemplatePool.Projection)BY_NAME.get(string);
		}

		public ImmutableList<StructureProcessor> getProcessors() {
			return this.processors;
		}
	}
}
