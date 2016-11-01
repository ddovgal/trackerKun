package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.service.Emoji

class StartCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName = "start"

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ANYTHING

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = StartCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val firstCheck = super<ReservedCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
    }

    override fun exec() {
        trackerKun.sendSimpleMessage("Hello, young otaku ${Emoji.RAISED_HAND}\n" +
                "I will observe your manga for you. So, what I have to track ?\n" +
                "Please, tell me by /add command ${Emoji.SMILING_FACE_WITH_OPEN_MOUTH_AND_SMILING_EYES}", chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ANYTHING)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = StartCommand()
    }
    //endregion
}