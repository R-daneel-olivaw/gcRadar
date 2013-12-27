package com.gcr.monitors.modules.monitoring.structs;

public interface MonitorThreadYeildController {
	boolean shouldYield(int countSinceYield);
}
