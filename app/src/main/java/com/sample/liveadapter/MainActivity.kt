package com.sample.liveadapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravikoradiya.liveadapter.LiveAdapter
import com.sample.liveadapter.databinding.ActivityMainBinding
import com.sample.liveadapter.databinding.RowRvDataBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    lateinit var data: MutableLiveData<ArrayList<MyData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.rvTest.layoutManager = LinearLayoutManager(this)

        createDummyData()
        setLiveData()

        mBinding.btnAdd.setOnClickListener {
            if (mBinding.position.text.trim().isNotEmpty()) {
                val pos = mBinding.position.text.toString().toInt()
                val size = data.value?.size ?: 0
                val index = pos.coerceAtMost(size)

                val list = data.value
                list?.add(index, MyData("${1 + index}"))
                data.value = list
            }
        }

        mBinding.btnDelete.setOnClickListener {
            if (mBinding.position.text.trim().isNotEmpty()) {
                val pos = mBinding.position.text.toString().toInt()
                val size = data.value?.size ?: 0
                val index = pos.coerceAtMost(size).coerceAtLeast(0)

                val list = data.value
                list?.removeAt(index)
                data.value = list
            }
        }

        mBinding.btnChange.setOnClickListener {
            if (mBinding.position.text.trim().isNotEmpty()) {
                val pos = mBinding.position.text.toString().toInt()
                val size = data.value?.size ?: 0
                val index = pos.coerceAtMost(size).coerceAtLeast(0)

                val list = data.value
                list?.set(index, MyData("${1 + index} ${1 + index}"))
                data.value = list
            }
        }
    }

    private fun createDummyData() {
        data = MutableLiveData()
        data.value = ArrayList<MyData>()
        for (i in 0..40) {
            data.value?.add(MyData("${1 + (data.value?.size ?: 0)}"))
        }
    }

    private fun setLiveData() {

        LiveAdapter(data, this@MainActivity, BR.data)
            .map<MyData, RowRvDataBinding>(R.layout.row_rv_data)
            .diffUtils(callback = object :DiffUtil.Callback(){
                override fun getOldListSize(): Int {
                    TODO("Not yet implemented")
                }

                override fun getNewListSize(): Int {
                    TODO("Not yet implemented")
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    TODO("Not yet implemented")
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    TODO("Not yet implemented")
                }

            })
            .into(mBinding.rvTest)
    }

    data class MyData(var text: String)
}
