package com.dsoumis.dbpediavirtualsparqlqueryconstructor.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourcePropertiesExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> properties;
    private final Map<String, List<String>> valuesByProperties;

    public ResourcePropertiesExpandableListAdapter(final Context context, final Map<String, List<String>> valuesByProperties) {
        this.context = context;
        this.properties = new ArrayList<>(valuesByProperties.keySet());
        this.valuesByProperties = valuesByProperties;
    }

    @Override
    public int getGroupCount() {
        return properties.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(valuesByProperties.get(properties.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return properties.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(valuesByProperties.get(properties.get(groupPosition))).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {

        if (view == null) {
            final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.resource_properties_parent_layout, null);
        }

        final TextView propertyView = view.findViewById(R.id.textParent);
        final String property = (String) getGroup(groupPosition);
        propertyView.setText(property.substring(property.lastIndexOf('/') + 1).trim());

        return view;

    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {

        if (view == null) {
            final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.resource_properties_child_layout, null);
        }

        final TextView propertyView = view.findViewById(R.id.textChild);
        final String value = (String) getChild(groupPosition, childPosition);
        propertyView.setText(value.substring(value.lastIndexOf('/') + 1).trim());

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
