package com.android.settings.gestures;

import android.app.ActivityManager;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.utils.WorkPolicyUtils;

import lineageos.providers.LineageSettings;

import java.util.ArrayList;
import java.util.List;

public class GestureNavigationImeSpaceUtils {

    private static final String TAG = "GestureNavigationImeSpaceUtils";

    public static final String NAVBAR_IME_SPACE_KEY = "navigation_bar_ime_space";

    private static final String OVERLAY_NAVIGATION_HEADER =
            "org.lineageos.overlay.customization.navbar";

    private Context mContext;

    public GestureNavigationImeSpaceUtils(Context context) {
        mContext = context;
    }

    public int getNavbarSpaceType() {
        return LineageSettings.System.getInt(
                mContext.getContentResolver(), LineageSettings.System.NAVIGATION_BAR_IME_SPACE, 0);
    }

    public void updateSpace(String key) {
        updateSpace(key, getNavbarSpaceType());
    }

    public void updateSpace(String key, int type) {
        if (!SystemNavigationGestureSettings.KEY_SYSTEM_NAV_GESTURAL.equals(key)
                && !NAVBAR_IME_SPACE_KEY.equals(key)) {
            updateSpaceInternal(false, type);
            return;
        }

        updateSpaceInternal(type != 0, type);
    }

    private void updateSpaceInternal(boolean state, int type) {
        final String overlayNarrow = OVERLAY_NAVIGATION_HEADER + ".narrow_space";
        final String overlayNoSpace = OVERLAY_NAVIGATION_HEADER + ".no_space";

        switch (type) {
            case 1: // narrow
                enableOverlayManagedUser(overlayNarrow, state);
                enableOverlayManagedUser(overlayNoSpace, false);
                return;
            case 2: // hidden
                enableOverlayManagedUser(overlayNarrow, false);
                enableOverlayManagedUser(overlayNoSpace, state);
                return;
        }

        enableOverlayManagedUser(overlayNarrow, false);
        enableOverlayManagedUser(overlayNoSpace, false);
    }

    private void enableOverlayManagedUser(String overlay, boolean state) {
        final List<Integer> allId = new ArrayList<Integer>();
        final int userId = ActivityManager.getCurrentUser();
        final WorkPolicyUtils wpu = new WorkPolicyUtils(mContext);
        final int workId = wpu.getManagedProfileUserId();
        final int cloneId = AppUtils.getCloneUserId(mContext);
        allId.add(userId);
        if (workId > -1) {
            allId.add(workId);
        }
        if (cloneId > -1) {
            allId.add(cloneId);
        }
        for (Integer uid : allId) {
            enableOverlay(overlay, state, uid);
        }
    }

    private void enableOverlay(String overlay, boolean state, int userId) {
        final IOverlayManager iom =
                IOverlayManager.Stub.asInterface(
                        ServiceManager.getService(Context.OVERLAY_SERVICE));
        try {
            final OverlayInfo info = iom.getOverlayInfo(overlay, userId);
            if (info == null || info.isEnabled() == state) return;
            iom.setEnabled(overlay, state, userId);
            if (state) {
                // As overlays are also used to apply navigation mode, it is needed to set
                // our customization overlay to highest priority to ensure it is applied.
                iom.setHighestPriority(overlay, userId);
            }
        } catch (IllegalArgumentException | RemoteException e) {
            Log.e(
                    TAG,
                    "Failed to "
                            + (state ? "enable" : "disable")
                            + " overlay "
                            + overlay
                            + " for user "
                            + userId);
        }
    }
}
