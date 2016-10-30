package ua.ddovgal.trackerKunBot.command


interface SelectingCommand : Command {

    val inputData: CommandInputData
    val selected: Int
        get() = extractSelected(inputData)

    fun fabricMethod(inputData: CommandInputData): SelectingCommand

    companion object {
        val NO_SELECTION = -1
    }

    fun extractSelected(inputData: CommandInputData): Int {
        var result: Int = NO_SELECTION
        val text = inputData.update.message?.text
        text?.let {
            if (text.length > 1 && text[0] == '/') {
                val cut = text.substring(1)
                try {
                    result = cut.toInt()
                } catch(e: NumberFormatException) {
                }
            }
        }
        return result
    }

    override fun getIfSuitable(inputData: CommandInputData): Command? =
            if (extractSelected(inputData) != NO_SELECTION) fabricMethod(inputData) else null
}