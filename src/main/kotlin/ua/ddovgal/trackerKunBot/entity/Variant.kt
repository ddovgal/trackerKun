package ua.ddovgal.trackerKunBot.entity

import com.j256.ormlite.field.DatabaseField

data class Variant(
        @DatabaseField(
                foreign = true,
                foreignAutoRefresh = true,
                uniqueCombo = true,
                index = true,
                columnName = Variant.SUBSCRIBER_COLUMN_NAME) val subscriber: Subscriber = Subscriber(),
        @DatabaseField(
                foreign = true,
                foreignAutoRefresh = true,
                uniqueCombo = true,
                columnName = Variant.TITLE_COLUMN_NAME) val title: Title = Title()
) {

    companion object {
        const val SUBSCRIBER_COLUMN_NAME = "subscriber"
        const val TITLE_COLUMN_NAME = "title"
    }
}