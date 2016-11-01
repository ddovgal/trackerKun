package ua.ddovgal.trackerKunBot

import org.apache.log4j.PropertyConfigurator
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.exceptions.TelegramApiException

fun main(args: Array<String>) {
    try {
        PropertyConfigurator.configure("log4j.properties")
        TelegramBotsApi().registerBot(TrackerKunBot)
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}


