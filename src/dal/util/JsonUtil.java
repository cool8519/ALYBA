package dal.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtil {

	/**
	 * json to object
	 * @param json
	 * @param t
	 * @param <T>
	 * @return
	 */
	public static <T> T jsonToObject(String json, Class<T> t) throws Exception {
		if(null != json && null != t) {
			return JSONObject.parseObject(json, t);
		}
		return null;
	}

	/**
	 * json to Map
	 * @param json
	 * @param keyType
	 * @param valueType
	 * @param <K>
	 * @param <V>
	 * @return
	 */

	public static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) throws Exception {
		if(null != json && null != keyType && null != valueType) {
			return JSON.parseObject(json, new TypeReference<Map<K, V>>(keyType, valueType) {});
		}
		return null;
	}

	/**
	 * Convert java objects to JSON data
	 * The fastJson conversion string defaults to ignoring the display of the null field. When converting, the SerializerFeature.WriteMapNullValue is null will also be displayed.
	 * @param o java object
	 * @return JSON data
	 */
	public static <T> String objectToJsonString(T o) throws Exception {
		if(null != o) {
			return JSONObject.toJSONString(o, SerializerFeature.WriteMapNullValue);
		}
		return null;
	}

	/**
	 * Convert json string to array
	 * @param json
	 * @param t types in arrays
	 * @return returns object for example
	 */
	public static <T> Object[] jsonToArray(String json, Class<T> t) throws Exception {
		if(null != json) {
			return JSON.parseArray(json, t).toArray();
		}
		return null;
	}

	/**
	 * Convert JSON data into a list of specified java objects
	 * @param json JSON data
	 * @param t The specified java object t is empty The default is jsonObject type
	 * @return List<T>
	 */
	public static <T> List<T> jsonToList(String json, Class<T> t) throws Exception {
		if(null != json) {
			return JSON.parseArray(json, t);
		}
		return null;

	}

	/**
	 * Convert JSON data to a more complicated List<Map<K, V>>
	 * @param json JSON data
	 * @return List<Map<K, V>>
	 */
	public static <K, V> List<Map<K, V>> jsonToListMap(String json, Class<K> keyType, Class<V> valueType) throws Exception {
		if(null != json && null != keyType && null != valueType) {
			return JSON.parseObject(json, new TypeReference<List<Map<K, V>>>(keyType, valueType) {});
		}
		return null;
	}

	/**
	 * Use the SerializerFeature feature in JSON to format dates
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) throws Exception {
		if(null != date) {
			return JSON.toJSONString(date, SerializerFeature.WriteDateUseDateFormat);
		}
		return null;
	}

	/**
	 * Output format of custom date in JSON
	 * @param date time
	 * @param type time format For example: yyyy-MM-dd
	 * @return
	 */
	public static String dateToString(Date date, String type) throws Exception {
		if(null != date) {
			return JSON.toJSONStringWithDateFormat(date, type);
		}
		return null;
	}

}
