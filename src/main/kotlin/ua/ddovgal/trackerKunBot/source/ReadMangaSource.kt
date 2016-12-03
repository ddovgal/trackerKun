package ua.ddovgal.trackerKunBot.source

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.jsoup.Jsoup
import ua.ddovgal.trackerKunBot.entity.Source
import ua.ddovgal.trackerKunBot.entity.Title
import ua.ddovgal.trackerKunBot.service.TryCaughtException
import java.net.URL
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

open class ReadMangaSource : Source {

    constructor() : super("http://readmanga.me", "Read Manga", Language.RUSSIAN)
    //just for MintManga overriding
    protected constructor(url: String, name: String) : super(url, name, Language.RUSSIAN)

    override fun searchForTitle(name: String): List<Title> {
        val document = Jsoup.connect("$url/search?q=$name").get()
        val blocks = document.getElementsByAttributeValue("class", "tile col-sm-6")

        return blocks.map {
            val titleNode = it.child(2).child(1).child(0)
            val titleName = titleNode.textNodes()[0].text()
            val titleUrl = titleNode.attr("href")
            val rssUrl = "$url/rss/manga?name=${titleUrl.substring(1)}"
            Title(titleName, rssUrl)
        }
    }

    /**
     * @return Last chapter release url, or 'null' if it not exist
     * @throws [TryCaughtException] if there is no chapters in feed
     */
    override fun checkLastChapterUrl(titleUrl: String): String? {
        val feed = getFeed(titleUrl)
        try {
            val lastChapter = feed.entries.first()
            return lastChapter.link
        } catch(e: NoSuchElementException) {
            throw TryCaughtException("There is no chapters in [$titleUrl] feed", e)
        }
    }

    /**
     * @return Last chapter name, or 'null' if it not exist
     * @throws [NoSuchElementException] if there is no chapters in feed
     */
    override fun checkLastChapterName(titleUrl: String): String? {
        val feed = getFeed(titleUrl)
        val title = feed.title
        val lastChapter = feed.entries.first()
        return lastChapter.title?.replace("${title.removePrefix("Манга ")}: ", "")
    }

    /**
     * @return Last chapter release date, or 'null' if it not exist
     * @throws [NoSuchElementException] if there is no chapters in feed
     */
    override fun checkLastChapterReleaseDate(titleUrl: String): Date? {
        val feed = getFeed(titleUrl)
        val lastChapter = feed.entries.first()
        return lastChapter.publishedDate
    }

    //region Closure manipulation to lazy lambda feed
    private fun getFeed(url: String) = lambda.invoke(url)

    private val lambda by lazy { getFeedLambda() }
    private fun getFeedLambda(): (String) -> SyndFeed {

        var feed: SyndFeed? = null
        var lastCheckTime: LocalDateTime = LocalDateTime.now()

        return { url: String ->
            if (feed == null
                    || feed?.link != url
                    || lastCheckTime.isBefore(LocalDateTime.now().minus(15, ChronoUnit.MINUTES))
            ) {
                val received = SyndFeedInput().build(XmlReader(URL(url)))
                feed = received
                lastCheckTime = LocalDateTime.now()
            }
            feed as SyndFeed
        }
    }
    //endregion
}