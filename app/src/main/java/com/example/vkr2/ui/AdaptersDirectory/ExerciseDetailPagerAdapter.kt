package com.example.vkr2.ui.AdaptersDirectory

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.InfoFragment
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.StatsFragment

class ExerciseDetailPagerAdapter(
    fragment: Fragment,
    private val exerciseId: Int
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InfoFragment.newInstance(exerciseId)
            1 -> StatsFragment.newInstance(exerciseId)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
