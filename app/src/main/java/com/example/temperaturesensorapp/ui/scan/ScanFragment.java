package com.example.temperaturesensorapp.ui.scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.temperaturesensorapp.MainActivityViewModel;
import com.example.temperaturesensorapp.R;
import com.example.temperaturesensorapp.databinding.FragmentScanBinding;
import com.google.android.material.snackbar.Snackbar;

public class ScanFragment extends Fragment {

    private MainActivityViewModel mainActivityViewModel;
    private FragmentScanBinding binding;

    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { permission }, requestCode);
        }
        else {

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mainActivityViewModel =
                new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        binding = FragmentScanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.BLUETOOTH,1);
                checkPermission(Manifest.permission.BLUETOOTH_ADMIN,2);
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT,3);
                mainActivityViewModel.getBleData();
                if(mainActivityViewModel.getResult()){
                    NavHostFragment.findNavController(ScanFragment.this)
                            .navigate(R.id.navigation_temperature);
                }
                else{
                    Snackbar warning = Snackbar.make(view, "Cannot reach Arduino", 2000);
                    warning.show();
                    //warning
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}