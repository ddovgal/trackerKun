package ua.ddovgal.trackerKunBot.command

enum class BotState {
    NOT_STARTED,
    JUST_STARTED,
    WAITING_FOR_ANY,
    WAITING_FOR_ADD_STRING,
    WAITING_FOR_DELETE_SELECTION,
    WAITING_FOR_ADD_SELECTION,
    WAITING_FOR_COMMAND_STRING
}