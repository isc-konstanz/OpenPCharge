package org.openmuc.framework.app.pcharge.port;

/**
 * Interface used by OpenMUC Record Listeners to notify the {@link ChargePort } about events
 */
public interface ChargePortListenerCallbacks {

	void onChargePortEvent();

	void onWaitForStart();

    void onChargingPaused();

    void onChargingStopped();

    void onTimeout();
}
