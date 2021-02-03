package com.sample.liveadapter.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravikoradiya.liveadapter.LiveAdapter
import com.sample.liveadapter.BR
import com.sample.liveadapter.R
import com.sample.liveadapter.databinding.ActivityLivedataVsObservablelistBinding
import com.sample.liveadapter.databinding.RowRvData2Binding
import com.sample.liveadapter.databinding.RowRvDataBinding

class LiveDataVsObservableListActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityLivedataVsObservablelistBinding
    lateinit var data: MutableLiveData<ArrayList<Any>>
    lateinit var observableData: ObservableArrayList<Any>
    private val isLiveDataAttached = ObservableBoolean()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_livedata_vs_observablelist)

        mBinding.rvTest.layoutManager = LinearLayoutManager(this)

        isLiveDataAttached.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if ((sender as ObservableBoolean).get()) {
                    createDummyData()
                    setLiveData()
                } else {
                    createObservableDummyData()
                    setObservableData()
                }
            }
        })

        isLiveDataAttached.set(true)
        isLiveDataAttached.set(false)

        mBinding.dataSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            isLiveDataAttached.set(isChecked)
        }

        mBinding.btnAdd.setOnClickListener {
            if (mBinding.position.text.trim().isNotEmpty()) {
                val pos = mBinding.position.text.toString().toInt()
                addDataAt(pos)
            }
        }

        mBinding.btnDelete.setOnClickListener {
            if (mBinding.position.text.trim().isNotEmpty()) {
                val pos = mBinding.position.text.toString().toInt()
                removeDataAt(pos)
            }
        }

        mBinding.btnChange.setOnClickListener {
            if (mBinding.position.text.trim().isNotEmpty()) {
                val pos = mBinding.position.text.toString().toInt()
                changeDataAt(pos)
            }
        }
    }

    private fun createDummyData() {
        data = MutableLiveData()
        data.value = ArrayList()
        for (i in 0..15) {
            val id = System.currentTimeMillis() + i
            data.value?.add(getRandomData(id))
        }
    }

    private fun createObservableDummyData() {
        observableData = ObservableArrayList()
        for (i in 0..15) {
            val id = System.currentTimeMillis() + i
            observableData.add(getRandomData(id))
        }
    }

    private fun setLiveData() {
        LiveAdapter(data, this@LiveDataVsObservableListActivity, BR.data)
            .map<MyData, RowRvDataBinding>(R.layout.row_rv_data) {
                areContentsTheSame { old, new ->
                    return@areContentsTheSame old.id == new.id
                }
            }
            .map<MyData2, RowRvData2Binding>(R.layout.row_rv_data2) {
                areContentsTheSame { old, new ->
                    return@areContentsTheSame old.id == new.id
                }
            }.map<String>(R.layout.item_menu) {
                onBind {
                    it.binding.getViewById<TextView>(R.id.menu_label)?.text = it.binding.data
                }

                areContentsTheSame { old, new ->
                    return@areContentsTheSame old == new
                }
            }
            .onNoData {

            }
            .into(mBinding.rvTest)
    }

    private fun setObservableData() {
        LiveAdapter(observableData, BR.data)
            .map<MyData, RowRvDataBinding>(R.layout.row_rv_data)
            .map<MyData2, RowRvData2Binding>(R.layout.row_rv_data2)
            .map<String>(R.layout.item_menu) {
                onBind {
                    it.binding.getViewById<TextView>(R.id.menu_label)?.text = it.binding.data
                }

                areContentsTheSame { old, new ->
                    return@areContentsTheSame old == new
                }
            }
            .into(mBinding.rvTest)
    }

    private fun getRandomData(id: Long): Any {
        return if (id % 3 == 0L) {
            MyData(id, "$id")
        } else if (id % 3 == 1L) {
            MyData2(id, "$id")
        } else {
            "String: $id"
        }
    }

    private fun addDataAt(pos: Int) {
        val index = pos.coerceAtMost(getDataSize())
        val id = System.currentTimeMillis()

        try {
            if (isLiveDataAttached.get()) {
                val list = data.value
                list?.add(index, getRandomData(id))
                data.value = list

            } else {
                observableData.add(index, getRandomData(id))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeDataAt(pos: Int) {
        val index = pos.coerceAtMost(getDataSize()).coerceAtLeast(0)
        val id = System.currentTimeMillis()

        try {
            if (isLiveDataAttached.get()) {
                val list = data.value
                list?.set(index, getRandomData(id))
                data.value = list

            } else {
                observableData.set(index, getRandomData(id))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeDataAt(pos: Int) {
        val index = pos.coerceAtMost(getDataSize())
        try {
            if (isLiveDataAttached.get()) {
                val list = data.value
                list?.removeAt(index)
                data.value = list
            } else {
                observableData.removeAt(index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDataSize(): Int {
        return if (isLiveDataAttached.get()) {
            data.value?.size ?: 0
        } else {
            observableData.size
        }
    }

    data class MyData(
        var id: Long,
        var text: String
    )

    data class MyData2(
        var id: Long,
        var text: String
    )
}