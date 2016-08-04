package wikiscraper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class Utils<V> {

	private static final Logger LOGGER = Logger.getGlobal();

	public V retry(int maxRetries, long waitIntervalInMillis, boolean backoff, Callable<V> method) throws Throwable {
		int attempts = 0;
		while (true) {
			try {
				return method.call();
			} catch (Exception e) {
				if (attempts++ >= maxRetries) {
					throw e;
				}
				long sleepInMillis = backoff ? (long) Math.pow(waitIntervalInMillis, attempts) : waitIntervalInMillis;
				try {
					Thread.sleep(sleepInMillis);
				} catch (InterruptedException e1) {
				}
			}
		}
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
				LOGGER.warning("Sleeping interruptted. Resuming ...");
			}
		}
	}
}
