package ua.ddovgal.trackerKunBot.source


abstract class AbstractSource {

    abstract val sourceUrl: String
    abstract val sourceName: String
    val titleURL: String
    val title: String
    var lastCheckedChapter: String = ""

    constructor() : this("", "")
    constructor(titleURL: String, title: String) {
        this.titleURL = titleURL
        this.title = title
    }

    abstract fun getLastChapter(): String
    abstract fun getLastChapterURL(): String
    abstract fun searchForTitle(name: String): List<AbstractSource>
}