package ua.ddovgal.trackerKunBot.service.worker

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.exceptions.TelegramApiRequestException
import ua.ddovgal.trackerKunBot.TrackerKunBot
import ua.ddovgal.trackerKunBot.entity.Subscriber
import ua.ddovgal.trackerKunBot.entity.Title
import ua.ddovgal.trackerKunBot.service.Emoji
import ua.ddovgal.trackerKunBot.service.TryCaughtException
import java.io.IOException

class BackgroundMonitor : Runnable {

    private val botInstance = TrackerKunBot
    private val dbConnector = DatabaseConnector
    private val logger = LoggerFactory.getLogger(BackgroundMonitor::class.java)

    override fun run() = inspection()

    private fun inspection() {
        val allActiveTitles = dbConnector.getAllPermanentTitles()

        allActiveTitles.forEach {
            val latestChapterUrl: String

            try {
                latestChapterUrl = it.checkLastChapterUrl() ?: throw RuntimeException("No url found in entry")
            } catch(e: TryCaughtException) {
                logger.warn("There is no chapters in [${it.name}/${it.url}]")
                return@forEach
            } catch (e: IOException) {
                logger.warn("Unsuccessful request to manga source [${it.name}/${it.url}]", e)
                return@forEach
            } catch (e: Exception) {
                logger.error("Cant load latest chapter's URL of [${it.name}/${it.url}]", e)
                return@forEach
            } finally { //need this because there may be 429 HTTP response, which means "Too Many Requests"
                Thread.sleep(500) //so just wait a half of second
            }

            if (latestChapterUrl != it.lastCheckedChapterUrl) {
                it.lastCheckedChapterUrl = latestChapterUrl

                try {
                    it.lastCheckedChapterName = it.checkLastChapterName() ?: throw RuntimeException("No name found in entry")
                } catch(e: Exception) {
                    logger.warn("Cant load latest chapter's name of [${it.name}/${it.url}]", e)
                }

                try {
                    it.lastCheckedChapterReleaseDate = it.checkLastChapterReleaseDate() ?: throw RuntimeException("No release date found in entry")
                } catch(e: Exception) {
                    logger.warn("Cant load latest chapter's release date of [${it.name}/${it.url}]", e)
                }

                dbConnector.updateTitle(it)
                val activeSubscribersOfTitle = dbConnector.getSubscribersOfTitle(it)
                notifySubscribers(activeSubscribersOfTitle, it, false)
            }
        }
    }

    private fun notifySubscribers(subscribers: List<Subscriber>, title: Title, wasUpdated: Boolean) {
        var newMessageText = "${Emoji.RAISED_HAND} Hey, new ${title.name} chapter at ${title.source.name}" +
                "(${title.source.language.shortName}) ${Emoji.SMIRKING_FACE}\n"

        newMessageText += if (title.lastCheckedChapterName.isNotEmpty()) "${title.lastCheckedChapterName} " +
                "is here ${Emoji.PARTY_POPPER}"
        else "And its so new, it still has no name... ${Emoji.PENSIVE_FACE}"

        val updateMessageText = "${Emoji.RAISED_HAND} Hey, latest ${title.name} chapter at ${title.source.name}" +
                "(${title.source.language.shortName}) was updated ${Emoji.SMILING_FACE_WITH_SUNGLASSES}\n" +
                "Time to check what's new ${Emoji.CAT_FACE_WITH_WRY_SMILE}"

        val message = SendMessage()
        val urlButton = InlineKeyboardButton()
        val keyboardMarkup = InlineKeyboardMarkup()

        if (wasUpdated) message.text = updateMessageText
        else message.text = newMessageText

        urlButton.text = "Check it"
        urlButton.url = title.lastCheckedChapterUrl
        keyboardMarkup.keyboard = listOf(listOf(urlButton))
        message.replyMarkup = keyboardMarkup

        subscribers.forEach {
            message.chatId = it.chatId.toString()
            try {
                botInstance.sendMessage(message)
            } catch (e: TelegramApiRequestException) {
                if (e.errorCode == 403) {
//                    dbConnector.unsubscribe(title, it.chatId)
                    logger.debug("Bot was blocked by user", e)
                } else throw e
            } catch(e: TelegramApiException) {
                logger.error("Cant send message", e)
            }
        }
    }
}