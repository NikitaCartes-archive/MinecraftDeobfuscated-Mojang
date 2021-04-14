package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SelectorComponent extends BaseComponent implements ContextAwareComponent {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String pattern;
	@Nullable
	private final EntitySelector selector;
	protected final Optional<Component> separator;

	public SelectorComponent(String string, Optional<Component> optional) {
		this.pattern = string;
		this.separator = optional;
		EntitySelector entitySelector = null;

		try {
			EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
			entitySelector = entitySelectorParser.parse();
		} catch (CommandSyntaxException var5) {
			LOGGER.warn("Invalid selector component: {}: {}", string, var5.getMessage());
		}

		this.selector = entitySelector;
	}

	public String getPattern() {
		return this.pattern;
	}

	@Nullable
	public EntitySelector getSelector() {
		return this.selector;
	}

	public Optional<Component> getSeparator() {
		return this.separator;
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack != null && this.selector != null) {
			Optional<? extends Component> optional = ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i);
			return ComponentUtils.formatList(this.selector.findEntities(commandSourceStack), optional, Entity::getDisplayName);
		} else {
			return new TextComponent("");
		}
	}

	@Override
	public String getContents() {
		return this.pattern;
	}

	public SelectorComponent plainCopy() {
		return new SelectorComponent(this.pattern, this.separator);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof SelectorComponent)) {
			return false;
		} else {
			SelectorComponent selectorComponent = (SelectorComponent)object;
			return this.pattern.equals(selectorComponent.pattern) && super.equals(object);
		}
	}

	@Override
	public String toString() {
		return "SelectorComponent{pattern='" + this.pattern + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
	}
}
