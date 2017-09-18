package org.openmuc.framework.app.pcharge.listener;

import org.openmuc.framework.app.pcharge.PChargeControl;

/**
 * Interface used by OpenMUC Record Listeners to notify the {@link PChargeControl } about events
 */
public interface PChargeListenerCallbacks {
	
	void onChargePortEvent(int port);
	
	void onWaitForStart(int port);

    void onChargingPaused(int port);

    void onChargingStopped(int port);

    void onTimeout(int port);
}
