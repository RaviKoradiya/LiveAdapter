package com.sample.liveadapter.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.ravikoradiya.liveadapter.BR
import com.ravikoradiya.liveadapter.LiveAdapter
import com.sample.liveadapter.R
import com.sample.liveadapter.databinding.ActivityRoomWithLiveDataBinding
import com.sample.liveadapter.databinding.ItemUserBinding
import com.sample.liveadapter.db.AppDatabase
import com.sample.liveadapter.db.User
import com.sample.liveadapter.db.UserDao
import java.util.concurrent.Executors

class RoomWithLiveDataActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityRoomWithLiveDataBinding
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_room_with_live_data)
        mBinding.rvRoomData.layoutManager = LinearLayoutManager(this)

        initDB()
        getDataAndDisplay()

        mBinding.btnAdd.setOnClickListener {
            Executors.newSingleThreadExecutor().execute {
                userDao.insertAll(User(firstName = "Test", lastName = "Name"))
            }
        }
    }

    private fun initDB() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "live-adapter-user-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        userDao = db.userDao()
    }

    private fun getDataAndDisplay() {
        val users: LiveData<List<User>> = userDao.getAll()

        LiveAdapter(users, this, BR.item)
            .map<User, ItemUserBinding>(R.layout.item_user) {
                onBind { holder ->
                    holder.binding.btnDelete.setOnClickListener {
                        holder.binding.item?.let {
                            Executors.newSingleThreadExecutor().execute {
                                userDao.delete(it)
                            }
                        }
                    }
                }
                areContentsTheSame { old, new ->
                    return@areContentsTheSame old.uid == new.uid
                }
            }.onNoData { noData ->
                mBinding.textNoDataFound.visibility =
                    if (noData)
                        View.VISIBLE
                    else
                        View.GONE
            }
            .into(mBinding.rvRoomData)
    }
}


