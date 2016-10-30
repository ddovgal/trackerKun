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
        val subscriptions = dbConnector.getSubscriptionsOfSubscriber(chatId)

        val message = subscriptions
                .mapIndexed { i, title ->
                    "${Emoji.PAGE_WITH_CURL}/${i + 1} " +
                            "[${title.source.name}/${title.source.language.shortName}] ${title.name}"
                }
                .joinToString(separator = "\n")

        trackerKun.sendSimpleMessage(message, chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_REMOVE_SELECTION)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = DeleteCommand()
    }
    //endregion
}