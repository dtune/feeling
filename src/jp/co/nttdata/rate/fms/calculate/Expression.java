package jp.co.nttdata.rate.fms.calculate;

import java.util.Map;

public interface Expression{ 
      
        @SuppressWarnings("unchecked")
		public Object evaluate(Map context); 
      
        public Object evaluate(Object... keyValue); 
}
