package com.chenghakfan.magroupassignment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import my.edu.utar.mobileappass.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConverterActivity extends AppCompatActivity {

    private EditText etSourceAmount, etTargetAmount;
    private Spinner spinnerSourceCurrency, spinnerTargetCurrency;
    private TextView tvExchangeRate;
    private ImageView btnSwap;

    private Map<String, Double> ratesMap = new HashMap<>();
    private final String[] currencies = {"MYR", "USD", "SGD", "GBP", "EUR", "JPY", "CNY", "AUD", "CAD", "INR", "KRW"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        etSourceAmount = findViewById(R.id.etSourceAmount);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        spinnerSourceCurrency = findViewById(R.id.spinnerSourceCurrency);
        spinnerTargetCurrency = findViewById(R.id.spinnerTargetCurrency);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        btnSwap = findViewById(R.id.btnSwap);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSourceCurrency.setAdapter(adapter);
        spinnerTargetCurrency.setAdapter(adapter);

        spinnerSourceCurrency.setSelection(0); // MYR
        spinnerTargetCurrency.setSelection(1); // USD

        fetchRates();

        etSourceAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                convert();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerSourceCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchRates();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTargetCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convert();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSwap.setOnClickListener(v -> {
            int sourcePos = spinnerSourceCurrency.getSelectedItemPosition();
            int targetPos = spinnerTargetCurrency.getSelectedItemPosition();
            spinnerSourceCurrency.setSelection(targetPos);
            spinnerTargetCurrency.setSelection(sourcePos);
        });
    }

    private void fetchRates() {
        String base = spinnerSourceCurrency.getSelectedItem().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.er-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CurrencyService service = retrofit.create(CurrencyService.class);
        service.getLatestRates(base).enqueue(new Callback<CurrencyResponse>() {
            @Override
            public void onResponse(Call<CurrencyResponse> call, Response<CurrencyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Open-API uses 'rates' field instead of 'conversion_rates'
                    ratesMap = response.body().rates;
                    if (ratesMap == null) ratesMap = response.body().conversion_rates;

                    convert();
                } else {
                    tvExchangeRate.setText("API Error: Use a valid key or public endpoint");
                }
            }

            @Override
            public void onFailure(Call<CurrencyResponse> call, Throwable t) {
                Toast.makeText(ConverterActivity.this, "Network Error. Please check connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convert() {
        String input = etSourceAmount.getText().toString().trim();
        if (input.isEmpty()) {
            etTargetAmount.setText("");
            return;
        }

        try {
            double sourceVal = Double.parseDouble(input);
            String target = spinnerTargetCurrency.getSelectedItem().toString();
            String source = spinnerSourceCurrency.getSelectedItem().toString();

            if (ratesMap != null && ratesMap.containsKey(target)) {
                double rate = ratesMap.get(target);
                double result = sourceVal * rate;
                etTargetAmount.setText(String.format(Locale.getDefault(), "%.2f", result));
                tvExchangeRate.setText(String.format(Locale.getDefault(), "1 %s = %.4f %s", source, rate, target));
            }
        } catch (NumberFormatException e) {
            etTargetAmount.setText("");
        }
    }
}
