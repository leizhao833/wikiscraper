package wikiscraper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class Utils<V> {

	private static final Logger LOGGER = Logger.getGlobal();

	public V retry(int maxRetries, long waitIntervalInMillis, boolean backoff, Callable<V> method) throws Throwable {
		int attempts = 1;
		while (true) {
			try {
				return method.call();
			} catch (Exception e) {
				LOGGER.fine(String.format("\tfailed at attempt [%d] with exception %s", attempts, e.getMessage()));
				if (attempts++ > maxRetries) {
					LOGGER.fine(String.format("\ttotal attempts exceeding max retries [%d]. give up", maxRetries));
					throw e;
				}
				long sleepInMillis = backoff ? waitIntervalInMillis * (long) Math.pow(2, attempts)
						: waitIntervalInMillis;
				LOGGER.fine(String.format("\tbackoff [%b]. sleep for [%dms] and retry", backoff, sleepInMillis));
				exceptionFreeSleep(sleepInMillis);
			}
		}
	}

	public static void exceptionFreeSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public static void sleepForever() {
		sleepUntil(LocalDateTime.now().plusYears(1));
	}

	public static void sleepUntil(LocalDateTime next) {
		while (true) {
			try {
				LocalDateTime now = LocalDateTime.now();
				long intervalInMillis = now.until(next, ChronoUnit.MILLIS);
				if (intervalInMillis > 0) {
					Thread.sleep(intervalInMillis);
				}
				return;
			} catch (InterruptedException e) {
				LOGGER.fine("sleeping interrupted. resuming to sleep ...");
			}
		}
	}
}
