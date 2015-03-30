package com.example.kavitha.simpletodo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements TextWatcher {

    private ListView lvItems;
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    Button btnAddItems;
    EditText etNewItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddItems = (Button) findViewById(R.id.btnAddItems);
        etNewItem = (EditText) findViewById(R.id.eTNewItem);
        etNewItem.addTextChangedListener(this);
        lvItems = (ListView) findViewById(R.id.lvItems);
        items = new ArrayList<String>();
        readItems();
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        setupListViewListener();
    }

    public void setupListViewListener() {
        lvItems.setOnItemLongClickListener(new
            AdapterView.OnItemLongClickListener() {
                @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                    items.remove(pos);
                    itemsAdapter.notifyDataSetChanged();
                    writeItems();
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
                etEditItem.setText(items.get(i));
                builder.setView(dialogView)
                    .setPositiveButton(getString(R.string.btn_save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // save edited value
                            items.set(i, etEditItem.getText().toString());
                            writeItems();
                            itemsAdapter.notifyDataSetChanged();
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

    public void onAddItem(View v){

        String newTodoItem = etNewItem.getText().toString().trim();
        if (newTodoItem.length() > 0) {
            itemsAdapter.add(newTodoItem);
            writeItems();
        }
        etNewItem.setText("");
    }

    public void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        }
        catch(IOException e) {
            items = new ArrayList<String>();
        }
    }

    public void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            FileUtils.writeLines(todoFile, items);
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
}
