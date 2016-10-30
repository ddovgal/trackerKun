package ua.ddovgal.trackerKunBot.command

interface ReservedCommand : Command {
    val commandName: String

    fun fabricMethod(inputData: CommandInputData): ReservedCommand
    fun extractCommandName(inputData: CommandInputData): String?

    override fun getIfSuitable(inputData: CommandInputData): Command? =
            if (commandName == extractCommandName(inputData)) fabricMethod(inputData) else null
}