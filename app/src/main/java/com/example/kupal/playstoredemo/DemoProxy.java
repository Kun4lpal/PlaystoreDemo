package com.example.kupal.playstoredemo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Locale;

/**
 * Created by kupal on 8/13/2017.
 */

public class DemoProxy extends AccessibilityService {

    //<-------------------------------  private Data Members  ---------------------------------------->

    private FrameLayout overlay;
    private FrameLayout overlay_wish;
    private TextToSpeech mTts;
    private int result;
    private AccessibilityServiceInfo info;
    private WindowManager wm;
    private WindowManager.LayoutParams lp;
    private WindowManager.LayoutParams lp_wish;
    private boolean once;
    private boolean once_wish;
    private ArrayList<AccessibilityNodeInfo> listOfNodes;
    private AccessibilityNodeInfo current_overlay_previous_node;
    private AccessibilityNodeInfo current_overlay_next_node;
    private boolean annotation_overlay_exist = false;
    private boolean accessibility_rating_overlay_exist = false;

    //<-------------------------------  onCreate method  ---------------------------------------->

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //<-------------------------------  override onServiceConnected  ---------------------------------------->

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onServiceConnected() {
        info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        //<-------------------------------  create Text to Speech object  ----------------------------------->

        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    result = mTts.setLanguage(Locale.US);
                } else {
                    Toast.makeText(getApplicationContext(), "Feature not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    //<-------------------------------  override onAccessibilityEvent  ---------------------------------------->

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


        //<-------------------------------  remove overlay after exiting app  ----------------------------->

        if (!event.getPackageName().equals("com.android.vending")) {
            if (overlay != null) {
                wm.removeView(overlay);
                overlay = null;
                once = false;
            }
            if (overlay_wish != null) {
                wm.removeView(overlay_wish);
                overlay_wish = null;
                once_wish = false;
            }
            return;
        }

        //<-------------------------------  test  ---------------------------------------->

        if (event.getPackageName().equals("com.android.systemui")) {
            Toast.makeText(this, "testing UI", Toast.LENGTH_SHORT).show();
            return;
        }

        //<-------------------------------  add overlay to app ---------------------------------------->

        if (event.getPackageName().equals("com.android.vending")) {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                if(event.getText().toString().contains("wishlist")){
                    if(!once_wish){
                        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                        overlay_wish = new FrameLayout(this);
                        lp_wish = new WindowManager.LayoutParams();
                        lp_wish.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                        lp_wish.format = PixelFormat.TRANSLUCENT;
                        lp_wish.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        lp_wish.width = WindowManager.LayoutParams.WRAP_CONTENT;
                        lp_wish.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        lp_wish.gravity = Gravity.TOP;
                        lp_wish.alpha = 100;
                        LayoutInflater inflater = LayoutInflater.from(this);
                        inflater.inflate(R.layout.actionbutton2, overlay_wish);
                        configureWishButton();
                        wm.addView(overlay_wish, lp_wish);
                        once_wish = true;
                    }else{
                        wm.removeView(overlay_wish);
                        overlay_wish = null;
                        once_wish = false;
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser(event.getText().toString());
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser(event.getText().toString());
                }

            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser("Scrolling");
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser("Scrolling");
                }
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED ) {
                final Context context = this;
                if (!once) {
                    wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                    overlay = new FrameLayout(this);
                    lp = new WindowManager.LayoutParams();
                    lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                    lp.format = PixelFormat.TRANSLUCENT;
                    lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    lp.gravity = Gravity.TOP;
                    lp.alpha = 100;
                    LayoutInflater inflater = LayoutInflater.from(this);
                    inflater.inflate(R.layout.actionbutton, overlay);
                    configureSwipeButton();
                    //configureVolumeButton();
                    configureScrollButton();
                    configureRemoveButton();
                    wm.addView(overlay, lp);
                    once = true;
                }

                annotation_overlay_exist = true;

            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                Toast.makeText(this, "test1", Toast.LENGTH_SHORT).show();
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                Toast.makeText(this, "test2", Toast.LENGTH_SHORT).show();
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED) {
                Toast.makeText(this, "test3", Toast.LENGTH_SHORT).show();
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
                Toast.makeText(this, "test4", Toast.LENGTH_SHORT).show();
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SELECTED) {
                Toast.makeText(this, "test5", Toast.LENGTH_SHORT).show();
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
                Toast.makeText(this, "test6", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //<-------------------------------  helper function  ---------------------------------------->

    private void speakToUser(String eventText) {
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, "Feature not supported", Toast.LENGTH_SHORT).show();
        } else {
            if (!eventText.contains("null")) {
                Toast.makeText(this, eventText, Toast.LENGTH_SHORT).show();
                mTts.speak(eventText, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }


    //<-------------------------------  helper  ---------------------------------------->

    private void configureRemoveButton() {
        Button volumeUpButton = (Button) overlay.findViewById(R.id.Remove);
        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakToUser("Remove Overlay");
                if (overlay != null) {
                    wm.removeView(overlay);
                    overlay = null;
                    once = false;
                }

                if(overlay_wish!=null){
                    wm.removeView(overlay_wish);
                    overlay_wish = null;
                    once_wish = false;
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }

    //<-------------------------------  helper  ---------------------------------------->

    private void configureScrollButton() {
        Button scrollButton = (Button) overlay.findViewById(R.id.scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());
                if (scrollable != null) {
                    speakToUser("Scrolling Right!");
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
            }
        });
    }


    private void configureWishButton() {
        Button scrollButton = (Button) overlay_wish.findViewById(R.id.wish);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                speakToUser("Wish Button Clicked");
                TextView textView = (TextView) overlay_wish.findViewById(R.id.textview);
                textView.setText("Text has been added!");
            }
        });

    }


    //<-------------------------------  helper  ---------------------------------------->

    private void configureSwipeButton() {
        Button swipeButton = (Button) overlay.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                speakToUser("Browsing Apps!");
                Path swipePath = new Path();
                swipePath.moveTo(1000, 1000);
                swipePath.lineTo(100, 1000);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
                dispatchGesture(gestureBuilder.build(), null, null);
            }
        });
    }
}
//<------------------------------------------------  END  ------------------------------------------------->

