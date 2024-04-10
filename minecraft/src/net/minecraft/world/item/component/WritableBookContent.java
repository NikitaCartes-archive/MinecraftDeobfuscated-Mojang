package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.Filterable;

public record WritableBookContent(List<Filterable<String>> pages) implements BookContent<String, WritableBookContent> {
	public static final WritableBookContent EMPTY = new WritableBookContent(List.of());
	public static final int PAGE_EDIT_LENGTH = 1024;
	public static final int MAX_PAGES = 100;
	private static final Codec<Filterable<String>> PAGE_CODEC = Filterable.codec(Codec.string(0, 1024));
	public static final Codec<List<Filterable<String>>> PAGES_CODEC = PAGE_CODEC.sizeLimitedListOf(100);
	public static final Codec<WritableBookContent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(WritableBookContent::pages)).apply(instance, WritableBookContent::new)
	);
	public static final StreamCodec<ByteBuf, WritableBookContent> STREAM_CODEC = Filterable.streamCodec(ByteBufCodecs.stringUtf8(1024))
		.apply(ByteBufCodecs.list(100))
		.map(WritableBookContent::new, WritableBookContent::pages);

	public WritableBookContent(List<Filterable<String>> pages) {
		if (pages.size() > 100) {
			throw new IllegalArgumentException("Got " + pages.size() + " pages, but maximum is 100");
		} else {
			this.pages = pages;
		}
	}

	public Stream<String> getPages(boolean bl) {
		return this.pages.stream().map(filterable -> (String)filterable.get(bl));
	}

	public WritableBookContent withReplacedPages(List<Filterable<String>> list) {
		return new WritableBookContent(list);
	}
}
