package commons.util.html;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 *Author:WangXin69
 *Date:2018年5月2日
 *
 */
public class HtmlFormatHelper {

	
	/**
	 * 多个二维表格转化为htmlbody.
	 * key:table名字
	 * value:表格数据
	 */
	public static String multiTableToHtml(LinkedHashMap<String, List<List<String>>> tables)
	{
		StringBuilder sb = new StringBuilder();
		
		for(Entry<String, List<List<String>>> table: tables.entrySet())
		{
			String tablename = table.getKey();
			List<List<String> > lines = table.getValue();
			
			// 表格转化为htmlbody.
			String tableinfo = simpleContentToTable(lines);
			
			sb.append(tr(td(hl(tablename))));
			sb.append(tr(td(tableinfo)));
			sb.append(tr(td("")));
		}
		return table(sb.toString());
	}
	
	public static void addLine(List<List<String>> lines, String [] cols)
	{
		List<String> list = new ArrayList<String>();
		for(String col: cols)
		{
			list.add(col);
		}
		lines.add(list);
	}
	
	public static String hl(String text)
	{
		return "<strong><font color=\"red\">"+text+"</font></strong>";
	}
	/**
	 * 二维表格转化为htmlbody.
	 * @param contentList
	 * @return
	 */
	public static String simpleContentToHtml(List<List<String>> contentList){
		return body(simpleContentToTable(contentList));
	}
	
	public static String simpleContentToTable(Map<String, Map<String, String>> contentMap){
		List<List<String>> contentList = _transMapToList(contentMap);
		if (contentList == null || contentList.size() == 0){
			return null;
		}
		return simpleContentToTable(contentList);
	}
	
	public static String simpleContentToTable(List<List<String>> contentList){
		String content = "";
		for (List<String> clist: contentList){
			String line = "";
			for (String text: clist){
				line += td(text);
			}
			content += tr(line);
		}
		return table(content);
	}


	private static String table(String content){
		String start = "<table  style='font-size:12px; border-collapse:collapse;' border='1'  cellspacing='0' cellpadding='1'>";
		String end = "</table>";
		return start + content + end;
	}
	private static String tr(String content){
		return "<tr>" + content + "<tr>";
	}
	
	private static String td(String content) {
		return "<td>" + content + "</td>";
	}
	
	private static String body(String content) {
		String header = "<html><body>";
		String tailler = "</body></html>";
		return header + content + tailler;
	}
	
	private static List<List<String>> _transMapToList(Map<String, Map<String, String>> contentMap){
		List<List<String>> contentList = new ArrayList<List<String>>();
		
		List<String> rowTitleList = new ArrayList<String>();
		List<String> columnTitleList = new ArrayList<String>();
		
		columnTitleList.addAll(contentMap.keySet());
		if (columnTitleList.size() == 0){
			return null;
		}
		
		rowTitleList.addAll(contentMap.get(columnTitleList.get(0)).keySet());
		List<String> firstLine = new ArrayList<String>();
		firstLine.add("");
		firstLine.addAll(rowTitleList);
		contentList.add(firstLine);
		for (Map.Entry<String, Map<String, String>> e1: contentMap.entrySet()){
			List<String> lineList = new ArrayList<String>();
			lineList.add(e1.getKey());
			for (Map.Entry<String, String> e2: e1.getValue().entrySet()){
				lineList.add(e2.getValue());
			}
			contentList.add(lineList);
		}
		
		return contentList;
	}
}


