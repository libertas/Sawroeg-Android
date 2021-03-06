package org.roeg.sawroeg;

import org.roeg.cytokenizer.Word;

import java.util.Iterator;
import java.util.List;

public abstract class Dict {

	public static int min(List a) {
		int tmp = (Integer) a.get(0);
		for(int i = 0; i < a.size(); i++) {
			if((Integer) a.get(i) < tmp)
				tmp = (Integer) a.get(i);
		}
		return tmp;
	}

	public static boolean isCharChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isStringChinese(String s) {
		for(char i:s.toCharArray()) {
			if(isCharChinese(i))
				return true;
		}
		return false;
	}

	public static int wordsSimilarity(List<Word> a, List<Word> b) {
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
