package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWritableBookPagesFunction extends LootItemConditionalFunction {
	public static final Codec<SetWritableBookPagesFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<List<Filterable<String>>, ListOperation>and(
					instance.group(
						WritableBookContent.PAGES_CODEC.fieldOf("pages").forGetter(setWritableBookPagesFunction -> setWritableBookPagesFunction.pages),
						ListOperation.codec(100).forGetter(setWritableBookPagesFunction -> setWritableBookPagesFunction.pageOperation)
					)
				)
				.apply(instance, SetWritableBookPagesFunction::new)
	);
	private final List<Filterable<String>> pages;
	private final ListOperation pageOperation;

	protected SetWritableBookPagesFunction(List<LootItemCondition> list, List<Filterable<String>> list2, ListOperation listOperation) {
		super(list);
		this.pages = list2;
		this.pageOperation = listOperation;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY, this::apply);
		return itemStack;
	}

	public WritableBookContent apply(WritableBookContent writableBookContent) {
		List<Filterable<String>> list = this.pageOperation.apply(writableBookContent.pages(), this.pages, 100);
		return writableBookContent.withReplacedPages(list);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_WRITABLE_BOOK_PAGES;
	}
}
