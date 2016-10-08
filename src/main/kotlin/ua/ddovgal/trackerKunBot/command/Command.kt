package ua.ddovgal.trackerKunBot.command

import ua.ddovgal.trackerKunBot.TrackerKun

interface Command {
    fun exec(chatId: Long, trackerKun: TrackerKun)
}