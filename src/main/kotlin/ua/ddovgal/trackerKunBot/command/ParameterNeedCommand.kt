package ua.ddovgal.trackerKunBot.command

import org.telegram.telegrambots.api.objects.Update


/**
 * For all subclasses, it's desirable to close empty constructor by applying self private empty constructor,
 * to avoid usage of [inputData] property
 */
abstract class ParameterNeedCommand : BaseCommand {

    var inputData: CommandInputData
    protected abstract val chatId: Long
    protected abstract val stateNeed: SubscriberState

    protected constructor() : this(CommandInputData(Update()))

    constructor(inputData: CommandInputData) {
        this.inputData = inputData
    }

    protected abstract fun fabricMethod(inputData: CommandInputData): ParameterNeedCommand

    protected abstract fun extractState(inputData: CommandInputData): SubscriberState?

    override fun getIfSuitable(inputData: CommandInputData): Command? =
            if (stateNeed == extractState(inputData)) fabricMethod(inputData) else null
}