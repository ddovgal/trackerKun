package ua.ddovgal.trackerKunBot.service

import org.slf4j.LoggerFactory

class TryCaughtException : Exception {

    private val logger = LoggerFactory.getLogger(TryCaughtException::class.java)

    constructor(e: Exception) : this("Some exception has occurred and caught", e)
    constructor(message: String, e: Exception) : super(message, e) {
        logger.error(message, e)
    }
}