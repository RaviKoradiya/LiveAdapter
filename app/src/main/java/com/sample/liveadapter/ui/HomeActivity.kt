package com.sample.liveadapter.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravikoradiya.liveadapter.LiveAdapter
import com.sample.liveadapter.BR
import com.sample.liveadapter.R
import com.sample.liveadapter.databinding.ActivityHomeBinding
import com.sample.liveadapter.databinding.ItemMenuBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityHomeBinding
    private val menuList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        mBinding.rvMenu.layoutManager = LinearLayoutManager(this)

        createMenus()
        setAdapter()

    }

    private fun createMenus() {
        menuList.add(getString(R.string.menu_livedata_vs_observablelist))
        menuList.add(getString(R.string.menu_room_and_livedata))
    }

    private fun setAdapter() {
        LiveAdapter(menuList, BR.item)
            .map<String, ItemMenuBinding>(R.layout.item_menu) {
                onClick {

                    val destination = when (it.binding.item) {
                        getString(R.string.menu_livedata_vs_observablelist) ->
                            LiveDataVsObservableListActivity::class.java
                        getString(R.string.menu_room_and_livedata) ->
                            RoomWithLiveDataActivity::class.java
                        else ->
                            null
                    }

                    destination?.let { destination ->
                        startActivity(
                            Intent(
                                this@HomeActivity,
                                destination
                            )
                        )
                    }
                }
            }
            .into(mBinding.rvMenu)
    }
}