package tools;

import java.util.Comparator;

public class ComparatorDouble implements Comparator {
		@Override
		public final int compare(Object a, Object b) {
			// TODO Auto-generated method stub
			return ((Double)a).compareTo((Double)b);
		}




}
