package ua.ddovgal.trackerKunBot.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import ua.ddovgal.trackerKunBot.source.Language
import java.util.*

@DatabaseTable
data class Title(@DatabaseField val name: String,
                 @DatabaseField(id = true) val url: String,
                 @DatabaseField(foreign = true, foreignAutoRefresh = true) val source: Source) {

    @DatabaseField var lastCheckedChapterName: String = ""
    @DatabaseField var lastCheckedChapterUrl: String = ""
    @DatabaseField var lastCheckedChapterReleaseDate: Date = Date()
    @DatabaseField var subscribersCount: Long = 0
    @DatabaseField var asVariantUsingCount: Long = 0

    //no param constructor with just empty implementation of Source
    constructor() : this("", "", object : Source("", "", Language.ENGLISH) {
        override fun checkLastChapterName(titleUrl: String) = throw UnsupportedOperationException("not implemented")
        override fun checkLastChapterUrl(titleUrl: String) = throw UnsupportedOperationException("not implemented")
        override fun checkLastChapterReleaseDate(titleUrl: String) = throw UnsupportedOperationException("not implemented")
        override fun searchForTitle(name: String) = throw UnsupportedOperationException("not implemented")
    })

    fun checkLastChapterName() = source.checkLastChapterName(url)
    fun checkLastChapterUrl() = source.checkLastChapterUrl(url)
    fun checkLastChapterReleaseDate() = source.checkLastChapterReleaseDate(url)
}