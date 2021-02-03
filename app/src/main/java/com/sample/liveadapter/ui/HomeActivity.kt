package com.sample.liveadapter.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravikoradiya.liveadapter.LiveAdapter
import com.sample.liveadapter.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    //    private lateinit var mBinding: ActivityHomeBinding
    private val menuList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        setContentView(R.layout.activity_home)
        rv_menu.layoutManager = LinearLayoutManager(this)

        createMenus()
        setAdapter()

    }

    private fun createMenus() {
        menuList.add(getString(R.string.menu_livedata_vs_observablelist))
        menuList.add(getString(R.string.menu_room_and_livedata))
    }

    private fun setAdapter() {
        LiveAdapter(menuList)
            .map<String>(R.layout.item_menu) {
                onBind {
                    it.binding.getViewById<TextView>(R.id.menu_label)?.text = it.binding.data
                }

                onClick {

                    val destination = when (it.binding.data) {
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
            .into(rv_menu)

    }
}