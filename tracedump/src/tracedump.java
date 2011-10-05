import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.android.traceview.*;

public class tracedump {

	public static boolean include_method(MethodData d) {
		// boolean include = false;
		String class_name = d.getClassName();
		if (class_name.indexOf("Thread") >= 0) {
			return true;
		}
		if (class_name.indexOf("android") >= 0) {
			return true;
		}
		if (class_name.indexOf("dalvik") >= 0) {
			return true;
		}
		if (class_name.indexOf("context") >= 0) {
			return true;
		}

		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(args.length);
		if (args.length != 1) {
			return;
		}
		DmTraceReader reader = null;
		// TODO Auto-generated method stub
		try {
			reader = new DmTraceReader(args[0], false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<TimeLineView.Record> records = reader.getThreadTimeRecords();
		TimeLineView.Record[] record_array = records
				.toArray(new TimeLineView.Record[records.size()]);
		Arrays.sort(record_array, new Comparator<TimeLineView.Record>() {
			public int compare(TimeLineView.Record rec1,
					TimeLineView.Record rec2) {
				Long start1 = new Long(rec1.block.getStartTime());
				Long start2 = new Long(rec2.block.getStartTime());
				return start1.compareTo(start2);
			}
		});
		for (TimeLineView.Record record : record_array) {
			TimeLineView.Block block = record.block;
			TimeLineView.Row row = record.row;
			MethodData data = block.getMethodData();
			if (include_method(data)) {
				System.out.println(row.getName() + "::" + block.getStartTime()
						+ "::" + data.getClassName() + '.'
						+ data.getMethodName());
			}
		}
	}
}
