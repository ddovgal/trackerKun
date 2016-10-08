package ua.ddovgal.trackerKunBot.service

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ua.ddovgal.trackerKunBot.TrackerKun
import ua.ddovgal.trackerKunBot.command.BotState
import ua.ddovgal.trackerKunBot.source.AbstractSource


object TrackerThread : Thread() {

    lateinit var trackerKun: TrackerKun

    private val SLEEP_TIME_IN_MINUTES = 15L
    var runFlag = true
    private val subscriptions = mutableMapOf<AbstractSource, MutableList<Long>>()
        get() = synchronized(field) { return field }
    private val chatStates = mutableMapOf<Long, BotState>()
        get() = synchronized(field) { return field }
    val states = chatStates.orEmpty()
    private val chatSelectVariants = mutableMapOf<Long, List<Any>>()
        get() = synchronized(field) { return field }
    val selectVariants = chatSelectVariants.orEmpty()

    fun putSubscription(titleSource: AbstractSource, chatId: Long) {
        val list = subscriptions[titleSource]
        if (list == null) {
            titleSource.lastCheckedChapter = titleSource.getLastChapter()
            subscriptions.put(titleSource, mutableListOf(chatId))
        } else list.add(chatId)
    }

    fun removeSubscription(titleSource: AbstractSource, chatId: Long) {
        val list = subscriptions[titleSource]
        if (list != null && list.contains(chatId)) {
            list.remove(chatId)
            if (list.isEmpty()) subscriptions.remove(titleSource)
        }
    }

    fun subscriptionForId(chatId: Long): List<AbstractSource> =
            subscriptions.entries.filter { it.value.contains(chatId) }.map { it.key }

    fun changeState(chatId: Long, newBotState: BotState?) {
        changeMap(chatId, newBotState, chatStates)
        /*synchronized(chatStates) {
            if (chatStates[chatId] != null) chatStates[chatId] = newBotState
            else chatStates.put(chatId, newBotState)
        }*/
    }

    fun changeVariants(chatId: Long, newVariants: List<Any>?) {
        changeMap(chatId, newVariants, chatSelectVariants)
        /*synchronized(chatSelectVariants) {
            if (newVariants != null) {
                if (chatStates[chatId] != null) {
                    chatSelectVariants[chatId] = newVariants
                } else chatSelectVariants.put(chatId, newVariants)

            } else {
                if (chatStates[chatId] != null) chatSelectVariants.remove(chatId)
                else {
                }
            }
        }*/
    }

    fun <T : Any?> changeMap(key: Long, value: T?, map: MutableMap<Long, T>) {
        synchronized(map) {
            if (value != null) map.put(key, value)
            else map.remove(key)
        }
    }

    override fun run() {
        while (runFlag) {
            try {
                subscriptions.entries.forEach {
                    val checkedChapter = it.key.getLastChapter()
                    if (checkedChapter != it.key.lastCheckedChapter) {
                        it.key.lastCheckedChapter = checkedChapter
                        notifySubscribers(it.toPair())
                    }
                }
                Thread.sleep(1000 * 60 * SLEEP_TIME_IN_MINUTES)
            } catch(e: InterruptedException) {
                println("[EXITING] On interrupt")
                //e.printStackTrace()
            }
        }
    }

    fun notifySubscribers(sub: Pair<AbstractSource, MutableList<Long>>) {
        val messageText = "Hey, new ${sub.first.title} chapter at ${sub.first.sourceName} ${Emoji.SMILING_CAT_FACE_WITH_OPEN_MOUTH}\n" +
                "${sub.first.lastCheckedChapter} is here !"
        val message = SendMessage()
        val urlButton = InlineKeyboardButton()
        val keyboardMarkup = InlineKeyboardMarkup()

        message.text = messageText
        urlButton.text = "Check it"
        urlButton.url = sub.first.getLastChapterURL()
        keyboardMarkup.keyboard = listOf(listOf(urlButton))
        message.replyMarkup = keyboardMarkup

        sub.second.forEach {
            message.chatId = it.toString()
            trackerKun.sendMessage(message)
        }
    }
}