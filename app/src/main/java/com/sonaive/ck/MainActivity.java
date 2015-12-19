package com.sonaive.ck;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.sonaive.toaster.MimosaLayout;
import com.sonaive.toaster.Toaster;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout mRootView;
    private Toaster mToaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ArrayList<String> data = new ArrayList<>();
        for( int i = 0; i < 30; i++) {
            data.add(String.valueOf(i));
        }
        mRootView = (CoordinatorLayout) findViewById(R.id.root_view);
        final MimosaLayout mimosaLayout = (MimosaLayout) findViewById(R.id.mimosa);
        ListView listView = (ListView) findViewById(R.id.list_view);
        EditText editText = (EditText) findViewById(R.id.edit_text);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && !Toaster.isShown()) {
                    mToaster = Toaster.make(mRootView);
                    mimosaLayout.setToaster(mToaster);
                    mToaster.showView();
                }
                return false;
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Toaster.isShown()) {
                mToaster.hideView();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
