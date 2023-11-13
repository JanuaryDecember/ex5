package com.csconter.zadanie3;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.DatePickerDialog;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class TaskFragment extends Fragment {
    private Task task;
    private EditText nameField;
    private EditText dateField;
    private CheckBox doneCheckBox;
    public static final String ARG_TASK_ID = "task_id";

    private final Calendar calendar = Calendar.getInstance();

    public TaskFragment() {}

    public static TaskFragment newInstance(UUID taskId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_TASK_ID, taskId);
        TaskFragment taskFragment = new TaskFragment();
        taskFragment.setArguments(bundle);
        return taskFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID taskId = (UUID) getArguments().getSerializable(ARG_TASK_ID);
        task = TaskStorage.getInstance().getTask(taskId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        nameField = view.findViewById(R.id.task_name);
        nameField.setText(task.getName());

        dateField = view.findViewById(R.id.task_date);

        dateField.setOnClickListener(v -> showDatePickerDialog());

        Spinner categorySpinner = view.findViewById(R.id.task_category);
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Category.values()
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = categoryAdapter.getItem(position);
                task.setCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int categoryPosition = categoryAdapter.getPosition(task.getCategory());
        categorySpinner.setSelection(categoryPosition);

        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                task.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dateField.setText(task.getDate().toString());


        doneCheckBox = view.findViewById(R.id.task_done);
        doneCheckBox.setChecked(task.isDone());

        doneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setDone(isChecked);
        });

        setupDateFieldValue(task.getDate());

        return view;
    }
    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateListener = (view12, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            setupDateFieldValue(calendar.getTime());
            task.setDate(calendar.getTime());
        };

        new DatePickerDialog(requireContext(), dateListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setupDateFieldValue(Date date) {
        Locale locale = new Locale("pl", "PL");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        dateField.setText(dateFormat.format(date));
    }
}