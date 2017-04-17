package scraper.db;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * The crawl record, including the crawl time as well as the time range of
 * changes
 * 
 * @author leizh
 *
 */
public class CrawlRecordDoc extends AbstractDocument {

	public long crawlTime;
	public long changeTimeMin;
	public long changeTimeMax;

	private static final String QRY_EXACT = "SELECT * FROM c WHERE c.crawlTime = %d";

	@Override
	public String toString() {
		return String.format("%s\t<%s ... %s>", crawlTimeAsZonedDateTime(), changeTimeMinAsZonedDateTime(),
				changeTimeMaxAsZonedDateTime());
	}

	public CrawlRecordDoc() {
	}

	public CrawlRecordDoc(long crawlTime, long changeTimeMin, long changeTimeMax) {
		this.crawlTime = crawlTime;
		this.changeTimeMin = changeTimeMin;
		this.changeTimeMax = changeTimeMax;
	}

	public CrawlRecordDoc(ZonedDateTime crawlTime, ZonedDateTime changeTimeMin, ZonedDateTime changeTimeMax) {
		this.crawlTime = crawlTime.toEpochSecond();
		this.changeTimeMin = changeTimeMin.toEpochSecond();
		this.changeTimeMax = changeTimeMax.toEpochSecond();
	}

	public ZonedDateTime crawlTimeAsZonedDateTime() {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(crawlTime), ZoneOffset.UTC);
	}

	public ZonedDateTime changeTimeMinAsZonedDateTime() {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(changeTimeMin), ZoneOffset.UTC);
	}

	public ZonedDateTime changeTimeMaxAsZonedDateTime() {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(changeTimeMax), ZoneOffset.UTC);
	}

	@Override
	public String getQueryStringEqual() {
		return String.format(QRY_EXACT, crawlTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof CrawlRecordDoc) {
			CrawlRecordDoc other = (CrawlRecordDoc) obj;
			return this.crawlTime == other.crawlTime && this.changeTimeMin == other.changeTimeMin
					&& this.changeTimeMax == other.changeTimeMax;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) crawlTime | (int) changeTimeMin | (int) changeTimeMax;
	}
}
