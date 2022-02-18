package com.example.temperaturesensorapp.ui.temperature;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.temperaturesensorapp.MainActivityViewModel;
import com.example.temperaturesensorapp.databinding.FragmentTemperatureBinding;

import java.text.DecimalFormat;

public class TemperatureFragment extends Fragment {

    private MainActivityViewModel mainActivityViewModel;
    private FragmentTemperatureBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainActivityViewModel =
                new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.temperature;
        final Button unitButton = binding.unitButton;

        if(mainActivityViewModel.getTooFar()){
            unitButton.setVisibility(View.GONE);
            textView.setText("Please get closer to the sensor");
            textView.setTextSize(20);
        } else if(mainActivityViewModel.getTooClose()){
            unitButton.setVisibility(View.GONE);
            textView.setText("Please be at least 1 cm away from the sensor");
            textView.setTextSize(20);
        } else if(mainActivityViewModel.getTableLength() == 0){
            unitButton.setVisibility(View.GONE);
            textView.setText("Please Scan");
            textView.setTextSize(20);
        } else{
            unitButton.setText("°C/°F");
            if(mainActivityViewModel.getUnit() == 'C'){
                printCelsius();
            } else if(mainActivityViewModel.getUnit() == 'F'){
                printFahrenheit();
            }
        }
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.unitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mainActivityViewModel.getUnit() == 'C'){
                    printFahrenheit();
                    mainActivityViewModel.setUnit('F');
                } else if(mainActivityViewModel.getUnit() == 'F'){
                    printCelsius();
                    mainActivityViewModel.setUnit('C');
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void printCelsius() {
        final TextView textView = binding.temperature;
        DecimalFormat df=new DecimalFormat("#.#");
        textView.setText(df.format(mainActivityViewModel.getTempC()) + " °C");
        //Color
        if(mainActivityViewModel.getTempC() < 37.2){
            textView.setTextColor(Color.rgb(6,145,6));
        } else if(mainActivityViewModel.getTempC() > 38){
            textView.setTextColor(Color.rgb(255,0,0));
        } else {
            textView.setTextColor(Color.rgb(219,219,0));
        }
    }

    void printFahrenheit(){
        final TextView textView = binding.temperature;
        DecimalFormat df = new DecimalFormat("#.#");
        textView.setText(df.format(mainActivityViewModel.getTempF()) + " °F");
        //Color
        if (mainActivityViewModel.getTempF() < 99) {
            textView.setTextColor(Color.rgb(6, 145, 6));
        } else if (mainActivityViewModel.getTempF() > 100.4) {
            textView.setTextColor(Color.rgb(255, 0, 0));
        } else {
            textView.setTextColor(Color.rgb(219, 219, 0));
        }
    }
}