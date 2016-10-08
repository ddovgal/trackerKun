package ua.ddovgal.trackerKunBot.command.impl.reserved

import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.command.ReservedCommand
import ua.ddovgal.trackerKunBot.service.Emoji


class StartCommand(params: List<String>) : ReservedCommand(params) {
    override fun exec(chatId: Long, trackerKun: TrackerKun) {
        trackerKun.trackerThread.changeState(chatId, BotState.JUST_STARTED)
        trackerKun.sendSimpleMessage("Hello, young otaku ${Emoji.RAISED_HAND}\n" +
                "I will observe your manga for you. So, what I must to track ?\n" +
                "Please, tell me by /add command ${Emoji.SMILING_FACE_WITH_OPEN_MOUTH_AND_SMILING_EYES}", chatId)
    }
}