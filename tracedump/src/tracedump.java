import java.io.IOException;
import java.util.ArrayList;

import com.android.traceview.*;
public class tracedump {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(args.length);
		if (args.length != 1){
			return;
		}
		// TODO Auto-generated method stub
		try {
			DmTraceReader reader = new DmTraceReader(args[0], false);
			ArrayList<TimeLineView.Record> records = reader.getThreadTimeRecords();
			for(TimeLineView.Record record : records){
				TimeLineView.Block block = record.block;
				TimeLineView.Row row= record.row;
				MethodData data = block.getMethodData();
				System.out.println(row.getName()+"::"+block.getStartTime()+"::"+data.getClassName()+'.'+data.getMethodName());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
