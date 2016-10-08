package ua.ddovgal.trackerKunBot.source.impl

import org.jsoup.Jsoup
import ua.ddovgal.trackerKunBot.source.AbstractSource

open class ReadMangaSource : AbstractSource {

    override val sourceUrl = "http://readmanga.me"
    override val sourceName = "Readmanga"

    constructor() : super()
    constructor(titleURL: String, title: String) : super(titleURL, title)

    override fun getLastChapter(): String {
        val document = Jsoup.connect(sourceUrl + titleURL).get()
        val node = document.getElementsByClass("read-last").first().child(0)
        val currentChapterString = node.textNodes()[0].text().removePrefix("Читать ")
        return currentChapterString
    }

    override fun getLastChapterURL(): String {
        val document = Jsoup.connect(sourceUrl + titleURL).get()
        val node = document.getElementsByClass("read-last").first().child(0)
        return sourceUrl + node.attr("href")
    }

    override fun searchForTitle(name: String): List<Triple<String, String, String>> {
        val document = Jsoup.connect("${sourceUrl}search?q=$name").get()
        val blocks = document.getElementsByClass("tile col-sm-6")
        return blocks.map {
            val titleNode = it.child(2).child(1).child(0)
            val title = titleNode.textNodes()[0].text()
            val url = titleNode.attr("href")
            Triple(sourceName, title, url)
        }
    }
}