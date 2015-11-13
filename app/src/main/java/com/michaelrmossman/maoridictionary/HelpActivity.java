package com.michaelrmossman.maoridictionary;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.idunnololz.widgets.AnimatedExpandableListView;
import com.idunnololz.widgets.AnimatedExpandableListView.AnimatedExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HelpActivity extends AppCompatActivity {
    private AnimatedExpandableListView listView;
    private Button expandAll;
    private Boolean isExpanded;
    private Integer numVars;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        expandAll = (Button) findViewById(R.id.expand_all_button);
        /* Number of NORMAL help items, as opposed to the number of
           LANGUAGE-CONTEXT help items, which is currently 31. Note :
           This is an EXACT count; all for loops are based on <= numVars */
        Integer defaults = 10; // Number of normal-help entries in values/help_* files
        isExpanded = false;

        Bundle b = getIntent().getExtras();
        String whichList = b.getString("whichList", "help"); // Defaults to NORMAL help/usage
        if (whichList.equals("help")) {
            numVars = defaults;
        } else {
            numVars = 31; // Number of language-context entries in values/context_* files
        }
        String myGroup, myChild, myHint;
        if (Objects.equals(numVars, defaults)) {
            myGroup = "help_title_";
            myChild = "help_text_";
            myHint = "help_hint_";
        } else {
            myGroup = "context_title_";
            myChild = "context_text_";
            myHint = "context_hint_";
            android.support.v7.app.ActionBar myAB = getSupportActionBar();
            if (myAB != null) myAB.setTitle(getString(R.string.context_title));
        }

        String thisGroup, thisChild, thisHint;
        List<GroupItem> items = new ArrayList<>();
        // Populate our list with groups and it's child(ren)
        for(int i = 1; i <= numVars; i++) {
            GroupItem item = new GroupItem();
            thisGroup = myGroup + i;
            item.title = getString(getResources().getIdentifier(thisGroup, "string", this.getPackageName()));

            ChildItem child = new ChildItem();
            thisChild = myChild + i;
            child.title = getString(getResources().getIdentifier(thisChild, "string", this.getPackageName()));
            thisHint = myHint + i;
            child.hint = getString(getResources().getIdentifier(thisHint, "string", this.getPackageName()));
            item.items.add(child);
            items.add(item);
        }

        ExampleAdapter adapter = new ExampleAdapter(this);
        adapter.setData(items);
        listView = (AnimatedExpandableListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        // In order to show animations, we need to use a custom click handler
        // for our ExpandableListView.
        listView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // We call collapseGroupWithAnimation(int) and
                // expandGroupWithAnimation(int) to animate group
                // expansion/collapse.
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }
                return true;
            }
        });
    }

    // Added by MM
    public void expandAll(View view) {
        try {
            if (!isExpanded) {
                for(int i = 0; i <= numVars; i++) {
                    if (!listView.isGroupExpanded(i)) listView.expandGroupWithAnimation(i);
                }
                expandAll.setText(getString(R.string.help_back_to_top));
                isExpanded = true;
            }
            listView.setSelection(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                listView.setSelectionFromTop(0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Added by MM
    public void collapseAll(View view) {
        try {
            for(int i = 0; i <= numVars; i++) {
                if (listView.isGroupExpanded(i)) listView.collapseGroupWithAnimation(i);
            }
            expandAll.setText(getString(R.string.help_expand_all));
            isExpanded = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class GroupItem {
        String title;
        List<ChildItem> items = new ArrayList<>();
    }

    private static class GroupHolder {
        TextView title;
    }

    private static class ChildItem {
        String title;
        String hint;
    }

    private static class ChildHolder {
        TextView title;
        TextView hint;
    }

    /**
     * Adapter for our list of {@link GroupItem}s.
     */
    private class ExampleAdapter extends AnimatedExpandableListAdapter {
        private LayoutInflater inflater;

        private List<GroupItem> items;

        public ExampleAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            GroupItem item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }
            holder.title.setText(item.title);
            return convertView;
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            ChildItem item = getChild(groupPosition, childPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                holder.hint = (TextView) convertView.findViewById(R.id.textHint);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }
            holder.title.setText(item.title);
            holder.hint.setText(item.hint);
            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

    }

}
