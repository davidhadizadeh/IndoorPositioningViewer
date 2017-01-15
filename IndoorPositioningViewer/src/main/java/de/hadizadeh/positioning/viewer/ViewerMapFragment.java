package de.hadizadeh.positioning.viewer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.hadizadeh.positioning.controller.MappedPositionListener;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.android.MapFragment;
import de.hadizadeh.positioning.roommodel.android.ViewerMapSegment;
import de.hadizadeh.positioning.roommodel.model.ContentElement;
import de.hadizadeh.positioning.viewer.content.ContentFragment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying a map for the room model, without lines and without popup information
 */
public class ViewerMapFragment extends MapFragment implements MappedPositionListener {
    protected TextView floorTv;
    private PositionHandler positionHandler;
    private boolean followPosition;
    private float scrolledX;
    private float scrolledY;
    private MappingPoint personPosition;

    private boolean debugLoggingEnabled;
    private boolean debugFlagCorrect;
    private List<Boolean> debugLoggingData;

    /**
     * Sets the mode if the focus of the map should automatically follow the current position
     *
     * @param followPosition true, if the position should be set as focus, else it will not be
     */
    public void setFollowPosition(boolean followPosition) {
        this.followPosition = followPosition;
    }

    /**
     * Returns if the focus of the map will automatically follow the current position
     *
     * @return true, if the position will be set as focus, else it will not be
     */
    public boolean getFollowPosition() {
        return this.followPosition;
    }

    /**
     * Creates the fragment view
     *
     * @param inflater           layout inflater
     * @param container          view contrainer
     * @param savedInstanceState saved instances
     * @return created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            followPosition = savedInstanceState.getBoolean(getString(R.string.extra_follow_position));
        }
        if (followPosition) {
            super.setLayoutIds(R.layout.fragment_map_view, R.id.fragment_map_canvas_box, -1);
        } else {
            super.setLayoutIds(R.layout.fragment_map, R.id.fragment_map_canvas_box, R.id.fragment_map_floor_sp);
        }
        View view = super.onCreateView(inflater, container, savedInstanceState);
        floorTv = (TextView) view.findViewById(R.id.fragment_map_floor_tv);
        ViewerMapSegment.disableDrawLines();

        if (followPosition) {
            changeFloor(0);
        }
        canvasBox.setScale(canvasBox.getScale() / 2);

        //startTesting(view);

        return view;
    }

    private void startTesting(View view) {
        debugLoggingData = new ArrayList<Boolean>();
        LinearLayout buttonsLl = (LinearLayout) view.findViewById(R.id.fragment_map_buttons_ll);
        buttonsLl.setVisibility(View.VISIBLE);
        if (buttonsLl.getVisibility() == View.VISIBLE) {
            debugFlagCorrect = true;
            debugLoggingEnabled = true;
            Button correctBtn = (Button) view.findViewById(R.id.fragment_map_correct_btn);
            Button wrongBtn = (Button) view.findViewById(R.id.fragment_map_wrong_btn);

            correctBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    debugFlagCorrect = true;
                }
            });

            wrongBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    debugFlagCorrect = false;
                }
            });

            wrongBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    debugLoggingEnabled = false;
                    Toast.makeText(getActivity(), "Testing stopped.", Toast.LENGTH_LONG).show();
                    return false;
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (debugLoggingEnabled) {
                            debugLoggingData.add(debugFlagCorrect);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    String text = "Time;";
                    for (int i = 1; i <= debugLoggingData.size(); i++) {
                        text += i + ";";
                    }
                    text += "\nStatus:;";
                    for (Boolean data : debugLoggingData) {
                        text += (data ? 1 : 0) + ";";
                    }

                    File file = new File(Environment.getExternalStorageDirectory(), "testingData.csv");
                    try {
                        FileWriter fileWriter = new FileWriter(file);
                        BufferedWriter out = new BufferedWriter(fileWriter);
                        out.write(text);
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i("Persistence", "File created.");
                }
            }).start();
        }
    }

    /**
     * Saved instance state
     *
     * @param outState out state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(getString(R.string.extra_follow_position), followPosition);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when a new position has been received and sends it to the handler
     *
     * @param mappingPoint new position
     */
    @Override
    public void positionReceived(MappingPoint mappingPoint) {
        if (positionHandler == null) {
            positionHandler = new PositionHandler(this);
        }
        Message message = positionHandler.obtainMessage();
        message.obj = mappingPoint;
        positionHandler.sendMessage(message);
    }

    /**
     * Called when a new position has been received
     *
     * @param list new positions
     */
    @Override
    public void positionReceived(List<MappingPoint> list) {

    }

    private void markPerson(MappingPoint mappingPoint) {
        if (viewerMap != null) {
            viewerMap.mark(mappingPoint);
            if (followPosition) {
                personPosition = mappingPoint;
                scrollToPosition(personPosition);
            } else {
                render();
            }
        }
    }

    public void scrollToPosition(MappingPoint mappingPoint) {
        if (mappingPoint != null) {
            int floor = mappingPoint.getZ() / viewerMap.getFloorHeight();
            changeFloor(floor);
            int visibleColumns = calculateVisibleColumns();
            int visibleRows = calculateVisibleRows();
            int x = mappingPoint.getX() - visibleColumns / 2;
            int y = mappingPoint.getY() - visibleRows / 2;
            scrolledX = Math.max(0, Math.min(x, viewerMap.getColumns() - 1 - visibleColumns));
            scrolledY = Math.max(0, Math.min(y, viewerMap.getRows() - 1 - visibleRows));
            canvasBox.setX(scrolledX);
            canvasBox.setY(scrolledY);
            super.onTouchViewChange(scrolledX, scrolledY, canvasBox.getScale());
        }
    }

    /**
     * Changes the current floor
     *
     * @param floor new floor number
     * @return formatted floor text
     */
    public String changeFloor(int floor) {
        currentFloor = floor;
        viewerMap.setCurrentFloor(currentFloor);
        String floorString = (floor + 1) + ". " + floorText;
        if (floorTv != null) {
            floorTv.setText(floorString);
        } else if (floorSp != null) {
            floorSp.setSelection(floor);
        }
        return floorString;
    }

    /**
     * Scrolls to position or zooms in/out
     *
     * @param x     touched x coordinate
     * @param y     touched y coordinate
     * @param scale scale factor
     */
    @Override
    public void onTouchViewChange(float x, float y, float scale) {
        canvasBox.setScale(scale);
        if (followPosition) {
            super.onTouchViewChange(scrolledX, scrolledY, scale);
            scrollToPosition(personPosition);
        } else {
            super.onTouchViewChange(x, y, scale);
        }
    }

    /**
     * Scrolls to position and opens a content element if there is one at the position
     *
     * @param x touched x coordinate
     * @param y touched y coordinate
     */
    @Override
    public void onTouch(float x, float y) {
        super.onTouch(x, y);
        if (x >= 0 && y >= 0 && touchedRow >= 0 && touchedColumn >= 0) {
            ContentElement contentElement = viewerMap.onTouch(x, y).getContent();
            viewerMap.onTouch(-1, -1);
            if (contentElement != null) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getBackStackEntryCount() <= 1) {
                    ContentFragment currentContentFragment = ContentFragment.createContentFragment(contentElement);
                    currentContentFragment.disableScanning();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.activity_main_fragment_fl, currentContentFragment, null);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    fragmentManager.executePendingTransactions();
                }
            }
        }
    }

    /**
     * Sends a message to mark the current position on the map
     */
    static class PositionHandler extends Handler {
        private final ViewerMapFragment fragment;

        PositionHandler(ViewerMapFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void handleMessage(Message msg) {
            fragment.markPerson((MappingPoint) msg.obj);
        }
    }
}
