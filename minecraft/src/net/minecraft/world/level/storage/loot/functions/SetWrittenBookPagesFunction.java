package net.minecraft.world.level.storage.loot.functions;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetWrittenBookPagesFunction extends LootItemConditionalFunction {
	public static final Codec<SetWrittenBookPagesFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<List<Filterable<Component>>, ListOperation>and(
					instance.group(
						WrittenBookContent.PAGES_CODEC.fieldOf("pages").forGetter(setWrittenBookPagesFunction -> setWrittenBookPagesFunction.pages),
						ListOperation.Type.OPERATION_MAP_CODEC.forGetter(setWrittenBookPagesFunction -> setWrittenBookPagesFunction.pageOperation)
					)
				)
				.apply(instance, SetWrittenBookPagesFunction::new)
	);
	private final List<Filterable<Component>> pages;
	private final ListOperation pageOperation;

	protected SetWrittenBookPagesFunction(List<LootItemCondition> list, List<Filterable<Component>> list2, ListOperation listOperation) {
		super(list);
		this.pages = list2;
		this.pageOperation = listOperation;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
		return itemStack;
	}

	@VisibleForTesting
	public WrittenBookContent apply(WrittenBookContent writtenBookContent) {
		List<Filterable<Component>> list = this.pageOperation.apply(writtenBookContent.pages(), this.pages, 100);
		return writtenBookContent.withReplacedPages(list);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_WRITTEN_BOOK_PAGES;
	}
}
