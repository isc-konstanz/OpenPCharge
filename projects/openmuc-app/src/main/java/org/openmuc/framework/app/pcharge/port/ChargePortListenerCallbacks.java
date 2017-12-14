package org.openmuc.framework.app.pcharge.port;

/**
 * Interface used by OpenMUC Record Listeners to notify the {@link ChargePort } about events
 */
public interface ChargePortListenerCallbacks {

	void onChargePortEvent();

	void onChargingStartRequest();

    void onChargingPaused();

    void onChargingComplete();

    void onChargingStopped();

    void onChargingAborted();

    void onError(ChargePortError error);

}
