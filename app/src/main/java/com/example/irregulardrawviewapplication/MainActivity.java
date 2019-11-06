package com.example.irregulardrawviewapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView image = findViewById(R.id.image);
        try {
            InputStream inputStream = getAssets().open("timg.jpeg");
            //获得图片的宽，高
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            BitmapFactory.decodeStream(inputStream,null,options);
            int width = options.outWidth;
            int height = options.outHeight;
            //设置显示图片的中心区域
            BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream,false);
            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(width/2-100,height/2-100,width/2+100,height/2+100),options1);
            image.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }

        BigView bigView = findViewById(R.id.bigview);
        try {
            bigView.setImage(getAssets().open("bigpic.jpeg"));
        }catch (IOException e){
            e.printStackTrace();
        }
        final IrregularDrawView idv = findViewById(R.id.idv);
        idv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String colorName = (String)idv.getTag(idv.getId());
                Toast.makeText(MainActivity.this, colorName, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
