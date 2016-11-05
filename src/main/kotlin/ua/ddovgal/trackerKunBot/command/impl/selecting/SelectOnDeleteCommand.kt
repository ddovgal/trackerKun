package ua.ddovgal.trackerKunBot.command.impl.selecting

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.entity.Title
import ua.ddovgal.trackerKunBot.service.Emoji
import java.util.*


class SelectOnDeleteCommand : ParameterNeedCommand, SelectingCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_REMOVE_SELECTION

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = SelectOnDeleteCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val firstCheck = super<SelectingCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
    }

    override fun exec() {

        if (selected == 0) {
            trackerKun.sendSimpleMessage("Right choice ${Emoji.THUMBS_UP_SIGN}", chatId)
            dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ANYTHING)
            return
        }

        val selected: Title
        try {
            selected = dbConnector.getSpecificSubscriptionOfSubscriber(chatId, this.selected.toLong())
        } catch(e: NoSuchElementException) {
            trackerKun.sendSimpleMessage("Wrong number ${Emoji.FACE_SCREAMING_IN_FEAR}\nTry again", chatId)
            return
        }

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