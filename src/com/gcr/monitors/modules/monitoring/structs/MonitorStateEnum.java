/*This file is part of gcRadar.

gcRadar is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation version 3 of the License.

gcRadar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with gcRadar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gcr.monitors.modules.monitoring.structs;

/**
 * The Enum MonitorState is used by monitors to reflect their state.
 * 
 * @author R.daneel.olivaw
 * @since 0.4
 */
public enum MonitorStateEnum {

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
