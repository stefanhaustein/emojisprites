package org.kobjects.krash.demo.lines;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.krash.android.Pen;
import org.kobjects.krash.android.AndroidScreen;

public class MainActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EmojiManager.install(new TwitterEmojiProvider());

    AndroidScreen screen = new AndroidScreen(this);
    // screen.setViewport(320, 200, true);

    screen.getView().setBackgroundColor(Color.DKGRAY);
    setContentView(screen.getView());
    final Pen pen = screen.createPen();

    new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        for (int i = 0; i < 1; i++) {
          pen.setLineColor((int) (Math.random() * 0xffffff) | 0xff000000);
          pen.drawLine(
              (float) (Math.random() * 1000 - 500),
              (float) (Math.random() * 1000 - 500),
              (float) (Math.random() * 1000 - 500),
              (float) (Math.random() * 1000 - 500));
        }
      }
    }).start();
  }
}