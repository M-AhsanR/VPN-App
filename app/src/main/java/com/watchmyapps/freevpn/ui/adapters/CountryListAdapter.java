package com.watchmyapps.freevpn.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jwang123.flagkit.FlagKit;
import com.watchmyapps.freevpn.R;

import java.util.List;

public class CountryListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<String> countryList;
    private List<String> countryCodeList;
    private Context context;

    public CountryListAdapter(Context c, List<String> countryList, List<String> countryCodeList) {
        inflater = LayoutInflater.from(c);
        context = c;
        this.countryList = countryList;
        this.countryCodeList = countryCodeList;
    }


    @Override
    public int getCount() {
        return countryList.size();
    }

    @Override
    public String getItem(int position) {
        return countryList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {

        v = inflater.inflate(R.layout.list_country_popup, parent, false);
        ImageView countryFlag = v.findViewById(R.id.countryFlag);
        TextView countryName = v.findViewById(R.id.countryName);
        TextView countryCode = v.findViewById(R.id.countryCode);
        if (countryCodeList != null && countryCodeList.size() > 0) {
            if (countryCodeList.get(position) != null && !countryCodeList.get(position).toLowerCase().equals("ps")) {
                countryCode.setText(countryCodeList.get(position));
                try {
                    countryFlag.setImageDrawable(FlagKit.drawableWithFlag(context, countryCodeList.get(position).toLowerCase()));
                } catch (Resources.NotFoundException e) {
                    Log.i("RESOURCE_ERROR", e.getLocalizedMessage());
                }
            }
        }
        countryName.setText(countryList.get(position));
        return v;
    }
}
