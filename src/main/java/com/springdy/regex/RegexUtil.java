package com.springdy.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	public static String singleExtract(String content, Pattern pattern, int groupIndex) {
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(groupIndex);
		}
		return null;
	}

	public static String singleExtract(String content, String regex, int groupIndex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(groupIndex);
		}
		return null;
	}

	public static List<String> multiExtract(String content, String regex, int groupIndex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		List<String> list = new ArrayList<String>();
		while (matcher.find()) {
			list.add(matcher.group(groupIndex));
		}
		return list;
	}

	public static String[] multiExtract(String content, String regex, int groupIndex, int arrayLength) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		String[] array = new String[arrayLength];
		int i = 0;
		while (matcher.find()) {
			array[i] = matcher.group(groupIndex);
			i++;
			if (i >= arrayLength)
				break;
		}
		return array;
	}

	public static MatchResult singleExtractToMatchResult(String content, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.toMatchResult();
		}
		return null;
	}

	public static List<MatchResult> multiExtractToMatchResult(String content, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		List<MatchResult> matchResults = new ArrayList<MatchResult>();
		while (matcher.find()) {
			matchResults.add(matcher.toMatchResult());
		}
		return matchResults;
	}
	
	public static List<MatchResult> multiExtractToMatchResult(String content, Pattern pattern) {
		Matcher matcher = pattern.matcher(content);
		List<MatchResult> matchResults = new ArrayList<MatchResult>();
		while (matcher.find()) {
			matchResults.add(matcher.toMatchResult());
		}
		return matchResults;
	}

	public static boolean match(String content, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);
		return matcher.find();
	}
	
	public static boolean match(String content, Pattern pattern) {
		Matcher matcher = pattern.matcher(content);
		return matcher.find();
	}

	public static void main(String[] args) {
		System.out.println(RegexUtil.match("5201691310895640", "^(\\d{6})(18|19|20)?(\\d{2})([0-1]\\d)([0-3]\\d)(\\d{3})(\\d|X)?$"));
	}

}
