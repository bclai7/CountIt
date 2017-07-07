package com.takezeroapps.countit;

import java.util.ArrayList;
import  java.util.List;

import  android.content.Context;
import android.util.Log;
import  android.util.SparseBooleanArray;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;
import  android.widget.ImageView;
import  android.widget.TextView;

public class  MultiCounterListViewAdapter extends ArrayAdapter<String> implements Filterable {
    Context myContext;
    LayoutInflater inflater;
    List<String> DataList;
    private  SparseBooleanArray mSelectedItemsIds;
    int rid;

    // Constructor for get Context and  list
    public  MultiCounterListViewAdapter(Context context, int resourceId,  List<String> lists) {
        super(context,  resourceId, lists);
        mSelectedItemsIds = new  SparseBooleanArray();
        myContext = context;
        DataList = lists;
        inflater =  LayoutInflater.from(context);
        rid=resourceId;
    }

    // Container Class for item
    private class ViewHolderA {
        TextView tvTitle;
    }
    private class ViewHolderB{
        CheckedTextView ctv;
    }

    public View getView(int position,  View view, ViewGroup parent) {
        ViewHolderA  holderA;
        ViewHolderB  holderB;

        holderA = new ViewHolderA();
        holderB = new ViewHolderB();

        if (view == null) {

            if(rid == R.layout.mcounters_text_format) {
                view = inflater.inflate(R.layout.mcounters_text_format, null);
                holderA.tvTitle = (TextView) view.findViewById(R.id.textview);
                view.setTag(holderA);
            }
            else if(rid==android.R.layout.simple_list_item_multiple_choice)
            {
                view = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);
                //CheckedTextView ctv = (CheckedTextView)findViewById(android.R.id.text1);
                holderB.ctv = (CheckedTextView)view.findViewById(android.R.id.text1);
                view.setTag(holderB);
            }


        } else {
            if(rid == R.layout.mcounters_text_format) {
                holderA = (ViewHolderA)  view.getTag();
            }
            else if(rid==android.R.layout.simple_list_item_multiple_choice)
            {
                holderB = (ViewHolderB) view.getTag();
            }
        }
        if(rid == R.layout.mcounters_text_format) {
            holderA.tvTitle.setText(DataList.get(position-1).toString());
        }
        else if(rid==android.R.layout.simple_list_item_multiple_choice)
        {
            holderB.ctv.setText(DataList.get(position).toString());
        }

        return view;
    }

    @Override
    public void remove(String  object) {
        DataList.remove(object);
        notifyDataSetChanged();
    }

    // get List after update or delete
    public  List<String> getMyList() {
        return DataList;
    }

    public void  toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position,  value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public  SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public List<String> orig=null;

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<String> results = new ArrayList<String>();
                if (orig == null)
                    orig = DataList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final String g : orig) {
                            if (g.toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults (CharSequence constraint,
                    FilterResults results){

                DataList = (List<String>) results.values;

                notifyDataSetChanged();
            }

        };
    }
}