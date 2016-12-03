package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.service.Emoji

/**
 * Its a special command. Can be called in any state. Its don't use [stateNeed] in suitability check
 */
class CancelCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName = "cancel"

    override val chatId = inputData.chatIdFromMessage

    //don't use state in suitability check
    override val stateNeed = SubscriberState.USELESS

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = CancelCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? = super<ReservedCommand>.getIfSuitable(inputData)

    override fun exec() {
        if (inputData.chatStateFromMessage == SubscriberState.WAITING_FOR_ADD_SELECTION)
            dbConnector.removeVariantsOfSubscriber(chatId)

        trackerKun.sendSimpleMessage("Canceled ${Emoji.ROCKET}", chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ANYTHING)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = CancelCommand()
    }
    //endregion
}