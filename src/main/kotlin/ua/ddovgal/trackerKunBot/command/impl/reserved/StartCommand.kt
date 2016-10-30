package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.CommandInputData
import ua.ddovgal.trackerKunBot.command.ParameterNeedCommand
import ua.ddovgal.trackerKunBot.command.ReservedCommand
import ua.ddovgal.trackerKunBot.command.SubscriberState
import ua.ddovgal.trackerKunBot.service.Emoji

/**
 * Its a special command. Can be called in any state. Its don't use [stateNeed] in suitability check
 */
class StartCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName = "start"

    override val chatId = inputData.chatIdFromMessage

    //don't use state in suitability check
    override val stateNeed = SubscriberState.WAITING_FOR_ANYTHING

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = StartCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData) = super<ReservedCommand>.getIfSuitable(inputData)

    override fun exec() {
        trackerKun.sendSimpleMessage("Hello, young otaku ${Emoji.RAISED_HAND}\n" +
                "I will observe your manga for you. So, what I have to track ?\n" +
                "Please, tell me by /add command ${Emoji.SMILING_FACE_WITH_OPEN_MOUTH_AND_SMILING_EYES}", chatId)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = StartCommand()
    }
    //endregion
}