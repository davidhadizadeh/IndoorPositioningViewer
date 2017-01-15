package de.hadizadeh.positioning.viewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import de.hadizadeh.positioning.controller.MappedPositionListener;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.model.SignalInformation;
import de.hadizadeh.positioning.roommodel.android.*;
import de.hadizadeh.positioning.roommodel.android.technologies.BluetoothLeDevice;
import de.hadizadeh.positioning.roommodel.android.technologies.BluetoothLeProximityTechnology;
import de.hadizadeh.positioning.roommodel.android.technologies.BluetoothLeTechnology;
import de.hadizadeh.positioning.viewer.content.ContentFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Manages the menu and the displayed fragments and menu options
 */
public class MainActivity extends PositioningActivity implements MappedPositionListener {
    private ListView menuLv;
    private ArrayAdapter<MenuItem> menuAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private boolean drawerOpen;
    private boolean replaceFragment;
    private TextView titleTv;
    private ImageView gearIv;

    private ViewerMapFragment autoPositionFragment;
    private ViewerMapFragment discoverMapFragment;
    private ExploreContentFragment exploreContentFragment;
    private ScanningFragment scanningFragment;

    private boolean loadClosestContent;
    private MappingPoint lastPosition;
    private String language = Locale.getDefault().getLanguage();

    private boolean autoWifiConnection;

    private MenuItem debugMenuItem;

    private BluetoothLeTechnology btleTechnology;
    private boolean bluetoothManuallyEnabled;

    /**
     * Initializes the view elements and loads the room model and content data
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        connectToWifi();
        String[] materialNames = getResources().getStringArray(R.array.material_names);

        SharedPreferences sharedPref = getSharedPreferences(Settings.PREFERENCE_FILE, Context.MODE_PRIVATE);
        //if(sharedPref.getString(Settings.WEBSERVICE, null) == null) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Settings.WEBSERVICE, getString(R.string.settings_default_webservice));
        editor.putString(Settings.PROJECT, getString(R.string.settings_default_project));
        editor.commit();
        //}

        setData(getString(R.string.settings_main_data_path), materialNames, R.id.activity_main_fragment_fl,
                getString(R.string.opening_data), getString(R.string.enable_location_service),
                getString(R.string.enable_location_service_yes), getString(R.string.enable_location_service_no),
                getString(R.string.confirmation), getString(R.string.downloading_data), getString(R.string.question_confirm_download));
        //addTechnology(new CompassTechnology(this, "COMPASS", 80));

        //btleTechnology = new BluetoothLeStrengthTechnology("BLUETOOTH_LE_STRENGTH", 3000, null);
        btleTechnology = new BluetoothLeProximityTechnology("BLUETOOTH_LE_PROXIMITY", new ArrayList<String>(), 3000);
        //btleTechnology = new BluetoothLeProximityCategoryTechnology("BLUETOOTH_LE_PROXIMITY_CATEGORY", new ArrayList<String>(), 3000);
        //btleTechnology.setMatcher(new OrderMatcher());
        bluetoothManuallyEnabled = btleTechnology.enableHardware();
        //addTechnology(btleTechnology);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(this, getString(R.string.menu_explore_map), R.drawable.ic_action_map));
        menuItems.add(new MenuItem(this, getString(R.string.menu_auto_position), R.drawable.ic_action_make_available_offline));
        menuItems.add(new MenuItem(this, getString(R.string.menu_auto_content), R.drawable.ic_action_play_over_video));
        menuItems.add(new MenuItem(this, getString(R.string.menu_explore_content), R.drawable.ic_action_storage));

        //menuItems.add(new MenuItem(this, "DEBUG_SIGNAL_DATA", R.drawable.ic_action_storage));
        //debugMenuItem = menuItems.get(menuItems.size() - 1);

        autoPositionFragment = new ViewerMapFragment();
        autoPositionFragment.setFollowPosition(true);
        discoverMapFragment = new ViewerMapFragment();
        discoverMapFragment.setFollowPosition(false);
        exploreContentFragment = new ExploreContentFragment();
        scanningFragment = new ScanningFragment();

        if (savedInstanceState == null) {
            replaceFragment = true;
        } else {
            lastPosition = new MappingPoint(savedInstanceState.getInt(getString(R.string.extra_last_x)),
                    savedInstanceState.getInt(getString(R.string.extra_last_y)), savedInstanceState.getInt(getString(R.string.extyra_last_z)));
            Fragment currentFragment = fragmentManager.findFragmentById(layoutIdfragmentBox);
            if (currentFragment instanceof MapFragment) {
                ((MapFragment) currentFragment).setData(viewerMap, getString(R.string.floor));
            } else if (currentFragment instanceof LoadingFragment) {
                replaceFragment = true;
            }
        }

        if (viewerMap != null && savedInstanceState == null) {
            loadDiscoverMapFragment();
        }

        titleTv = (TextView) findViewById(R.id.activity_main_title_tv);
        gearIv = (ImageView) findViewById(R.id.activity_main_gear_iv);
        menuLv = (ListView) findViewById(R.id.activity_main_menu_lv);
        menuAdapter = new MenuListAdapter(this, R.layout.menu_item, menuItems.toArray(new MenuItem[menuItems.size()]), R.id.menu_item_tv, R.id.menu_item_iv);
        menuLv.setAdapter(menuAdapter);
        menuLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadClosestContent = false;
                if (id == 0) {
                    loadDiscoverMapFragment();
                } else if (id == 1) {
                    autoPositionFragment.setData(viewerMap, getString(R.string.floor));
                    replaceFragment(autoPositionFragment);
                    if (lastPosition != null) {
                        autoPositionFragment.positionReceived(lastPosition);
                    }
                } else if (id == 2) {
                    loadClosestContent = true;
                    replaceFragment(scanningFragment);
                    if (lastPosition != null) {
                        loadClosestContent(lastPosition);
                    }
                } else if (id == 3) {
                    exploreContentFragment.setData(contentController.getContents(language), lastPosition);
                    replaceFragment(exploreContentFragment);
                }
                titleTv.setText(((TextView) view.findViewById(R.id.menu_item_tv)).getText());
                if (!(fragmentManager.findFragmentById(layoutIdfragmentBox) instanceof LoadingFragment)) {
                    try {
                        fragmentManager.popBackStackImmediate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                drawerLayout.closeDrawers();
            }
        });

        addMenu();
    }

    private void loadDiscoverMapFragment() {
        discoverMapFragment.setData(viewerMap, getString(R.string.floor));
        if (lastPosition != null) {
            int floor = lastPosition.getZ() / viewerMap.getFloorHeight();
            discoverMapFragment.changeFloor(floor);
            discoverMapFragment.positionReceived(lastPosition);
        }
        replaceFragment(discoverMapFragment);
    }

    /**
     * Removes the position listener, if the activity stops
     */
    @Override
    protected void onStop() {
        if (mappedPositionManager != null) {
            mappedPositionManager.removePositionListener(this);
        }
        super.onStop();
    }

    /**
     * Disables the positioning hardware if the app shuts down and the hardware has been automatically turned on
     */
    @Override
    protected void onDestroy() {
        if (bluetoothManuallyEnabled) {
            btleTechnology.disableHardware();
        }
        super.onDestroy();
    }

    /**
     * Closes the drawer when back pressed or closes the app if the drawer was not open
     */
    @Override
    public void onBackPressed() {
        if (drawerOpen) {
            drawerLayout.closeDrawers();
        } else {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * Saves the state of the last position
     *
     * @param outState out state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (lastPosition != null) {
            outState.putInt(getString(R.string.extra_last_x), lastPosition.getX());
            outState.putInt(getString(R.string.extra_last_y), lastPosition.getY());
            outState.putInt(getString(R.string.extyra_last_z), lastPosition.getZ());
        }
        super.onSaveInstanceState(outState);
    }

    private void addMenu() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_gray));
        }

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.activity_main_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                drawerOpen = false;
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                drawerOpen = true;
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    /**
     * Sets the initialize fragment after data have been loaded
     */
    @Override
    protected void dataLoaded() {
        super.dataLoaded();
        if (replaceFragment) {
            loadDiscoverMapFragment();
            downloadPositioningFile(false);
            disconnectWifi();
        }
        if (!mappedPositionManager.isPositioningStarted()) {
            mappedPositionManager.registerPositionListener(this);
            mappedPositionManager.startPositioning(2000);
            //positionReceived(new MappingPoint(5, 16, 0));
        }
    }

    private void presentationSleep(long timeToStay) {
        try {
            Thread.sleep(timeToStay);
        } catch (InterruptedException e) {
        }
    }

    private void presentationOpenContent(final int contentId) {
        menuLv.post(new Runnable() {
            public void run() {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getBackStackEntryCount() <= 1) {
                    ContentFragment currentContentFragment = ContentFragment.createContentFragment(contentController.getContents("de").get(contentId));
                    currentContentFragment.disableScanning();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.activity_main_fragment_fl, currentContentFragment, null);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    fragmentManager.executePendingTransactions();
                }
            }
        });
    }

    private void presentationCloseContent() {
        menuLv.post(new Runnable() {
            public void run() {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                }
            }
        });
    }

    private void presentationMove(final double x, final double y, long timeToStay) {
        try {
            menuLv.post(new Runnable() {
                public void run() {
                    positionReceived(new MappingPoint((int) (x * 2), (int) (58.0 - (2 * y)), 0));
                }
            });

            Thread.sleep(timeToStay);
        } catch (InterruptedException e) {
        }
    }

    private void presentaionScroll(final double x, final double y) {
        menuLv.post(new Runnable() {
            public void run() {
                discoverMapFragment.scrollToPosition(new MappingPoint((int) (x * 2), (int) (58.0 - (2 * y)), 0));
            }
        });
    }

    private void loadClosestContent(MappingPoint mappingPoint) {
        ContentFragment contentFragment = ContentFragment.createContentFragment(contentController.getContents(language), mappingPoint);
        if (contentFragment != null) {
            contentFragment.enableScanning();
            replaceFragment(contentFragment);
        }
    }

    /**
     * Forwards the hardware button presses to the current content fragment
     *
     * @param event key event
     * @return true, if the event was handled, else it was not
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean exploreResult = false;
        boolean contentResult = false;
        Fragment currentFragment = fragmentManager.findFragmentById(layoutIdfragmentBox);
        if (currentFragment instanceof ExploreContentFragment) {
            exploreResult = ((ExploreContentFragment) currentFragment).dispatchKeyEvent(event);
        } else if (currentFragment instanceof ContentFragment) {
            contentResult = ((ContentFragment) currentFragment).dispatchKeyEvent(event);
        }
        if (exploreResult || contentResult) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    /**
     * Notifies the current fragment, that a new position has been received
     *
     * @param mappingPoint new position
     */
    @Override
    public void positionReceived(MappingPoint mappingPoint) {
        System.out.println(mappingPoint);
        lastPosition = mappingPoint;
        if (loadClosestContent) {
            loadClosestContent(mappingPoint);
        } else {
            Fragment currentFragment = fragmentManager.findFragmentById(layoutIdfragmentBox);
            if (currentFragment instanceof MappedPositionListener) {
                ((MappedPositionListener) currentFragment).positionReceived(mappingPoint);
            } else if (currentFragment instanceof LoadingFragment) {
                loadDiscoverMapFragment();
            }
        }
    }

    /**
     * Notifies the current fragment, that a new position has been received
     *
     * @param list new positions
     */
    @Override
    public void positionReceived(List<MappingPoint> list) {

    }

    private void disconnectWifi() {
        if (autoWifiConnection) {
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
        }
    }

    private void connectToWifi() {
        String ssid = getString(R.string.settings_default_wifi_ssid);
        if (!"".equals(ssid)) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!wifiInfo.isConnected()) {
                WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                autoWifiConnection = true;
                connectToWifi(ssid, getString(R.string.settings_default_wifi_password));
            }
        }
    }

    private void connectToWifi(String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }
}
