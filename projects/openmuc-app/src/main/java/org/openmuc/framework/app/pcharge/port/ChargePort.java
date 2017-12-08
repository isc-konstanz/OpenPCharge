package org.openmuc.framework.app.pcharge.port;

import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import org.openmuc.framework.app.pcharge.PChargeConfigException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.ReadRecordContainer;
import org.openmuc.pcharge.data.ChargeAuthorizationStatus;
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
	private final Channel portStatus;
	private final Channel authStatus;
	private final Channel completeStatus;
	private final Channel currentLimit;

	private final int startIntervalMin;
	private volatile long startTimeLast = 0;


	public ChargePort(DataAccessService dataAccessService, String id, Preferences configs) throws PChargeConfigException {
		this.data = dataAccessService;
		this.id = id;
		
		logger.info("Activating P-CHARGE Control for Charge Port \"{}\"", id);

		startIntervalMin = configs.getInt(ChargePortConst.START_INTERVAL_MIN, ChargePortConst.START_INTERVAL_MIN_DEFAULT)*60000;
		
		portStatus = getChannel(configs, ChargePortConst.PORT_STATUS);
		completeStatus = getChannel(configs, ChargePortConst.COMPLETE_STATUS);
		authStatus = getChannel(configs, ChargePortConst.AUTHORIZATION_STATUS);
		currentLimit = getChannel(configs, ChargePortConst.CURRENT_LIMIT);
		
		Channel event = getChannel(configs, ChargePortConst.EVENT);
		eventListener = new ChargePortEventListener(this);
		event.addListener(eventListener);
		
		portStatusListener = new ChargePortStatusListener(this, id, completeStatus);
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
		
		if (time - startTimeLast >= startIntervalMin) {
			List<Record> records = new LinkedList<Record>();
			if (optimized) {
				records.add(new Record(new IntValue(ChargePortStartStop.OPTIMIZED_ACTIVATE.getCode()), time));
			}
			records.add(new Record(new IntValue(ChargePortStartStop.START.getCode()), time));
			
			portStatus.write(records);
			currentLimit.write(new IntValue(limit));
			
			startTimeLast = time;
		}
	}

	public synchronized void stopCharging() {
		long time = System.currentTimeMillis();
		
		List<Record> records = new LinkedList<Record>();
		records.add(new Record(new IntValue(ChargePortStartStop.INTERRUPT_CHARGING.getCode()), time));

		portStatus.write(records);
	}

	public synchronized void setChargingCurrent(int limit) {
		currentLimit.write(new IntValue(limit));
	}

	@Override
	public void onWaitForStart() {
		startCharging(false, CURRENT_LIMIT);
	}

	@Override
	public void onChargingPaused() {
		logger.warn("Charging paused for \"{}\"", id);
		// TODO: Better understand the pause functionality and the necessity to react to it
		stopCharging();
		startCharging(false, CURRENT_LIMIT);
	}

	@Override
	public void onChargingStopped() {
		logger.warn("Charging stopped for \"{}\"", id);
		
		Record authStatusRecord = authStatus.getLatestRecord();
		if (authStatusRecord != null && authStatusRecord.getFlag() == Flag.VALID) {
			if (ChargeAuthorizationStatus.newStatus(authStatusRecord.getValue().asByte()) == ChargeAuthorizationStatus.SERVER_REQUESTED_START) {
				stopCharging();
				startCharging(false, CURRENT_LIMIT);
			}
		}
	}

	@Override
	public void onTimeout() {
		// TODO Check behavior on timeout
		logger.warn("Connection Timeout at {}", id);
		stopCharging();
		startCharging(false, CURRENT_LIMIT);
	}

	@Override
	public void onChargePortEvent() {
		logger.debug("Charge port event detected at {}", id);
		
		List<ReadRecordContainer> recordContainers = new LinkedList<ReadRecordContainer>();
		recordContainers.add(completeStatus.getReadContainer());
		recordContainers.add(portStatus.getReadContainer());
		
		data.read(recordContainers);
	}
}
