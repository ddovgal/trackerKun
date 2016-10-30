package ua.ddovgal.trackerKunBot.service.worker

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import ua.ddovgal.trackerKunBot.DATABASE_DRIVER
import ua.ddovgal.trackerKunBot.DATABASE_URL
import ua.ddovgal.trackerKunBot.command.SubscriberState
import ua.ddovgal.trackerKunBot.entity.*
import ua.ddovgal.trackerKunBot.service.TryCaughtException
import java.net.URI
import java.sql.SQLException

object DatabaseConnector {

    private val connection: ConnectionSource
    private val subscriberDao: Dao<Subscriber, Long>
    private val sourceDao: Dao<Source, String>
    private val titleDao: Dao<Title, String>
    private val subscriptionDao: Dao<Subscription, *>
    private val variantDao: Dao<Variant, *>

    init {
        val dbUri = URI(DATABASE_URL)

        val username = dbUri.userInfo.split(":")[0]
        val password = dbUri.userInfo.split(":")[1]
        val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"

        Class.forName(DATABASE_DRIVER)

        connection = JdbcConnectionSource(dbUrl, username, password)

        subscriberDao = DaoManager.createDao(connection, Subscriber::class.java)
        sourceDao = DaoManager.createDao(connection, Source::class.java)
        titleDao = DaoManager.createDao(connection, Title::class.java)
        subscriptionDao = DaoManager.createDao(connection, Subscription::class.java)
        variantDao = DaoManager.createDao(connection, Variant::class.java)

        sourceDao.setObjectCache(true)
        titleDao.setObjectCache(true)

        TableUtils.createTableIfNotExists(connection, Source::class.java)
        TableUtils.createTableIfNotExists(connection, Subscriber::class.java)
        TableUtils.createTableIfNotExists(connection, Title::class.java)
        TableUtils.createTableIfNotExists(connection, Subscription::class.java)
        TableUtils.createTableIfNotExists(connection, Variant::class.java)
    }

    fun getSubscriber(chatId: Long): Subscriber? = subscriberDao.queryForId(chatId)

    fun saveSubscriber(subscriber: Subscriber) = subscriberDao.create(subscriber)

    fun updateSubscribersState(chatId: Long, newState: SubscriberState) {
        val subscriber = subscriberDao.queryForId(chatId)
        subscriber.state = newState
        subscriberDao.update(subscriber)
    }

    fun getSubscribersOfTitle(title: Title): List<Subscriber> {
        val subscriptionQueryBuilder = subscriptionDao.queryBuilder()
        val subscriberQueryBuilder = subscriberDao.queryBuilder()
        subscriptionQueryBuilder.where().eq(Subscription.TITLE_COLUMN_NAME, title)
        val result = subscriberQueryBuilder
                .join(subscriptionQueryBuilder)
                .where()
                .eq(Subscriber.SUBSCRIPTIONS_ACTIVE_STATUS_COLUMN_NAME, true).query()
        return result
    }

    fun updateTitle(title: Title) = titleDao.update(title)

    fun getAllPermanentTitles(): List<Title> {
        val allTitlesInSubscriptionsQuery = subscriptionDao.queryBuilder()
        val result = titleDao.queryBuilder().join(allTitlesInSubscriptionsQuery).query()
        return result
    }

    /**
     * @throws [TryCaughtException] if user is already have such [title] in subscriptions
     */
    fun subscribe(title: Title, chatId: Long) {
        val subscriber = subscriberDao.queryForId(chatId)

        title.subscribersCount++
        title.asVariantUsingCount--
        titleDao.update(title)

        subscriber.subscriptionCount++
        subscriberDao.update(subscriber)

        try {
            subscriptionDao.create(Subscription(subscriber, title))
        } catch(e: SQLException) {
            throw TryCaughtException("It seems, that [${subscriber.chatId}] user is already have [${title.url}] subscription", e)
        }
    }

    fun unsubscribe(title: Title, chatId: Long) {
        val subscriber = subscriberDao.queryForId(chatId)
        val deleteBuilder = subscriptionDao.deleteBuilder()
        deleteBuilder.where()
                .eq(Subscription.TITLE_COLUMN_NAME, title)
                .and()
                .eq(Subscription.SUBSCRIBER_COLUMN_NAME, subscriber).query()
        deleteBuilder.delete()

        title.subscribersCount--
        // delete, only if no one is used. Not as subscription, nor variant
        if (title.subscribersCount == 0L && title.asVariantUsingCount == 0L) titleDao.delete(title)
        else titleDao.update(title)

        subscriber.subscriptionCount--
        subscriberDao.update(subscriber)
        //todo: do i need to store inactive users ? do i need to clean user if it have no subscriptions ?
        /*if (subscriber.subscriptionCount == 0) subscriberDao.delete(subscriber)
        else subscriberDao.update(subscriber)*/
    }

    private inline fun <reified T> getSomeDateOfSubscriber(chatId: Long, position: Long?): List<Title> {
        val subscriber = subscriberDao.queryForId(chatId)

        //empty yet created Dao for some table class
        val dao = DaoManager.lookupDao(connection, T::class.javaClass)

        val somethingQueryBuilder = dao.queryBuilder()
        val titleQueryBuilder = titleDao.queryBuilder()

        //if position != null then we need a specific position of 'Something', not all them
        position?.let { somethingQueryBuilder.limit(1).offset(it - 1) }

        somethingQueryBuilder.where().eq(Subscription.SUBSCRIBER_COLUMN_NAME, subscriber)

        // if position != null (need a specific position of 'Something', not all them)
        // result will contain 1 element maximum, cause limit = 1, so in place,
        // we use this method to found 'Something' at {position}, we need just call '.first()'
        val result = titleQueryBuilder.join(somethingQueryBuilder).query()
        return result
    }

    fun getSpecificSubscriptionOfSubscriber(chatId: Long, position: Long): Title =
            getSomeDateOfSubscriber<Subscription>(chatId, position).first()

    fun getSubscriptionsOfSubscriber(chatId: Long): List<Title> =
            getSomeDateOfSubscriber<Subscription>(chatId, null)

    fun getSpecificVariantOfSubscriber(chatId: Long, position: Long): Title =
            getSomeDateOfSubscriber<Variant>(chatId, position).first()

//    fun getVariantsOfSubscriber(chatId: Long): List<Title> =
//            getSomeDateOfSubscriber<Variant>(chatId, null)

    fun putVariantsForSubscriber(chatId: Long, variants: List<Title>) {
        val subscriber = subscriberDao.queryForId(chatId)
        val variantEntities = variants.map {
            var found = titleDao.queryForSameId(it)
            if (found == null) found = titleDao.createIfNotExists(it)
            found.asVariantUsingCount++
            titleDao.update(found)
            Variant(subscriber, found)
        }
        variantDao.create(variantEntities)
    }

    fun removeVariantsOfSubscriber(chatId: Long) {
        val subscriber = subscriberDao.queryForId(chatId)
        val variantDeleteBuilder = variantDao.queryBuilder()
        val variantsToDelete = variantDeleteBuilder.where().eq(Subscription.SUBSCRIBER_COLUMN_NAME, subscriber).query()
        val titlesToDelete = variantsToDelete.filter {
            it.title.asVariantUsingCount--
            if (it.title.asVariantUsingCount == 0L && it.title.subscribersCount == 0L) true
            else {
                titleDao.update(it.title)
                false
            }
        }.map { it.title }
        titleDao.delete(titlesToDelete)
        variantDao.delete(variantsToDelete)
    }

    fun changeSubscriptionStateOfSubscriber(chatId: Long): Boolean {
        val subscriber = subscriberDao.queryForId(chatId)

        subscriber.subscriptionsActiveStatus = !subscriber.subscriptionsActiveStatus
        subscriberDao.update(subscriber)
        return subscriber.subscriptionsActiveStatus
    }
}