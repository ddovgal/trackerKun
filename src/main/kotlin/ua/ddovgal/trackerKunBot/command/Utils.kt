package ua.ddovgal.trackerKunBot.command

import ua.ddovgal.trackerKunBot.command.impl.PlainTextCommand
import ua.ddovgal.trackerKunBot.command.impl.SelectionCommand
import ua.ddovgal.trackerKunBot.command.impl.reserved.AddCommand
import ua.ddovgal.trackerKunBot.command.impl.reserved.DeleteCommand
import ua.ddovgal.trackerKunBot.command.impl.reserved.ListCommand
import ua.ddovgal.trackerKunBot.command.impl.reserved.StartCommand

fun defineQuery(input: String): Pair<InputStringType, String> {
    if (input.startsWith('/')) {
        val cutInput = input.removePrefix("/")
        try {
            input.removePrefix("/").toInt()
            return InputStringType.SELECTION_COMMAND to cutInput
        } catch (e: NumberFormatException) {
            return InputStringType.RESERVED_COMMAND to cutInput
        }
    } else return InputStringType.PLAIN_TEXT to input
}

fun buildCommand(dataInput: Pair<InputStringType, String>): Command {
    val type = dataInput.first
    val stringInput = dataInput.second
    when (type) {
        InputStringType.RESERVED_COMMAND -> {
            val splitArray = stringInput.split(" ").toMutableList()
            val commandName = splitArray.removeAt(0)
            when (commandName) {
                "start" -> return StartCommand(splitArray)
                "add" -> return AddCommand(splitArray)
                "delete" -> return DeleteCommand(splitArray)
                "list" -> return ListCommand(splitArray)
                else -> throw CommandPerformationException("Fond wrong command name after input line parsing")
            }
        }
        InputStringType.SELECTION_COMMAND -> return SelectionCommand(stringInput.toInt())
        InputStringType.PLAIN_TEXT -> return PlainTextCommand(stringInput)
    }
}