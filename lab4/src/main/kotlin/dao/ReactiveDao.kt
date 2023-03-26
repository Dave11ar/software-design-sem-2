package dao

import com.mongodb.rx.client.Success
import rx.Observable
import model.Item
import model.User

interface ReactiveDao {
    fun getUserById(id: Long): Observable<User>
    fun getItemById(id: Long): Observable<Item>
    fun getItemsForUser(id: Long): Observable<Item>

    fun addUser(user: User): Observable<Boolean>
    fun addItem(item: Item): Observable<Boolean>
    fun deleteAllUsers(): Observable<Success>
    fun deleteAllItems(): Observable<Success>
}