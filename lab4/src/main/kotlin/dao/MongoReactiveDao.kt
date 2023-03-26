package dao

import com.mongodb.client.model.Filters
import com.mongodb.rx.client.MongoDatabase
import com.mongodb.rx.client.Success
import currency.ConverterProvider
import model.Currency
import model.Item
import model.User
import org.bson.Document
import rx.Observable
import rx.schedulers.Schedulers

class MongoReactiveDao(
    private val db: MongoDatabase,
    private val converterProvider: ConverterProvider
) : ReactiveDao {
    override fun getUserById(id: Long): Observable<User> =
        db.getCollection("users")
            .find(Filters.eq("id", id))
            .toObservable()
            .map {
                User(
                    id,
                    Currency.fromString(it.getString("currency"))
                )
            }.subscribeOn(scheduler)


    override fun getItemById(id: Long): Observable<Item> =
        db.getCollection("items")
            .find(Filters.eq("id", id))
            .toObservable()
            .map {
                Item(
                    id,
                    it.getString("name"),
                    it.getDouble("price"),
                    Currency.fromString(it.getString("currency"))
                )
            }.subscribeOn(scheduler)

    override fun getItemsForUser(id: Long): Observable<Item> {
        val converter = converterProvider.get()
        return getUserById(id).flatMap { user ->
            db.getCollection("items")
                .find()
                .toObservable()
                .map {
                    val originalPrice = it.getDouble("price")
                    val originalCurrency = Currency.fromString(it.getString("currency"))
                    val convertedPrice = converter.convert(originalPrice, originalCurrency, user.preferredCurrency)
                    Item(it.getLong("id"), it.getString("name"), convertedPrice, user.preferredCurrency)
                }.subscribeOn(scheduler)
        }
    }

    override fun addUser(user: User): Observable<Boolean> =
        getUserById(user.id)
            .singleOrDefault(null)
            .flatMap { possibleUser ->
                if (possibleUser != null) {
                    Observable.just(false)
                } else {
                    val document = Document(
                        mutableMapOf(
                            Pair("id", user.id),
                            Pair("currency", user.preferredCurrency.toString())
                        ) as Map<String, Any>?
                    )
                    db.getCollection("users")
                        .insertOne(document)
                        .asObservable()
                        .isEmpty
                        .map { !it }
                }
            }.subscribeOn(scheduler)

    override fun addItem(item: Item): Observable<Boolean> =
        getItemById(item.id)
            .singleOrDefault(null)
            .flatMap { possibleItem ->
                if (possibleItem != null) {
                    Observable.just(false)
                } else {
                    val document = Document(
                        mutableMapOf(
                            Pair("id", item.id),
                            Pair("name", item.name),
                            Pair("price", item.price),
                            Pair("currency", item.currency.toString())
                        ) as Map<String, Any>?
                    )
                    db.getCollection("items")
                        .insertOne(document)
                        .asObservable()
                        .isEmpty
                        .map { !it }
                }
            }.subscribeOn(scheduler)

    override fun deleteAllUsers(): Observable<Success> {
        return db.getCollection("users").drop().subscribeOn(scheduler)
    }

    override fun deleteAllItems(): Observable<Success> {
        return db.getCollection("items").drop().subscribeOn(scheduler)
    }

    companion object {
        private val scheduler = Schedulers.io()
    }
}
