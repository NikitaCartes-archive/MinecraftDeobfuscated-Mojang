package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Instruments;

public class InstrumentTagsProvider extends TagsProvider<Instrument> {
	public InstrumentTagsProvider(PackOutput packOutput) {
		super(packOutput, Registry.INSTRUMENT);
	}

	@Override
	protected void addTags() {
		this.tag(InstrumentTags.REGULAR_GOAT_HORNS)
			.add(Instruments.PONDER_GOAT_HORN)
			.add(Instruments.SING_GOAT_HORN)
			.add(Instruments.SEEK_GOAT_HORN)
			.add(Instruments.FEEL_GOAT_HORN);
		this.tag(InstrumentTags.SCREAMING_GOAT_HORNS)
			.add(Instruments.ADMIRE_GOAT_HORN)
			.add(Instruments.CALL_GOAT_HORN)
			.add(Instruments.YEARN_GOAT_HORN)
			.add(Instruments.DREAM_GOAT_HORN);
		this.tag(InstrumentTags.GOAT_HORNS).addTag(InstrumentTags.REGULAR_GOAT_HORNS).addTag(InstrumentTags.SCREAMING_GOAT_HORNS);
	}
}
