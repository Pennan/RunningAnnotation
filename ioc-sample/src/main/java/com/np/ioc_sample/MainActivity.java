package com.np.ioc_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.np.annotation.BindView;
import com.np.ioc.ViewInjector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.lv_main)
    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewInjector.injectView(this);
        List<String> dataList = initData();
        lvMain.setAdapter(new MyAdapter(this, dataList, R.layout.item));
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> initData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("哈哈" + i);
        }
        return data;
    }
}
