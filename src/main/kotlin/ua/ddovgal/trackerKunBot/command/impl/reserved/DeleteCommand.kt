package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.service.Emoji


class DeleteCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName: String = "delete"

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ANYTHING

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = DeleteCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val firstCheck = super<ReservedCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
    }

    override fun exec() {
        val subscriptions = dbConnector.getSubscriptionsOfSubscriber(chatId)

        val message: String

        if (subscriptions.isEmpty()) {
            message = "There is nothing in your observable list ${Emoji.PENSIVE_FACE}"
        } else {
            val nothing = "${Emoji.CROSS_MARK}/0 I changed my mind :D\n"

            message = nothing + subscriptions.mapIndexed { i, title ->
                "${Emoji.PAGE_WITH_CURL}/${i + 1} [${title.source.name}/" +
                        "${title.source.language.shortName}] ${title.name}"
            }.joinToString(separator = "\n")
            dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_REMOVE_SELECTION)
        }

        trackerKun.sendSimpleMessage(message, chatId)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = DeleteCommand()
    }
    //endregion
}