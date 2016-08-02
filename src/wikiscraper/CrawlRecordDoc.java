package wikiscraper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CrawlRecordDoc {

	public long crawlTime;
	public long changeTimeMin;
	public long changeTimeMax;

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

}
