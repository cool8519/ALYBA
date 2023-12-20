package dal.util;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.expression4j.core.Expression;
import fr.expression4j.factory.ExpressionFactory;

public class MathUtil {

	public static DecimalFormat decimalFormat = new DecimalFormat("#.##############");
	
	static {
		Logger.getLogger("fr.expression4j").setLevel(Level.OFF);
	}
	
    public static double evaluateExpression(String expression, List<Double> variables) throws Exception {
    	for (int i = 0; i < variables.size(); i++) {
    		double value = variables.get(i);
    		if(Double.isNaN(value)) continue;
            expression = expression.replaceAll("\\$" + (i+1) + "(?=[^\\d]|$)", decimalFormat.format(value));
        }
    	expression = expression.replace(" ", "");
    	Expression expr = ExpressionFactory.createExpression("f()=" + expression);
		return expr.evaluate(null).getRealValue();
    }
	
}
