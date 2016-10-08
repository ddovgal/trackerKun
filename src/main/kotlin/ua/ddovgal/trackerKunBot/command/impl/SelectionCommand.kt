package ua.ddovgal.trackerKunBot.command.impl

import org.telegram.telegrambots.exceptions.TelegramApiRequestException
import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.command.Command
import ua.ddovgal.trackerKunBot.service.Emoji
import ua.ddovgal.trackerKunBot.source.AbstractSource


class SelectionCommand(val selectedNumber: Int) : Command {
    override fun exec(chatId: Long, trackerKun: TrackerKun) {
        when (trackerKun.trackerThread.states[chatId]) {
            BotState.WAITING_FOR_ADD_SELECTION -> {
                val sources = trackerKun.trackerThread.selectVariants[chatId] as List<*>
                val selected = sources[selectedNumber] as AbstractSource
                trackerKun.trackerThread.putSubscription(selected, chatId)
                val message = "Great !\n" +
                        "${selected.title} was added to your observable list ${Emoji.THUMBS_UP_SIGN}"
                trackerKun.sendSimpleMessage(message, chatId)
            }
            BotState.WAITING_FOR_DELETE_SELECTION -> {
                val sources = trackerKun.trackerThread.selectVariants[chatId] as List<*>
                val selected = sources[selectedNumber] as AbstractSource
                trackerKun.trackerThread.removeSubscription(selected, chatId)
                val message = "Yep, ${selected.title} sadly has gone away ${Emoji.CRYING_FACE}"
                trackerKun.sendSimpleMessage(message, chatId)
            }
            else -> throw TelegramApiRequestException("Chat's state is not some of expecting states")
        }
    }
}