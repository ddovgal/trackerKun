package ua.ddovgal.trackerKunBot.command

interface Command {
    fun exec()

    /**
     * Method worries about existence of the message\text by self. Absolutely all check is HIS task
     */
    fun getIfSuitable(inputData: CommandInputData): Command?
}