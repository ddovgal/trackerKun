package ua.ddovgal.trackerKunBot.command.impl

import ua.ddovgal.trackerKunBot.command.CommandInputData
import ua.ddovgal.trackerKunBot.command.ParameterNeedCommand
import ua.ddovgal.trackerKunBot.command.SubscriberState
import ua.ddovgal.trackerKunBot.service.Emoji
import ua.ddovgal.trackerKunBot.source.MintMangaSource
import ua.ddovgal.trackerKunBot.source.ReadMangaSource


class FindTitlesCommand : ParameterNeedCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ADD_STRING

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = FindTitlesCommand(inputData)

    override fun exec() {
        val titleName = inputData.update.message.text
        val found = ReadMangaSource().searchForTitle(titleName)
                .plus(MintMangaSource().searchForTitle(titleName))

        dbConnector.putVariantsForSubscriber(chatId, found)

        val message = found
                .mapIndexed { i, title ->
                    "${Emoji.PAGE_WITH_CURL}/${i + 1} " +
                            "[${title.source.name}/${title.source.language.shortName}] ${title.name}"
                }
                .joinToString(separator = "\n")

        trackerKun.sendSimpleMessage(message, chatId)
        dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ADD_SELECTION)
    }

    //region For CommandFactory list only
    private constructor() : super()

    companion object {
        val empty = FindTitlesCommand()
    }
    //endregion
}