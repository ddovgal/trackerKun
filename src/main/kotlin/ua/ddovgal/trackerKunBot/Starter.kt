package ua.ddovgal.trackerKunBot

import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.exceptions.TelegramApiException

fun main(args: Array<String>) {
    try {
        TelegramBotsApi().registerBot(TrackerKunBot)
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}


