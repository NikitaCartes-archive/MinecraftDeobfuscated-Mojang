package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidTags {
	private static TagCollection<Fluid> source = new TagCollection<>(resourceLocation -> Optional.empty(), "", false, "");
	private static int resetCount;
	public static final Tag<Fluid> WATER = bind("water");
	public static final Tag<Fluid> LAVA = bind("lava");

	public static void reset(TagCollection<Fluid> tagCollection) {
		source = tagCollection;
		resetCount++;
	}

	public static TagCollection<Fluid> getAllTags() {
		return source;
	}

	private static Tag<Fluid> bind(String string) {
		return new FluidTags.Wrapper(new ResourceLocation(string));
	}

	public static class Wrapper extends Tag<Fluid> {
		private int check = -1;
		private Tag<Fluid> actual;

		public Wrapper(ResourceLocation resourceLocation) {
			super(resourceLocation);
		}

		public boolean contains(Fluid fluid) {
			if (this.check != FluidTags.resetCount) {
				this.actual = FluidTags.source.getTagOrEmpty(this.getId());
				this.check = FluidTags.resetCount;
			}

			return this.actual.contains(fluid);
		}

		@Override
		public Collection<Fluid> getValues() {
			if (this.check != FluidTags.resetCount) {
				this.actual = FluidTags.source.getTagOrEmpty(this.getId());
				this.check = FluidTags.resetCount;
			}

			return this.actual.getValues();
		}

		@Override
		public Collection<Tag.Entry<Fluid>> getSource() {
			if (this.check != FluidTags.resetCount) {
				this.actual = FluidTags.source.getTagOrEmpty(this.getId());
				this.check = FluidTags.resetCount;
			}

			return this.actual.getSource();
		}
	}
}
