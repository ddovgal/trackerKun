package ua.ddovgal.trackerKunBot.command

import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
enum class SubscriberState(val description: String) {
    USELESS("If state is useless for some command"),
    WAITING_FOR_ANYTHING("Just started/same as just started, but after some number of command loops"),
    WAITING_FOR_ADD_STRING("Waiting for plain text at manga adding. What should to search"),
    WAITING_FOR_REMOVE_SELECTION("Waiting for /d string type at manga deleting. Which should to remove"),
    WAITING_FOR_ADD_SELECTION("Waiting for /d string type at manga adding. Choosing from found"),
    WAITING_FOR_COMMAND_STRING("Waiting for /<RESERVED_COMMAND> string type. What action What actions should make")
}