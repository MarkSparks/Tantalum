package com.futurice.s40rssreader;

import com.futurice.tantalum2.log.Log;
import com.futurice.tantalum2.net.xml.RSSItem;
//#ifndef Profile
import com.nokia.mid.ui.frameanimator.FrameAnimator;
import com.nokia.mid.ui.frameanimator.FrameAnimatorListener;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;
//#endif
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * The main canvas for displaying RSS Feed list and item details
 *
 * @author ssaa
 */
//#ifdef Profile
//# public final class RSSReaderCanvas extends Canvas {
//#else    
public final class RSSReaderCanvas extends Canvas implements GestureListener, FrameAnimatorListener {
//#endif

    private static RSSReaderCanvas instance;
    private final RSSReader rssReader;
//#ifndef Profile
    private final FrameAnimator animator;
//#endif
    private final IconListView iconListView;
    private final DetailsView detailsView;
    private View currentView;
    public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font FONT_DESCRIPTION = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font FONT_DATE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final int MARGIN = FONT_TITLE.getHeight() / 2;
    private static volatile boolean repaintIsCurrentlyQueued = false;

    /**
     * Constructor for RSSReaderCanvas
     *
     * @param rssReader
     */
    public RSSReaderCanvas(RSSReader rssReader) {
        super();
        instance = this;
        this.rssReader = rssReader;

        iconListView = new IconListView(this);
        detailsView = new DetailsView(this);

//#ifndef Profile        
        //register frameanimator with default values
        animator = new FrameAnimator();
        short pps = 0;
        short fps = 0;
        animator.register(0, 0, fps, pps, this);

        //register for gesturevents
        final GestureInteractiveZone giz = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_ALL);
        GestureRegistrationManager.register(this, giz);
        GestureRegistrationManager.setListener(this, this);
//#endif

        setCurrentView(iconListView);
    }

    /**
     * Returns the instance of RSSReaderCanvas
     *
     * @return RSSReaderCanvas
     */
    public static RSSReaderCanvas getInstance() {
        return instance;
    }

    public RSSReader getRssReader() {
        return rssReader;
    }

    public IconListView getIconListView() {
        return iconListView;
    }

    public DetailsView getDetailsView() {
        return detailsView;
    }

    /**
     * Paints the content of the current view
     *
     * @param g
     */
    public void paint(final Graphics g) {
        repaintIsCurrentlyQueued = false;
        currentView.render(g, getWidth() - View.SCROLL_BAR_WIDTH, getHeight());
    }

    /**
     * Call this instead of repaint() to ensure that only one repaint()
     * operation at a time is queued up on the EDT.
     *
     */
    public void queueRepaint() {
        if (!repaintIsCurrentlyQueued) {
            repaintIsCurrentlyQueued = true;
            repaint();
        }
    }

    /**
     * Shows the list view
     */
    public void showList() {
//#ifndef Profile
        animator.stop();
//#endif        
//        detailsView.getCurrentItem().setThumbnailImage(null);
        detailsView.hide();
//        iconListView.rssModel.itemNextTo(selectedItem, true), iconListView.rssModel.itemNextTo(selectedItem, false), 0);
        setCurrentView(iconListView);
    }

    /**
     * Show the details view
     *
     * @param selectedItem
     */
    public void showDetails(final RSSItem selectedItem, final int x) {
        if (selectedItem == null) {
            //#debug
            Log.l.log("Show details on null item", "Selected item has been cleared on another thread");
            return;
        }
//#ifndef Profile
        animator.stop();
//#endif        
        detailsView.setCurrentItem(selectedItem, iconListView.rssModel.itemNextTo(selectedItem, true), iconListView.rssModel.itemNextTo(selectedItem, false), x);
        setCurrentView(detailsView);
    }

//#ifndef Profile    
    /**
     * @see GestureListener.gestureAction(Object o, GestureInteractiveZone giz,
     * GestureEvent ge)
     */
    public void gestureAction(Object o, GestureInteractiveZone giz, GestureEvent ge) {
        switch (ge.getType()) {
            case GestureInteractiveZone.GESTURE_DRAG:
                if (currentView == iconListView) {
                    iconListView.setSelectedIndex(-1);
                }
                currentView.setRenderY(currentView.getRenderY() + ge.getDragDistanceY());
                animator.drag(0, currentView.getRenderY() + ge.getDragDistanceY());
                break;
            case GestureInteractiveZone.GESTURE_DROP:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_FLICK:
                boolean scroll = true;
                if (currentView == iconListView) {
                    iconListView.setSelectedIndex(-1);
                } else if (currentView == detailsView) {
                    scroll = !detailsView.horizontalFlick(ge.getFlickDirection());
                }
                if (scroll) {
                    animator.kineticScroll(ge.getFlickSpeed(), FrameAnimator.FRAME_ANIMATOR_FREE_ANGLE, FrameAnimator.FRAME_ANIMATOR_FRICTION_LOW, ge.getFlickDirection());
                }
                break;
            case GestureInteractiveZone.GESTURE_LONG_PRESS:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED:
                //no-op
                break;
            case GestureInteractiveZone.GESTURE_TAP:
                if (currentView == iconListView) {
                    //selects the tapped item
                    iconListView.selectItem(ge.getStartX(), ge.getStartY(), true);
                }
                break;
        }
    }
//#endif

    protected void pointerPressed(int x, int y) {
        //just paints the highlight on the selected item
        iconListView.selectItem(x, y, false);
    }

    protected void pointerReleased(int x, int y) {
        //just paints the highlight on the selected item
        iconListView.deselectItem();
        //#ifdef Profile
//#         if (currentView == iconListView) {
//#             //selects the tapped item
//#             iconListView.selectItem(x, y, true);
//#         }
        //#endif
    }

//    public void sizeChanged(final int widht, final int height) {
//        verticalListView.canvasSizeChanged();
//    }
//#ifndef Profile    
    /*
     * @see FrameAnimatorListener.animate(FrameAnimator fa, int x, int y, short
     * delta, short deltaX, short deltaY, boolean lastFrame)
     */
    public void animate(FrameAnimator fa, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
        currentView.setRenderY(y);
        queueRepaint();
    }
//#endif

    public void setCurrentView(final View nextView) {
        if (this.currentView != null) {
            for (int i = 0; i < this.currentView.getCommands().length; i++) {
                removeCommand(this.currentView.getCommands()[i]);
            }
        }
        this.currentView = nextView;
        for (int i = 0; i < currentView.getCommands().length; i++) {
            addCommand(currentView.getCommands()[i]);
        }
        setCommandListener(currentView);
        queueRepaint();
    }
}