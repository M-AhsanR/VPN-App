package com.watchmyapps.freevpn.ui.activities;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jwang123.flagkit.FlagKit;
import com.watchmyapps.freevpn.R;
import com.watchmyapps.freevpn.ui.adapters.ServerListAdapter;
import com.watchmyapps.freevpn.localdata.model.Server;

import java.util.List;

import de.blinkt.openvpn.core.VpnStatus;

public class ServersListActivity extends BaseActivity {
    private ListView listView;
    private ServerListAdapter serverListAdapter;
    ImageView flagImage;
    TextView countryName;
    TextView serverCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers_list);

        if (!VpnStatus.isVPNActive())
            connectedServer = null;

        listView = findViewById(R.id.list);
        flagImage = findViewById(R.id.flagImage);
        countryName = findViewById(R.id.countryName);
        serverCount = findViewById(R.id.serverCount);

    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();

        buildList();
    }

    @Override
    protected void ipInfoResult() {
        serverListAdapter.notifyDataSetChanged();
    }

    private void buildList() {
        String country = getIntent().getStringExtra(MainActivity.EXTRA_COUNTRY);
        countryName.setText(country);
        String countryCode = getIntent().getStringExtra(MainActivity.EXTRA_COUNTRY_CODE);
        flagImage.setImageDrawable(FlagKit.drawableWithFlag(getApplicationContext(), countryCode.toLowerCase()));
        final List<Server> serverList = dataBaseHelper.getServersFromCountry(countryCode);
        serverCount.setText(serverList.size()+" Servers");
        serverListAdapter = new ServerListAdapter(this, serverList);

        listView.setAdapter(serverListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Server server = serverList.get(position);
                Intent intent = new Intent(ServersListActivity.this, ServerActivity.class);
                intent.putExtra(Server.class.getCanonicalName(), server);
                ServersListActivity.this.startActivity(intent);
            }
        });

        getIpInfo(serverList);
    }
}