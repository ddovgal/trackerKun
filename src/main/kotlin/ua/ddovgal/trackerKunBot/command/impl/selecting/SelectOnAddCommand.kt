package ua.ddovgal.trackerKunBot.command.impl.selecting

import ua.ddovgal.trackerKunBot.command.*
import ua.ddovgal.trackerKunBot.entity.Title
import ua.ddovgal.trackerKunBot.service.Emoji
import ua.ddovgal.trackerKunBot.service.TryCaughtException
import java.util.*


class SelectOnAddCommand : ParameterNeedCommand, SelectingCommand {

    constructor(inputData: CommandInputData) : super(inputData)

    override val chatId = inputData.chatIdFromMessage

    override val stateNeed = SubscriberState.WAITING_FOR_ADD_SELECTION

    override fun extractState(inputData: CommandInputData) = inputData.chatStateFromMessage

    override fun fabricMethod(inputData: CommandInputData) = SelectOnAddCommand(inputData)

    override fun getIfSuitable(inputData: CommandInputData): Command? {
        val firstCheck = super<SelectingCommand>.getIfSuitable(inputData)
        if (firstCheck != null
                && super<ParameterNeedCommand>.getIfSuitable(inputData) != null) return firstCheck
        else return null
    }

    override fun exec() {
        if (selected == 0) {
            trackerKun.sendSimpleMessage("Okay... ${Emoji.DISAPPOINTED_BUT_RELIEVED_FACE}", chatId)
            dbConnector.removeVariantsOfSubscriber(chatId)
            dbConnector.updateSubscribersState(chatId, SubscriberState.WAITING_FOR_ANYTHING)
            return
        }

        val selected: Title

        try {
            selected = dbConnector.getSpecificVariantOfSubscriber(chatId, this.selected.toLong())
        } catch(e: NoSuchElementException) {
            trackerKun.sendSimpleMessage("Wrong number ${Emoji.FACE_SCREAMING_IN_FEAR}\nTry again", chatId)
            return
        }

        //if it was jut added/never got updates
        if (selected.lastCheckedChapterUrl == "") {
            try {
                selected.checkLastChapterUrl()?.let { selected.lastCheckedChapterUrl = it }
                selected.checkLastChapterName()?.let { selected.lastCheckedChapterName = it }
                selected.checkLastChapterReleaseDate()?.let { selected.lastCheckedChapterReleaseDate = it }
                //don't need to update, because 'dbConnector.subscribe(selected, chatId)' will do it
                //dbConnector.updateTitle(selected)
            } catch(e: Exception) {
                logger.warn("Empty title has increased it's subscribers count", e)
            }
        }

        val message = try {
            dbConnector.subscribe(selected, chatId)
            "Great ${Emoji.THUMBS_UP_SIGN}\n${selected.name} was added to your observable list"
        } catch(e: TryCaughtException) {
            "It seems, that you already have ${selected.name} in your observable list " +
                    "${Emoji.SMILING_FACE_WITH_SMILING_EYES}"
        } finally {
            dbConnector.removeVariantsOfSubscriber(chatId)
        }

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