package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.command.*

class AddCommand : ParameterNeedCommand, ReservedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val commandName: String = "add"

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ANYTHING

    override fun extractCommandName(inputData: CommandInputData) = inputData.commandNameFromMessage

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = AddCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val firstCheck = super<ReservedCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
    }

    override fun exec() {
        trackerKun.sendSimpleMessage("What manga you are reading ? I'll try to find some.", chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ADD_STRING)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = AddCommand()
    }
    //endregion
}