package query

import dao.ReactiveDao
import rx.Observable

class GetUserQuery(private val id: Long) : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.getUserById(id).map { it.toString() }
}