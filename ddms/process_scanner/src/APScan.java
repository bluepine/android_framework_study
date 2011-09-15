import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DebugPortManager;
import com.android.ddmlib.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Properties;

public class APScan{
    public static void main(String[] args) {
	Log.d("APScan", "Initializing");
	System.out.println("Hello World\n");
    }
}
