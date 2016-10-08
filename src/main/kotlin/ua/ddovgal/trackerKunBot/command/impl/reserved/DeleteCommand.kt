package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.command.ReservedCommand

class DeleteCommand(params: List<String>) : ReservedCommand(params) {
    override fun exec(chatId: Long, trackerKun: TrackerKun) {
        trackerKun.trackerThread.changeState(chatId, BotState.WAITING_FOR_DELETE_SELECTION)

        val subscriptions = trackerKun.trackerThread.subscriptionForId(chatId)
        trackerKun.trackerThread.changeVariants(chatId, subscriptions)

        val message = subscriptions
                .mapIndexed { i, source -> "/$i [${source.sourceName}]${source.title}" }
                .joinToString(separator = "\n")
        trackerKun.sendSimpleMessage(message, chatId)
    }
}