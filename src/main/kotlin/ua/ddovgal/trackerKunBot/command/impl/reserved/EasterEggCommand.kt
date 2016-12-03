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
        val firstCheck = super<ReservedCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
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