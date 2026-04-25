package my.edu.utar.mobileappass;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ChartsActivity extends AppCompatActivity {

    PieChart pieChart;
    TextView tvChartInsight, tvMonth, btnPrevMonth, btnNextMonth;
    ImageView navHome, navWallet, navCharts, navMore;
    DatabaseHelper db;

    int selectedYear;
    int selectedMonth; // 0 - 11

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        db = new DatabaseHelper(this);

        pieChart = findViewById(R.id.pieChart);
        tvChartInsight = findViewById(R.id.tvChartInsight);
        tvMonth = findViewById(R.id.tvMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        navHome = findViewById(R.id.navHome);
        navWallet = findViewById(R.id.navWallet);
        navCharts = findViewById(R.id.navCharts);
        navMore = findViewById(R.id.navMore);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);

        btnPrevMonth.setOnClickListener(v -> {
            selectedMonth--;
            if (selectedMonth < 0) {
                selectedMonth = 11;
                selectedYear--;
            }
            loadExpenseChart();
        });

        btnNextMonth.setOnClickListener(v -> {
            selectedMonth++;
            if (selectedMonth > 11) {
                selectedMonth = 0;
                selectedYear++;
            }
            loadExpenseChart();
        });

        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        navWallet.setOnClickListener(v ->
                startActivity(new Intent(this, WalletActivity.class)));

        navMore.setOnClickListener(v ->
                startActivity(new Intent(this, MoreActivity.class)));

        loadExpenseChart();
    }

    private void loadExpenseChart() {
        String monthKey = String.format(Locale.getDefault(), "%04d-%02d", selectedYear, selectedMonth + 1);
        String monthDisplay = new DateFormatSymbols().getMonths()[selectedMonth] + " " + selectedYear;
        tvMonth.setText(monthDisplay);

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        double food = db.getCategoryExpenseForMonth("Food", monthKey);
        double transport = db.getCategoryExpenseForMonth("Transport", monthKey);
        double entertainment = db.getCategoryExpenseForMonth("Entertainment", monthKey);
        double education = db.getCategoryExpenseForMonth("Education", monthKey);
        double income = db.getCategoryExpenseForMonth("Income", monthKey);
        double others = db.getCategoryExpenseForMonth("Others", monthKey);

        if (food > 0) {
            entries.add(new PieEntry((float) food, "Food"));
            colors.add(Color.parseColor("#4CAF50"));
        }

        if (transport > 0) {
            entries.add(new PieEntry((float) transport, "Transport"));
            colors.add(Color.parseColor("#2196F3"));
        }

        if (entertainment > 0) {
            entries.add(new PieEntry((float) entertainment, "Entertainment"));
            colors.add(Color.parseColor("#FF9800"));
        }

        if (education > 0) {
            entries.add(new PieEntry((float) education, "Education"));
            colors.add(Color.parseColor("#9C27B0"));
        }

        if (income > 0) {
            entries.add(new PieEntry((float) income, "Income"));
            colors.add(Color.parseColor("#00BCD4"));
        }

        if (others > 0) {
            entries.add(new PieEntry((float) others, "Others"));
            colors.add(Color.parseColor("#607D8B"));
        }

        pieChart.clear();

        if (entries.isEmpty()) {
            tvChartInsight.setText("");
            pieChart.setNoDataText("No expense data for this month");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterText("Total\nRM" + String.format(Locale.getDefault(), "%.2f",
                db.getTotalExpensesForMonth(monthKey)));
        pieChart.setCenterTextSize(14f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(800);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(12f);
        legend.setFormSize(10f);

        pieChart.invalidate();

        tvChartInsight.setText(AiHelper.generateRecommendation(db, monthKey));
    }
}