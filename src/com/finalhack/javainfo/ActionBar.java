package com.finalhack.javainfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;


public class ActionBar extends View {

    //A few tracking flags and internal defaults
    private static final boolean DRAW_ICON_SEPARATOR = false;
    
    //Style information
    private int colorActionBarBackground = 0xff72aff3;
    private int colorActionBarMenuIcon = 0xff7d7d7d;
    private int colorActionBarText = 0xffeeeeee; 
    private int actionBarHeight = 48;
    private int actionBarIconHeight = 48;
    private int actionBarIconWidth = 48;
    private int actionBarTextSize = 18;
    private int paddingAfterLogo = 5;
    private float actionBarTextHangPadding = 0.2f;
    private int dialogPadding = 12;
     
    //Misc statistical information
    private float density;
    private XY screenDimensions = new XY();
    private int nextXCoordinateForMenuIcon;
    
    //Actual action bar display info holders
    private String actionBarTitle;
    private List<TouchPoint> touchPoints = new ArrayList<TouchPoint>();
    //for concurrent removal/addition -- used when the user touches an actionbar button
    //and the button removes or adds touchpoints to the actionbar
    private List<TouchPoint> touchPointsToRemove = new ArrayList<TouchPoint>();
    private List<TouchPoint> touchPointsToAdd = new ArrayList<TouchPoint>();
    
    //View for overflow
    private Dialog viewDialog;
    
    //button names for the dialog
    private List<String> btnNames = new ArrayList<String>();
    
    public ActionBar(Context context, AttributeSet attrSet)
    {
        super(context, attrSet);
        
        btnNames.add(getResources().getString(R.string.menu_question_feedback));
        
        actionBarTitle = getResources().getString(R.string.app_name);
        
        if (isInEditMode()) return;        
        
        //Save screen and device info that won't change     
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        screenDimensions.x = display.getWidth();
        screenDimensions.y = display.getHeight();
                
        density = getResources().getDisplayMetrics().density;
        
        actionBarHeight *= density;
        actionBarIconHeight *= density;
        actionBarIconWidth *= density;
        actionBarTextSize *= density;
        paddingAfterLogo *= density;
        dialogPadding *= density;
        //Start two widths from the end because the settings menu will always be first
        nextXCoordinateForMenuIcon = (int)screenDimensions.x - (actionBarIconWidth);
        
        Method method = null;
        try{method = this.getClass().getMethod("showNavigationMenu", (Class[])null);}catch(Exception e){if (BuildConfig.DEBUG) Log.d("", e.getMessage(), e);}
        addTouchPoint(method, this, R.drawable.ic_action_overflow);

    } 
    
    //A callback for a standard icon that is included by default
    public void showNavigationMenu()
    {
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //
        //  I tried using a listView instead of creating individual buttons but the listView would always fill_parent
        //  instead of wrap_content no matter what I tried to do.  The only way I was able to make the listView smaller
        //  than the screen size was to hard code the layout_width to be a certain number of pixels.  Making the dialog
        //  have buttons instead of a listView didn't present any of the issues that were there with the listView.  The
        //  buttons did wrap_content like expected.  This way works, but it's not as elegant of a solution.
        //
        //margin that pushes the dialog box 1% from the right edge. It's from the right edge because of Gravity.RIGHT 
        //that we set below.
        float horizontalMargin = .01f;
        
        //create a dialog to look like the drop down menu of the action overflow
        viewDialog = new Dialog(getContext()); 
        //cancel dialog when pressed outside
        viewDialog.setCanceledOnTouchOutside(true);

        //make the background not dim behind the dialog
        viewDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        
        //set the background to custom 9 patch
        viewDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        //put the dialog on the top of the screen and set the y value to be right below the actionBar
        viewDialog.getWindow().getAttributes().gravity=Gravity.TOP |Gravity.RIGHT;
        viewDialog.getWindow().getAttributes().y=actionBarHeight;
        viewDialog.getWindow().getAttributes().width = LayoutParams.WRAP_CONTENT;
        //give the view a margin on the right of the dialog box so it's not on the far right edge
        viewDialog.getWindow().getAttributes().horizontalMargin=horizontalMargin;
        //remove the title bar
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        viewDialog.setContentView(R.layout.action_overflow);
        LinearLayout linearLayout = (LinearLayout)viewDialog.findViewById(R.id.action_overflow_layout);
        
        //add buttons to the dialog
        for (int i= 0; i < btnNames.size(); i++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            //create a new button
            Button btnTag = new Button(getContext());
            btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            //transparent background
            btnTag.setBackgroundResource(android.R.color.transparent);
            //white text
            btnTag.setTextColor(Color.WHITE);
            btnTag.setText(btnNames.get(i));
            //set the text size
            //not sure if getDimension scales the sp value in action_bar_overflow_text_size and then setTextSize
            //scales it again. It seems to act the same as if I put a hard-coded float value here.
            //set the tag to be used in the onClickListener to know which button was pressed so we know
            //which activity to start
            btnTag.setTag(i);
            
            btnTag.setPadding(dialogPadding, dialogPadding, dialogPadding, dialogPadding);
            btnTag.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    viewDialog.dismiss();
                    
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"javainfoapp@gmail.com"});
                    String currentQuestionId = "";
                    if (QuestionUtil.CURRENT_QUESTION_ID != null) currentQuestionId = " for qid" + QuestionUtil.CURRENT_QUESTION_ID; 
                    intent.putExtra(Intent.EXTRA_SUBJECT, "JavaInfo feedback" + currentQuestionId);
                    ActionBar.this.getContext().startActivity(intent);
                }
            });
            //add the button to the row
            row.addView(btnTag);
            //add the row to the view
            linearLayout.addView(row);  
            //we don't want to add a line after the last button
            if(i < (btnNames.size() - 1)) {
                //add a horizontal line
                View ruler = new View(getContext()); 
                //make it white
                ruler.setBackgroundColor(Color.WHITE);
                //add the line to the view
                linearLayout.addView(ruler, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, 2));
            }
        }
        
        viewDialog.show(); 
    }
    
    //Set the title...
    public void setTitle(int stringId)
    {
        actionBarTitle = getResources().getString(stringId);
    }
    
    public void setTitle(String str)
    {
        actionBarTitle = str;
    }
    
    //Add a new touch point
    public TouchPoint addTouchPoint(Method method, Object object, int iconResource)
    {  
        //If something is not set, don't add the touch point 
        if (method == null || object == null) return null;

        //Compute the touch point of the next available icon space
        Rect rect = new Rect(nextXCoordinateForMenuIcon, 0, nextXCoordinateForMenuIcon + actionBarIconWidth, actionBarIconHeight);
        
        //Create and add the touch point, setting private methods to be accessible
        TouchPoint touchPoint = new TouchPoint(rect, method, object, iconResource);
        touchPoint.method.setAccessible(true);
        touchPoints.add(touchPoint);

        //Update the location of our next icon
        nextXCoordinateForMenuIcon -= actionBarIconWidth;
        
        return touchPoint;
    }
    
    //Add a new touch point when the user presses an icon on the actionbar. This puts the 
    //touchpoints into an array and then adds them at the end of onTouchEvent after it iterates
    //through the current touchpoints. The creation of this function was to avoid concurrent modification
    //exceptions when the user touches the actionbar and we call a function to remove/add icons to the 
    //actionbar.
    public TouchPoint addTouchPointConcurrent(Method method, Object object, int iconResource)
    {  
        //If something is not set, don't add the touch point 
        if (method == null || object == null) return null;

        //Compute the touch point of the next available icon space
        Rect rect = new Rect(nextXCoordinateForMenuIcon, 0, nextXCoordinateForMenuIcon + actionBarIconWidth, actionBarIconHeight);
        
        //Create and add the touch point, setting private methods to be accessible
        TouchPoint touchPoint = new TouchPoint(rect, method, object, iconResource);
        touchPoint.method.setAccessible(true);
        touchPointsToAdd.add(touchPoint);

        //Update the location of our next icon
        nextXCoordinateForMenuIcon -= actionBarIconWidth;
        
        return touchPoint;
    }
        
    //Remove a specific icon
    public void removeTouchPoint(TouchPoint touchPoint)
    {
        boolean removed = touchPoints.remove(touchPoint);
        if (removed) nextXCoordinateForMenuIcon += actionBarIconWidth;
    }
    
    //for removing touchpoints when the user presses an icon on the actionbar. This puts the 
    //touchpoints into an array and then removes them at the end of onTouchEvent after it iterates
    //through the current touchpoints. The creation of this function was to avoid concurrent modification
    //exceptions when the user touches the actionbar and we call a function to remove/add icons to the 
    //actionbar.
    public void removeTouchPointConcurrent(TouchPoint touchPoint) 
    {
        touchPointsToRemove.add(touchPoint);
        nextXCoordinateForMenuIcon += actionBarIconWidth;
    }
    //Some screens don't want the standard icons
    public void removeAllTouchPoints()
    {
        touchPoints.clear();
        nextXCoordinateForMenuIcon = (int)screenDimensions.x - (actionBarIconWidth);
    }

    @Override 
    protected void onDraw(Canvas canvas)   
    { 
        //Draw the main action bar
        super.onDraw(canvas); 

        Paint actionBarPaint = new Paint();
        actionBarPaint.setColor(colorActionBarBackground); 
        canvas.drawRect(new Rect(0,0,(int)screenDimensions.x,(int)(actionBarHeight)), actionBarPaint);
        //Draw the app icon
        Paint appIconPaint = new Paint();
        Bitmap appIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        //Get bitmap offsets (what is the difference between the action bar icon height and the actual bitmap) / 2 for centering
        int bitmapHeightOffset = (actionBarIconHeight - appIconBitmap.getHeight()) / 2;
        int bitmapWidthOffset = (actionBarIconWidth - appIconBitmap.getWidth()) / 2;
        canvas.drawBitmap(appIconBitmap, bitmapWidthOffset, bitmapHeightOffset, appIconPaint);
        
        //Draw the app name text
        Paint appTextPaint = new Paint();
        appTextPaint.setTextSize(actionBarTextSize);
        appTextPaint.setColor(colorActionBarText);
        appTextPaint.setAntiAlias(true);
        float textY = (actionBarHeight / 2) + (actionBarTextSize / 2) - (actionBarTextSize * actionBarTextHangPadding);
        canvas.drawText(actionBarTitle, actionBarIconWidth + paddingAfterLogo, textY, appTextPaint);

        //Draw all registered quick menu icons
        Paint menuIconPaint = new Paint();
        menuIconPaint.setColor(colorActionBarMenuIcon);
        Paint whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        for (TouchPoint touchPoint : touchPoints)
        {           
            //Draw out our next icon
            Bitmap quickIconBitmap = BitmapFactory.decodeResource(getResources(), touchPoint.iconResource);
            //Get bitmap offsets (what is the difference between the action bar icon height and the actual bitmap) / 2 for centering
            bitmapHeightOffset = (actionBarIconHeight - quickIconBitmap.getHeight()) / 2;
            bitmapWidthOffset = (actionBarIconWidth - quickIconBitmap.getWidth()) / 2;
            canvas.drawBitmap(quickIconBitmap, touchPoint.rect.left + bitmapWidthOffset, 0 + bitmapHeightOffset, menuIconPaint);
            if (DRAW_ICON_SEPARATOR)
            {
                canvas.drawLine(touchPoint.rect.left + 0, 0, touchPoint.rect.left + 0, actionBarHeight, menuIconPaint);
                canvas.drawLine(touchPoint.rect.left + 1, 0, touchPoint.rect.left + 1, actionBarHeight, whitePaint);
            }
        }

    }
    
    //Check to see if a touch event is within any region that should be handled
    public boolean onTouchEvent(MotionEvent event)
    {
        //Grab the current touch coordinates
        float x = event.getX();
        float y = event.getY();
        
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        
        //Loop through all our touch regions and see if we have a match
        for (TouchPoint touchPoint : touchPoints)
        {
            //If there is a match, run it's associated method
            if (isXYInRect(x, y, touchPoint.rect))
            {
                try { touchPoint.method.invoke(touchPoint.object, (Object[])null); } catch(Exception e) {if (BuildConfig.DEBUG) Log.d("", e.getMessage(), e);}
            }
        }
        //for concurrent removal of icons when editing groups
        //this happens when a user presses an icon and that button removes icons from
        //the actionbar or adds icons to the actionbar. There was an issue of looping through
        //the touchpoints in the loop above this statement and the method that was called
        //by the touchpoint modified the touchpoint arraylist.  So this lets it loop through that
        //list and adds the touchpoints that it needs to remove or add to a different list and 
        //it takes care of them here.
        for (TouchPoint touchPointRemove : touchPointsToRemove) {
            touchPoints.remove(touchPointRemove);           
        }
        for (TouchPoint touchPointAdd : touchPointsToAdd) {
            touchPoints.add(touchPointAdd);
        }
        //clear out the lists so there's nothing in there for future button presses.
        touchPointsToRemove.clear();
        touchPointsToAdd.clear();
        
        return true;
    } 
    
    //A helper method to determine if a coordinate is within a rectangle
    private boolean isXYInRect(float x, float y, Rect rect)
    {
        //If it is within the bounds...
        if (x > rect.left &&
            x < rect.right &&
            y > rect.top &&
            y < rect.bottom)
        {
            //Then it's a hit
            return true;
        }
        
        //Otherwise, it's a miss
        return false;
    }

    //Standard view override for requesting the needed space on a screen
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension((int)screenDimensions.x, (int)(actionBarHeight));
    }
    
    //This is a touch handler for the icon which should not do anything
    //We need one to cut down on code duplication (use the same code to draw the icon as the actual buttons)
    public void doNothing()
    {
        //Literally do nothing
    }
    
    //Track screen points, which when touched should execute something
    public class TouchPoint
    {
        public Rect rect;
        public Method method;
        public Object object;
        public int iconResource;
        
        //A convenience constructor
        public TouchPoint(Rect rect, Method method, Object object, int iconResource)
        {
            this.rect = rect;
            this.method = method;
            this.object = object;
            this.iconResource = iconResource;
        }
    }
    
    //A simple x,y pair class to keep associated Xs and Ys together
    private class XY
    {
        public float x;
        @SuppressWarnings("unused")
        public float y;
    }   
}
