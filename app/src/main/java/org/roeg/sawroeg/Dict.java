package org.roeg.sawroeg;

import org.roeg.cytokenizer.Word;

import java.util.Iterator;
import java.util.List;

public abstract class Dict {
	protected static String languageFilter(String s, boolean returnChinese) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(isCharChinese(c) == returnChinese) {
				sb.append(c);
			} else {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	protected static boolean isCharChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
			return true;
		}
		return false;
	}

	protected static boolean isStringChinese(String s) {
		for(char i:s.toCharArray()) {
			if(isCharChinese(i))
				return true;
		}
		return false;
	}

	protected static int wordsSimilarity(List<Word> a, List<Word> b) {
		if(a.size() == 0 || b.size() == 0) {
			return Integer.MAX_VALUE;
		}

		int result[] = new int[a.size() + b.size() - 1];

		int results[][] = new int[a.size()][b.size()];

		for(int i = 0; i < a.size(); i++) {
			Word wa = a.get(i);
			String sa = wa.toString();

			for(int j = 0; j < b.size(); j++) {
				Word wb = b.get(j);

				String sb = wb.toString();

				if(sa.equals(sb)) {
					results[i][j] = 0;
				} else if(wa.getMehsing().equals(wb.getMehsing())
						&& wa.getMehyinh().equals(wb.getMehyinh())) {
					results[i][j] = 1;
				} else {
					results[i][j] = 3;
				}
			}
		}

		for (int n = 0; n < b.size(); n++) {
			for(int i = 0, j = n ; i < a.size() && j < b.size(); i++, j++) {
				result[n] += results[i][j];
			}
		}

		for(int n = 0; n < a.size() - 1; n++) {
			for(int i = n + 1, j = 0; i < a.size() && j < b.size(); i++, j++) {
				result[b.size() + n] += results[i][j];
			}
		}

		int ans = Integer.MAX_VALUE;

		for(int i = 0; i < result.length; i++) {
			if(result[i] < ans) {
				ans = result[i];
			}
		}

		return ans;
	}

	public abstract Iterator<String> search(String keyword, int limit_length);
	public abstract List<String> getAll();
}
