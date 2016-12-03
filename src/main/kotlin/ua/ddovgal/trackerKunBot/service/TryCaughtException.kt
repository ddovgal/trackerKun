package ua.ddovgal.trackerKunBot.service

class TryCaughtException : Exception {
    constructor(e: Exception) : this("Some exception has occurred and caught", e)
    constructor(message: String, e: Exception) : super(message, e)
}