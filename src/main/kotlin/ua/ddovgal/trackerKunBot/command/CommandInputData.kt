package ua.ddovgal.trackerKunBot.command

import org.telegram.telegrambots.api.objects.Update
import ua.ddovgal.trackerKunBot.entity.Subscriber
import ua.ddovgal.trackerKunBot.service.worker.DatabaseConnector


class CommandInputData(val update: Update) {

    // must throw RuntimeException("No chatId present"), but we need some value
    // for empty command objects in CommandFactory
    val chatIdFromMessage: Long by lazy { update.message?.chatId ?: 0 }

    val chatStateFromMessage: SubscriberState? by lazy {
        val chatId = update.message?.chatId
        if (chatId == null) null
        else getFromDbChatIdStateOrCreateIfNotExist(chatId)
    }

    val commandNameFromMessage: String? by lazy {
        var result: String? = null
        val text = update.message?.text
        text?.let {
            if (text.length > 1 && text[0] == '/') {
                val cut = text.substring(1)
                result = cut.split(" ")[0]
            }
        }
        result
    }

    private fun getFromDbChatIdStateOrCreateIfNotExist(chatId: Long): SubscriberState {
        val state = DatabaseConnector.getSubscriber(chatId)?.state

        return if (state != null) state
        else {
            val newSubscriber = Subscriber(
                    chatId = chatId,
                    fName = update.message.chat.firstName,
                    sName = update.message.chat.lastName
            )
            DatabaseConnector.saveSubscriber(newSubscriber)
            newSubscriber.state
        }
    }
}