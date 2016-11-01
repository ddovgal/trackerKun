package ua.ddovgal.trackerKunBot.command.impl

import ua.ddovgal.trackerKunBot.command.CommandInputData
import ua.ddovgal.trackerKunBot.command.ParameterNeedCommand
import ua.ddovgal.trackerKunBot.command.SubscriberState
import ua.ddovgal.trackerKunBot.service.Emoji
import ua.ddovgal.trackerKunBot.source.SourceManager


class FindTitlesCommand : ParameterNeedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ADD_STRING

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = FindTitlesCommand(inputData)

    override fun exec() {
        val titleName = inputData.update.message.text
        val found = SourceManager.searchForTitle(titleName)

        dbConnector.putVariantsForSubscriber(chatId, found)

        val message: String
        val newSubscriberState: SubscriberState

        if (found.isEmpty()) {
            message = "Nothing founded ${Emoji.PENSIVE_FACE}"
            newSubscriberState = SubscriberState.WAITING_FOR_ANYTHING
        } else {
            val nothing = "${Emoji.CROSS_MARK}/0 Nothing out of this\n"

            message = nothing + found.mapIndexed { i, title ->
                "${Emoji.PAGE_WITH_CURL}/${i + 1} [${title.source.name}/" +
                        "${title.source.language.shortName}] ${title.name}"
            }.joinToString(separator = "\n")
            newSubscriberState = SubscriberState.WAITING_FOR_ADD_SELECTION
        }

        trackerKun.sendSimpleMessage(message, chatId)
        dbConnector.updateSubscribersState(chatId, newSubscriberState)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = FindTitlesCommand()
    }
    //endregion
}