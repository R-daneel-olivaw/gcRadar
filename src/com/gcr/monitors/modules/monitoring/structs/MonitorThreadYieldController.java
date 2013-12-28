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

import com.gcr.monitors.modules.monitoring.impl.MonitoringModule;

/**
 * The Interface MonitorThreadYieldController is used to control the execution
 * rate of the {@link MonitoringModule} Thread in the {@link MonitoringModule}.
 * 
 * @author R.daneel.olivaw
 * @since 0.4
 */
public interface MonitorThreadYieldController {

	/**
	 * This method is used to decide if the monitoring thread should yield
	 * execution if should yield.
	 * 
	 * @param countSinceYield
	 *            the count since last yield
	 * @return true, if the thread should yield
	 */
	boolean shouldYield(int countSinceYield);
}
