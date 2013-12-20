package com.gcr.structs;

/**
 * The Enum MonitorState is used by monitors to reflect their state.
 */
public enum MonitorState {

	/**
	 * The new state represents a monitor that has been instantiated but the
	 * monitoring has not been started.
	 */
	NEW,
	/** The running state means that the monitoring thread is running. */
	RUNNING,
	/**
	 * The held state means that the monitoring thread has been halted
	 * artificially.
	 */
	HELD,
	/**
	 * The terminated state means that the monitor has completed it monitoring
	 * and the monitoring thread has stopped after completing its job.
	 */
	TERMINATED;
}
