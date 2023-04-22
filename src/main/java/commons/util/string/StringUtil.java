package commons.util.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	private static Pattern yearPattern = Pattern.compile("\\d{2,4}[ ]*款");
	/**
	 * 将车款年份转化为年
	 * @param result
	 * @return
	 */
	public static int parseModelYear(String result) {

		if (null != result) {
			result = result.replace("款", "").trim();
			if (result.length() == 2) {
				if (Integer.parseInt(result) < 60) {
					return Integer.parseInt("20" + result);
				} else {
					return Integer.parseInt("19" + result);
				}
			} else {
				return Integer.parseInt(result);
			}
		}
		return 0;
	}
	/**
	 * 从车款名中提取年份
	 * @param modelText
	 * @return
	 */
	public static int getModelYear(String modelText) {
		Matcher matcher = yearPattern.matcher(modelText);
		String result = null;
		if (matcher.find()) {
			result = matcher.group();
		}
		return parseModelYear(result);
	}
}
