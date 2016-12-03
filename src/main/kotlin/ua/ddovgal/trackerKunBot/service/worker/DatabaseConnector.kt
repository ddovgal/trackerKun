package ua.ddovgal.trackerKunBot.service.worker

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import ua.ddovgal.trackerKunBot.DATABASE_DRIVER
import ua.ddovgal.trackerKunBot.DATABASE_URL
import ua.ddovgal.trackerKunBot.command.SubscriberState
import ua.ddovgal.trackerKunBot.entity.Subscriber
import ua.ddovgal.trackerKunBot.entity.Subscription
import ua.ddovgal.trackerKunBot.entity.Title
import ua.ddovgal.trackerKunBot.entity.Variant
import ua.ddovgal.trackerKunBot.service.TryCaughtException
import java.net.URI
import java.sql.SQLException

object DatabaseConnector {

    private val connection: ConnectionSource
    private val subscriberDao: Dao<Subscriber, Long>
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
        titleDao = DaoManager.createDao(connection, Title::class.java)
        subscriptionDao = DaoManager.createDao(connection, Subscription::class.java)
        variantDao = DaoManager.createDao(connection, Variant::class.java)

        titleDao.setObjectCache(true)

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

        try {
            subscriptionDao.create(Subscription(subscriber, title, System.currentTimeMillis()))

            title.subscribersCount++
            subscriber.subscriptionCount++
            subscriberDao.update(subscriber)
            titleDao.update(title)
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
                .eq(Subscription.SUBSCRIBER_COLUMN_NAME, subscriber)
        deleteBuilder.delete()

        title.subscribersCount--
        // remove, only if nobody is used. Not as subscription, nor as variant
        if (title.subscribersCount == 0L && title.asVariantUsingCount == 0L) titleDao.delete(title)
        else titleDao.update(title)

        subscriber.subscriptionCount--
        subscriberDao.update(subscriber)
    }

    fun getSpecificSubscriptionOfSubscriber(chatId: Long, position: Long): Title {
        val subscriptionQueryBuilder = subscriptionDao.queryBuilder()
        val titleQueryBuilder = titleDao.queryBuilder()

        subscriptionQueryBuilder.orderBy(Subscription.TIME_COLUMN_NAME, true)
                .where().eq(Subscription.SUBSCRIBER_COLUMN_NAME, chatId)

        val result = titleQueryBuilder.join(subscriptionQueryBuilder).limit(1).offset(position - 1).query()
        return result.first()
    }

    fun getSubscriptionsOfSubscriber(chatId: Long): List<Title> {
        val subscriptionQueryBuilder = subscriptionDao.queryBuilder()
        val titleQueryBuilder = titleDao.queryBuilder()

        subscriptionQueryBuilder.where().eq(Subscription.SUBSCRIBER_COLUMN_NAME, chatId)
        subscriptionQueryBuilder.orderBy(Subscription.TIME_COLUMN_NAME, true)

        val result = titleQueryBuilder.join(subscriptionQueryBuilder).query()
        return result
    }

    fun getSpecificVariantOfSubscriber(chatId: Long, position: Long): Title {
        val variantQueryBuilder = variantDao.queryBuilder()
        val titleQueryBuilder = titleDao.queryBuilder()

        variantQueryBuilder.where()
                .eq(Variant.SUBSCRIBER_COLUMN_NAME, chatId)
                .and()
                .eq(Variant.POSITION_COLUMN_NAME, position)

        val result = titleQueryBuilder.join(variantQueryBuilder).query().first()
        return result
    }

    fun putVariantsForSubscriber(chatId: Long, variants: List<Title>) {
        //empty Subscriber. It need only it's chatId
        val subscriber = Subscriber(chatId = chatId)
        val variantEntities = variants.mapIndexed { i, it ->
            var title = titleDao.queryForId(it.url)

            if (title == null) {
                title = it
                titleDao.create(title)
            } else {
                title.asVariantUsingCount++
                titleDao.update(title)
            }

            Variant(subscriber, title, i + 1)
        }
        variantDao.create(variantEntities)
    }

    fun removeVariantsOfSubscriber(chatId: Long) {
        val variantQueryBuilder = variantDao.queryBuilder()
        val titleQueryBuilder = titleDao.queryBuilder()

        variantQueryBuilder.where().eq(Subscription.SUBSCRIBER_COLUMN_NAME, chatId)
        val variantTitlesToDelete = titleQueryBuilder.join(variantQueryBuilder).query()

        val titlesToDelete = variantTitlesToDelete.filter {
            it.asVariantUsingCount--
            if (it.asVariantUsingCount == 0L && it.subscribersCount == 0L) true
            else {
                titleDao.update(it)
                false
            }
        }
        titleDao.delete(titlesToDelete)

        val variantDeleteBuilder = variantDao.deleteBuilder()
        variantDeleteBuilder.where().eq(Subscription.SUBSCRIBER_COLUMN_NAME, chatId)
        variantDeleteBuilder.delete()
    }

    fun changeSubscriptionStateOfSubscriber(chatId: Long): Boolean {
        val subscriber = subscriberDao.queryForId(chatId)

        subscriber.subscriptionsActiveStatus = !subscriber.subscriptionsActiveStatus
        subscriberDao.update(subscriber)
        return subscriber.subscriptionsActiveStatus
    }
}