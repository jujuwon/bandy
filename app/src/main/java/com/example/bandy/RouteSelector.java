package com.example.bandy;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RouteSelector extends AppCompatActivity {

    private final String key = "-";
    private List<String> list;
    private ListView listView;

    private ArrayList<String> arrayList;
    private int eventType;
    Resources res;
    PreparationAdapter mAdapter;
    ArrayList<PreparationItem> items;
    private String selectedNodeId;
    private String selectedNodeName;
    private Integer searchedRouteCount;

    private ArrayList<String> searchedRouteIdList = new ArrayList<String>();
    private ArrayList<String> searchedRouteNameList = new ArrayList<String>();

    public ArrayList<String> selectedRouteIdList;
    public ArrayList<String> selectedRouteNameList;
    public int selectedRouteCount = 0;

    private myDBHelper busHelper = new myDBHelper(this);;
    SQLiteDatabase bandy;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Window Blur
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.0f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.activity_route_selector);

        //Window Title
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //WindowSize
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.9); // Display ???????????? 90%
        int height = (int) (dm.heightPixels * 0.6);  // Display ???????????? 90%
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        //????????? ?????? ??????
        Intent node = getIntent();
        selectedNodeId = node.getStringExtra("nodeid");
        bandy = busHelper.getReadableDatabase();

        //????????? ????????? ???????????? ?????? ?????? ????????????
        cursor = bandy.rawQuery("Select nodeName, routeId, routeName from RouteInNode where nodeid = ?;",new String[] {selectedNodeId});
        cursor.moveToFirst();
        searchedRouteCount = cursor.getCount();
        selectedNodeName = cursor.getString(0);

        //????????? ?????? ?????? ??????
        searchedRouteIdList.clear();
        searchedRouteNameList.clear();

        for(int i = 0; i < cursor.getCount();i++){
            searchedRouteIdList.add(cursor.getString(1));
            searchedRouteNameList.add(cursor.getString(2));
            Log.d("id",searchedRouteIdList.get(i));
            Log.d("name",searchedRouteNameList.get(i));
            cursor.moveToNext();
        }


        //????????? ????????? ????????? ?????? ????????? ?????????
        initItem();
        listView = findViewById(R.id.bus_listview);
        mAdapter = new PreparationAdapter(items);
        listView.setAdapter(mAdapter);
    }

    private void initItem(){
        items = new ArrayList<PreparationItem>();
        for(int i = 0; i < searchedRouteCount;i++){
            String sid = searchedRouteIdList.get(i);
            String snm = searchedRouteNameList.get(i);
            boolean b = false;
            PreparationItem item = new PreparationItem(b, sid, snm);
            items.add(item);
        }
    }

    //?????? ?????? ??? ????????????????????? ??????
    public void OnClick(View v){

        //????????? ?????? ??????
        selectedRouteNameList = new ArrayList<String>();
        selectedRouteIdList = new ArrayList<String>();

        if (mAdapter.getCheckedCount() > 2) {
            Toast.makeText(getApplicationContext(), "????????? 2????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
        } else {
            for(int i = 0; i < searchedRouteCount;i++) {
                //????????? ?????? ??? ??????????????? ????????? ????????? ???????????? ??????.
                if(mAdapter.isChecked(i)){
                    PreparationItem saver = (PreparationItem) mAdapter.getItem(i);
                    selectedRouteNameList.add(saver.ItemStringname);
                    selectedRouteIdList.add(saver.ItemStringid);
                }
            }

            for(int i = 0;i < selectedRouteIdList.size();i++){
                Log.d("selected1", selectedRouteIdList.get(i));
                Log.d("selected2", selectedRouteNameList.get(i));
            }

            //?????????????????? ????????? ????????? id, ???????????? ????????????
            Intent intent = new Intent(this, SettingActivity.class);
            intent.putExtra("RouteId_List", selectedRouteIdList);
            intent.putExtra("RouteName_List",selectedRouteNameList);

            for(int i = 0; i< selectedRouteIdList.size();i++){
                Log.d("before",selectedRouteIdList.get(i));
                Log.d("before",selectedRouteNameList.get(i));
            }
            setResult(RESULT_OK, intent);
            bandy.close();
            finish();
        }
    }
    public void OnClose(View v){
        selectedRouteNameList = new ArrayList<String>();
        selectedRouteIdList = new ArrayList<String>();
        bandy.close();

        //????????????(??????) ??????
        finish();
    }

    public class myDBHelper extends SQLiteOpenHelper {
        @Override
        public void onCreate(SQLiteDatabase db) {
            String createNoticeQuery = "CREATE TABLE IF NOT EXISTS Notice(notiId integer primary key AUTOINCREMENT, notiName text, nodeId text,notiTime int ,startAt text, endAt text, days integer,  isOn integer(1));";
            String createRouteInNoticeQuery = "CREATE TABLE IF NOT EXISTS RouteInNotice(id integer primary key AUTOINCREMENT, notiId integer, routeID text, routeName text, foreign key(notiId) references Notice(notiId));";
            db.execSQL(createNoticeQuery);
            db.execSQL(createRouteInNoticeQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        }

        public myDBHelper(Context context){
            super(context, "bandy",null,1);
        }
    }
}