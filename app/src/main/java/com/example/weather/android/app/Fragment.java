package com.example.weather.android.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.miraclegarden.library.app.utils.ViewBindingUtil;

public class Fragment<Binding extends ViewBinding> extends androidx.fragment.app.Fragment {
    public Binding binding;

    public Binding getBinding() {
        return binding;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ViewBindingUtil.inflate(getClass(), inflater, container);
        return binding.getRoot();
    }
}
