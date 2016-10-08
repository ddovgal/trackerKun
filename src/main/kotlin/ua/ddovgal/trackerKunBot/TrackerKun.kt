package ua.ddovgal.trackerKunBot

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import ua.ddovgal.trackerKunBot.command.buildCommand
import ua.ddovgal.trackerKunBot.command.defineQuery
import ua.ddovgal.trackerKunBot.service.BOT_TOKEN
import ua.ddovgal.trackerKunBot.service.BOT_USERNAME
import ua.ddovgal.trackerKunBot.service.TrackerThread

class TrackerKun : TelegramLongPollingBot() {

    val trackerThread = TrackerThread

    init {
        trackerThread.trackerKun = this
        Thread(trackerThread).start()
    }

    override fun getBotUsername() = BOT_USERNAME

    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            if (message.hasText()) {
                val command = buildCommand(defineQuery(message.text))
                command.exec(message.chatId, this)
            }
        }
    }

    fun sendSimpleMessage(text: String, chatId: Long) {
        val sendMessageRequest = SendMessage()
        sendMessageRequest.chatId = chatId.toString()
        if (text.length < 4096) {
            sendMessageRequest.text = text
            try {
                sendMessage(sendMessageRequest)
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        } else {
            val lines = text.split("\n")
            var piece = ""
            for (line in lines) {
                if (piece.length < 4096 - line.length) piece += "$line\n"
                else {
                    sendMessageRequest.text = piece
                    try {
                        sendMessage(sendMessageRequest)
                    } catch (e: TelegramApiException) {
                        e.printStackTrace()
                    }
                    piece = line
                }
            }
            sendMessageRequest.text = piece
            try {
                sendMessage(sendMessageRequest)
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
    }

    fun finalize() {
        trackerThread.runFlag = false
        trackerThread.interrupt()
    }
}