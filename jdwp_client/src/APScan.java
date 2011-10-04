import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.ClientData.IHprofDumpHandler;
import com.android.ddmlib.ClientData.IMethodProfilingHandler;
import com.android.ddmlib.ClientData.MethodProfilingStatus;
import com.android.ddmlib.IDevice;
import com.android.hit.ClassObj;
import com.android.hit.HprofParser;
import com.android.hit.Instance;
import com.android.hit.Queries;
import com.android.hit.State;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread;
import java.util.Map;
import java.util.Set;

public class APScan {

	AndroidDebugBridge adb;
	String device = null;

	public class MethodProfilingHandler implements IMethodProfilingHandler {

		@Override
		public void onSuccess(String remoteFilePath, Client client) {
			System.out.println("method profiling dump: " + remoteFilePath
					+ " from " + client.getClientData().getPid());
		}

		@Override
		public void onSuccess(byte[] data, Client client) {
			int pid = client.getClientData().getPid();
			System.out.println("method profiling dump: " + data.toString()
					+ " from " + pid);
			String filename = pid + "_mprof.dump";
			System.out.println("mprof dump data from " + pid);
			OutputStream out = null;
			try {
				out = new FileOutputStream(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.write(data);
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// parse and dump
			FileInputStream fis;
			BufferedInputStream bis;
			DataInputStream dis;

			try {
				fis = new FileInputStream(filename);
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onStartFailure(Client client, String message) {
			System.out.println("method profiling failure starts: " + message
					+ " from " + client.getClientData().getPid());
		}

		@Override
		public void onEndFailure(Client client, String message) {
			// TODO Auto-generated method stub
			System.out.println("method profiling failure : " + message
					+ " from " + client.getClientData().getPid());
		}

	}

	private class HProfHandler implements IHprofDumpHandler {
		private void testClassesQuery(State state) {
			String[] x = new String[] { "char[", "javax.", "org.xml.sax" };

			Map<String, Set<ClassObj>> someClasses = Queries.classes(state, x);

			for (String thePackage : someClasses.keySet()) {
				System.out.println("------------------- " + thePackage);

				Set<ClassObj> classes = someClasses.get(thePackage);

				for (ClassObj theClass : classes) {
					System.out.println("     " + theClass.mClassName);
				}
			}
		}

		private void testAllClassesQuery(State state) {
			Map<String, Set<ClassObj>> allClasses = Queries.allClasses(state);

			for (String thePackage : allClasses.keySet()) {
				System.out.println("------------------- " + thePackage);

				Set<ClassObj> classes = allClasses.get(thePackage);

				for (ClassObj theClass : classes) {
					System.out.println("     " + theClass.mClassName);
				}
			}
		}

		private void testFindInstancesOf(State state) {
			Instance[] instances = Queries.instancesOf(state,
					"java.lang.String");

			System.out.println("There are " + instances.length + " Strings.");
		}

		private void testFindAllInstancesOf(State state) {
			Instance[] instances = Queries.allInstancesOf(state,
					"android.graphics.drawable.Drawable");

			System.out.println("There are " + instances.length
					+ " instances of Drawables and its subclasses.");
		}

		@Override
		public void onSuccess(String remoteFilePath, Client client) {
			System.out.println("hprof dump: " + remoteFilePath + " from "
					+ client.getClientData().getPid());
		}

		@Override
		public void onSuccess(byte[] data, Client client) {
			int pid = client.getClientData().getPid();
			String filename = pid + "_hprof.dump";
			System.out.println("hprof dump data from " + pid);
			OutputStream out = null;
			try {
				out = new FileOutputStream(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.write(data);
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// parse and dump
			FileInputStream fis;
			BufferedInputStream bis;
			DataInputStream dis;

			try {
				fis = new FileInputStream(filename);
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				State state = (new HprofParser(dis)).parse();
				dis.close();
				testClassesQuery(state);
				testAllClassesQuery(state);
				testFindInstancesOf(state);
				testFindAllInstancesOf(state);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onEndFailure(Client client, String message) {
			System.out.println("hprof failure: " + message + " from "
					+ client.getClientData().getPid());
		}

	}

	private boolean wait_for_connection() {
		int i;
		for (i = 0; i < 5; i++) {
			if (adb.isConnected()) {
				return true;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	abstract class CmdHandler {
		String cmd;

		abstract int handle(String[] args);

		CmdHandler(String cmd) {
			this.cmd = cmd;
		}

		boolean check_cmd(String arg) {
			return cmd.equals(arg);
		}
	};

	IDevice find_device() {
		int i;
		IDevice device_array[];
		IDevice dev;
		if (null == device) {
			System.out.println("device not set!\n");
			return null;
		}

		device_array = adb.getDevices();
		if (device_array.length == 0) {
			System.out.println("no device found!\n");
			return null;
		}
		dev = null;
		for (i = 0; i < device_array.length; i++) {
			if (device_array[i].getSerialNumber().equals(device)) {
				dev = device_array[i];
				break;
			}
		}
		if (dev == null) {
			System.out.println("device " + device + " not found!");
		}
		return dev;
	}

	void command_loop() {
		int i;
		String[] args;
		String input = "";
		BufferedReader inStr = new BufferedReader(new InputStreamReader(
				System.in));
		CmdHandler handlers[] = new CmdHandler[5];
		handlers[0] = new CmdHandler("list_process") {
			public int handle(String[] args) {
				IDevice dev;
				Client cll[];
				ClientData cd;
				if (false == check_cmd(args[0])) {
					return 0;
				}

				dev = find_device();
				if (dev == null) {
					return -1;
				}

				cll = dev.getClients();
				for (Client c : cll) {
					cd = c.getClientData();
					System.out.println("pid: " + cd.getPid() + " debug port: "
							+ c.getDebuggerListenPort());
				}

				return 0;
			}
		};
		handlers[1] = new CmdHandler("set_device") {
			public int handle(String[] args) {
				if (false == check_cmd(args[0])) {
					return 0;
				}
				if (args.length < 2) {
					System.out
							.println("please specify serial number for device");
					return -1;
				}
				device = args[1];
				return 0;
			}
		};
		handlers[2] = new CmdHandler("list_device") {
			public int handle(String[] args) {
				if (false == check_cmd(args[0])) {
					return 0;
				}
				for (IDevice dev : adb.getDevices()) {
					System.out.println(dev.getSerialNumber());
				}
				return 0;
			}
		};
		handlers[3] = new CmdHandler("dump_hprof") {
			public int handle(String[] args) {
				int pid;
				IDevice dev;
				Client cll[];
				ClientData cd;
				if (false == check_cmd(args[0])) {
					return 0;
				}
				if (args.length != 2) {
					return -1;
				}
				pid = Integer.parseInt(args[1]);
				dev = find_device();
				if (dev == null) {
					return -1;
				}
				cll = dev.getClients();
				for (Client c : cll) {
					cd = c.getClientData();
					if (pid == cd.getPid()) {
						c.dumpHprof();
						return 0;
					}
				}
				System.out.println("process " + pid + " not found!");
				return -1;
			}
		};
		handlers[4] = new CmdHandler("toggle_mprof") {
			public int handle(String[] args) {
				int pid;
				IDevice dev;
				Client cll[];
				ClientData cd;
				MethodProfilingStatus mpstatus;
				if (false == check_cmd(args[0])) {
					return 0;
				}
				if (args.length != 2) {
					return -1;
				}
				pid = Integer.parseInt(args[1]);
				dev = find_device();
				if (dev == null) {
					return -1;
				}
				cll = dev.getClients();
				for (Client c : cll) {
					cd = c.getClientData();
					if (pid == cd.getPid()) {
						mpstatus = cd.getMethodProfilingStatus();
						System.out
								.println("mpstatus is " + mpstatus.toString());
						c.toggleMethodProfiling();
						System.out
								.println("mpstatus is " + mpstatus.toString());
						return 0;
					}
				}
				System.out.println("process " + pid + " not found!");
				return -1;
			}
		};
		ClientData.setHprofDumpHandler(new HProfHandler());
		ClientData.setMethodProfilingHandler(new MethodProfilingHandler());
		// ClientData.setHprofDumpHandler();
		while (true) {
			try {
				input = inStr.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (input.equals("q")) {
				return;
			}
			args = input.split(" ");
			if (args.length == 0) {
				continue;
			}
			if (args.length == 1 && args[0].equals("help")) {
				for (i = 0; i < handlers.length; i++) {
					System.out.println(handlers[i].cmd);
				}
			} else {
				for (i = 0; i < handlers.length; i++) {
					if (handlers[i].cmd.equals(args[0])) {
						System.out.println(handlers[i].handle(args));
					}
				}
			}
		}
	}

	private APScan() {
	}

	void run() {
		AndroidDebugBridge.init(true);
		System.out.println("AndroidDebugBridge initialized\n");
		// adb = AndroidDebugBridge.createBridge(adb_path, true);
		adb = AndroidDebugBridge.createBridge();
		if (adb == null) {
			System.out.println("createBridge() failed\n");
		} else {
			System.out.println("createBridge() succeeded\n");

			if (wait_for_connection()) {
				System.out.println("adb is connected\n");
				command_loop();
			} else {
				System.out.println("adb is not connected\n");
			}
		}
		// AndroidDebugBridge.disconnectBridge();
		AndroidDebugBridge.terminate();
		System.out.println("AndroidDebugBridge terminated\n");
	}

	public static void main(String[] args) {
		APScan a = new APScan();
		a.run();
	}
}
