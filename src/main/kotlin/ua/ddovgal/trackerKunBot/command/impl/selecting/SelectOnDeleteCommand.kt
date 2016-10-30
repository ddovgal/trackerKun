package ua.ddovgal.trackerKunBot.command.impl.selecting

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.service.Emoji


class SelectOnDeleteCommand : ParameterNeedCommand, SelectingCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_REMOVE_SELECTION

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = SelectOnDeleteCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val afterSelectingCheck = super<SelectingCommand>.getIfSuitable(inputData)
        var afterChatIdCheck: Command? = null
        afterSelectingCheck?.let {
            afterChatIdCheck = super<ParameterNeedCommand>.getIfSuitable(inputData)
        }
        afterChatIdCheck?.let {
            if (it == afterSelectingCheck) return it
        }
        return null
    }

    override fun exec() {
        val selected = dbConnector.getSpecificSubscriptionOfSubscriber(chatId, selected.toLong())
        dbConnector.unsubscribe(selected, chatId)
        val message = "Yep, ${selected.name} sadly has gone away ${Emoji.CRYING_FACE}"
        trackerKun.sendSimpleMessage(message, chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ANYTHING)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = SelectOnDeleteCommand()
    }
    //endregion
}