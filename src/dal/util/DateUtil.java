package dal.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtil {

	public static SimpleDateFormat SDF_DATETIME_NOSEC_TZ = new SimpleDateFormat("yyyy.MM.dd HH:mm z");
	public static SimpleDateFormat SDF_DATETIME_TZ = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
	public static SimpleDateFormat SDF_DATETIME_NOSEC = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	public static SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	public static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy.MM.dd");
	public static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
	public static SimpleDateFormat SDF_TIME_NOSEC = new SimpleDateFormat("HH:mm");

	public static String dateToString(Date d, String format_str) {
		if(d == null || format_str == null) {
			return null;
		} else {
			SimpleDateFormat format = new SimpleDateFormat(format_str);
			return format.format(d);
		}
	}

	public static String dateToString(Date d, DateFormat format) {
		if(d == null || format == null) {
			return null;
		} else {
			return format.format(d);
		}
	}

	public static Date getFirstOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date getLastOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date addDateUnit(Date dt, int calendarUnit, int delta) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(calendarUnit, delta);
		return c.getTime();
	}

	public static Calendar addCalendarUnit(Calendar cal, int calendarUnit, int delta) {
		Calendar c = Calendar.getInstance();
		c.setTime(cal.getTime());
		c.add(calendarUnit, delta);
		return c;
	}

	public static long diff(Date first, Date second, TimeUnit unit) {
	    long diff_ms = Math.abs(first.getTime() - second.getTime());
	    return unit.convert(diff_ms, TimeUnit.MILLISECONDS);
	}
	
}
