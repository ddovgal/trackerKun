package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.service.Emoji


class EasterEggCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName: String = "easterEgg"

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ANYTHING

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = EasterEggCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val afterCommandNameCheck = super<ReservedCommand>.getIfSuitable(inputData)
        var afterChatIdCheck: Command? = null
        afterCommandNameCheck?.let {
            afterChatIdCheck = super<ParameterNeedCommand>.getIfSuitable(inputData)
        }
        afterChatIdCheck?.let {
            if (it == afterCommandNameCheck) return it
        }
        return null
    }

    override fun exec() {
        trackerKun.sendSimpleMessage("Lol, you got me ${Emoji.ROCKET}", chatId)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = EasterEggCommand()
    }
    //endregion
}

/*
* // getIfSuitable realization for ParameterNeedCommand-ReservedCommand
    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val afterCommandNameCheck = super<ReservedCommand>.getIfSuitable(inputData)
        var afterChatIdCheck: Command? = null
        afterCommandNameCheck?.let {
            afterChatIdCheck = super<ParameterNeedCommand>.getIfSuitable(inputData)
        }
        afterChatIdCheck?.let {
            if (it == afterCommandNameCheck) return it
        }
        return null
    }
* */