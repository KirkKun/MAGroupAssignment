package my.edu.utar.mobileappass;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SavingsGoalActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView recyclerGoals;
    private Button btnAddGoal;
    private ImageView btnBack;
    private SavingsGoalAdapter adapter;
    private ArrayList<SavingsGoalModel> goalsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        db = new DatabaseHelper(this);
        recyclerGoals = findViewById(R.id.recyclerGoals);
        btnAddGoal = findViewById(R.id.btnAddGoal);
        btnBack = findViewById(R.id.btnBack);

        recyclerGoals.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnAddGoal.setOnClickListener(v -> showAddGoalDialog());

        loadGoals();
        checkGoalsMet();
    }

    private void loadGoals() {
        goalsList = db.getAllSavingsGoals();
        adapter = new SavingsGoalAdapter(this, goalsList);
        recyclerGoals.setAdapter(adapter);
    }

    private void checkGoalsMet() {
        double totalAssets = (db.getTotalIncome() - db.getTotalExpenses()) + db.getTotalAssetsValue();
        ArrayList<SavingsGoalModel> goals = db.getAllSavingsGoals();

        for (SavingsGoalModel goal : goals) {
            if (totalAssets >= goal.getTargetAmount()) {
                new AlertDialog.Builder(this)
                        .setTitle("Goal Achieved! 🎉")
                        .setMessage("Congratulations! Your total assets (RM" + String.format("%.2f", totalAssets) +
                                ") have reached your goal for: " + goal.getTitle() + " (RM" + String.format("%.2f", goal.getTargetAmount()) + ")")
                        .setPositiveButton("Awesome", null)
                        .show();
            }
        }
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Savings Goal");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        EditText etTitle = view.findViewById(R.id.etGoalTitle);
        EditText etTarget = view.findViewById(R.id.etGoalTarget);
        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String targetStr = etTarget.getText().toString().trim();

            if (!title.isEmpty() && !targetStr.isEmpty()) {
                try {
                    double target = Double.parseDouble(targetStr);
                    if (db.addSavingsGoal(title, target)) {
                        Toast.makeText(this, "Goal added!", Toast.LENGTH_SHORT).show();
                        loadGoals();
                        checkGoalsMet();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
