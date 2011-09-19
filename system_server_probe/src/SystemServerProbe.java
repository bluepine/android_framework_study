import java.util.List;

import android.os.*;
import com.android.internal.os.BinderInternal;
import android.app.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

class SystemServerProbe extends Application{
	void listSystemServices(){
		IBinder binder;
		binder = BinderInternal.getContextObject();
		if(binder == null){
			System.out.println("getContextObject failed\n");
			return;
		}
		if(binder.isBinderAlive() != true){
			System.out.println("context binder is dead.\n");
			return;
		}
		if(binder.pingBinder() == false){
			System.out.println("ping binder failed\n");
			return;
		}
		try{
			System.out.println("binder descriptor:"+binder.getInterfaceDescriptor());
		}catch (RemoteException e){
			System.out.println("RemoteException: " + e.getMessage() +" "+ e.toString());
		}

		IServiceManager sm = (IServiceManager)(binder.queryLocalInterface("android.os.IServiceManager"));
		if(sm == null){
			System.out.println("queryLocalInterface failed\n");
			return;
		}
		for(int j=0; j<3; j++){
			try {
				String[] services = sm.listServices();;
				//String[] services = ServiceManager.listServices();;
				for (int i = 0; i < services.length; i++) {
					System.out.println("Services " + i + " : " + services[i]);
				}
			} catch (RemoteException e) {
				System.out.println("RemoteException: " + e.getMessage() +" "+ e.toString());
			}
		}
	}
	void listPackages(){
		PackageManager pm = getPackageManager();
		List<PackageInfo> pil = pm.getInstalledPackages(0);
		for(PackageInfo pi : pil){
			System.out.println("package name: "+pi.packageName);
		}
	}
	void probe(){
		listSystemServices();
		listPackages();
	}

	private SystemServerProbe(){
	}
	private static SystemServerProbe ssp = null;
	public static void main(String[] args) {
		//	System.out.println("testing android runtime: sdk int:"+Build.VERSION.SDK_INT+"\n"); 
		System.out.println("API number:"+Build.VERSION.SDK_INT+"\n");
		ssp = new SystemServerProbe();
		ssp.probe();
	}
}
