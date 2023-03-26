package query

import dao.ReactiveDao
import model.User
import rx.Observable

class AddUserQuery(private val user: User) : Query {
    override fun process(dao: ReactiveDao): Observable<String> = dao.addUser(user).map { it.toString() }
}