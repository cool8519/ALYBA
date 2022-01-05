package dal.tool.analyzer.alyba.ui.chart.extension;

public class Distribution {

	public static enum Boundary { Equal, NotEqual, Less, LessOrEqual, Greater, GreaterOrEqual };

	
	public static class ValueBoundaray {
		public Boundary boundary = Boundary.Equal;
		public double value = 0;
		
		public ValueBoundaray(Boundary boundary, double value) {
			this.boundary = boundary;
			this.value = value;
		}
		
		public String getBoundaryString(boolean leftSymbol) {
			String symbol = ""; 
			if(boundary == Boundary.Equal) {
				symbol = "=";
			} else if(boundary == Boundary.NotEqual) {
				symbol = "≠";
			} else if(boundary == Boundary.Less) {
				symbol = "<";
			} else if(boundary == Boundary.LessOrEqual) {
				symbol = "≤";
			} else if(boundary == Boundary.Greater) {
				symbol = ">";
			} else if(boundary == Boundary.GreaterOrEqual) {
				symbol = "≥";
			}
			String value_str = (Math.abs(this.value)-Math.abs((long)this.value) > 0.0D) ? String.valueOf(this.value) : String.valueOf((long)this.value);
			return leftSymbol ? (symbol+value_str) : (value_str+symbol);
		}
		
		public boolean isValueInBoundary(double value) {
			if(boundary == Boundary.Equal) {
				return value == this.value;
			} else if(boundary == Boundary.NotEqual) {
				return value != this.value;
			} else if(boundary == Boundary.Less) {
				return value < this.value;
			} else if(boundary == Boundary.LessOrEqual) {
				return value <= this.value;
			} else if(boundary == Boundary.Greater) {
				return value > this.value;
			} else if(boundary == Boundary.GreaterOrEqual) {
				return value >= this.value;
			} else {
				return false;
			}
		}
	}

	
	public static class ValueRange {		
		public ValueBoundaray from = null;
		public ValueBoundaray to = null;
		public long count = 0;
		
		public ValueRange(ValueBoundaray from, ValueBoundaray to) {
			this.from = from;
			this.to = to;
		}
		
		public ValueRange(ValueBoundaray from) {
			this.from = from;
		}
		
		public String getRangeString() {
			if(to == null) {
				return "x" + from.getBoundaryString(true);
			} else {
				return from.getBoundaryString(false) + "x" + to.getBoundaryString(true);
			}
		}
		
		public boolean isValueInRange(double value) {
			if(to != null) {
				return from.isValueInBoundary(value) && to.isValueInBoundary(value);
			} else {
				return from.isValueInBoundary(value);
			}
		}
	}

}
