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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Enum MonitorThreadAggressionEnum is used to set the presidency the
 * monitor thread commands while monitoring.
 * 
 * @author R.daneel.olivaw
 * @since 0.4
 */
public enum MonitorThreadAggressionEnum implements MonitorThreadYeildController {

	/**
	 * The low aggression mode means that the monitoring thread will not take a
	 * lot of cpu time and yield execution within short intervals.
	 */
	LOW_AGGRESSION(10),
	/**
	 * The medium aggression means that the monitoring thread will yield
	 * execution less frequently than
	 * {@link MonitorThreadAggressionEnum#LOW_AGGRESSION}.
	 */
	MEDIUM_AGGRESSION(50),
	/**
	 * The high aggression means that the monitoring thread will yield execution
	 * least often when compared to
	 * {@link MonitorThreadAggressionEnum#LOW_AGGRESSION} and
	 * {@link MonitorThreadAggressionEnum#MEDIUM_AGGRESSION}.
	 */
	HIGH_AGGRESSION(100);

	private AtomicInteger maximum;

	MonitorThreadAggressionEnum(int max) {
		maximum.set(max);
	}

	public boolean shouldYield(int countSinceYield) {
		int value = maximum.intValue();

		return (countSinceYield < value);
	}

}
