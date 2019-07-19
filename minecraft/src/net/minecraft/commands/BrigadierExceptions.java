package net.minecraft.commands;

import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TranslatableComponent;

public class BrigadierExceptions implements BuiltInExceptionProvider {
	private static final Dynamic2CommandExceptionType DOUBLE_TOO_SMALL = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.double.low", object2, object)
	);
	private static final Dynamic2CommandExceptionType DOUBLE_TOO_BIG = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.double.big", object2, object)
	);
	private static final Dynamic2CommandExceptionType FLOAT_TOO_SMALL = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.float.low", object2, object)
	);
	private static final Dynamic2CommandExceptionType FLOAT_TOO_BIG = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.float.big", object2, object)
	);
	private static final Dynamic2CommandExceptionType INTEGER_TOO_SMALL = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.integer.low", object2, object)
	);
	private static final Dynamic2CommandExceptionType INTEGER_TOO_BIG = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.integer.big", object2, object)
	);
	private static final Dynamic2CommandExceptionType LONG_TOO_SMALL = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.long.low", object2, object)
	);
	private static final Dynamic2CommandExceptionType LONG_TOO_BIG = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("argument.long.big", object2, object)
	);
	private static final DynamicCommandExceptionType LITERAL_INCORRECT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.literal.incorrect", object)
	);
	private static final SimpleCommandExceptionType READER_EXPECTED_START_OF_QUOTE = new SimpleCommandExceptionType(
		new TranslatableComponent("parsing.quote.expected.start")
	);
	private static final SimpleCommandExceptionType READER_EXPECTED_END_OF_QUOTE = new SimpleCommandExceptionType(
		new TranslatableComponent("parsing.quote.expected.end")
	);
	private static final DynamicCommandExceptionType READER_INVALID_ESCAPE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.quote.escape", object)
	);
	private static final DynamicCommandExceptionType READER_INVALID_BOOL = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.bool.invalid", object)
	);
	private static final DynamicCommandExceptionType READER_INVALID_INT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.int.invalid", object)
	);
	private static final SimpleCommandExceptionType READER_EXPECTED_INT = new SimpleCommandExceptionType(new TranslatableComponent("parsing.int.expected"));
	private static final DynamicCommandExceptionType READER_INVALID_LONG = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.long.invalid", object)
	);
	private static final SimpleCommandExceptionType READER_EXPECTED_LONG = new SimpleCommandExceptionType(new TranslatableComponent("parsing.long.expected"));
	private static final DynamicCommandExceptionType READER_INVALID_DOUBLE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.double.invalid", object)
	);
	private static final SimpleCommandExceptionType READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(new TranslatableComponent("parsing.double.expected"));
	private static final DynamicCommandExceptionType READER_INVALID_FLOAT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.float.invalid", object)
	);
	private static final SimpleCommandExceptionType READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(new TranslatableComponent("parsing.float.expected"));
	private static final SimpleCommandExceptionType READER_EXPECTED_BOOL = new SimpleCommandExceptionType(new TranslatableComponent("parsing.bool.expected"));
	private static final DynamicCommandExceptionType READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("parsing.expected", object)
	);
	private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(
		new TranslatableComponent("command.unknown.command")
	);
	private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(
		new TranslatableComponent("command.unknown.argument")
	);
	private static final SimpleCommandExceptionType DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(
		new TranslatableComponent("command.expected.separator")
	);
	private static final DynamicCommandExceptionType DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("command.exception", object)
	);

	@Override
	public Dynamic2CommandExceptionType doubleTooLow() {
		return DOUBLE_TOO_SMALL;
	}

	@Override
	public Dynamic2CommandExceptionType doubleTooHigh() {
		return DOUBLE_TOO_BIG;
	}

	@Override
	public Dynamic2CommandExceptionType floatTooLow() {
		return FLOAT_TOO_SMALL;
	}

	@Override
	public Dynamic2CommandExceptionType floatTooHigh() {
		return FLOAT_TOO_BIG;
	}

	@Override
	public Dynamic2CommandExceptionType integerTooLow() {
		return INTEGER_TOO_SMALL;
	}

	@Override
	public Dynamic2CommandExceptionType integerTooHigh() {
		return INTEGER_TOO_BIG;
	}

	@Override
	public Dynamic2CommandExceptionType longTooLow() {
		return LONG_TOO_SMALL;
	}

	@Override
	public Dynamic2CommandExceptionType longTooHigh() {
		return LONG_TOO_BIG;
	}

	@Override
	public DynamicCommandExceptionType literalIncorrect() {
		return LITERAL_INCORRECT;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedStartOfQuote() {
		return READER_EXPECTED_START_OF_QUOTE;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedEndOfQuote() {
		return READER_EXPECTED_END_OF_QUOTE;
	}

	@Override
	public DynamicCommandExceptionType readerInvalidEscape() {
		return READER_INVALID_ESCAPE;
	}

	@Override
	public DynamicCommandExceptionType readerInvalidBool() {
		return READER_INVALID_BOOL;
	}

	@Override
	public DynamicCommandExceptionType readerInvalidInt() {
		return READER_INVALID_INT;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedInt() {
		return READER_EXPECTED_INT;
	}

	@Override
	public DynamicCommandExceptionType readerInvalidLong() {
		return READER_INVALID_LONG;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedLong() {
		return READER_EXPECTED_LONG;
	}

	@Override
	public DynamicCommandExceptionType readerInvalidDouble() {
		return READER_INVALID_DOUBLE;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedDouble() {
		return READER_EXPECTED_DOUBLE;
	}

	@Override
	public DynamicCommandExceptionType readerInvalidFloat() {
		return READER_INVALID_FLOAT;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedFloat() {
		return READER_EXPECTED_FLOAT;
	}

	@Override
	public SimpleCommandExceptionType readerExpectedBool() {
		return READER_EXPECTED_BOOL;
	}

	@Override
	public DynamicCommandExceptionType readerExpectedSymbol() {
		return READER_EXPECTED_SYMBOL;
	}

	@Override
	public SimpleCommandExceptionType dispatcherUnknownCommand() {
		return DISPATCHER_UNKNOWN_COMMAND;
	}

	@Override
	public SimpleCommandExceptionType dispatcherUnknownArgument() {
		return DISPATCHER_UNKNOWN_ARGUMENT;
	}

	@Override
	public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
		return DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
	}

	@Override
	public DynamicCommandExceptionType dispatcherParseException() {
		return DISPATCHER_PARSE_EXCEPTION;
	}
}
