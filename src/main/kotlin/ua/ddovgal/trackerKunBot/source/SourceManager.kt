package ua.ddovgal.trackerKunBot.source

import ua.ddovgal.trackerKunBot.entity.Source
import ua.ddovgal.trackerKunBot.entity.Title

object SourceManager {

    private val readManga = ReadMangaSource()
    private val mintManga = MintMangaSource()
    private val sources = listOf(readManga, mintManga)

    fun searchForTitle(name: String): List<Title> =
            sources.map { it.searchForTitle(name) }.reduce { list1, list2 -> list1.plus(list2) }

    fun getSourceByTitleUrl(url: String): Source = sources.first { url.contains(it.url) }
}