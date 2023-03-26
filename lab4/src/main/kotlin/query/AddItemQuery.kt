package query

import dao.ReactiveDao
import model.Item
import rx.Observable

class AddItemQuery(private val item: Item) : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.addItem(item).map { it.toString() }
}