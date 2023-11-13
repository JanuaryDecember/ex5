package com.csconter.zadanie3;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskListFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    public static final String KEY_EXTRA_TASK_ID = "tasklistfragment.task_id";
    private static final String KEY_SUBTITLE_VISIBLE = "subtitleVisible";
    private boolean subtitleVisible;

    public TaskListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        recyclerView = view.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        TaskStorage taskStorage = TaskStorage.getInstance();
        List<Task> tasks = taskStorage.getTasks();

        if (adapter == null) {
            adapter = new TaskAdapter(tasks);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_menu, menu);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (subtitleVisible) subtitleItem.setTitle(R.string.hide_subtitle);
        else subtitleItem.setTitle(R.string.show_subtitle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            subtitleVisible = savedInstanceState.getBoolean(KEY_SUBTITLE_VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.new_task) {
            Task task = new Task();
            TaskStorage.getInstance().addTask(task);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(KEY_EXTRA_TASK_ID, task.getId());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.show_subtitle) {
            subtitleVisible = !subtitleVisible;
            getActivity().invalidateOptionsMenu();
            updateSubtitle();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void updateSubtitle() {
        String subtitle = null;
        if (subtitleVisible) {
            TaskStorage taskStorage = TaskStorage.getInstance();
            List<Task> tasks = taskStorage.getTasks();
            int todoTasksCount = 0;
            for (Task task : tasks) {
                if (!task.isDone()) ++todoTasksCount;
            }
            subtitle = getString(R.string.subtitle_format, todoTasksCount);
            if (!subtitleVisible) subtitle = null;
        }
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);
    }


    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CheckBox taskCheckbox;
        private ImageView iconImageView;
        private ImageView otherIconImageView;
        private TextView nameTextView, dateTextView;
        private Task task;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_task, parent, false));
            itemView.setOnClickListener(this);

            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            iconImageView = itemView.findViewById(R.id.imageView2);
            otherIconImageView = itemView.findViewById(R.id.imageView3);
            nameTextView = itemView.findViewById(R.id.task_item_name);
            dateTextView = itemView.findViewById(R.id.task_item_date);
        }

        public CheckBox getCheckBox() {
            return taskCheckbox;
        }

        public void bind(Task task) {
            this.task = task;


            taskCheckbox.setChecked(task.isDone());


            if (task.getCategory() == Category.STUDIES) {
                iconImageView.setImageResource(R.drawable.ic_studies);
                otherIconImageView.setVisibility(View.GONE);
            } else if (task.getCategory() == Category.HOME) {
                iconImageView.setImageResource(R.drawable.ic_house);
                otherIconImageView.setVisibility(View.GONE);
            }


            String taskName = task.getName();
            if (task.isDone()) {
                nameTextView.setText(getStrikethroughText(taskName));
            } else {

                nameTextView.setText(getEllipsizedText(taskName, 50));
            }

            dateTextView.setText(task.getDate().toString());
        }

        private CharSequence getStrikethroughText(String text) {
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(new StrikethroughSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        }

        private CharSequence getEllipsizedText(String text, int maxLength) {
            if (text.length() > maxLength) {
                return text.substring(0, maxLength) + "...";
            } else {
                return text;
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(KEY_EXTRA_TASK_ID, task.getId());
            startActivity(intent);
        }
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {
        private List<Task> tasks;

        public TaskAdapter(List<Task> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TaskHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
            Task task = tasks.get(position);
            holder.bind(task);
            CheckBox checkBox = holder.getCheckBox();
            checkBox.setChecked(task.isDone());
            checkBox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                tasks.get(holder.getBindingAdapterPosition()).setDone(isChecked);
                if (isChecked) {
                    holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }
}