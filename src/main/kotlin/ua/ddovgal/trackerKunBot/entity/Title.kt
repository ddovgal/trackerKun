package ua.ddovgal.trackerKunBot.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import ua.ddovgal.trackerKunBot.source.SourceManager
import java.util.*

@DatabaseTable
data class Title(@DatabaseField val name: String,
                 @DatabaseField(id = true) val url: String) {

    @DatabaseField var lastCheckedChapterName: String = ""
    @DatabaseField var lastCheckedChapterUrl: String = ""
    @DatabaseField var lastCheckedChapterReleaseDate: Date = Date()
    @DatabaseField var subscribersCount: Long = 0
    @DatabaseField var asVariantUsingCount: Long = 1

    val source: Source by lazy { SourceManager.getSourceByTitleUrl(url) }

    //no param constructor for ORM
    constructor() : this("", "")

    fun checkLastChapterName() = source.checkLastChapterName(url)
    fun checkLastChapterUrl() = source.checkLastChapterUrl(url)
    fun checkLastChapterReleaseDate() = source.checkLastChapterReleaseDate(url)
}