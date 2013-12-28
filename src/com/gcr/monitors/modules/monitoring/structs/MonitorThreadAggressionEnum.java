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
	 * lot of cpu time and yield execution within short intervals. The maximum
	 * Permissible count is 10, this means that in this mode the thread will
	 * yield execution after polling 10 objects.
	 */
	LOW_AGGRESSION(10),
	/**
	 * The medium aggression means that the monitoring thread will yield
	 * execution less frequently than
	 * {@link MonitorThreadAggressionEnum#LOW_AGGRESSION}. The maximum
	 * Permissible count is 50, this means that in this mode the thread will
	 * yield execution after polling 50 objects.
	 */
	MEDIUM_AGGRESSION(50),
	/**
	 * The high aggression means that the monitoring thread will yield execution
	 * least often when compared to
	 * {@link MonitorThreadAggressionEnum#LOW_AGGRESSION} and
	 * {@link MonitorThreadAggressionEnum#MEDIUM_AGGRESSION}. The maximum
	 * Permissible count is 100, this means that in this mode the thread will
	 * yield execution after polling 100 objects.
	 */
	HIGH_AGGRESSION(100),
	/**
	 * The highest aggression configurable. In this mode the thread will yield
	 * execution only when
	 * <ul>
	 * <li>countSinceYield is >= {@link Integer#MAX_VALUE}</li>
	 * <li>the monitoring thread completes an iteration of the objects under
	 * monitoring</li>
	 * </ul>
	 */
	HIGHEST_AGGRESSION(Integer.MAX_VALUE);

	private AtomicInteger maximum = new AtomicInteger();

	private MonitorThreadAggressionEnum(int max) {
		maximum.set(max);
	}

	/**
	 * Returns true if countSinceYield >= the maximum count permissible for that
	 * aggression level.
	 * 
	 * @see com.gcr.monitors.modules.monitoring.structs.MonitorThreadYeildController
	 *      #shouldYield(int)
	 */
	public boolean shouldYield(int countSinceYield) {
		int value = maximum.intValue();

		return (countSinceYield >= value);
	}

}
