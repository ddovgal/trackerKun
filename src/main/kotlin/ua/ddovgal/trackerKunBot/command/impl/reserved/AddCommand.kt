package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.command.ReservedCommand


class AddCommand(params: List<String>) : ReservedCommand(params) {
    override fun exec(chatId: Long, trackerKun: TrackerKun) {
        trackerKun.trackerThread.changeState(chatId, BotState.WAITING_FOR_ADD_STRING)
        trackerKun.sendSimpleMessage("What manga you are reading ? I'll try to find some.", chatId)
    }
}