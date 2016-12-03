package ua.ddovgal.trackerKunBot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import ua.ddovgal.trackerKunBot.command.CommandFactory
import ua.ddovgal.trackerKunBot.service.Emoji
import ua.ddovgal.trackerKunBot.service.worker.BackgroundMonitor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object TrackerKunBot : TelegramLongPollingBot() {

    private val logger: Logger = LoggerFactory.getLogger(TrackerKunBot::class.java)
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val backgroundTask = BackgroundMonitor()

    private val PERIOD_OF_INSPECTION_IN_MINUTES = 15L

    init {
        executor.scheduleAtFixedRate(backgroundTask, 0, PERIOD_OF_INSPECTION_IN_MINUTES, TimeUnit.MINUTES)
    }

    override fun getBotUsername() = BOT_USERNAME

    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(update: Update) {
        try {
            val suitableCommand = CommandFactory.getSuitableInstance(update)

            if (suitableCommand == null) {
                val warnMessage = "None of the commands are suitable for update"
                val textMessage = "I can't understand you ${Emoji.PENSIVE_FACE}\n" +
                        "On update, you send, there are no variants of command possible"

                notifyUserAboutError(warnMessage, textMessage, update)
            } else {
                suitableCommand.exec()
            }

        } catch(e: RuntimeException) {
            val warnMessage = "There are several variants of command possible"
            val textMessage = "On update, you send, there are several variants of command possible\n" +
                    "Please, be more precise ${Emoji.PENSIVE_FACE}"

            notifyUserAboutError(warnMessage, textMessage, update)
        }
    }

    private fun notifyUserAboutError(warnMessage: String, textMessage: String, update: Update) {
        logger.warn(warnMessage)
        val chatId = update.message?.chatId
        if (chatId == null) logger.error("Cant notify user, that there are several/no variants of command possible. No chatId")
        else sendSimpleMessage(textMessage, chatId)
    }

    /**
     * Can get [RuntimeException] exception from [splitTextIfNeed] method, which used inside
     * @throws [RuntimeException] if the word, which length is more than 4096 letters exist
     */
    fun sendSimpleMessage(text: String, chatId: Long) {
        val messageToSend = SendMessage()
        messageToSend.chatId = chatId.toString()

        try {
            val textPieces = splitTextIfNeed(text)
            textPieces.forEach {
                messageToSend.text = it
                safeSendMessage(messageToSend)
            }
        } catch(e: RuntimeException) {
            logger.warn("Trying to send message to [$chatId], but there is word, which length is more than 4096 letters")
        }
    }

    private fun safeSendMessage(message: SendMessage) {
        try {
            sendMessage(message)
        } catch (e: TelegramApiException) {
            logger.error("Cant send message to [${message.chatId}](text is less, than 4096 letters)", e)
        }
    }

    /**
     * @throws [RuntimeException] if the line, which length >= 4096 letters exist
     */
    private fun splitTextIfNeed(text: String): List<String> {
        if (text.length < 4096) return listOf(text)

        val pieces = text.split("\n")
        if (pieces.any { it.length > 4096 }) throw RuntimeException("There is line, which length is more 4096 letters")

        val result = mutableListOf<String>()
        val piece: StringBuilder = StringBuilder()
        var currentPieceLength = 0

        pieces.forEach {
            if (currentPieceLength + it.length + 1 < 4096) {
                currentPieceLength += it.length + 1
                piece.append(it).append("\n")
            } else {
                result.add(piece.toString())
                currentPieceLength = it.length
                piece.setLength(0)
                piece.append(it).append("\n")
            }
        }

        if (piece.isNotEmpty()) result.add(piece.substring(0, piece.lastIndex).toString())

        return result
    }

    fun finalize() {
        try {
            executor.shutdown()
            executor.awaitTermination(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            logger.warn("Tasks interrupted", e)
        } finally {
            if (!executor.isTerminated) {
                logger.warn("Cancel non-finished tasks")
            }
            executor.shutdownNow()
            logger.info("Shutdown finished")
        }
    }
}