package ua.ddovgal.trackerKunBot.entity

import com.j256.ormlite.field.DatabaseField


data class Subscription(
        @DatabaseField(
                foreign = true,
                foreignAutoRefresh = true,
                uniqueCombo = true,
                index = true,
                columnName = Subscription.SUBSCRIBER_COLUMN_NAME) val subscriber: Subscriber = Subscriber(),
        @DatabaseField(
                foreign = true,
                foreignAutoRefresh = true,
                uniqueCombo = true,
                columnName = Subscription.TITLE_COLUMN_NAME) val title: Title = Title(),
        @DatabaseField(columnName = Subscription.TIME_COLUMN_NAME) val time: Long = 0
) {

    companion object {
        const val SUBSCRIBER_COLUMN_NAME = "subscriber"
        const val TITLE_COLUMN_NAME = "title"
        const val TIME_COLUMN_NAME = "time"
    }
}