package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.*


class SwitchCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName: String = "switch"

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ANYTHING

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = SwitchCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val firstCheck = super<ReservedCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
    }

    override fun exec() {
        val newSubscriptionState = dbConnector.changeSubscriptionStateOfSubscriber(chatId)
        val message = "Ok, now your subscriptions notification state is ${if (newSubscriptionState) "ON" else "OFF"}"
        trackerKun.sendSimpleMessage(message, chatId)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = SwitchCommand()
    }
    //endregion
}