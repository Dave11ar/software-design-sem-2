package query

import dao.ReactiveDao
import rx.Observable

class DeleteItemsQuery : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.deleteAllItems().map { it.toString() }
}