package org.openmuc.framework.app.pcharge.port;

import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import org.openmuc.framework.app.pcharge.PChargeConfigException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.FutureValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.ReadRecordContainer;
import org.openmuc.pcharge.data.ChargeCompleteStatus;
import org.openmuc.pcharge.data.ChargePortStartStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChargePort implements ChargePortListenerCallbacks {
	private final static Logger logger = LoggerFactory.getLogger(ChargePort.class);

	private final static int CURRENT_LIMIT = 16;

	private final DataAccessService data;

	private final String id;

	private final ChargePortEventListener eventListener;
	private final ChargePortStatusListener portStatusListener;
//	private final Channel authStatus;
	private final Channel completeStatus;
	private final Channel portStatus;
	private final Channel currentLimit;


	public ChargePort(DataAccessService dataAccessService, String id, Preferences configs) throws PChargeConfigException {
		this.data = dataAccessService;
		this.id = id;
		
		logger.info("Activating P-CHARGE Control for Charge Port \"{}\"", id);

//		authStatus = getChannel(configs, ChargePortConst.AUTHORIZATION_STATUS);
		completeStatus = getChannel(configs, ChargePortConst.COMPLETE_STATUS);
		portStatus = getChannel(configs, ChargePortConst.PORT_STATUS);
		currentLimit = getChannel(configs, ChargePortConst.CURRENT_LIMIT);
		
		Channel event = getChannel(configs, ChargePortConst.EVENT);
		eventListener = new ChargePortEventListener(this);
		event.addListener(eventListener);
		
		int startIntervalMin = configs.getInt(ChargePortConst.START_INTERVAL_MIN, ChargePortConst.START_INTERVAL_MIN_DEFAULT)*60000;
		portStatusListener = new ChargePortStatusListener(this, startIntervalMin);
		portStatus.addListener(portStatusListener);
	}

	private Channel getChannel(Preferences configs, String key) throws PChargeConfigException {
		String id = configs.get(key, null);
		if (id != null) {
			Channel channel = data.getChannel(id);
			if (channel != null) {
				return channel;
			}
			throw new PChargeConfigException("Unable to find configured " + key + " channel: " + id);
		}
		throw new PChargeConfigException("Unable to find channel configuration for: " + key);
	}

	public synchronized void startCharging(boolean optimized, int limit) {
		long time = System.currentTimeMillis();
		
		List<FutureValue> values = new LinkedList<FutureValue>();
		if (optimized) {
			logger.debug("Start optimized charging for electric vehicle at \"{}\" with up to {}A", id, limit);
			values.add(new FutureValue(new IntValue(ChargePortStartStop.OPTIMIZED_ACTIVATE.getCode()), time));
		}
		else {
			logger.debug("Start charging for electric vehicle at \"{}\" with up to {}A", id, limit);
			values.add(new FutureValue(new IntValue(ChargePortStartStop.OPTIMIZED_DEACTIVATE.getCode()), time));
		}
		values.add(new FutureValue(new IntValue(ChargePortStartStop.START.getCode()), time));
		
		portStatus.writeFuture(values);
		currentLimit.write(new IntValue(limit));
	}

	public synchronized void stopCharging() {
		long time = System.currentTimeMillis();

		List<FutureValue> values = new LinkedList<FutureValue>();
		values.add(new FutureValue(new IntValue(ChargePortStartStop.INTERRUPT_CHARGING.getCode()), time));
		
		portStatus.writeFuture(values);
	}

	public synchronized void setChargingCurrent(int limit) {
		currentLimit.write(new IntValue(limit));
	}

	@Override
	public void onChargePortEvent() {
		logger.debug("Charge port event detected at \"{}\"", id);
		
		List<ReadRecordContainer> recordContainers = new LinkedList<ReadRecordContainer>();
		recordContainers.add(completeStatus.getReadContainer());
		recordContainers.add(portStatus.getReadContainer());
		
		data.read(recordContainers);
	}

	@Override
	public void onChargingStartRequest() {
		startCharging(false, CURRENT_LIMIT);
	}

	@Override
	public void onChargingPaused() {
		logger.debug("Charging for electric vehicle at \"{}\" paused", id);
		
		// TODO: Better understand the pause functionality and the necessity to react to it
		
	}

	@Override
	public void onChargingComplete() {
		ChargeCompleteStatus completeStatus = ChargeCompleteStatus.UNKNOWN_STATUS;
		Record completeStatusRecord = this.completeStatus.getLatestRecord();
		if (completeStatusRecord != null && completeStatusRecord.getFlag() == Flag.VALID) {
			completeStatus = ChargeCompleteStatus.newStatus(completeStatusRecord.getValue().asByte());
		}
		
		switch(completeStatus) {
		case OK_COMPLETE:
			logger.debug("Electric vehicle at \"{}\" completed charging successfully", id);
			break;
		case OK_STOP:
			logger.debug("Electric vehicle at \"{}\" completed charging: "
					+ "Stopped by user", id);
			
			onChargingStopped();
			break;
		default:
			break;
		}
	}

	@Override
	public void onChargingStopped() {
		logger.debug("Charging for electric vehicle at \"{}\" stopped", id);
		
		// TODO: Better understand the stop functionality and the necessity to react to it
		
//		Record authStatusRecord = authStatus.getLatestRecord();
//		if (authStatusRecord != null && authStatusRecord.getFlag() == Flag.VALID) {
//			if (ChargeAuthorizationStatus.newStatus(authStatusRecord.getValue().asByte()) == ChargeAuthorizationStatus.SERVER_REQUESTED_START) {
//				stopCharging();
//				startCharging(false, CURRENT_LIMIT);
//			}
//		}
	}

	@Override
	public void onChargingAborted() {
		ChargeCompleteStatus completeStatus = ChargeCompleteStatus.UNKNOWN_STATUS;
		Record completeStatusRecord = this.completeStatus.getLatestRecord();
		if (completeStatusRecord != null && completeStatusRecord.getFlag() == Flag.VALID) {
			completeStatus = ChargeCompleteStatus.newStatus(completeStatusRecord.getValue().asByte());
		}
		
		switch(completeStatus) {
		case OK_STOP:
			logChargingAbort("Stopped by user");
			onChargingStopped();
			break;
		case OK_CABLE_PULLED:
			logChargingAbort("Cable got removed by user");
			break;
		case ERROR_CABLE_LOST:
			logChargingAbort("Lost contact to cable");
			break;
		case ERROR_CIRCUIT_BREAKER:
			logChargingAbort("Circuit breaker fault detected");
			break;
		case ERROR_METER:
			logChargingAbort("Current meter fault detected");
			break;
		case ERROR_TIMEOUT:
			logChargingAbort("Server timeout");
			onTimeout();
			break;
		case ERROR_VENTING:
			logChargingAbort("Venting not supported");
			break;
		case ERROR_PMW:
			logChargingAbort("PMW-signal unstable. Charging interrupted to avoid damage");
			break;
		default:
			break;
		}
	}

	public void onTimeout() {
		// TODO Check behavior on timeout
		stopCharging();
		startCharging(false, CURRENT_LIMIT);
	}

	@Override
	public void onError(ChargePortError error) {
		logger.warn("Error detected at \"{}\": {}", id, error);
	}

	private void logChargingAbort(String message) {
		logger.warn("Electric vehicle at \"{}\" aborted charging: {}", id, message);
	}

}
