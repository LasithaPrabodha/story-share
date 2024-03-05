package com.capstoneprojectg8.schoolscheduleapp.ui.schedule

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstoneprojectg8.schoolscheduleapp.R
import com.capstoneprojectg8.schoolscheduleapp.database.ClassesDatabase
import com.capstoneprojectg8.schoolscheduleapp.databinding.FragmentScheduleBinding
import com.capstoneprojectg8.schoolscheduleapp.models.ScheduleSlot
import com.capstoneprojectg8.schoolscheduleapp.repository.ClassesRepository
import com.capstoneprojectg8.schoolscheduleapp.ui.schedule.add_slot.AddClassSlotViewModel
import com.capstoneprojectg8.schoolscheduleapp.utils.DateHelper
import com.capstoneprojectg8.schoolscheduleapp.utils.DummySlots
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private var cellWidth: Int = 0
    private var step = 0
    private lateinit var weekGrid: RelativeLayout
    private lateinit var weekTimelineAdapter: WeekTimelineAdapter
    private lateinit var calendarRowAdapter: CalendarRowAdapter
    private val viewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(ClassesRepository(ClassesDatabase(requireContext())))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        weekGrid = binding.weekGrid

        setCellWidth()

        initRecycleViews()

        generateSlots()

        scrollToFirstSlot()

        setupListeners()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.calendar_nav, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.next -> {
                        step++
                        updateCalendar()
                        true
                    }

                    R.id.previous -> {
                        step--
                        updateCalendar()
                        true
                    }

                    R.id.current_week -> {
                        step = 0
                        updateCalendar()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupListeners() {
        binding.addClassToScheduleBtn.setOnClickListener {
            findNavController().navigate(
                ScheduleFragmentDirections.actionNavigationScheduleToAddClassSlot()
            )
        }
    }

    private fun generateSlots() {
        val today = LocalDate.now()
        val startDate = today.plusWeeks(step.toLong()).with(DayOfWeek.MONDAY)
        viewModel.getAllClassSlots().observe(viewLifecycleOwner) { slots ->


            Log.d("SCH", slots.toString())
            val startOfWeek = DateHelper.startOfTheWeek(startDate)
            val endOfWeek = DateHelper.endOfTheWeek(startDate)

            val filtered = slots.filter { slot ->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val ld = LocalDate.parse(slot.date, formatter)

                ld >= startOfWeek && ld <= endOfWeek
            }

            weekGrid.removeViews(1, weekGrid.childCount - 1)

            for (slot in filtered) {
                generateSlot(slot)
            }
        }

    }

    private fun setCellWidth() {
        val displayMetrics = resources.displayMetrics

        var dpWidth = displayMetrics.widthPixels
        dpWidth -= resources.getDimension(R.dimen.grid_time_indicator_layout_width).toInt()
        cellWidth = dpWidth / 5
    }

    private fun initRecycleViews() {
        val timeline = viewModel.generateHourRows(step == 0)
        calendarRowAdapter = CalendarRowAdapter(requireContext(), timeline)

        binding.hourList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = calendarRowAdapter
        }

        val dayOfWeek: MutableList<Map<String, String>> = DateHelper.generateDaysOfTheWeek()
        weekTimelineAdapter = WeekTimelineAdapter(requireContext(), dayOfWeek, cellWidth)

        binding.dayList.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false
            )
            adapter = weekTimelineAdapter
        }
    }

    private fun scrollToFirstSlot() {
        val scheduleScroll = binding.scheduleScroll

        val dip = resources.getDimensionPixelSize(R.dimen.grid_cell_layout_height)
        scheduleScroll.post { scheduleScroll.scrollTo(0, dip * 9) }
    }

    private fun updateCalendar() {
        val today = LocalDate.now()
        val startDate = today.plusWeeks(step.toLong()).with(DayOfWeek.MONDAY)
        val daysOfTheWeek: MutableList<Map<String, String>> =
            DateHelper.generateDaysOfTheWeek(startDate)

        weekTimelineAdapter.updateList(daysOfTheWeek)

        val timeline = viewModel.generateHourRows(step == 0)
        calendarRowAdapter.updateList(timeline)

        generateSlots()

    }

    private fun generateSlot(slot: ScheduleSlot) {
        val linearLayout = LinearLayout(requireActivity())
        linearLayout.orientation = LinearLayout.VERTICAL

        val className = TextView(requireActivity())
        val classRoom = TextView(requireActivity())

        className.text = slot.className
        classRoom.text = slot.classRoom

        // set background rounded rectangle
        val shape = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.rounded_rectangle
        ) as GradientDrawable

        shape.setColor(ContextCompat.getColor(requireContext(), slot.color))
        ViewCompat.setBackground(linearLayout, shape)

        // Style the text views
        className.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.medium_font_size)
        )
        className.setPadding(20, 25, 20, 0)
        className.typeface = Typeface.DEFAULT_BOLD
        val hourLayoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.grid_cell_layout_width),
            LayoutParams.WRAP_CONTENT
        )
        className.layoutParams = hourLayoutParams

        classRoom.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.small_font_size)
        )
        classRoom.setPadding(20, 15, 20, 15)

        linearLayout.addView(className)
        linearLayout.addView(classRoom)


        val linearLayoutParams = LinearLayout.LayoutParams(
            cellWidth - 30,
            resources.getDimension(R.dimen.grid_cell_layout_height)
                .toInt() * slot.noOfHours - 30
        )

        linearLayoutParams.topMargin =
            slot.startingHour * resources.getDimension(R.dimen.grid_cell_layout_height)
                .toInt() + 30 + (0.5 * slot.startingHour).toInt()

        if (slot.dayOfTheWeek == 1) {
            linearLayoutParams.leftMargin =
                resources.getDimension(R.dimen.grid_time_indicator_layout_width).toInt() + 15
        } else {
            val slotWidth = resources.getDimension(R.dimen.grid_time_indicator_layout_width)
                .toInt() + 15
            val margins = 30 * (slot.dayOfTheWeek - 1)

            val widthOfPrevCols = (slot.dayOfTheWeek - 1) * (cellWidth - 30)

            linearLayoutParams.leftMargin = slotWidth + margins + widthOfPrevCols
        }

        weekGrid.addView(linearLayout, linearLayoutParams)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}