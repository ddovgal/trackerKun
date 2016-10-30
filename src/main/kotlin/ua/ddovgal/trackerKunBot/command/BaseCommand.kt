package ua.ddovgal.trackerKunBot.command

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ua.ddovgal.trackerKunBot.TrackerKunBot
import ua.ddovgal.trackerKunBot.service.worker.DatabaseConnector

abstract class BaseCommand : Command {
    protected val trackerKun = TrackerKunBot
    protected val dbConnector = DatabaseConnector
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)
}