package akhil.DataUnlimited.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import akhil.DataUnlimited.model.types.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DULogger {
	private DULogger() {
	}

	private static Object lock = new Object();
	private static int logLevel = 400;
	private static List<String> log = new ArrayList<>();
	private static boolean logChanged = false;

	public static void clear() {
		log.clear();
		Types.clearLogColor();
		lastUpdated = new Date().getTime();
		logChanged = true;
	}

	private static long lastUpdated = new Date().getTime();
	private static Thread t;

	public static void checkLoggerThread() {
		if (t == null) {
			log(400, "...Starting Logger Thread...");
			startLoggerThread();

		} else {
			if (t.getState().toString().equals("TERMINATED")) {
				log(400, "\n...Starting Logger Thread... again");
				startLoggerThread();

			}
		}
	}

	private static void startLoggerThread() {
		t = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if (logChanged) {
					synchronized (lock) {
						if (new Date().getTime() - lastUpdated > 500) {

							try {
								Types.getInstance().getUI().logRefresh(log);
							} catch (Exception e) {
								DULogger.log(0, LogStackTrace.get(e));
							}
							logChanged = false;
						} else {
							Types.getInstance().getUI()
									.writeToOutput("\n...Checking... Output log will be refreshed, if any...");
						}
					}
				}
			}
		});
		t.start();
	}

	public static int getLogLevel() {
		return logLevel;
	}

	public static void setLogLevel(int i) {
		if (i == 0 || i == 100 || i == 200 || i == 300 || i == 400 || i == 500 || i == 600)
			logLevel = i;
		else {
			logLevel = 400;
			if (Types.getInstance().getIsUI())
				log(200, "ERROR: Bad log level, value can be 0, 100, 200, 300, 400, 500, 600. Defaulting to 400 - Info.");
			else
				lo.error(
						"ERROR: Bad log level, value can be 0, 100, 200, 300, 400, 500, 600. Defaulting to 400 - Info.");
		}
	}

	static final Logger lo = LogManager.getLogger(DULogger.class.getName());

	public static void log(int level, String s) {
		if (Types.getInstance().getIsUI() && level <= logLevel) {
			synchronized (lock) {
				Types.addLogColor(s, level);
				logChanged = true;
				log.add(s + "\n");
				lastUpdated = new Date().getTime();
			}
		}
	}
}
