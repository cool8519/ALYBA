package dal.tool.analyzer.alyba.ui.urimapping;

import java.util.Comparator;

public class UriPatternComparator implements Comparator<String> {

	@Override
	public int compare(String a, String b) {
		if(a == null && b == null) {
			return 0;
		}
		if(a == null) {
			return 1;
		}
		if(b == null) {
			return -1;
		}
		int sa = score(a);
		int sb = score(b);
		if(sa != sb) {
			return Integer.compare(sb, sa);
		}
		int la = literalSegmentCount(a);
		int lb = literalSegmentCount(b);
		if(la != lb) {
			return Integer.compare(lb, la);
		}
		int va = variableCount(a);
		int vb = variableCount(b);
		if(va != vb) {
			return Integer.compare(va, vb);
		}
		int ra = regexConstraintCount(a);
		int rb = regexConstraintCount(b);
		if(ra != rb) {
			return Integer.compare(rb, ra);
		}
		int cmp = Integer.compare(b.length(), a.length());
		if(cmp != 0) {
			return cmp;
		}
		return a.compareTo(b);
	}

	private static int score(String p) {
		return literalSegmentCount(p) * 1000 + regexConstraintCount(p) * 100 - variableCount(p) * 10;
	}

	private static int literalSegmentCount(String p) {
		if(p == null || p.isEmpty()) {
			return 0;
		}
		String[] segs = p.split("/");
		int n = 0;
		for(String s : segs) {
			if(s.length() == 0) {
				continue;
			}
			if(!s.contains("{")) {
				n++;
			}
		}
		return n;
	}

	private static int variableCount(String p) {
		int n = 0;
		for(int i = 0; i < p.length(); i++) {
			if(p.charAt(i) == '{') {
				n++;
			}
		}
		return n;
	}

	private static int regexConstraintCount(String p) {
		int n = 0;
		int i = 0;
		while(i < p.length()) {
			int o = p.indexOf('{', i);
			if(o < 0) {
				break;
			}
			int c = p.indexOf(':', o + 1);
			int cl = p.indexOf('}', o);
			if(c > 0 && cl > c) {
				n++;
			}
			i = o + 1;
		}
		return n;
	}
}
