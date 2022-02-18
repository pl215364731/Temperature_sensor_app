package com.example.temperaturesensorapp.ui.records;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.temperaturesensorapp.MainActivityViewModel;
import com.example.temperaturesensorapp.databinding.FragmentRecordsBinding;

public class RecordsFragment extends Fragment {

    private MainActivityViewModel mainActivityViewModel;
    private FragmentRecordsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecordsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivityViewModel =
                new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        ListView listView = binding.recordedList;

        ArrayAdapter arrayAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, mainActivityViewModel.getStringList());
        listView.setAdapter(arrayAdapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}