package com.hrysenko.dailyquest.presentation.quests

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.models.quest.room.Quest
import com.hrysenko.dailyquest.models.quest.room.QuestDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import android.text.InputFilter
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter

class CustomQuestFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var questDao: QuestDao
    private lateinit var addTaskButton: Button
    private var addQuestDialog: AlertDialog? = null
    private val questAdapter = QuestListAdapter(
        onDeleteClick = { quest -> deleteQuest(quest) },
        onDoneClick = { quest, position -> markQuestAsDone(quest, position) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_custom_quest, container, false)
        recyclerView = view.findViewById(R.id.daily_tasks_recycler)
        addTaskButton = view.findViewById(R.id.add_task_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questDao = AppDatabase.getDatabase(requireContext()).questDao()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = questAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()


        recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.recycler_view_layout_animation)

        lifecycleScope.launch {
            questDao.getAllQuests().collectLatest { quests ->
                questAdapter.submitList(quests)

                recyclerView.scheduleLayoutAnimation()
            }
        }

        addTaskButton.setOnClickListener { openAddQuestDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addQuestDialog?.dismiss()
        addQuestDialog = null
    }

    private fun openAddQuestDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_quest, null)
        val questNameInput: EditText = dialogView.findViewById(R.id.quest_name_input)
        val complexityDropdown: MaterialAutoCompleteTextView = dialogView.findViewById(R.id.complexity_dropdown)
        val questAmountInput: EditText = dialogView.findViewById(R.id.quest_amount_input)
        val saveButton: Button = dialogView.findViewById(R.id.save_button)


        val complexityLevels = resources.getStringArray(R.array.complexity_levels)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, complexityLevels)
        complexityDropdown.setAdapter(adapter)

        questNameInput.filters = arrayOf(InputFilter.LengthFilter(18))
        questAmountInput.filters = arrayOf(InputFilter.LengthFilter(8))

        addQuestDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        addQuestDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        saveButton.setOnClickListener {
            if (isAdded && !isDetached) {
                val name = questNameInput.text.toString().trim()
                val complexity = complexityDropdown.text.toString()
                val amount = questAmountInput.text.toString().trim()

                if (name.isNotEmpty() && amount.isNotEmpty()) {
                    val newQuest = Quest(name = name, complexity = complexity, amount = amount, isReady = false)
                    addQuestToDatabase(newQuest)
                    addQuestDialog?.dismiss()
                }
            }
        }

        addQuestDialog?.show()
    }

    private fun addQuestToDatabase(quest: Quest) {
        lifecycleScope.launch(Dispatchers.IO) {
            questDao.insertQuest(quest)
        }
    }

    private fun deleteQuest(quest: Quest) {
        lifecycleScope.launch(Dispatchers.IO) {
            questDao.deleteQuest(quest)
        }
    }

    private fun markQuestAsDone(quest: Quest, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            quest.isReady = true
            questDao.updateQuest(quest)

            launch(Dispatchers.Main) {
                questAdapter.notifyItemChanged(position)
            }
        }
    }

    private class QuestListAdapter(
        private val onDeleteClick: (Quest) -> Unit,
        private val onDoneClick: (Quest, Int) -> Unit
    ) : ListAdapter<Quest, QuestListAdapter.QuestViewHolder>(QuestDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
            return QuestViewHolder(view, onDeleteClick, onDoneClick)
        }

        override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
            holder.bind(getItem(position), position)
        }

        class QuestViewHolder(
            itemView: View,
            private val onDeleteClick: (Quest) -> Unit,
            private val onDoneClick: (Quest, Int) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            private val nameText: TextView = itemView.findViewById(R.id.quest_name)
            private val complexityText: TextView = itemView.findViewById(R.id.complexity)
            private val amountText: TextView = itemView.findViewById(R.id.quest_amount)
            private val doneButton: MaterialButton = itemView.findViewById(R.id.done_btn)
            private val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_btn)

            fun bind(quest: Quest, position: Int) {
                nameText.text = quest.name
                complexityText.text = quest.complexity
                amountText.text = quest.amount

                itemView.alpha = if (quest.isReady) 0.5f else 1.0f
                doneButton.isEnabled = !quest.isReady

                doneButton.setOnClickListener {
                    if (!quest.isReady) {
                        onDoneClick(quest, position)
                    }
                }
                deleteButton.setOnClickListener { onDeleteClick(quest) }
            }
        }

        private class QuestDiffCallback : DiffUtil.ItemCallback<Quest>() {
            override fun areItemsTheSame(oldItem: Quest, newItem: Quest): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Quest, newItem: Quest): Boolean {
                return oldItem == newItem
            }
        }
    }
}