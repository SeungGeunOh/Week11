package com.example.oh.week11;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    DatePicker datePicker;
    EditText memo;
    LinearLayout linearLayout1, linearLayout2;
    ListView listView;
    ArrayList<String> diaryList = new ArrayList<>();
    int index = 0;
    int num;
    ArrayAdapter adapter;
    TextView tvCount;
    Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("내맘대로 메모장");
        listView = (ListView)findViewById(R.id.listview);
        linearLayout1 = (LinearLayout)findViewById(R.id.linear1);
        linearLayout2 = (LinearLayout)findViewById(R.id.linear2);
        datePicker = (DatePicker)findViewById(R.id.date);
        memo = (EditText)findViewById(R.id.memo);
        tvCount = (TextView)findViewById(R.id.tvCount);
        save = (Button)findViewById(R.id.btnsave);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, diaryList);
        setPermission();
        createDirectory();
        allFile();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    save.setText("수정");
                    String path = getExternalPath();
                    BufferedReader br = new BufferedReader(new FileReader(path + "diary/" + diaryList.get(position)));
                    num = position;
                    String readStr = "";
                    String str = null;
                    while ((str = br.readLine()) != null)
                        readStr += str + " \n";
                    br.close();
                    memo.setText(readStr.substring(0, readStr.length() - 1));
                    linearLayout1.setVisibility(View.INVISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);

                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                num = position;
                dlg.setTitle("정말로")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(diaryList.get(num) + "를 삭제 하시겠습니까 ?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String path = getExternalPath();
                                File file = new File(path + "diary/" + diaryList.get(num));
                                file.delete();
                                diaryList.remove(num);
                                adapter.notifyDataSetChanged();
                                index--;
                                tvCount.setText("등록된 메모 개수: " + Integer.toString(index));
                                Toast.makeText(getApplicationContext(), "삭제 되었습니다.",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("아니요", null)
                        .show();
                return false;
            }
        });
    }
    private void allFile(){
        String path2 = getExternalPath();
        File[] files = new File(path2 + "diary").listFiles();

        String str = "";
        for (File f : files) {
            diaryList.add(f.getName());
            index++;
        }
        listView.setAdapter(adapter);
        tvCount.setText("등록된 메모 개수: " + Integer.toString(index));
    }

    private void createDirectory(){
        String path1 = getExternalPath();
        File file = new File(path1 + "diary");
        if (!file.isDirectory())
            file.mkdir();
    }

    private void getDatePicker(){
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd");

        String str = simpleDateFormat.format(new Date(year, month, day));
        diaryList.add(str + ".memo");
    }
    private void setPermission(){
        int permissionInfo = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionInfo == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(getApplicationContext(), "SDCard 쓰기 권한 있음", Toast.LENGTH_SHORT).show();
        else { // 재요청
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                Toast.makeText(getApplicationContext(), "권한의 필요성 설명", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }
    @Override // 안써도 상관없음
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String str = null;

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                str = "SD Card 쓰기권한 승인";
            else
                str = "SD Card 쓰기권한 거부";
            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        }
    }

    public String getExternalPath(){
        String sdPath = "";
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED))
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        else{
            sdPath = getFilesDir() + "";
            Toast.makeText(getApplicationContext(), sdPath, Toast.LENGTH_SHORT).show();
        }
        return sdPath;
    }
    public void onClick(View v){
        if (v.getId() == R.id.btnsave){ // 저장
            try{
                if (save.getText().toString() == "수정"){
                    String path = getExternalPath();
                    File file = new File(path + "diary/" + diaryList.get(num));
                    file.delete();
                    diaryList.remove(num);
                    adapter.notifyDataSetChanged();
                    index--;
                }
                getDatePicker();
                String path = getExternalPath();
                BufferedWriter bw = new BufferedWriter(new FileWriter(path + "diary/" + diaryList.get(index), true));
                index++;
                bw.write(memo.getText().toString());
                bw.close();
                listView.setAdapter(adapter);
                tvCount.setText("등록된 메모 개수: " + Integer.toString(index));
                Toast.makeText(getApplicationContext(), "저장완료", Toast.LENGTH_SHORT).show();
                save.setText("저장");
                memo.setText("");
            }
            catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage() + " : " + getFilesDir(), Toast.LENGTH_SHORT).show();
            }
        }
        else if (v.getId() == R.id.btncancel){ // 취소
            Collections.sort(diaryList, dataAsc);
            memo.setText("");
            adapter.notifyDataSetChanged();
            linearLayout1.setVisibility(View.VISIBLE);
            linearLayout2.setVisibility(View.INVISIBLE);
        }
        else if (v.getId() == R.id.btn1){ // 일기등록
            save.setText("저장");
            linearLayout1.setVisibility(View.INVISIBLE);
            linearLayout2.setVisibility(View.VISIBLE);
        }
    }

    Comparator<String> dataAsc = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    };
}
