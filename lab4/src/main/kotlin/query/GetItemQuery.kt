package query

import dao.ReactiveDao
import rx.Observable

class GetItemQuery(private val id: Long) : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.getItemById(id).map { it.toString() }
}
