package b.jakub.bigfilefinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        List<File> files = (List<File>)getIntent().getSerializableExtra(Storage.TASK_RESULT);
        ResultAdapter resultAdapter = new ResultAdapter(this, files);
        RecyclerView rvResults = findViewById(R.id.rv_results);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvResults.setLayoutManager(layoutManager);
        rvResults.setAdapter(resultAdapter);
    }
}