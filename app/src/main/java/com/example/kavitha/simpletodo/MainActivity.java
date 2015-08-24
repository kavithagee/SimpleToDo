package com.example.kavitha.simpletodo;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kavitha.simpletodo.DBHelper.TodoValue;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements TextWatcher {

    private ListView lvItems;
    ArrayList<TodoValue> items;
    TodoAdapter itemsAdapter;
    Button btnAddItems;
    EditText etNewItem;
    private DBHelper mydb ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mydb = new DBHelper(this);
        btnAddItems = (Button) findViewById(R.id.btnAddItems);
        etNewItem = (EditText) findViewById(R.id.eTNewItem);
        etNewItem.addTextChangedListener(this);
        lvItems = (ListView) findViewById(R.id.lvItems);
        items = new ArrayList<TodoValue>();

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,
                intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        // Schedule the alarm!
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, sender);

        readItems();
        itemsAdapter = new TodoAdapter(this, R.layout.list_item, items);
        lvItems.setAdapter(itemsAdapter);
        setupListViewListener();
    }

    public void setupListViewListener() {
        lvItems.setOnItemLongClickListener(new
            AdapterView.OnItemLongClickListener() {
                @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                    TodoValue itemToDelete = items.get(pos);;
                    items.remove(pos);
                    itemsAdapter.notifyDataSetChanged();
                    mydb.deleteTodoItem(itemToDelete);
                    return true;
                }
            });



        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.edit_dialog, null);
                final EditText etEditItem = (EditText) dialogView.findViewById(R.id.etEditItem);
                final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radioGroup);
                final RadioButton rb_day = (RadioButton) radioGroup.findViewById(R.id.radioButton1);
                final RadioButton rb_week = (RadioButton) radioGroup.findViewById(R.id.radioButton2);
                final RadioButton rb_month = (RadioButton) radioGroup.findViewById(R.id.radioButton3);
                etEditItem.setText(items.get(i).mItemString);
                switch (items.get(i).mItemType) {
                    case 1:
                        rb_day.setChecked(true);
                        break;
                    case 2:
                        rb_week.setChecked(true);
                        break;
                    case 3:
                        rb_month.setChecked(true);
                        break;
                }
                builder.setView(dialogView)
                        .setPositiveButton(getString(R.string.btn_save), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // save edited value
                                int timeLimitVal = 1;
                                if (rb_week.isChecked()) {
                                    timeLimitVal = 2;
                                } else if (rb_month.isChecked()) {
                                    timeLimitVal = 3;
                                }
                                TodoValue newVal = new TodoValue(etEditItem.getText().toString(), timeLimitVal);
                                TodoValue oldVal = items.get(i);

                                if (mydb.updateTodoItem(oldVal, newVal)) {
                                    items.set(i, newVal);
                                    itemsAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Duplicate item not updated!", Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.create().show();
            }
        });
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

    public void onAddItem(View v) {
        boolean isIinserted = false;
        TodoValue newTodoItem = new TodoValue(etNewItem.getText().toString().trim(), 1);
        if (newTodoItem.mItemString.length() > 0) {
            isIinserted = mydb.insertTodoItem(etNewItem.getText().toString().trim(), 1);
        }
        if(isIinserted) {
            itemsAdapter.add(newTodoItem);
            etNewItem.setText("");
        } else {
            Toast.makeText(getApplicationContext(), "Duplicate item", Toast.LENGTH_SHORT).show();
        }
    }

    public void readItems() {
        try {
            items = mydb.getAllTodoItems("date");
        }
        catch(Exception e) {
            items = new ArrayList<TodoValue>();
        }
    }

    public void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            ArrayList<String> strItems = new ArrayList<String>();
            for(TodoValue item: items) {
                strItems.add(item.mItemString + "AND" + item.mItemType);
            }
            FileUtils.writeLines(todoFile, strItems);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//      Nothing to do
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        btnAddItems.setEnabled(!TextUtils.isEmpty(charSequence));
    }

    @Override
    public void afterTextChanged(Editable editable) {
//      Nothing to do
    }

    private static class TodoAdapter extends ArrayAdapter<TodoValue> {
        private static LayoutInflater inflater = null;

        public TodoAdapter(Context context, int resource, ArrayList<TodoValue> objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.list_item, null);
            TodoValue tVal = getItem(position);
            TextView text = (TextView) vi.findViewById(R.id.itemText);
            text.setText(tVal.mItemString);
            View imgView = (View) vi.findViewById(R.id.itemColor);
            int color = Color.RED;
            if(tVal.mItemType == 2) {
                color = Color.BLUE;
            } else if(tVal.mItemType == 3) {
                color = Color.GREEN;
            }
            imgView.setBackgroundColor(color);
            return vi;
        }
    }
}
