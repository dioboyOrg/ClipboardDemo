
package com.diotek.clipboardtest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

    private static ClipboardManager mClipboardManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            mClipboardManager = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            // clipboard.setText(keyword);
        } else {
            mClipboardManager = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            mClipboardManager.addPrimaryClipChangedListener(mPrimaryChangeListener);
            // android.content.ClipData clip =
            // android.content.ClipData.newPlainText("Copied Text", keyword);
            // clipboard.setPrimaryClip(clip);
        }

        Button notiButton = (Button) findViewById(R.id.btnNoti);
        notiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.editText);
                String text = input.getText().toString();
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    mClipboardManager.setText("version low~ :" + text);
                } else {
                    android.content.ClipData clip = android.content.ClipData.newPlainText(
                            "Copied Text", text);
                    mClipboardManager.setPrimaryClip(clip);
                }
            }
        });
    }
    
    ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);

            String clipboardText = (String) item.getText();

            // noti 참고
            // http://wwwjdic.googlecode.com/svn-history/r1039/branches/2.0/wwwjdic/src/org/nick/wwwjdic/history/HistoryFragmentBase.java
            if (clipboardText != null) {
                Context context = MainActivity.this.getApplicationContext();

                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

                // Build notification
                // Actions are just fake
                String previewString = "noti text : " + clipboardText;
                
                RemoteViews views = new RemoteViews(MainActivity.this.getPackageName(), R.layout.noti_drawer_layout);
                
                /** TextView 에서 bitmap 을 읽어 ImageView에 넣는방법 */
                final LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);      
                final View parentLayout = (View) inflate.inflate(R.layout.noti_drawer_layout, null);
                
                ImageView iv = (ImageView) parentLayout.findViewById(R.id.noti_preview);
                parentLayout.requestLayout();
                
                TextView tv = (TextView)inflate.inflate(R.layout.widget_textview, null);
                tv.setText(previewString);
                
                if(mCalculPaint == null) {
                    mCalculPaint = new TextPaint();
                }
                mCalculPaint.set(tv.getPaint());
                
                File file = new File(Environment.getExternalStorageDirectory().getPath()+"/DioDict4/ds-digit.ttf");
                
                if(file != null && file.exists()) {
                    Log.e("Clipboardtest", file.getAbsolutePath());
                    Typeface tf = Typeface.createFromFile(file);
                    tv.setTypeface(tf);
                }
                
                final Bitmap bitmap = loadBitmapFromView(tv, iv, previewString);
                if(bitmap != null) {
                    views.setBitmap(R.id.noti_preview, "setImageBitmap", bitmap);
                }
                
                Notification noti = new NotificationCompat.Builder(context)
                        .setContentTitle("CopyClipboard")
//                        .setContentText(previewString)        // 기본 notify UI 에 String 설정하는 api
                        .setContent(views)
                        .setTicker(previewString)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pIntent)
                        .setSmallIcon(R.drawable.ic_launcher).build();
                 
                /** 다른 API 예 */
                // .addAction(DioDictR.drawable.ic_launcher, "Call", pIntent)
                // .addAction(DioDictR.drawable.ic_launcher, "More", pIntent)
                // .addAction(DioDictR.drawable.ic_launcher, "And more",
                
                /** 호출부분 */
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(context.NOTIFICATION_SERVICE);
                // hide the notification after its selected
                noti.flags |= Notification.FLAG_AUTO_CANCEL;
                noti.flags |= Notification.DEFAULT_VIBRATE;

                notificationManager.notify(0, noti);
            }
        }
    };
    
    public Bitmap loadBitmapFromView(View v, ImageView img, String preview) {
        // TODO : 동적으로 imageView 의 width height 가져올수 있는지 확인 필요
        final int width = 400;//img.getMeasuredWidth();
        final int height = 100;//img.getMeasuredHeight();

//        if(width == 0 || height == 0) {
//            return null;
//        }
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        bitmap.eraseColor(0);

        v.layout(0, 0, width, height);
        if(v instanceof TextView) {
            float y = getAutoFitTextSize(preview, width, height);
            ((TextView)v).setTextSize(TypedValue.COMPLEX_UNIT_DIP, y);
        }
        
        v.draw(canvas);
        return bitmap;
    }

    /**
     * 폰트사이즈 계산 부분
     */
    private static final int MIN_TEXT_SIZE = 10;
    private static final int MAX_TEXT_SIZE = 40;
    
    private static final int TEMP_MARGIN = 10;

    // Attributes
    private TextPaint mCalculPaint;
    private float mMinTextSize = MIN_TEXT_SIZE;
    private float mMaxTextSize = MAX_TEXT_SIZE;

    private float getAutoFitTextSize(String text, int availableWidth, int availableHeight) {
        float tryTextSize = mMaxTextSize;
        mCalculPaint.setTextSize(tryTextSize*mCalculPaint.density);
        int height = getTotalHeight(text, availableWidth);
        while (height > availableHeight) {
            tryTextSize -= 2;
            if (tryTextSize < mMinTextSize) {
                tryTextSize = mMinTextSize;
                return tryTextSize;
            }
            mCalculPaint.setTextSize(tryTextSize*mCalculPaint.density);
            height = getTotalHeight(text, availableWidth);
        }
        return tryTextSize;
    }
    
    private int getTotalHeight(String text, int availableWidth) {
        int lines = 0;
        Rect tempR = new Rect();
        mCalculPaint.getTextBounds(text, 0, text.length(), tempR);

        String tempText = text;
        if (availableWidth > 0) {
            int end = 0;
            do {
                end = mCalculPaint.breakText(tempText, true, availableWidth-TEMP_MARGIN, null);
                if (end > 0) {
                    tempText = tempText.substring(end);
                    lines++;
                }
            } while (end > 0);
        }
        return lines * (int) (tempR.height() * 2);
    }
}
