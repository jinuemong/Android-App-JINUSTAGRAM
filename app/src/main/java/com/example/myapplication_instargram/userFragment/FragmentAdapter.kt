package com.example.myapplication_instargram.userFragment

import android.util.Log
import android.util.Range
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.math.log

class FragmentAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    var fragments: ArrayList<Fragment> = ArrayList()
    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }


    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
        //구조 변화를 알림
        notifyItemInserted(fragments.size - 1)
    }

    fun removeFragment() {
        fragments.removeLast()
        notifyItemRemoved(fragments.size)
    }

    fun changeFragment(index:Int ,goFragment: Fragment) {
        fragments[index] = goFragment
    }
}