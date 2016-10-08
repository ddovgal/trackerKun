package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.command.ReservedCommand

class ListCommand(params: List<String>) : ReservedCommand(params) {
    override fun exec(chatId: Long, trackerKun: TrackerKun) {
        trackerKun.trackerThread.changeState(chatId, BotState.WAITING_FOR_ANY)

        val subscriptions = trackerKun.trackerThread.subscriptionForId(chatId)

        val message: String
        if (subscriptions.isEmpty()) {
            message = "Your list is empty"
        } else message = subscriptions
                .mapIndexed { i, source -> "${i + 1}.[${source.sourceName}] ${source.title}" }
                .joinToString(separator = "\n")
        trackerKun.sendSimpleMessage(message, chatId)
    }
}