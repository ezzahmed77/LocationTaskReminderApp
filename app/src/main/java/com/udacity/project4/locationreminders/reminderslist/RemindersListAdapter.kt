package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.content.Intent
import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter
import com.udacity.project4.base.DataBindingViewHolder
import com.udacity.project4.locationreminders.RemindersActivity


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit, val onClickListener: OnClickListener) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder

    override fun onBindViewHolder(holder: DataBindingViewHolder<ReminderDataItem>, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(item)

        }
    }
}

class OnClickListener(val clickListener: (selectedReminder: ReminderDataItem) -> Unit) {
    fun onClick(selectedReminder: ReminderDataItem) = clickListener(selectedReminder)
}