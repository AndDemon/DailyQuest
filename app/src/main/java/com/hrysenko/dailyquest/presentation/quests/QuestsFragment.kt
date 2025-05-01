package com.hrysenko.dailyquest.presentation.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hrysenko.dailyquest.R

class QuestsFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quests, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {

        val fragments = listOf(
            Pair(DailyQuestFragment(), getString(R.string.normal_quests)),
            Pair(CustomQuestFragment(), getString(R.string.custom_quests))
        )


        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size

            override fun createFragment(position: Int): Fragment = fragments[position].first
        }


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = fragments[position].second
        }.attach()
    }
}