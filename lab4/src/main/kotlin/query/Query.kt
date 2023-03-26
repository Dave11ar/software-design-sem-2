package query

import dao.ReactiveDao
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import model.Currency
import model.Item
import model.User
import rx.Observable
import java.lang.IllegalArgumentException

fun <T> HttpServerRequest<T>.getQueryParam(name: String): String {
    val paramsList = this.queryParameters[name]?.toList() ?: error("Parameter $name is required")
    return paramsList[0]
}

interface Query {
    fun process(dao: ReactiveDao): Observable<String>

    companion object {
        fun <T> fromRequest(request: HttpServerRequest<T>): Query = try {
            when (val queryName = request.decodedPath.substring(1)) {
                "add_user" -> {
                    val id = request.getQueryParam("id").toLong()
                    val currency = Currency.fromString(request.getQueryParam("currency"))
                    AddUserQuery(User(id, currency))
                }
                "add_item" -> {
                    val id = request.getQueryParam("id").toLong()
                    val currency = Currency.fromString(request.getQueryParam("currency"))
                    val price = request.getQueryParam("price").toDouble()
                    val name = request.getQueryParam("name")
                    AddItemQuery(Item(id, name, price, currency))
                }
                "delete_users" -> DeleteUsersQuery()
                "delete_items" -> DeleteItemsQuery()
                "get_user" -> {
                    val id = request.getQueryParam("id").toLong()
                    GetUserQuery(id)
                }
                "get_item" -> {
                    val id = request.getQueryParam("id").toLong()
                    GetItemQuery(id)
                }
                "get_items_for_user" -> {
                    val id = request.getQueryParam("id").toLong()
                    GetItemsForUserQuery(id);
                }
                else -> InvalidQuery(IllegalArgumentException("No such query: $queryName"))
            }
        } catch (e: Throwable) {
            InvalidQuery(e)
        }
    }
}