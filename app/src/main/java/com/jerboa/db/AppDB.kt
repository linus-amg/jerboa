package com.jerboa.db

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity
data class Account(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "default_") val default_: Boolean,
    @ColumnInfo(name = "instance") val instance: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "avatar") val avatar: String?,
    @ColumnInfo(name = "jwt") val jwt: String,
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    fun getAll(): Flow<List<Account>>

//    @Query(
//        "SELECT * FROM account WHERE selected = 1 " +
//            "LIMIT 1"
//    )
//    fun getSelected(): Account?

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = Account::class)
    suspend fun insert(account: Account)

    @Query("UPDATE account set default_ = 0 where default_ = 1")
    suspend fun removeDefault()

    @Query("UPDATE account set default_ = 1 where id = :accountId")
    suspend fun setDefault(accountId: Int)

    @Delete(entity = Account::class)
    suspend fun delete(account: Account)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class AccountRepository(private val accountDao: AccountDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allAccounts = accountDao.getAll()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(account: Account) {
        accountDao.insert(account)
    }

    @WorkerThread
    suspend fun removeDefault() {
        accountDao.removeDefault()
    }

    @WorkerThread
    suspend fun setDefault(accountId: Int) {
        accountDao.setDefault(accountId)
    }

    @WorkerThread
    suspend fun delete(account: Account) {
        accountDao.delete(account)
    }
}

@Database(entities = [Account::class], version = 1)
abstract class AppDB : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope,
        ): AppDB {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "jerboa"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

    val allAccounts: LiveData<List<Account>> = repository.allAccounts.asLiveData()

    fun insert(account: Account) = viewModelScope.launch {
        repository.insert(account)
    }

    fun removeDefault() = viewModelScope.launch {
        repository.removeDefault()
    }

    fun setDefault(accountId: Int) = viewModelScope.launch {
        repository.setDefault(accountId)
    }

    fun delete(account: Account) = viewModelScope.launch {
        repository.delete(account)
    }
}

class AccountViewModelFactory(private val repository: AccountRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
