package org.kobjects.krash;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

public abstract class PositionedViewHolder<T extends View> extends ViewHolder<AnchorLayout<T>> {
  public static final double MIN_OPACITY = 0.0001;

  protected float x;
  protected float y;
  protected float z;
  protected XAlign xAlign = XAlign.CENTER;
  protected YAlign yAlign = YAlign.CENTER;
  protected float opacity = 1;

  // For internal use!
  protected boolean visible = true;

  final Screen screen;
  boolean syncRequested;

  ViewHolder<?> anchor;
  private ArrayList<Runnable> changeListeners;

  PositionedViewHolder(Screen screen, T view) {
    super(new AnchorLayout<>(view));
    synchronized (screen.lock) {
      screen.allWidgets.add(this);
    }
    this.screen = screen;
    this.anchor = screen;
    view.setTag(this);
  }


  abstract void syncUi();


  void requestSync(boolean hard) {
    if (!syncRequested) {
      syncRequested = true;
        screen.activity.runOnUiThread(() -> {
          syncRequested = false;
          view.setVisibility(visible ? View.VISIBLE : View.GONE);
          view.wrapped.setAlpha(opacity);
          // visible is used internally to handle bubble visibility and to remove everything on clear, so it
          // gets special treatment here.
          boolean shouldBeAttached = visible && shouldBeAttached();
          ViewGroup expectedParent = shouldBeAttached ? anchor.view : null;
          if (view.getParent() != expectedParent) {
            if (view.getParent() != null) {
              ((ViewGroup) view.getParent()).removeView(view);
            }
            if (expectedParent == null) {
              return;
            }
            expectedParent.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
          }
          syncUi();

          view.setTranslationX(getRelativeX() * screen.scale);
          view.setTranslationY(getRelativeY() * screen.scale);

          view.setTranslationZ(z);

          if (changeListeners != null) {
            synchronized (changeListeners) {
              for (Runnable changeListener : changeListeners) {
                changeListener.run();
              }
            }
          }
        });
    }
  }


  boolean shouldBeAttached() {
    return opacity > MIN_OPACITY;
  }


  public ViewHolder<?> getAnchor() {
    return anchor;
  }

  public Screen getScreen() {
    return screen;
  }

  public T getWrapped() {
    return view.wrapped;
  }

  public float getRelativeX() {
    if (anchor == screen) {
      switch (xAlign) {
        case LEFT:
          return x;
        case RIGHT:
          return screen.getWidth() - x - getWidth();
        default:
          return (screen.getWidth() - getWidth()) / 2 + x;
      }
    } else {
      switch (xAlign) {
        case LEFT:
          return anchor.getWidth() + x;
        case RIGHT:
          return -x - getWidth();
        default:
          return (anchor.getWidth() - getWidth()) / 2 + x;
      }
    }
  }

  public float getRelativeY() {
    if (anchor == screen) {
      switch (yAlign) {
        case TOP:
          return y;
        case BOTTOM:
          return screen.getHeight() - getHeight() - y;
        default:
          return (screen.getHeight() - getHeight()) / 2 - y;
      }
    } else {
      switch (yAlign) {
        case TOP:
          return anchor.getHeight() + y;
        case BOTTOM:
          return -y - getHeight();
        default:
          return (anchor.getHeight() - getHeight()) / 2 - y;
      }
    }
  }

  public float getScreenCX() {
    float result = getRelativeX() + getWidth() / 2;
    ViewHolder current = anchor;
    while (current instanceof PositionedViewHolder) {
      result += ((PositionedViewHolder) current).getRelativeX();
      current = ((PositionedViewHolder) current).anchor;
    }
    return result;
  }

  public float getScreenCY() {
    float result = getRelativeY() + getHeight() / 2;
    ViewHolder current = anchor;
    while (current instanceof PositionedViewHolder) {
      result += ((PositionedViewHolder) current).getRelativeY();
      current = ((PositionedViewHolder) current).anchor;
    }
    return result;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getZ() {
    return z;
  }

  public float getOpacity() {
    return opacity;
  }

  public boolean setX(float x) {
    if (x == this.x) {
      return false;
    }
    this.x = x;
    requestSync(false);
    return true;
  }

  public boolean setY(float y) {
    if (y == this.y) {
      return false;
    }
    this.y = y;
    requestSync(false);
    return true;
  }


  public boolean setOpacity(float opacity) {
    opacity = Math.max(0, Math.min(opacity, 1));
    if (opacity == this.opacity) {
      return false;
    }
    this.opacity = opacity;
    requestSync(false);
    return true;
  }

  public boolean setAnchor(ViewHolder<?> anchor) {
    if (this.anchor == anchor) {
      return false;
    }
    this.anchor = anchor;
    requestSync(false);
    return true;
  }

  public boolean setZ(float z) {
    if (z == this.z) {
      return false;
    }
    this.z = z;
    requestSync(false);

    return true;
  }

  public boolean getVisible() {
    return visible;
  }

  public boolean setVisible(boolean value) {
    if (value == visible) {
      return false;
    }
    visible = value;
    requestSync(false);
    return true;
  }

  public XAlign getXAlign() {
    return xAlign;
  }

  public YAlign getYAlign() {
    return yAlign;
  }

  public boolean setXAlign(XAlign newValue) {
    if (xAlign == newValue) {
      return false;
    }
    xAlign = newValue;
    requestSync(false);
    return true;
  }


  public boolean setYAlign(YAlign newValue) {
    if (yAlign == newValue) {
      return false;
    }
    yAlign = newValue;
    requestSync(false);
    return true;
  }


  public void addChangeListener(Runnable changeListener) {
    synchronized (screen.lock) {
      if (changeListeners == null) {
        changeListeners = new ArrayList<>();
      }
    }
    synchronized (changeListeners) {
      changeListeners.add(changeListener);
    }
  }

}
