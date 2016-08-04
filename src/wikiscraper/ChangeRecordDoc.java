package wikiscraper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ChangeRecordDoc extends AbstractDocument {

	private static final String QRY_EXACT = "SELECT * FROM c WHERE c.url = '%s' AND c.timestamp = %d";

	public String url;
	public long timestamp;

	public ChangeRecordDoc(String url, long timestamp) {
		this.url = url;
		this.timestamp = timestamp;
	}

	public ChangeRecordDoc() {
	}

	public ChangeRecordDoc(String url, ZonedDateTime timestamp) {
		this.url = url;
		this.timestamp = timestamp.toEpochSecond();
	}

	public ZonedDateTime timestampAsZonedDateTime() {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
	}

	public void copyTo(ChangeRecordDoc other) {
		other.url = url;
		other.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return String.format("%s\t%s", url, timestampAsZonedDateTime());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof ChangeRecordDoc) {
			ChangeRecordDoc other = (ChangeRecordDoc) obj;
			return this.url.equals(other.url) && this.timestamp == other.timestamp;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return url.hashCode() | (int) timestamp;
	}

	@Override
	public String getQueryStringEqual() {
		return String.format(QRY_EXACT, url, timestamp);
	}

}
