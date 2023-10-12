package com.nishanthapamagarexample.suitcaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class ItemDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_SEND_SMS = 2;
    private static final int REQUEST_READ_CONTACTS = 3;
    private int pendingPermissionRequest = -1;

    private GestureDetector gestureDetector;
    private String imageUrl;
    private String itemName;
    private String itemPrice;
    private String itemDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        Toolbar toolbar = findViewById(R.id.toolbarItemsDetail);
        setSupportActionBar(toolbar);


        ImageView itemImageView = findViewById(R.id.itemImageView);
        TextView itemNameTextView = findViewById(R.id.itemNameTextView);
        TextView itemPriceTextView = findViewById(R.id.itemPriceTextView);
        TextView itemDescriptionTextView = findViewById(R.id.itemDescriptionTextView);

        imageUrl = getIntent().getStringExtra("imageUrl");
        itemName = getIntent().getStringExtra("itemName");
        itemPrice = getIntent().getStringExtra("itemPrice");
        itemDescription = getIntent().getStringExtra("itemDescription");

        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        itemImageView.setImageBitmap(resource);
                    }
                });

        itemNameTextView.setText(itemName);
        itemPriceTextView.setText(itemPrice);
        itemDescriptionTextView.setText(itemDescription);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_THRESHOLD_VELOCITY = 200;

                try {
                    if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        // Right swipe detected, take the user to the homepage
                        Intent intent = new Intent(ItemDetailsActivity.this, HomePage.class);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                } catch (Exception e) {
                    // Do nothing
                }

                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            // Handle the "Share" action here
            checkPermissionAndShare();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermissionAndShare() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request SEND_SMS permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS);
            pendingPermissionRequest = REQUEST_SEND_SMS;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request READ_CONTACTS permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
            pendingPermissionRequest = REQUEST_READ_CONTACTS;
        } else {
            // You have the required permissions, proceed with sharing
            shareItemDetails( itemName, itemPrice, itemDescription);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, check if there is a pending permission request
            if (pendingPermissionRequest == REQUEST_SEND_SMS) {
                // Request READ_CONTACTS permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
                pendingPermissionRequest = REQUEST_READ_CONTACTS;
            } else if (pendingPermissionRequest == REQUEST_READ_CONTACTS) {
                // Both permissions granted, proceed with sharing
                shareItemDetails( itemName, itemPrice, itemDescription);
            }
        } else {
            // Handle permission denied
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    private void shareItemDetails(String itemName, String itemPrice, String itemDescription) {
        // Create an intent to send an SMS
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));  // Opens the default SMS app

        // Set the message text
        String message = "Check out this item:\nName: " + itemName + "\nPrice: " + itemPrice + "\nDescription: " + itemDescription;
        sendIntent.putExtra("sms_body", message);

        // Start the SMS app
        startActivity(sendIntent);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
