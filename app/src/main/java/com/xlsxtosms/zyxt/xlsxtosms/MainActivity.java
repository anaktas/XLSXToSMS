package com.xlsxtosms.zyxt.xlsxtosms;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener{

    ListView LvlList;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    Button BtnOK;
    Button BtnCancel;

    String currentPath = null;
    String selectedFilePath = null;
    String selectedFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            LvlList = (ListView) findViewById(R.id.LvlList);
            BtnOK = (Button) findViewById(R.id.BtnOK);
            BtnCancel = (Button) findViewById(R.id.BtnCancel);

            LvlList.setOnItemClickListener(this);
            BtnOK.setOnClickListener(this);
            BtnCancel.setOnClickListener(this);

            setCurrentPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
        } catch (Exception ex) {
            Toast.makeText(this, "Σφάλμα κατά την εκκίνηση: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void setCurrentPath(String path) {
        ArrayList<String> folders = new ArrayList<String>();
        ArrayList<String> files = new ArrayList<String>();

        currentPath = path;

        File[] allEntries = new File(path).listFiles();

        for (int i = 0; i < allEntries.length; i++) {
            if (allEntries[i].isDirectory()) {
                folders.add(allEntries[i].getName());
            } else if (allEntries[i].isFile()) {
                files.add(allEntries[i].getName());
            }
        }

        Collections.sort(folders, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        listItems.clear();

        for (int i = 0; i < folders.size(); i++) {
            listItems.add(folders.get(i) + "/");
        }

        for (int i = 0; i < files.size(); i++) {
            listItems.add(files.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        adapter.notifyDataSetChanged();

        LvlList.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath() + "/")) {
            setCurrentPath(new File(currentPath).getParent() + "/");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String entryName = (String)parent.getItemAtPosition(position);

        if (entryName.endsWith("/")) {
            setCurrentPath(currentPath + entryName);
        } else {
            selectedFilePath = currentPath + entryName;
            selectedFileName = entryName;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtnOK:
                Toast.makeText(this, "OK: Ο φάκελος είναι:  " + selectedFilePath + ", και το αρχείο: " + selectedFileName, Toast.LENGTH_LONG).show();
                break;
            case R.id.BtnCancel:
                Toast.makeText(this, "Cancel: Ο φάκελος είναι:  " + selectedFilePath + ", και το αρχείο: " + selectedFileName, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
