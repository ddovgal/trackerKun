package ua.ddovgal.trackerKunBot.command.impl

import org.telegram.telegrambots.exceptions.TelegramApiRequestException
import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.command.Command
import ua.ddovgal.trackerKunBot.source.impl.MintMangaSource
import ua.ddovgal.trackerKunBot.source.impl.ReadMangaSource


class PlainTextCommand(val text: String) : Command {
    override fun exec(chatId: Long, trackerKun: TrackerKun) {
        when (trackerKun.trackerThread.states[chatId]) {
            BotState.WAITING_FOR_ADD_STRING -> {
                val fond = ReadMangaSource().searchForTitle(text)
                        .plus(MintMangaSource().searchForTitle(text))
                trackerKun.trackerThread.changeVariants(chatId, fond)

                val message = fond
                        .mapIndexed { i, triple -> "/$i [${triple.first}]${triple.second}" }
                        .joinToString(separator = "\n")
                trackerKun.sendSimpleMessage(message, chatId)
            }
            else -> throw TelegramApiRequestException("Chat's state is not some of expecting states")
        }
    }
}