package query

import dao.ReactiveDao
import rx.Observable

class InvalidQuery(private val e: Throwable) : Query {
    override fun process(dao: ReactiveDao): Observable<String> = Observable.just("Invalid query: $e")
}