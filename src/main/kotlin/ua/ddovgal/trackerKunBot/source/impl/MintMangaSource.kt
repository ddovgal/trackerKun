package ua.ddovgal.trackerKunBot.source.impl

class MintMangaSource : ReadMangaSource {
    override val sourceUrl = "http://mintmanga.com"
    override val sourceName = "Mintmanga"

    constructor() : super()
    constructor(titleURL: String, title: String) : super(titleURL, title)
}