package ua.ddovgal.trackerKunBot.command

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.objects.Update
import ua.ddovgal.trackerKunBot.command.impl.FindTitlesCommand
import ua.ddovgal.trackerKunBot.command.impl.reserved.*
import ua.ddovgal.trackerKunBot.command.impl.selecting.SelectOnAddCommand
import ua.ddovgal.trackerKunBot.command.impl.selecting.SelectOnDeleteCommand


object CommandFactory {

    private val logger = LoggerFactory.getLogger(CommandFactory::class.java)

    /**
     * @throws [RuntimeException] if there are several variants of command possible
     * @return 'null' if none of the commands are suitable
     */
    fun getSuitableInstance(update: Update): Command? {
        val commandInputData = CommandInputData(update)
        val possibleCommands = mutableListOf<Command>()

        //just for CancelCommand's return@loop
        run loop@ {
            allCommandsTogether.forEach {
                val commandCandidate = it.getIfSuitable(commandInputData)
                commandCandidate?.let {
                    possibleCommands.add(it)
                    //CancelCommand have HIGH priority
                    //CancelCommand is first in allCommandsTogether
                    if (it is CancelCommand) return@loop
                }
            }
        }

        when (possibleCommands.size) {
            0 -> return null
            1 -> return possibleCommands[0]
            else -> {
                val message = "There are several variants of command possible. But must be only one"
                logger.error(message)
                throw RuntimeException(message)
            }
        }
    }

    /**
     * For all of this commands, for your comfort, realize companion object which returns
     * empty instance of his [Command] class by calling it's private constructor
     */
    // contains empty variants of commands
    private val reservedCommands = listOf<ReservedCommand>(
            CancelCommand.empty,
            AddCommand.empty,
            DeleteCommand.empty,
            EasterEggCommand.empty,
            ListCommand.empty,
            StartCommand.empty,
            SwitchCommand.empty
    )

    // contains empty variants of commands
    private val selectingCommands = listOf<SelectingCommand>(
            SelectOnAddCommand.empty,
            SelectOnDeleteCommand.empty
    )

    // contains empty variants of commands
    private val undefinedCommands = listOf<Command>(
            FindTitlesCommand.empty
    )

    private val allCommandsTogether = reservedCommands + selectingCommands + undefinedCommands
}