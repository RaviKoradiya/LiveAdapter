package com.sample.liveadapter

import android.arch.lifecycle.MutableLiveData
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.ravikoradiya.liveadapter.LiveAdapter
import com.sample.liveadapter.databinding.ActivityMainBinding
import com.sample.liveadapter.databinding.RowRvDataBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        mBinding.rvTest.layoutManager = LinearLayoutManager(this)

        /*val data = MutableLiveData<ArrayList<MyData>>()
        data.value = ArrayList<MyData>()
        for (i in 0..40) {
            data.value?.add(MyData("${1 + (data.value?.size ?: 0)}"))
        }

        LiveAdapter<MyData>(data, BR.data)
            .map<MyData, RowRvDataBinding>(R.layout.row_rv_data)
            .into(mBinding.rvTest)

        mBinding.rvTest.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager1 = recyclerView?.layoutManager
                if (layoutManager1 is LinearLayoutManager) {
                    val lastItem = layoutManager1.findLastVisibleItemPosition()
                    if (lastItem == recyclerView.adapter.itemCount - 1 && !isLoading) {
                        isLoading = true
                        for (i in 0..9) {
                            val add = data.value
                            val randomIndex = Random(System.currentTimeMillis()).nextInt(data.value?.size ?: 0)
                            add?.add(randomIndex, MyData("${1 + (data.value?.size ?: 0)}"))
                            data.value = add
                        }
                        isLoading = false
                    }
                }
            }
        })*/

        val data = ObservableArrayList<MyData>()

        for (i in 0..40) {
            data.add(MyData("${1 + data.size}"))
        }

        LiveAdapter<Any>(data, BR.data)
            .map<MyData, RowRvDataBinding>(R.layout.row_rv_data)
            .into(mBinding.rvTest)

        mBinding.rvTest.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager1 = recyclerView?.layoutManager
                if (layoutManager1 is LinearLayoutManager) {
                    val lastItem = layoutManager1.findLastVisibleItemPosition()
                    if (lastItem == recyclerView.adapter.itemCount - 1 && !isLoading) {
                        isLoading = true
                        for (i in 0..9) {
                            val randomIndex = Random(System.currentTimeMillis()).nextInt(data.size)
                            data.add(randomIndex, MyData("${1 + data.size}"))
                        }
                        isLoading = false
                    }
                }
            }
        })
    }

    data class MyData(val text: String)
}
