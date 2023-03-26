package query

import dao.ReactiveDao
import rx.Observable

class DeleteUsersQuery : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.deleteAllUsers().map { it.toString() }
}