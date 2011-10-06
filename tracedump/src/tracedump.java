import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;

import com.android.traceview.*;

public class tracedump {

	public static boolean include_method(MethodData d) {
		// boolean include = false;
		String class_name = d.getClassName();
		String method_name = d.getMethodName();
		if (method_name != null) {
			if (method_name.indexOf("println") >= 0) {
				return false;
			}
		}

		if (class_name.indexOf("Thread") >= 0) {
			return true;
		}
		if (class_name.indexOf("android") >= 0) {
			return true;
		}
		if (class_name.indexOf("dalvik") >= 0) {
			return true;
		}
		if (class_name.indexOf("apache") >= 0) {
			return true;
		}

		if (class_name.equals("(context switch)")) {
			return true;
		}

		if (class_name.equals("(toplevel)")) {
			return true;
		}

		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int maxStackLevel = 0;
		int stackLimit = 50;
		ThreadData[] tda;
		DmTraceReader reader = null;
		HashMap<Integer, Stack<Long>> threadStackMap = null;
		Stack<Long> threadStack = null;
		Long endtime, startTime;
		int i;
		boolean contextSwitch;

		System.out.println(args.length);
		if (args.length < 1 || args.length > 2) {
			return;
		}
		if(args.length == 2){
			stackLimit = Integer.parseInt(args[1]);
		}
		// TODO Auto-generated method stub
		try {
			reader = new DmTraceReader(args[0], false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tda = reader.getThreads();
		threadStackMap = new HashMap<Integer, Stack<Long>>(tda.length);
		for (i = 0; i < tda.length; i++) {
			threadStackMap.put(tda[i].getId(), new Stack<Long>());
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
			threadStack = threadStackMap.get(row.getId());
			startTime = block.getStartTime();
			endtime = block.getEndTime();
			while (!threadStack.empty()) {
				if (threadStack.peek() <= startTime) {
					// a function call returned
					threadStack.pop();
				} else {
					if (threadStack.peek() < endtime) {
						System.out.println("stack prediction error!");
					}
					break;
				}
			}
			if (!data.getClassName().equals("(context switch)")) {
				contextSwitch = false;
				threadStack.push(endtime);
			} else {
				contextSwitch = true;
			}
			if (threadStack.size() > maxStackLevel) {
				maxStackLevel = threadStack.size();
			}
			if (threadStack.size() <= stackLimit) {
				//if (include_method(data)) {
					 if(true){

					for (i = 0; i < threadStack.size(); i++) {
						System.out.print("  ");
					}
					System.out.println(data.getClassName() + '.'
							+ data.getMethodName() + "          ::"
							+ +block.getStartTime() + "::" + block.getEndTime()
							+ "::" + threadStack.size() + "::" + row.getName());
				}
			}
			if (contextSwitch) {
				System.out.println("--------------------------");
			}
		}
		System.out.println("max stack level is " + maxStackLevel);
	}
}
