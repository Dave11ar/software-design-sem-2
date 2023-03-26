package query

import dao.ReactiveDao
import rx.Observable

class GetItemsForUserQuery(private val id: Long) : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.getItemsForUser(id).map { it.toString() }
}