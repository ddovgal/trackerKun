package ua.ddovgal.trackerKunBot.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import ua.ddovgal.trackerKunBot.command.SubscriberState

@DatabaseTable
data class Subscriber(@DatabaseField(id = true) val chatId: Long = 0,
                      @DatabaseField val fName: String = "",
                      @DatabaseField val sName: String = "",
                      @DatabaseField(
                              index = true,
                              columnName = Subscriber.SUBSCRIPTIONS_ACTIVE_STATUS_COLUMN_NAME)
                      var subscriptionsActiveStatus: Boolean = true,
                      @DatabaseField var state: SubscriberState = SubscriberState.WAITING_FOR_ANYTHING,
                      @DatabaseField var subscriptionCount: Int = 0) {

    companion object {
        const val SUBSCRIPTIONS_ACTIVE_STATUS_COLUMN_NAME = "subscriptionsActiveStatus"
    }
}