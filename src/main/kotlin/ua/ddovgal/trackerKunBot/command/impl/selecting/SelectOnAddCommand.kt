package ua.ddovgal.trackerKunBot.command.impl.selecting

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.service.Emoji


class SelectOnAddCommand : ParameterNeedCommand, SelectingCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ADD_SELECTION

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = SelectOnAddCommand(inputData)

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
        val selected = dbConnector.getSpecificVariantOfSubscriber(chatId, selected.toLong())
        dbConnector.subscribe(selected, chatId)
        dbConnector.removeVariantsOfSubscriber(chatId)
        val message = "Great ${Emoji.THUMBS_UP_SIGN}\n" +
                "${selected.name} was added to your observable list"
        trackerKun.sendSimpleMessage(message, chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ANYTHING)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = SelectOnAddCommand()
    }
    //endregion
}